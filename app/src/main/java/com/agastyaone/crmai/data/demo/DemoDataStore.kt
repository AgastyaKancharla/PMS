package com.agastyaone.crmai.data.demo

import com.agastyaone.crmai.data.billing.Invoice
import com.agastyaone.crmai.data.billing.InvoiceLineItem
import com.agastyaone.crmai.data.billing.PaymentStatus
import com.agastyaone.crmai.data.patients.Patient
import com.agastyaone.crmai.data.patients.PatientClinicalDetails
import com.agastyaone.crmai.data.patients.PatientDemographics
import com.agastyaone.crmai.data.scheduling.Appointment
import com.agastyaone.crmai.data.scheduling.AppointmentSource
import com.agastyaone.crmai.data.scheduling.AppointmentStatus
import com.agastyaone.crmai.ui.scheduling.combineWithTime
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

/**
 * In-memory responsive mock store for "Explore Live Demo Clinic" and offline resilient fallback.
 * Allows instant, flawless testing of patient creation, calendar appointments, FDI odontogram,
 * and billing without needing production Firebase backend claims.
 */
object DemoDataStore {

    fun isDemoClinic(clinicId: String?): Boolean {
        if (clinicId == null) return false
        return clinicId == "clinic-bengaluru" || clinicId.startsWith("demo-")
    }

    private val now = LocalDate.now()

    private val initialPatients = listOf(
        Patient(
            id = "demo-patient-1",
            name = "Rajesh Kumar",
            dob = "1988-06-15",
            gender = "Male",
            phone = "9876543210",
            email = "rajesh.kumar@example.com",
            address = "Indiranagar, Bengaluru",
            bloodGroup = "O+",
            allergies = listOf("Penicillin", "Latex"),
            chronicConditions = listOf("Hypertension"),
            currentMedications = listOf("Amlodipine 5mg"),
            medicalHistoryNotes = "History of root canal #16 in 2024.",
            createdAt = Timestamp.now(),
        ),
        Patient(
            id = "demo-patient-2",
            name = "Priya Sharma",
            dob = "1994-09-22",
            gender = "Female",
            phone = "9812345678",
            email = "priya.s@example.com",
            address = "Koramangala, Bengaluru",
            bloodGroup = "B+",
            allergies = emptyList(),
            chronicConditions = emptyList(),
            currentMedications = emptyList(),
            medicalHistoryNotes = "Routine bi-annual dental prophylaxis.",
            createdAt = Timestamp.now(),
        ),
        Patient(
            id = "demo-patient-3",
            name = "Anita Desai",
            dob = "1982-01-10",
            gender = "Female",
            phone = "9898989898",
            email = "anita.desai@example.com",
            address = "HSR Layout, Bengaluru",
            bloodGroup = "A+",
            allergies = listOf("Aspirin"),
            chronicConditions = listOf("Type 2 Diabetes"),
            currentMedications = listOf("Metformin 500mg"),
            medicalHistoryNotes = "Crown cementation scheduled #24.",
            createdAt = Timestamp.now(),
        ),
        Patient(
            id = "demo-patient-4",
            name = "Vikram Patel",
            dob = "2001-11-05",
            gender = "Male",
            phone = "9765432109",
            email = "vikram.p@example.com",
            address = "Whitefield, Bengaluru",
            bloodGroup = "AB+",
            allergies = emptyList(),
            chronicConditions = emptyList(),
            currentMedications = emptyList(),
            medicalHistoryNotes = "Orthodontic treatment in progress.",
            createdAt = Timestamp.now(),
        ),
    )

    private val initialAppointments = listOf(
        Appointment(
            id = "demo-appt-1",
            patientId = "demo-patient-1",
            patientName = "Rajesh Kumar",
            startTime = now.combineWithTime(LocalTime.of(10, 0)),
            endTime = now.combineWithTime(LocalTime.of(10, 45)),
            status = AppointmentStatus.CONFIRMED.id,
            source = AppointmentSource.PHONE.id,
            notes = "RCT Evaluation & Temporary Filling Review",
        ),
        Appointment(
            id = "demo-appt-2",
            patientId = "demo-patient-2",
            patientName = "Priya Sharma",
            startTime = now.combineWithTime(LocalTime.of(11, 30)),
            endTime = now.combineWithTime(LocalTime.of(12, 15)),
            status = AppointmentStatus.SCHEDULED.id,
            source = AppointmentSource.WALK_IN.id,
            notes = "Full mouth ultrasonic scaling & polishing",
        ),
        Appointment(
            id = "demo-appt-3",
            patientId = "demo-patient-3",
            patientName = "Anita Desai",
            startTime = now.combineWithTime(LocalTime.of(14, 0)),
            endTime = now.combineWithTime(LocalTime.of(15, 0)),
            status = AppointmentStatus.CHECKED_IN.id,
            source = AppointmentSource.PHONE.id,
            notes = "Crown seating Tooth #24",
        ),
        Appointment(
            id = "demo-appt-4",
            patientId = "demo-patient-4",
            patientName = "Vikram Patel",
            startTime = now.combineWithTime(LocalTime.of(16, 30)),
            endTime = now.combineWithTime(LocalTime.of(17, 0)),
            status = AppointmentStatus.SCHEDULED.id,
            source = AppointmentSource.ONLINE.id,
            notes = "Upper arch wire activation",
        ),
    )

    private val initialInvoices = listOf(
        Invoice(
            id = "demo-inv-1",
            invoiceNumber = "INV-0001",
            patientId = "demo-patient-1",
            lineItems = listOf(
                InvoiceLineItem(
                    procedureCode = "D3330",
                    procedureName = "Molar Root Canal Treatment",
                    hsnSacCode = "999312",
                    quantity = 1,
                    unitCost = 4500.0,
                    lineTotal = 4500.0,
                ).toRaw(),
                InvoiceLineItem(
                    procedureCode = "D0220",
                    procedureName = "Intraoral Periapical Radiograph",
                    hsnSacCode = "999312",
                    quantity = 2,
                    unitCost = 400.0,
                    lineTotal = 800.0,
                ).toRaw(),
            ),
            subtotal = 5300.0,
            total = 5300.0,
            paymentStatus = PaymentStatus.PAID.id,
            amountPaid = 5300.0,
            issuedAt = Timestamp.now(),
        ),
        Invoice(
            id = "demo-inv-2",
            invoiceNumber = "INV-0002",
            patientId = "demo-patient-2",
            lineItems = listOf(
                InvoiceLineItem(
                    procedureCode = "D1110",
                    procedureName = "Scaling & Polishing",
                    hsnSacCode = "999312",
                    quantity = 1,
                    unitCost = 1800.0,
                    lineTotal = 1800.0,
                ).toRaw(),
            ),
            subtotal = 1800.0,
            total = 1800.0,
            paymentStatus = PaymentStatus.UNPAID.id,
            amountPaid = 0.0,
            issuedAt = Timestamp.now(),
        ),
    )

    private val patientsFlow = MutableStateFlow(initialPatients)
    private val appointmentsFlow = MutableStateFlow(initialAppointments)
    private val invoicesFlow = MutableStateFlow(initialInvoices)

    // Patient Operations
    fun observePatients(): Flow<List<Patient>> = patientsFlow

    fun observePatient(patientId: String): Flow<Patient?> =
        patientsFlow.map { list -> list.firstOrNull { it.id == patientId } }

    fun getPatient(patientId: String): Patient? =
        patientsFlow.value.firstOrNull { it.id == patientId }

    fun searchPatients(query: String): List<Patient> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return patientsFlow.value
        return patientsFlow.value.filter {
            it.name.lowercase().contains(q) || (it.phone?.contains(q) == true)
        }
    }

    @Synchronized
    fun addPatient(demographics: PatientDemographics, createdByUid: String): Patient {
        val newPatient = Patient(
            id = "pat-${UUID.randomUUID().toString().take(8)}",
            name = demographics.name,
            dob = demographics.dob,
            gender = demographics.gender,
            phone = demographics.phone,
            email = demographics.email,
            address = demographics.address,
            emergencyContactName = demographics.emergencyContactName,
            emergencyContactPhone = demographics.emergencyContactPhone,
            bloodGroup = demographics.bloodGroup,
            createdAt = Timestamp.now(),
            createdByUid = createdByUid,
            lastEditedAt = Timestamp.now(),
            lastEditedByUid = createdByUid,
        )
        patientsFlow.value = listOf(newPatient) + patientsFlow.value
        return newPatient
    }

    @Synchronized
    fun updateDemographics(patientId: String, uid: String, demographics: PatientDemographics) {
        patientsFlow.value = patientsFlow.value.map { current ->
            if (current.id == patientId) {
                current.copy(
                    name = demographics.name,
                    dob = demographics.dob,
                    gender = demographics.gender,
                    phone = demographics.phone,
                    email = demographics.email,
                    address = demographics.address,
                    emergencyContactName = demographics.emergencyContactName,
                    emergencyContactPhone = demographics.emergencyContactPhone,
                    bloodGroup = demographics.bloodGroup,
                    lastEditedAt = Timestamp.now(),
                    lastEditedByUid = uid,
                )
            } else current
        }
    }

    @Synchronized
    fun updateClinicalDetails(patientId: String, uid: String, clinical: PatientClinicalDetails) {
        patientsFlow.value = patientsFlow.value.map { current ->
            if (current.id == patientId) {
                current.copy(
                    allergies = clinical.allergies,
                    chronicConditions = clinical.chronicConditions,
                    currentMedications = clinical.currentMedications,
                    medicalHistoryNotes = clinical.medicalHistoryNotes,
                    lastEditedAt = Timestamp.now(),
                    lastEditedByUid = uid,
                )
            } else current
        }
    }

    // Appointment Operations
    fun observeAppointments(): Flow<List<Appointment>> = appointmentsFlow

    fun observeAppointmentsInRange(start: Timestamp, end: Timestamp): Flow<List<Appointment>> =
        appointmentsFlow.map { list ->
            list.filter { appt ->
                val s = appt.startTime?.seconds ?: 0L
                s >= start.seconds && s < end.seconds
            }
        }

    fun observeAppointment(appointmentId: String): Flow<Appointment?> =
        appointmentsFlow.map { list -> list.firstOrNull { it.id == appointmentId } }

    @Synchronized
    fun addAppointment(
        patientId: String,
        patientName: String,
        startTime: Timestamp,
        endTime: Timestamp,
        source: AppointmentSource,
        notes: String?,
        createdByUid: String,
    ): Appointment {
        val newAppt = Appointment(
            id = "appt-${UUID.randomUUID().toString().take(8)}",
            patientId = patientId,
            patientName = patientName,
            startTime = startTime,
            endTime = endTime,
            status = AppointmentStatus.CONFIRMED.id,
            source = source.id,
            notes = notes,
            createdByUid = createdByUid,
            createdAt = Timestamp.now(),
        )
        appointmentsFlow.value = appointmentsFlow.value + newAppt
        return newAppt
    }

    @Synchronized
    fun updateAppointmentStatus(appointmentId: String, status: AppointmentStatus) {
        appointmentsFlow.value = appointmentsFlow.value.map { appt ->
            if (appt.id == appointmentId) appt.copy(status = status.id) else appt
        }
    }

    // Invoice Operations
    fun observeInvoices(): Flow<List<Invoice>> = invoicesFlow

    fun observeInvoice(invoiceId: String): Flow<Invoice?> =
        invoicesFlow.map { list -> list.firstOrNull { it.id == invoiceId } }

    fun getInvoice(invoiceId: String): Invoice? =
        invoicesFlow.value.firstOrNull { it.id == invoiceId }

    @Synchronized
    fun addInvoice(
        patientId: String,
        lineItems: List<InvoiceLineItem>,
        total: Double,
        subtotal: Double,
    ): Invoice {
        val count = invoicesFlow.value.size + 1
        val newInv = Invoice(
            id = "inv-${UUID.randomUUID().toString().take(8)}",
            invoiceNumber = "INV-%04d".format(count),
            patientId = patientId,
            lineItems = lineItems.map { it.toRaw() },
            subtotal = subtotal,
            total = total,
            paymentStatus = PaymentStatus.UNPAID.id,
            issuedAt = Timestamp.now(),
        )
        invoicesFlow.value = listOf(newInv) + invoicesFlow.value
        return newInv
    }
}
