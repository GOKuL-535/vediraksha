package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "factory_rooms")
data class RoomEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val roomNumber: String,
    val nameTa: String,
    val nameEn: String,
    val maxCapacityKg: Double,
    val currentTotalKg: Double,
    val hazardCategory: String, // CRITICAL, HIGH, MODERATE
    val supervisorName: String
) {
    val fillPercentage: Float
        get() = if (maxCapacityKg > 0) ((currentTotalKg / maxCapacityKg) * 100).toFloat() else 0f

    val isDanger: Boolean
        get() = fillPercentage >= 100f

    val isWarning: Boolean
        get() = fillPercentage in 80f..99.9f
}

@Entity(tableName = "chemical_entries")
data class ChemicalEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val roomId: Long,
    val chemicalNameTa: String,
    val chemicalNameEn: String,
    val batchCode: String,
    val quantityKg: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val workerName: String,
    val approverName: String,
    val isApproved: Boolean = true,
    val status: String = "SAFE", // SAFE, WARNING, BLOCKED
    val tamperHash: String = ""
)

@Entity(tableName = "shift_records")
data class ShiftEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workerId: String,
    val workerName: String,
    val shiftDate: String,
    val shiftType: String, // MORNING, AFTERNOON, NIGHT
    val clockInTime: Long = System.currentTimeMillis(),
    val clockOutTime: Long? = null,
    val activeRoom: String,
    val piecesProduced: Int = 0,
    val workCategory: String = "Bijili Cracker Rolling",
    val pieceRate: Double = 0.45,
    val calculatedWage: Double = 0.0,
    val status: String = "ACTIVE", // ACTIVE, COMPLETED
    val biometricVerified: Boolean = true,
    val clockInBiometricHash: String = "",
    val clockOutBiometricHash: String = "",
    val durationMinutes: Long = 0
)

@Entity(tableName = "wage_entries")
data class WageEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val workerName: String,
    val workTypeTa: String,
    val workTypeEn: String,
    val unitsCompleted: Int,
    val unitRate: Double,
    val grossAmount: Double,
    val deductions: Double,
    val netAmount: Double,
    val digitalSignatureHash: String,
    val workerBiometricSigned: Boolean = true,
    val supervisorBiometricSigned: Boolean = true,
    val status: String = "LOCKED", // LOCKED, PENDING_CONFIRMATION, DISPUTED
    val disputeReason: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "sos_events")
data class SosEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workerName: String,
    val triggerType: String, // MANUAL_SOS, SHOCK_DETECTED, CHEMICAL_OVERLOAD
    val location: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "ACTIVE", // ACTIVE, RELAYED_OFFLINE, RESOLVED
    val relayedDevices: Int = 4,
    val alertMessageTamil: String
)

@Entity(tableName = "factory_workers")
data class WorkerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workerId: String,               // e.g. "TN-SVK-4091"
    val nameTa: String,                 // e.g. "காளியப்பன்"
    val nameEn: String,                 // e.g. "Kaliappan"
    val roleTa: String,                 // e.g. "பிஜிலி பட்டாசு சுற்றல் நிபுணர்"
    val roleEn: String,                 // e.g. "Master Bijili Roller"
    val assignedRoom: String,           // e.g. "Room R-101"
    val shiftType: String = "MORNING",  // MORNING, AFTERNOON, NIGHT
    val status: String = "ON_DUTY",     // ON_DUTY, ON_BREAK, OFF_DUTY
    val experienceYears: Int = 5,
    val piecesProducedToday: Int = 0,
    val wageEarnedToday: Double = 0.0,
    val biometricClockInTime: Long = System.currentTimeMillis(),
    val emergencyPhone: String = "+91 94431 82910",
    val safetyCertExpiryDate: String = "31 Dec 2026",
    val isSafetyCertified: Boolean = true
)

@Entity(tableName = "cracker_stocks")
data class CrackerStockEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val crackerNameTa: String,
    val crackerNameEn: String,
    val targetQuantity: Int,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "chemical_stocks")
data class ChemicalStockEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val chemicalNameTa: String,
    val chemicalNameEn: String,
    val availableStockKg: Double,
    val unit: String = "kg"
)

@Entity(tableName = "chemical_allocations")
data class ChemicalAllocationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val roomId: Long,
    val roomNumber: String,
    val chemicalNameTa: String,
    val chemicalNameEn: String,
    val allocatedKg: Double,
    val allocatedBy: String,
    val timestamp: Long = System.currentTimeMillis()
)


