package com.pp.ppbacked

import lombok.AllArgsConstructor
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
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
    fun generate(@RequestParam("file") file: MultipartFile): ResponseEntity<ByteArray> {
        return try {
            val zipBytes = certificateService.generate(file)

            val headers = HttpHeaders()
            headers.contentType = MediaType.parseMediaType("application/zip")
            headers.set("Content-Disposition","attachment; filename=generated_files.zip")

            ResponseEntity(zipBytes, headers, HttpStatus.OK)
        } catch (e: Exception) {
            e.printStackTrace()
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
}