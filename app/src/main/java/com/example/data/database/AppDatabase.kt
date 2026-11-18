package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.AppDao
import com.example.data.model.ChemicalAllocationEntity
import com.example.data.model.ChemicalEntryEntity
import com.example.data.model.ChemicalStockEntity
import com.example.data.model.CrackerStockEntity
import com.example.data.model.RoomEntity
import com.example.data.model.ShiftEntity
import com.example.data.model.SosEventEntity
import com.example.data.model.WageEntryEntity
import com.example.data.model.WorkerEntity
import com.example.util.CryptoUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Database(
    entities = [
        RoomEntity::class,
        ChemicalEntryEntity::class,
        ShiftEntity::class,
        WageEntryEntity::class,
        SosEventEntity::class,
        WorkerEntity::class,
        CrackerStockEntity::class,
        ChemicalStockEntity::class,
        ChemicalAllocationEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "vediraksha_factory.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database.appDao())
                }
            }
        }

        private suspend fun populateInitialData(dao: AppDao) {
            // Seed Rooms
            val rooms = listOf(
                RoomEntity(
                    id = 1,
                    roomNumber = "R-101",
                    nameTa = "மருந்து அரைக்கும் அறை",
                    nameEn = "Chemical Mixing Room",
                    maxCapacityKg = 25.0,
                    currentTotalKg = 18.5, // ~74%
                    hazardCategory = "CRITICAL",
                    supervisorName = "செல்வராஜ் (Selvaraj K.)"
                ),
                RoomEntity(
                    id = 2,
                    roomNumber = "R-102",
                    nameTa = "மருந்து நிரப்பும் அறை",
                    nameEn = "Explosive Filling Room",
                    maxCapacityKg = 30.0,
                    currentTotalKg = 25.5, // 85% Warning Amber!
                    hazardCategory = "HIGH",
                    supervisorName = "முத்துக்குமார் (Muthukumar)"
                ),
                RoomEntity(
                    id = 3,
                    roomNumber = "R-103",
                    nameTa = "திரி அமைக்கும் அறை",
                    nameEn = "Fuse Cutting & Fixing",
                    maxCapacityKg = 20.0,
                    currentTotalKg = 8.0, // 40% Safe
                    hazardCategory = "MODERATE",
                    supervisorName = "மாரிமுத்து (Marimuthu M.)"
                ),
                RoomEntity(
                    id = 4,
                    roomNumber = "R-104",
                    nameTa = "உலர்த்தும் தளம் (வெயில்)",
                    nameEn = "Solar Drying Platform",
                    maxCapacityKg = 50.0,
                    currentTotalKg = 22.0, // 44%
                    hazardCategory = "MODERATE",
                    supervisorName = "கணேசன் (Ganesan P.)"
                ),
                RoomEntity(
                    id = 5,
                    roomNumber = "R-105",
                    nameTa = "சேமிப்பு கிடங்கு (மேகசின்)",
                    nameEn = "Licensed Storage Magazine",
                    maxCapacityKg = 100.0,
                    currentTotalKg = 45.0, // 45%
                    hazardCategory = "HIGH",
                    supervisorName = "செல்வராஜ் (Selvaraj K.)"
                )
            )
            dao.insertRooms(rooms)

            // Seed Chemical Entries
            val now = System.currentTimeMillis()
            val entries = listOf(
                ChemicalEntryEntity(
                    roomId = 1,
                    chemicalNameTa = "பொட்டாசியம் நைட்ரேட் (வெடியுப்பு)",
                    chemicalNameEn = "Potassium Nitrate (KNO3)",
                    batchCode = "BATCH-KN-883",
                    quantityKg = 12.0,
                    timestamp = now - 3600000 * 3,
                    workerName = "காளியப்பன் (Kaliappan)",
                    approverName = "செல்வராஜ் (Supervisor)",
                    status = "SAFE",
                    tamperHash = CryptoUtils.generateChemicalSeal(1, 1, "KNO3", "BATCH-KN-883", 12.0, now - 3600000 * 3)
                ),
                ChemicalEntryEntity(
                    roomId = 1,
                    chemicalNameTa = "கந்தகம் (சல்பர்)",
                    chemicalNameEn = "Sulphur Powder",
                    batchCode = "BATCH-SL-109",
                    quantityKg = 6.5,
                    timestamp = now - 3600000 * 2,
                    workerName = "காளியப்பன் (Kaliappan)",
                    approverName = "செல்வராஜ் (Supervisor)",
                    status = "SAFE",
                    tamperHash = CryptoUtils.generateChemicalSeal(2, 1, "Sulphur", "BATCH-SL-109", 6.5, now - 3600000 * 2)
                ),
                ChemicalEntryEntity(
                    roomId = 2,
                    chemicalNameTa = "அலுமினியம் பவுடர் (வெள்ளி தூள்)",
                    chemicalNameEn = "Aluminium Flake Powder",
                    batchCode = "BATCH-AL-542",
                    quantityKg = 15.5,
                    timestamp = now - 3600000,
                    workerName = "காளியப்பன் (Kaliappan)",
                    approverName = "முத்துக்குமார் (Supervisor)",
                    status = "WARNING",
                    tamperHash = CryptoUtils.generateChemicalSeal(3, 2, "Aluminium", "BATCH-AL-542", 15.5, now - 3600000)
                )
            )
            for (entry in entries) {
                dao.insertChemicalEntry(entry)
            }

            // Seed Wage History with Cryptographic Seals
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
            val wages = listOf(
                WageEntryEntity(
                    id = 1,
                    date = dateFormat.format(Date(now - 86400000L * 2)),
                    workerName = "காளியப்பன் (Kaliappan)",
                    workTypeTa = "பிஜிலி பட்டாசு சுற்றல்",
                    workTypeEn = "Bijili Cracker Rolling",
                    unitsCompleted = 2000,
                    unitRate = 0.35,
                    grossAmount = 700.0,
                    deductions = 0.0,
                    netAmount = 700.0,
                    digitalSignatureHash = CryptoUtils.generateWageHash(1, "Kaliappan", "Yesterday", "Rolling", 2000, 700.0, now - 86400000L * 2),
                    workerBiometricSigned = true,
                    supervisorBiometricSigned = true,
                    status = "LOCKED",
                    timestamp = now - 86400000L * 2
                ),
                WageEntryEntity(
                    id = 2,
                    date = dateFormat.format(Date(now - 86400000L)),
                    workerName = "காளியப்பன் (Kaliappan)",
                    workTypeTa = "பூந்தொட்டி மருந்து நிரப்புதல்",
                    workTypeEn = "Flowerpot Chemical Filling",
                    unitsCompleted = 1200,
                    unitRate = 0.60,
                    grossAmount = 720.0,
                    deductions = 20.0, // Safety gloves fee
                    netAmount = 700.0,
                    digitalSignatureHash = CryptoUtils.generateWageHash(2, "Kaliappan", "PrevDay", "Filling", 1200, 700.0, now - 86400000L),
                    workerBiometricSigned = true,
                    supervisorBiometricSigned = true,
                    status = "LOCKED",
                    timestamp = now - 86400000L
                )
            )
            dao.insertWages(wages)

            // Seed Historical Biometric Shift Records
            val shift1ClockIn = now - 86400000L * 2 + 28800000L // 8 AM 2 days ago
            val shift1ClockOut = shift1ClockIn + 28800000L // 8 hours later
            val shift2ClockIn = now - 86400000L + 28800000L // 8 AM yesterday
            val shift2ClockOut = shift2ClockIn + 27000000L // 7.5 hours later

            val initialShifts = listOf(
                ShiftEntity(
                    id = 1,
                    workerId = "TN-SVK-4091",
                    workerName = "காளியப்பன் (Kaliappan)",
                    shiftDate = dateFormat.format(Date(now - 86400000L * 2)),
                    shiftType = "MORNING",
                    clockInTime = shift1ClockIn,
                    clockOutTime = shift1ClockOut,
                    activeRoom = "Room R-101",
                    piecesProduced = 2000,
                    workCategory = "Bijili Cracker Rolling",
                    pieceRate = 0.35,
                    calculatedWage = 700.0,
                    status = "COMPLETED",
                    biometricVerified = true,
                    clockInBiometricHash = CryptoUtils.generateBiometricShiftHash("TN-SVK-4091", "CLOCK_IN", shift1ClockIn, "Room R-101"),
                    clockOutBiometricHash = CryptoUtils.generateBiometricShiftHash("TN-SVK-4091", "CLOCK_OUT", shift1ClockOut, "Room R-101"),
                    durationMinutes = 480
                ),
                ShiftEntity(
                    id = 2,
                    workerId = "TN-SVK-4091",
                    workerName = "காளியப்பன் (Kaliappan)",
                    shiftDate = dateFormat.format(Date(now - 86400000L)),
                    shiftType = "MORNING",
                    clockInTime = shift2ClockIn,
                    clockOutTime = shift2ClockOut,
                    activeRoom = "Room R-102",
                    piecesProduced = 1200,
                    workCategory = "Flowerpot Chemical Filling",
                    pieceRate = 0.60,
                    calculatedWage = 720.0,
                    status = "COMPLETED",
                    biometricVerified = true,
                    clockInBiometricHash = CryptoUtils.generateBiometricShiftHash("TN-SVK-4091", "CLOCK_IN", shift2ClockIn, "Room R-102"),
                    clockOutBiometricHash = CryptoUtils.generateBiometricShiftHash("TN-SVK-4091", "CLOCK_OUT", shift2ClockOut, "Room R-102"),
                    durationMinutes = 450
                )
            )
            for (shift in initialShifts) {
                dao.insertShift(shift)
            }

            // Seed Factory Laborers / Workers (Sivakasi Pyrotechnic Workforce)
            val initialWorkers = listOf(
                WorkerEntity(
                    id = 1,
                    workerId = "TN-SVK-4091",
                    nameTa = "காளியப்பன்",
                    nameEn = "Kaliappan K.",
                    roleTa = "பிஜிலி பட்டாசு சுற்றும் மாஸ்டர்",
                    roleEn = "Master Bijili Roller",
                    assignedRoom = "Room R-101",
                    shiftType = "MORNING",
                    status = "ON_DUTY",
                    experienceYears = 6,
                    piecesProducedToday = 2100,
                    wageEarnedToday = 840.0,
                    biometricClockInTime = now - 14400000L, // 4 hrs ago
                    emergencyPhone = "+91 94431 82910",
                    isSafetyCertified = true
                ),
                WorkerEntity(
                    id = 2,
                    workerId = "TN-SVK-3820",
                    nameTa = "முருகேசன்",
                    nameEn = "Murugesan P.",
                    roleTa = "வெடிமருந்து அரைக்கும் நிபுணர்",
                    roleEn = "Chemical Mixing Specialist",
                    assignedRoom = "Room R-101",
                    shiftType = "MORNING",
                    status = "ON_DUTY",
                    experienceYears = 8,
                    piecesProducedToday = 1800,
                    wageEarnedToday = 900.0,
                    biometricClockInTime = now - 14400000L,
                    emergencyPhone = "+91 98421 55678",
                    isSafetyCertified = true
                ),
                WorkerEntity(
                    id = 3,
                    workerId = "TN-SVK-5104",
                    nameTa = "செல்வகுமார்",
                    nameEn = "Selvakumar M.",
                    roleTa = "கந்தக வேதிப்பொருள் கையாளுபவர்",
                    roleEn = "Sulphur Handler",
                    assignedRoom = "Room R-101",
                    shiftType = "MORNING",
                    status = "ON_DUTY",
                    experienceYears = 4,
                    piecesProducedToday = 1400,
                    wageEarnedToday = 700.0,
                    biometricClockInTime = now - 14400000L,
                    emergencyPhone = "+91 97500 23411",
                    isSafetyCertified = true
                ),
                WorkerEntity(
                    id = 4,
                    workerId = "TN-SVK-4412",
                    nameTa = "லட்சுமி",
                    nameEn = "Lakshmi S.",
                    roleTa = "பூந்தொட்டி மருந்து நிரப்புதல்",
                    roleEn = "Flowerpot Explosive Filling",
                    assignedRoom = "Room R-102",
                    shiftType = "MORNING",
                    status = "ON_DUTY",
                    experienceYears = 5,
                    piecesProducedToday = 1250,
                    wageEarnedToday = 750.0,
                    biometricClockInTime = now - 14400000L,
                    emergencyPhone = "+91 99441 33221",
                    isSafetyCertified = true
                ),
                WorkerEntity(
                    id = 5,
                    workerId = "TN-SVK-2933",
                    nameTa = "ஆறுமுகம்",
                    nameEn = "Arumugam V.",
                    roleTa = "வெள்ளித்தூள் நிரப்பும் டெக்னீசியன்",
                    roleEn = "Flash Powder Technician",
                    assignedRoom = "Room R-102",
                    shiftType = "MORNING",
                    status = "ON_BREAK",
                    experienceYears = 9,
                    piecesProducedToday = 1600,
                    wageEarnedToday = 960.0,
                    biometricClockInTime = now - 18000000L,
                    emergencyPhone = "+91 94861 77889",
                    isSafetyCertified = true
                ),
                WorkerEntity(
                    id = 6,
                    workerId = "TN-SVK-6029",
                    nameTa = "சண்முகவேல்",
                    nameEn = "Shanmugavel R.",
                    roleTa = "பாதுகாப்பு திரி வெட்டுதல் & முறுக்குதல்",
                    roleEn = "Safety Fuse Specialist",
                    assignedRoom = "Room R-103",
                    shiftType = "MORNING",
                    status = "ON_DUTY",
                    experienceYears = 3,
                    piecesProducedToday = 2400,
                    wageEarnedToday = 840.0,
                    biometricClockInTime = now - 14400000L,
                    emergencyPhone = "+91 93610 44556",
                    isSafetyCertified = true
                )
            )
            for (worker in initialWorkers) {
                dao.insertWorker(worker)
            }

            // Seed Initial Cracker Stock Targets (Supervisor Planning)
            val initialCrackerStocks = listOf(
                CrackerStockEntity(
                    id = 1,
                    crackerNameTa = "கம்பி மத்தாப்பு",
                    crackerNameEn = "Sparklers",
                    targetQuantity = 10000,
                    updatedAt = now
                ),
                CrackerStockEntity(
                    id = 2,
                    crackerNameTa = "பூந்தொட்டி",
                    crackerNameEn = "Flower Pots",
                    targetQuantity = 5000,
                    updatedAt = now
                ),
                CrackerStockEntity(
                    id = 3,
                    crackerNameTa = "ராக்கெட்",
                    crackerNameEn = "Rockets",
                    targetQuantity = 3000,
                    updatedAt = now
                ),
                CrackerStockEntity(
                    id = 4,
                    crackerNameTa = "தரை சக்கரம்",
                    crackerNameEn = "Ground Chakras",
                    targetQuantity = 8000,
                    updatedAt = now
                ),
                CrackerStockEntity(
                    id = 5,
                    crackerNameTa = "சத்தம் வெடி (Bijili)",
                    crackerNameEn = "Sound Bombs",
                    targetQuantity = 4000,
                    updatedAt = now
                )
            )
            for (stock in initialCrackerStocks) {
                dao.insertCrackerStock(stock)
            }

            // Seed Initial Chemical Raw Material Stock (Main Warehouse Inventory)
            val initialChemicalStocks = listOf(
                ChemicalStockEntity(
                    id = 1,
                    chemicalNameTa = "பொட்டாசியம் நைட்ரேட் (KNO3)",
                    chemicalNameEn = "Potassium Nitrate (KNO3)",
                    availableStockKg = 450.0,
                    unit = "kg"
                ),
                ChemicalStockEntity(
                    id = 2,
                    chemicalNameTa = "கந்தகப் பொடி (Sulphur)",
                    chemicalNameEn = "Sulphur Powder",
                    availableStockKg = 180.0,
                    unit = "kg"
                ),
                ChemicalStockEntity(
                    id = 3,
                    chemicalNameTa = "அலுமினியம் பவுடர்",
                    chemicalNameEn = "Aluminium Flake Powder",
                    availableStockKg = 140.0,
                    unit = "kg"
                ),
                ChemicalStockEntity(
                    id = 4,
                    chemicalNameTa = "பேரியம் நைட்ரேட்",
                    chemicalNameEn = "Barium Nitrate",
                    availableStockKg = 250.0,
                    unit = "kg"
                ),
                ChemicalStockEntity(
                    id = 5,
                    chemicalNameTa = "கரிப்பொடி (Charcoal)",
                    chemicalNameEn = "Charcoal Powder",
                    availableStockKg = 120.0,
                    unit = "kg"
                )
            )
            for (chem in initialChemicalStocks) {
                dao.insertChemicalStock(chem)
            }

            // Seed Initial Chemical-to-Room Allocations
            val initialAllocations = listOf(
                ChemicalAllocationEntity(
                    id = 1,
                    roomId = 1,
                    roomNumber = "R-101",
                    chemicalNameTa = "பொட்டாசியம் நைட்ரேட் (KNO3)",
                    chemicalNameEn = "Potassium Nitrate (KNO3)",
                    allocatedKg = 10.0,
                    allocatedBy = "Selvaraj K. (Supervisor)",
                    timestamp = now - 7200000L
                ),
                ChemicalAllocationEntity(
                    id = 2,
                    roomId = 1,
                    roomNumber = "R-101",
                    chemicalNameTa = "கந்தகப் பொடி (Sulphur)",
                    chemicalNameEn = "Sulphur Powder",
                    allocatedKg = 8.5,
                    allocatedBy = "Selvaraj K. (Supervisor)",
                    timestamp = now - 7200000L
                ),
                ChemicalAllocationEntity(
                    id = 3,
                    roomId = 2,
                    roomNumber = "R-102",
                    chemicalNameTa = "பொட்டாசியம் நைட்ரேட் (KNO3)",
                    chemicalNameEn = "Potassium Nitrate (KNO3)",
                    allocatedKg = 15.0,
                    allocatedBy = "Selvaraj K. (Supervisor)",
                    timestamp = now - 3600000L
                ),
                ChemicalAllocationEntity(
                    id = 4,
                    roomId = 2,
                    roomNumber = "R-102",
                    chemicalNameTa = "அலுமினியம் பவுடர்",
                    chemicalNameEn = "Aluminium Flake Powder",
                    allocatedKg = 10.5,
                    allocatedBy = "Selvaraj K. (Supervisor)",
                    timestamp = now - 3600000L
                ),
                ChemicalAllocationEntity(
                    id = 5,
                    roomId = 3,
                    roomNumber = "R-103",
                    chemicalNameTa = "கரிப்பொடி (Charcoal)",
                    chemicalNameEn = "Charcoal Powder",
                    allocatedKg = 8.0,
                    allocatedBy = "Selvaraj K. (Supervisor)",
                    timestamp = now - 1800000L
                )
            )
            for (alloc in initialAllocations) {
                dao.insertAllocation(alloc)
            }
        }
    }
}
