package com.riky.museek.classes

class User (val uid: String, val email: String, val firstname: String, val lastname: String, val photoId: String, val phone: String) {
    constructor() : this("", "", "", "", "", "")
}