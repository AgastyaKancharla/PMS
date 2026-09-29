package com.agastyaone.crmai.data.billing

import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class InvoiceRepository(private val db: FirebaseFirestore = Firebase.firestore) {

    private fun tenantDoc(clinicId: String) = db.collection("tenants").document(clinicId)

    private fun invoicesCollection(clinicId: String) = tenantDoc(clinicId).collection("invoices")

    fun observeInvoicesForPatient(clinicId: String, patientId: String): Flow<List<Invoice>> =
        invoicesCollection(clinicId)
            .whereEqualTo("patientId", patientId)
            .orderBy("issuedAt", Query.Direction.DESCENDING)
            .asFlow()

    fun observeAllInvoices(clinicId: String): Flow<List<Invoice>> {
        if (com.agastyaone.crmai.data.demo.DemoDataStore.isDemoClinic(clinicId)) {
            return com.agastyaone.crmai.data.demo.DemoDataStore.observeInvoices()
        }
        return invoicesCollection(clinicId)
            .orderBy("issuedAt", Query.Direction.DESCENDING)
            .asFlow()
    }

    fun observeInvoice(clinicId: String, invoiceId: String): Flow<Invoice?> {
        if (com.agastyaone.crmai.data.demo.DemoDataStore.isDemoClinic(clinicId)) {
            return com.agastyaone.crmai.data.demo.DemoDataStore.observeInvoice(invoiceId)
        }
        return callbackFlow {
            val registration = invoicesCollection(clinicId).document(invoiceId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        android.util.Log.w("InvoiceRepository", "observeInvoice error: ${error.message}")
                        trySend(null)
                        return@addSnapshotListener
                    }
                    trySend(snapshot?.toObject(Invoice::class.java))
                }
            awaitClose { registration.remove() }
        }
    }

    suspend fun createInvoice(
        clinicId: String,
        issuedByUid: String,
        patientId: String,
        lineItems: List<InvoiceLineItem>,
        billingState: String,
        subtotal: Double,
        gst: GstBreakdown,
        total: Double,
        treatmentPlanId: String?,
    ): String {
        if (com.agastyaone.crmai.data.demo.DemoDataStore.isDemoClinic(clinicId)) {
            return com.agastyaone.crmai.data.demo.DemoDataStore.addInvoice(
                patientId = patientId,
                lineItems = lineItems,
                total = total,
                subtotal = subtotal,
            ).id
        }
        return try {
            val tenantRef = tenantDoc(clinicId)
            val invoiceRef = invoicesCollection(clinicId).document()
            db.runTransaction { transaction ->
                val tenantSnapshot = transaction.get(tenantRef)
                val nextNumber = (tenantSnapshot.getLong("invoiceCounter") ?: 0L) + 1
                val invoiceNumber = "INV-%04d".format(nextNumber)
                val data = hashMapOf<String, Any?>(
                    "patientId" to patientId,
                    "invoiceNumber" to invoiceNumber,
                    "issuedAt" to Timestamp.now(),
                    "issuedByUid" to issuedByUid,
                    "lineItems" to lineItems.map { it.toRaw() },
                    "billingState" to billingState,
                    "subtotal" to subtotal,
                    "cgst" to gst.cgst,
                    "sgst" to gst.sgst,
                    "igst" to gst.igst,
                    "total" to total,
                    "paymentStatus" to PaymentStatus.UNPAID.id,
                    "amountPaid" to 0.0,
                    "razorpayPaymentLinkId" to null,
                    "razorpayStatus" to null,
                    "treatmentPlanId" to treatmentPlanId,
                )
                transaction.set(invoiceRef, data)
                transaction.update(tenantRef, "invoiceCounter", nextNumber)
            }.await()
            invoiceRef.id
        } catch (e: Exception) {
            com.agastyaone.crmai.data.demo.DemoDataStore.addInvoice(
                patientId = patientId,
                lineItems = lineItems,
                total = total,
                subtotal = subtotal,
            ).id
        }
    }

    suspend fun recordPayment(clinicId: String, invoiceId: String, paymentStatus: PaymentStatus, amountPaid: Double) {
        invoicesCollection(clinicId).document(invoiceId).update(
            mapOf(
                "paymentStatus" to paymentStatus.id,
                "amountPaid" to amountPaid,
            ),
        ).await()
    }

    suspend fun voidInvoice(clinicId: String, invoiceId: String) {
        invoicesCollection(clinicId).document(invoiceId).delete().await()
    }
}

private inline fun <reified T : Any> Query.asFlow(): Flow<List<T>> = callbackFlow {
    val registration = addSnapshotListener { snapshot, error ->
        if (error != null) {
            android.util.Log.w("InvoiceRepository", "Snapshot error: ${error.message}")
            trySend(emptyList())
            return@addSnapshotListener
        }
        trySend(snapshot?.toObjects(T::class.java) ?: emptyList())
    }
    awaitClose { registration.remove() }
}
