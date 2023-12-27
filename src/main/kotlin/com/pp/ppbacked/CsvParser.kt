package com.pp.ppbacked

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class CsvParser {

    fun readCsvFile(file: MultipartFile): List<PersonCertificate> {
        val certificates = mutableListOf<PersonCertificate>()
        file.inputStream.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader()).use { csvParser ->
                    for (csvRecord in csvParser) {
                        val name = csvRecord.get("Name")
                        val firstName = csvRecord.get("FirstName")
                        val lastName = csvRecord.get("LastName")
                        val expirationDate = csvRecord.get("ExpirationDate")
                        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                        val localDate = LocalDate.parse(expirationDate, formatter)

                        val personCertificate = PersonCertificate(name, firstName, lastName, localDate)
                        certificates.add(personCertificate)
                    }
                }
            }
        }
        return certificates
    }
}