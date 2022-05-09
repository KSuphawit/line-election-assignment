package lineelection.utilities

import lineelection.constants.ATTACHMENT
import lineelection.constants.CSV_EXTENSION
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity

object ResponseEntityUtility {

    /**
     * Method for modify response to support file csv
     * @param filename : csv filename
     */
    fun <T> ResponseEntity<T>.modifyResponseForCSV(filename: String? = null): ResponseEntity<T> {
        val headers = HttpHeaders.writableHttpHeaders(this.headers)
        val finalFilename = filename?.substringBeforeLast(CSV_EXTENSION)

        finalFilename?.run {
            val contentDisposition = ContentDisposition.builder(ATTACHMENT).filename("${finalFilename}$CSV_EXTENSION").build()
            headers.contentDisposition = contentDisposition
        }

        headers.contentType = MediaType.APPLICATION_OCTET_STREAM

        return this
    }
}