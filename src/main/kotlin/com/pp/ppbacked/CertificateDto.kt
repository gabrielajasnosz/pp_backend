package com.pp.ppbacked

import java.time.LocalDate

class CertificateDto() {
    lateinit var receiverName: String
    lateinit var receiverSurname: String
    lateinit var checksum: String
    lateinit var expireDate: LocalDate
    lateinit var issueDate: LocalDate

    constructor(
        receiverName: String,
        receiverSurname: String,
        checksum: String,
        expireDate: LocalDate,
        issueDate: LocalDate
    ): this() {
          this.receiverName = receiverName
          this.receiverSurname = receiverSurname
          this.checksum = checksum
          this.expireDate = expireDate
        this.issueDate = issueDate
    }
}