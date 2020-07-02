package com.riky.museek

import java.time.LocalDate
import java.time.LocalDateTime

class Ad (val brand: String, val model: String, val price: Float, val category: String, val photoId: String, val uid: String, val date: String) {
    constructor() : this("", "", 0f, "", "", "", "")
}