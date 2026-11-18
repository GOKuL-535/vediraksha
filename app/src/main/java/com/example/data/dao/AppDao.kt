package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ChemicalEntryEntity
import com.example.data.model.ChemicalStockEntity
import com.example.data.model.ChemicalAllocationEntity
import com.example.data.model.CrackerStockEntity
import com.example.data.model.RoomEntity
import com.example.data.model.ShiftEntity
import com.example.data.model.SosEventEntity
import com.example.data.model.WageEntryEntity
import com.example.data.model.WorkerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // Rooms
    @Query("SELECT * FROM factory_rooms ORDER BY id ASC")
    fun getAllRooms(): Flow<List<RoomEntity>>

    @Query("SELECT * FROM factory_rooms WHERE id = :roomId LIMIT 1")
    suspend fun getRoomById(roomId: Long): RoomEntity?

    @Query("SELECT * FROM factory_rooms WHERE LOWER(roomNumber) = LOWER(:roomNumber) LIMIT 1")
    suspend fun getRoomByNumber(roomNumber: String): RoomEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRooms(rooms: List<RoomEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoom(room: RoomEntity): Long

    @Update
    suspend fun updateRoom(room: RoomEntity)

    @Query("DELETE FROM factory_rooms WHERE id = :id")
    suspend fun deleteRoom(id: Long)

    // Chemical Entries
    @Query("SELECT * FROM chemical_entries ORDER BY timestamp DESC")
    fun getAllChemicalEntries(): Flow<List<ChemicalEntryEntity>>

    @Query("SELECT * FROM chemical_entries WHERE roomId = :roomId ORDER BY timestamp DESC")
    fun getChemicalEntriesForRoom(roomId: Long): Flow<List<ChemicalEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChemicalEntry(entry: ChemicalEntryEntity): Long

    // Shifts
    @Query("SELECT * FROM shift_records ORDER BY clockInTime DESC")
    fun getAllShifts(): Flow<List<ShiftEntity>>

    @Query("SELECT * FROM shift_records WHERE status = 'ACTIVE' LIMIT 1")
    fun getActiveShift(): Flow<ShiftEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShift(shift: ShiftEntity): Long

    @Update
    suspend fun updateShift(shift: ShiftEntity)

    // Wages
    @Query("SELECT * FROM wage_entries ORDER BY timestamp DESC")
    fun getAllWages(): Flow<List<WageEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWage(wage: WageEntryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWages(wages: List<WageEntryEntity>)

    @Update
    suspend fun updateWage(wage: WageEntryEntity)

    // SOS
    @Query("SELECT * FROM sos_events ORDER BY timestamp DESC")
    fun getAllSosEvents(): Flow<List<SosEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSosEvent(event: SosEventEntity): Long

    @Update
    suspend fun updateSosEvent(event: SosEventEntity)

    @Query("UPDATE sos_events SET status = 'RESOLVED' WHERE id = :id")
    suspend fun resolveSosEvent(id: Long)

    // Workers / Labors Management
    @Query("SELECT * FROM factory_workers ORDER BY nameEn ASC")
    fun getAllWorkers(): Flow<List<WorkerEntity>>

    @Query("SELECT * FROM factory_workers WHERE assignedRoom = :roomNumber")
    fun getWorkersInRoom(roomNumber: String): Flow<List<WorkerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorker(worker: WorkerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkers(workers: List<WorkerEntity>)

    @Update
    suspend fun updateWorker(worker: WorkerEntity)

    @Query("DELETE FROM factory_workers WHERE id = :id")
    suspend fun deleteWorker(id: Long)

    // Cracker Stocks (Target Planning)
    @Query("SELECT * FROM cracker_stocks ORDER BY id ASC")
    fun getAllCrackerStocks(): Flow<List<CrackerStockEntity>>

    @Query("SELECT * FROM cracker_stocks WHERE LOWER(crackerNameEn) = LOWER(:name) LIMIT 1")
    suspend fun getCrackerStockByName(name: String): CrackerStockEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrackerStock(stock: CrackerStockEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrackerStocks(stocks: List<CrackerStockEntity>)

    @Update
    suspend fun updateCrackerStock(stock: CrackerStockEntity)

    @Query("UPDATE cracker_stocks SET targetQuantity = :quantity, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateCrackerQuantity(id: Long, quantity: Int, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM cracker_stocks WHERE id = :id")
    suspend fun deleteCrackerStock(id: Long)

    // Chemical Stocks (Raw Materials Inventory)
    @Query("SELECT * FROM chemical_stocks ORDER BY id ASC")
    fun getAllChemicalStocks(): Flow<List<ChemicalStockEntity>>

    @Query("SELECT * FROM chemical_stocks WHERE LOWER(chemicalNameEn) = LOWER(:name) LIMIT 1")
    suspend fun getChemicalStockByName(name: String): ChemicalStockEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChemicalStock(stock: ChemicalStockEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChemicalStocks(stocks: List<ChemicalStockEntity>)

    @Update
    suspend fun updateChemicalStock(stock: ChemicalStockEntity)

    @Query("UPDATE chemical_stocks SET availableStockKg = :stockKg WHERE id = :id")
    suspend fun updateChemicalStockKg(id: Long, stockKg: Double)

    // Chemical Allocations (Room Distribution)
    @Query("SELECT * FROM chemical_allocations ORDER BY timestamp DESC")
    fun getAllAllocations(): Flow<List<ChemicalAllocationEntity>>

    @Query("SELECT * FROM chemical_allocations WHERE roomId = :roomId ORDER BY timestamp DESC")
    fun getAllocationsForRoom(roomId: Long): Flow<List<ChemicalAllocationEntity>>

    @Query("SELECT * FROM chemical_allocations WHERE roomId = :roomId")
    suspend fun getAllocationsForRoomSync(roomId: Long): List<ChemicalAllocationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllocation(allocation: ChemicalAllocationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllocations(allocations: List<ChemicalAllocationEntity>)

    @Update
    suspend fun updateAllocation(allocation: ChemicalAllocationEntity)

    @Query("UPDATE chemical_allocations SET allocatedKg = :newKg, timestamp = :timestamp WHERE id = :id")
    suspend fun updateAllocationKg(id: Long, newKg: Double, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM chemical_allocations WHERE id = :id")
    suspend fun deleteAllocation(id: Long)

    @Query("DELETE FROM chemical_allocations WHERE roomId = :roomId")
    suspend fun deleteAllocationsForRoom(roomId: Long)
}
