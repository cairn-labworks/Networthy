package com.cairnlabworks.mywealth.data.local

import androidx.room.TypeConverter
import com.cairnlabworks.mywealth.domain.model.AssetType
import com.cairnlabworks.mywealth.domain.model.LiabilityType

class Converters {
    @TypeConverter
    fun assetTypeToString(type: AssetType): String = type.name

    @TypeConverter
    fun stringToAssetType(value: String): AssetType = AssetType.fromName(value)

    @TypeConverter
    fun liabilityTypeToString(type: LiabilityType): String = type.name

    @TypeConverter
    fun stringToLiabilityType(value: String): LiabilityType = LiabilityType.fromName(value)
}
