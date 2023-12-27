package com.pp.ppbacked

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import org.xhtmlrenderer.pdf.ITextRenderer
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.math.BigInteger
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.security.MessageDigest
import java.time.LocalDate


@Service
class CertificateService(val csvParser: CsvParser) {

    fun generate(file: MultipartFile, issuer: String) : MutableList<CertificateDto> {
        val certificates = csvParser.readCsvFile(file);
        return generateCertificates(certificates, issuer)
    }

    private fun generateCertificates(certificates: List<PersonCertificate>, issuer: String): MutableList<CertificateDto> {
        var certificatesAsPdfs = generatePdfBytes(certificates, issuer)
        return generateCertificatesResult(certificates, certificatesAsPdfs);
    }

    private fun generatePdfBytes(certificate: List<PersonCertificate>, issuer: String): MutableList<ByteArray> {
        val pdfs = mutableListOf<ByteArray>()
        for ((index, cert) in certificate.withIndex()) {
            val pdfBytes = generatePdfBytes(cert, issuer)
            saveFileToResources(pdfBytes, cert)
            pdfs.add(pdfBytes)
        }
        return pdfs
    }

    private fun saveFileToResources(pdfBytes: ByteArray, cert: PersonCertificate) {
        var timestamp = System.currentTimeMillis();
        val newFile: Path = Paths.get("src/main/resources/pdf/" + "certificate_${cert.firstName}_${cert.lastName}_${timestamp}.pdf")
        Files.createDirectories(newFile.parent)
        FileOutputStream("src/main/resources/pdf/" + "certificate_${cert.firstName}_${cert.lastName}_${timestamp}.pdf").use { fos -> fos.write(pdfBytes) }
    }

    private fun getChecksum(bytes: ByteArray): String {
        val hash: ByteArray = MessageDigest.getInstance("MD5").digest(bytes)
        val checksum = BigInteger(1, hash).toString(16)
        return checksum
    }

    private fun generateCertificatesResult(certificates: List<PersonCertificate>, certificatesAsPdf: List<ByteArray>): MutableList<CertificateDto> {
        var list = mutableListOf<CertificateDto>()
        certificates.forEachIndexed { index, it ->
            list.add(CertificateDto(it.firstName, it.lastName, getChecksum(certificatesAsPdf[index]), it.expirationDate, LocalDate.now()))
        }
        return list
    }

    private fun generatePdfBytes(personCertificate: PersonCertificate, issuer: String): ByteArray {
        val htmlContent = """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Certificate</title>
            <link href='https://fonts.googleapis.com/css?family=Libre Baskerville' rel='stylesheet'>
            <style>
                body {
                    font-family: 'Nunito Sans', sans-serif;
                }
                .certificate {
                    max-width: 800px;
                    margin: 10px;
                    border: 10px solid #507091;
                    border-radius: 10px;
                    padding: 50px;
                }
                .header {
                    font-family: 'Libre Baskerville', serif;
                    text-align: center;
                }
                .certificate-header {
                    font-size: 50px;
                    text-transform: uppercase;
                    display: block;
                }
                .certificate-subheader {
                    font-size: 25px;
                    text-transform: uppercase;
                }
                .certificate-receiver {
                    display: block;
                    font-family: 'Libre Baskerville', serif;
                    font-weight: bold;
                    text-decoration: underline;
                    font-size: 30px;
                    text-align: center;
                    margin: 10px 0;
                }
                .content {
                    margin-top: 30px;
                    text-align: center;
                }
                .issuer {
                    padding: 5px;
                    display: inline-block;
                    border-bottom: 2px solid #507091;
                }
                .certificate-course-name {
                    font-weight: bold;
                    font-size: 25px;
                    text-transform: capitalize;
                    font-family: 'Libre Baskerville', serif;
                    text-align: center;
                    display: block;
                    margin: 10px 0;
                }
                .my-footer {
                    text-align: end;
                    margin-top: 30px;
                    display: block;
                }
                .issuer-container {
                    display: inline-block;
                    text-align: center;
                }
                .issuer-label {
                    display: block;
                    margin-top: 5px;
                    font-family: 'Libre Baskerville', serif;
                }
            </style>
        </head>
    <body>
    <div class="certificate">
        <div class="header">
            <span class="certificate-header">Certificate</span>
            <span class="certificate-subheader">of Completion</span>
        </div>
        <div class="content">
            <div>This is to certify that</div>
            <div class="certificate-receiver">${personCertificate.firstName} ${personCertificate.lastName}</div>
            <div class="">has successfully completed the course</div>
            <div class="certificate-course-name">${personCertificate.name}</div>
            <div class="">Certificate valid until ${personCertificate.expirationDate}</div>
        </div>
        <div class="my-footer">
            <div class="issuer-container">
                <div class="issuer">
                    <span>${issuer}</span>
                </div>
                <span class="issuer-label">Issuer</span>
            </div>
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