package com.riky.museek.classes

class AdMemberBand (val aid: String, val bandName: String, val region: Int, val musician: Int, val description: String, val uid: String) {
    constructor() : this("", "", 0, 0, "", "")
}