package com.research.oproom

import com.google.gson.annotations.SerializedName

data class Post(
    @SerializedName("id")
    var id: Int = 1,

    @SerializedName("temperature")
    var temperature: Float = 0f,

    @SerializedName("humidity")
    var humidity: Float = 0f
)
