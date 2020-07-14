package com.riky.museek.classes

class Ad (val aid: String, val brand: String, val model: String, val price: Double, val category: Int, val condition: Int, val photoId: String, val uid: String, val date: String) {
    constructor() : this("", "", "", 0.0, 0, 0, "", "", "")
    constructor(ad: Ad) : this(ad.aid, ad.brand, ad.model, ad.price, ad.category, ad.condition, ad.photoId, ad.uid, ad.date)
}