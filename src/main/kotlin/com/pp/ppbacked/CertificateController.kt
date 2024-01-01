package com.pp.ppbacked

import lombok.AllArgsConstructor
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/pp")
@AllArgsConstructor
class CertificateController(private val csvParser: CsvParser, private val certificateService: CertificateService) {

    @PostMapping("/import")
    fun importCsv(@RequestParam("file") file: MultipartFile) : ResponseEntity<Void> {
        val readCsvFile = csvParser.readCsvFile(file);
        readCsvFile.stream().forEach { item -> println(item.name) }

        return ResponseEntity.ok(null);
    }

    @PostMapping("/generate")
    fun generate(@RequestPart file: MultipartFile, @RequestPart issuer: String): ResponseEntity<List<CertificateResponse>> {
        return try {
            val certificates = certificateService.generate(file, issuer)
            ResponseEntity.ok(certificates)
        } catch (e: Exception) {
            e.printStackTrace()
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
}