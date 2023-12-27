package com.pp.ppbacked

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import org.xhtmlrenderer.pdf.ITextRenderer
import java.io.ByteArrayOutputStream
import java.io.StringWriter
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@Service
class CertificateService(val csvParser: CsvParser) {

    fun generate(file: MultipartFile) : ByteArray {
        val certificate = csvParser.readCsvFile(file);
        return generateAndZipPersonCertificates(certificate);
    }


    private fun generateAndZipPersonCertificates(certificate: List<PersonCertificate>) : ByteArray {
        val zipBytes = ByteArrayOutputStream();
        ZipOutputStream(zipBytes).use { zipStream ->
            for ((index, cert) in certificate.withIndex()) {
                val pdfBytes = generatePdfBytes(cert)

                zipStream.putNextEntry(ZipEntry("certificate_${cert.firstName}_${cert.lastName}.pdf"))
                zipStream.write(pdfBytes)
                zipStream.closeEntry()
            }
        }

        return zipBytes.toByteArray()
    }

    private fun generatePdfBytes(personCertificate: PersonCertificate): ByteArray {
        val htmlContent = """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Certificate</title>
            <style>
                body {
                    font-family: Arial, sans-serif;
                }
                .certificate {
                    width: 100%;
                    max-width: 800px;
                    margin: 20px auto;
                    border: 2px solid #000;
                }
                .header {
                    text-align: center;
                }
                .content {
                    margin-top: 20px;
                    text-align: center;
                }
                .expiration-date {
                    margin-top: 30px;
                }
            </style>
        </head>
        <body>
            <div class="certificate">
                <div class="header">
                    <h1>Certificate of Completion</h1>
                </div>
                <div class="content">
                    <p>This is to certify that</p>
                    <h2>${personCertificate.firstName} ${personCertificate.lastName}</h2>
                    <p>has successfully completed the course</p>
                    <h3>${personCertificate.name}</h3>
                </div>
                <div class="expiration-date">
                    <p>Expiration Date: ${personCertificate.expirationDate}</p>
                </div>
            </div>
        </body>
        </html>
    """.trimIndent()

        val pdf = generatePdfFromHtml(htmlContent)
        return pdf
    }

    fun generatePdfFromHtml(htmlContent: String): ByteArray {
        val renderer = ITextRenderer()
        val writer = StringWriter()

        val document: Document = Jsoup.parse(htmlContent)
        document.outputSettings().syntax(Document.OutputSettings.Syntax.xml)
        document.outputSettings().escapeMode(org.jsoup.nodes.Entities.EscapeMode.xhtml)

        renderer.setDocumentFromString(document.html())
        renderer.layout()


        val pdfBytes = ByteArrayOutputStream()
        renderer.createPDF(pdfBytes)

        return pdfBytes.toByteArray()
    }
}