package com.pp.ppbacked

class CertificateResponse() {
    lateinit var checksum: String
    lateinit var recipientName: String
    lateinit var recipientSurname: String
    lateinit var daysValid: String
    lateinit var certUrl: String
    lateinit var owner: String

    constructor(
        checksum: String,
        recipientName: String,
        recipientSurname: String,
        daysValid: String,
        certUrl: String,
        owner: String
    ): this() {
        this.checksum = checksum
        this.recipientName = recipientName
        this.recipientSurname = recipientSurname
        this.daysValid = daysValid
        this.certUrl = certUrl
        this.owner = owner
    }
}