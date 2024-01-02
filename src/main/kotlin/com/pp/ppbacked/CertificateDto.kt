package com.pp.ppbacked

import java.time.LocalDate

class CertificateDto() {
    lateinit var recipientName: String
    lateinit var recipientSurname: String
    lateinit var checksum: String
    lateinit var expireDate: LocalDate
    lateinit var issueDate: LocalDate
    lateinit var url: String
    lateinit var owner: String

    constructor(
        recipientName: String,
        recipientSurname: String,
        checksum: String,
        expireDate: LocalDate,
        issueDate: LocalDate,
        url: String,
        owner: String
    ): this() {
          this.recipientName = recipientName
          this.recipientSurname = recipientSurname
          this.checksum = checksum
          this.expireDate = expireDate
          this.issueDate = issueDate
          this.url = url
          this.owner = owner
    }
}