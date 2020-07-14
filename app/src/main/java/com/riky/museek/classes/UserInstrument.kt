package com.riky.museek.classes

class UserInstrument (val nation: String, val city: String, val street: String, val civic: String, val inner: String? = "", val cap: String) {
    constructor() : this("", "", "", "", "", "")
}