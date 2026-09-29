package com.agastyaone.crmai.data.tenant

import com.agastyaone.crmai.data.charting.ToothNumberingSystem
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * The subset of `tenants/{clinicId}` fields (set once at signup by createClinicAndOwner,
 * see Phase 1) that Phase 4a's invoice letterhead and GST split need: [state] is compared
 * against an invoice's billingState to decide CGST+SGST vs IGST (see [com.agastyaone.crmai.data.billing.calculateGst]).
 */
data class Clinic(
    val clinicName: String = "",
    val address: String = "",
    val city: String = "",
    val state: String = "",
    val gstin: String? = null,
)

/**
 * Clinic-level settings stored directly on the `tenants/{clinicId}` document, which the
 * owner can already write per firestore.rules - no rule change needed for a new field there.
 */
class TenantRepository(private val db: FirebaseFirestore = Firebase.firestore) {

    private fun tenantDoc(clinicId: String) = db.collection("tenants").document(clinicId)

    fun observeClinic(clinicId: String): Flow<Clinic> {
        if (com.agastyaone.crmai.data.demo.DemoDataStore.isDemoClinic(clinicId)) {
            return kotlinx.coroutines.flow.flowOf(
                Clinic(
                    clinicName = "AgastyaOne Dental Clinic",
                    address = "102 Indiranagar 100ft Road",
                    city = "Bengaluru",
                    state = "Karnataka",
                    gstin = "29AAAAA0000A1Z5"
                )
            )
        }
        return callbackFlow {
            val registration = tenantDoc(clinicId).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.w("TenantRepository", "observeClinic error: ${error.message}")
                    trySend(Clinic(clinicName = "AgastyaOne Dental Clinic", city = "Bengaluru", state = "Karnataka"))
                    return@addSnapshotListener
                }
                trySend(
                    Clinic(
                        clinicName = snapshot?.getString("clinicName").orEmpty(),
                        address = snapshot?.getString("address").orEmpty(),
                        city = snapshot?.getString("city").orEmpty(),
                        state = snapshot?.getString("state").orEmpty(),
                        gstin = snapshot?.getString("gstin"),
                    ),
                )
            }
            awaitClose { registration.remove() }
        }
    }

    /** Display-layer only - see [com.agastyaone.crmai.data.charting.ToothChart] for why. */
    fun observeToothNumberingSystem(clinicId: String): Flow<ToothNumberingSystem> = callbackFlow {
        val registration = tenantDoc(clinicId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                android.util.Log.w("TenantRepository", "observeToothNumberingSystem error: ${error.message}")
                trySend(ToothNumberingSystem.FDI)
                return@addSnapshotListener
            }
            trySend(ToothNumberingSystem.fromId(snapshot?.getString("toothNumberingSystem")))
        }
        awaitClose { registration.remove() }
    }

    suspend fun setToothNumberingSystem(clinicId: String, system: ToothNumberingSystem) {
        tenantDoc(clinicId).set(mapOf("toothNumberingSystem" to system.id), SetOptions.merge()).await()
    }
}
