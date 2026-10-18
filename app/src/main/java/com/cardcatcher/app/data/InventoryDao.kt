package com.cardcatcher.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {
    @Insert
    suspend fun insertTransaction(transaction: InventoryTransactionEntity)

    @Query("SELECT * FROM transactions ORDER BY dateEpochDay DESC, id DESC")
    fun observeTransactions(): Flow<List<InventoryTransactionEntity>>

    @Query("SELECT DISTINCT productName FROM transactions ORDER BY productName ASC")
    fun observeKnownProducts(): Flow<List<String>>

    @Query("SELECT DISTINCT imageUri FROM transactions WHERE productName = :productName AND imageUri IS NOT NULL LIMIT 1")
    suspend fun getImageUriForProduct(productName: String): String?

    @Query("DELETE FROM transactions WHERE id = :transactionId")
    suspend fun deleteTransaction(transactionId: Long)
}
