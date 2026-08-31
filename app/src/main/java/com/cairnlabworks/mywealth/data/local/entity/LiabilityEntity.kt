package com.cairnlabworks.mywealth.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.cairnlabworks.mywealth.domain.model.LiabilityType

@Entity(
    tableName = "liabilities",
    foreignKeys = [
        ForeignKey(
            entity = PortfolioEntity::class,
            parentColumns = ["id"],
            childColumns = ["portfolioId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("portfolioId")],
)
data class LiabilityEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val portfolioId: Long,
    val type: LiabilityType,
    val name: String,
    val currency: String,
    /** Outstanding amount owed, in [currency]. */
    val amount: Double,
    val notes: String? = null,
    /** User-defined sort order within this liability's type. Lower shows first. */
    val position: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
) {
    val value: Double get() = amount
}
