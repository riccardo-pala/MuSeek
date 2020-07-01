package com.riky.museek

class User (val uid: String, val email: String, val firstname: String, val lastname: String) {
    constructor() : this("", "", "", "")
}