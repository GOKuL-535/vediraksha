package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.AppDatabase
import com.example.data.model.WorkerEntity
import com.example.data.repository.AppRepository
import com.example.util.CryptoUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: AppRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = AppRepository(database.appDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun readStringFromContext() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("VediRaksha", appName)
    }

    @Test
    fun testTamperProofSealGeneration() {
        val hash = CryptoUtils.generateWageHash(
            id = 101L,
            workerName = "Kaliappan",
            date = "04 Oct 2026",
            workType = "Bijili Cracker Rolling",
            quantity = 1500,
            amount = 600.0,
            timestamp = 1770000000000L
        )
        assertNotNull(hash)
        assertTrue(hash.length == 64) // Valid SHA-256 hex string
    }

    @Test
    fun testBiometricShiftClockInAndClockOutStoredLocallyInRoom() = runBlocking {
        // 1. Clock In via Repository with Biometrics
        val shiftId = repository.startShift(
            workerId = "TN-SVK-4091",
            workerName = "Kaliappan",
            shiftType = "MORNING",
            activeRoom = "Room R-101",
            workCategory = "Bijili Cracker Rolling",
            pieceRate = 0.40
        )
        assertTrue(shiftId > 0)

        // Verify stored in Room database
        val activeShift = repository.activeShift.first()
        assertNotNull(activeShift)
        assertEquals("ACTIVE", activeShift?.status)
        assertEquals("Room R-101", activeShift?.activeRoom)
        assertTrue(activeShift?.biometricVerified == true)
        assertTrue(activeShift?.clockInBiometricHash?.isNotBlank() == true)

        // 2. Add pieces produced
        repository.updateShiftProgress(activeShift!!, 500)

        val updatedShift = repository.activeShift.first()
        assertEquals(500, updatedShift?.piecesProduced)
        assertEquals(200.0, updatedShift?.calculatedWage ?: 0.0, 0.01)

        // 3. Clock Out via Repository with Biometrics
        val wageEntry = repository.endShift(updatedShift!!)

        // Verify Shift is now completed in Room
        val shifts = repository.allShifts.first()
        val completedShift = shifts.first { it.id == shiftId }
        assertEquals("COMPLETED", completedShift.status)
        assertNotNull(completedShift.clockOutTime)
        assertTrue(completedShift.clockOutBiometricHash.isNotBlank())

        // Verify Wage slip automatically generated and stored in Room
        val wages = repository.allWages.first()
        assertTrue(wages.any { it.workerName == "Kaliappan" && it.unitsCompleted == 500 })
        assertEquals(200.0, wageEntry.netAmount, 0.01)
        assertTrue(wageEntry.workerBiometricSigned)
    }

    @Test
    fun testLaborWorkforceManagementInRoomDatabase() = runBlocking {
        // 1. Add a worker to Room
        val newWorker = WorkerEntity(
            workerId = "TN-SVK-9999",
            nameEn = "Ramasamy",
            nameTa = "ராமசாமி",
            assignedRoom = "Room R-102",
            roleEn = "Mixer Specialist",
            roleTa = "மருந்து கலவை நிபுணர்",
            status = "ON_DUTY",
            shiftType = "MORNING",
            emergencyPhone = "+91 94431 11223"
        )
        val insertedId = repository.addWorker(newWorker)
        assertTrue(insertedId > 0)

        // 2. Fetch workers list from Room Flow
        val workersList = repository.allWorkers.first()
        val found = workersList.firstOrNull { it.workerId == "TN-SVK-9999" }
        assertNotNull(found)
        assertEquals("Room R-102", found?.assignedRoom)
        assertEquals("ON_DUTY", found?.status)

        // 3. Reassign room (Shed Management)
        repository.updateWorkerRoom(found!!, "Room R-101")
        val updatedList = repository.allWorkers.first()
        val updated = updatedList.first { it.workerId == "TN-SVK-9999" }
        assertEquals("Room R-101", updated.assignedRoom)

        // 4. Update worker duty/break status
        repository.updateWorkerStatus(updated, "ON_BREAK")
        val breakList = repository.allWorkers.first()
        val onBreakWorker = breakList.first { it.workerId == "TN-SVK-9999" }
        assertEquals("ON_BREAK", onBreakWorker.status)
    }
}
