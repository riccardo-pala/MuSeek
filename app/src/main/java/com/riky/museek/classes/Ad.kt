package com.riky.museek.classes

class Ad (val aid: String, val brand: String, val model: String, val price: Float, val category: Int, val photoId: String, val uid: String, val date: String) {
    constructor() : this("", "", "", 0f, 0, "", "", "")
}