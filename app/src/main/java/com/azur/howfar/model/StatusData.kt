package com.azur.howfar.model

import com.azur.howfar.models.StatusDeliveryType.PENDING

data class StatusData(
    var serverTime: String = "",
    var storageLink: String = "",
    var statusType: Int = 0,
    var caption: String = "",
    var timeSent: String = "",
    var captionBackgroundColor: String = "#660099",
    var statusDeliveryType: Int = PENDING,
    var image: String = "",
)