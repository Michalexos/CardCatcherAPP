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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCatalog(products: List<CatalogProductEntity>)

    @Query("SELECT * FROM transactions ORDER BY dateEpochDay DESC, id DESC")
    fun observeTransactions(): Flow<List<InventoryTransactionEntity>>

    @Query("SELECT * FROM catalog ORDER BY name ASC")
    fun observeCatalog(): Flow<List<CatalogProductEntity>>
}
