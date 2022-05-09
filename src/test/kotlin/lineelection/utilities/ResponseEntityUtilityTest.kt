package lineelection.utilities

import lineelection.utilities.ResponseEntityUtility.modifyResponseForCSV
import org.junit.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ResponseEntityUtilityTest {

    @Test
    fun testModifyResponseForCSV() {
        val responseEntity = ResponseEntity<Any>(HttpStatus.OK)
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_OCTET_STREAM
        var result = responseEntity.modifyResponseForCSV()
        assertNull(result.headers.contentDisposition.filename)
        assertEquals(MediaType.APPLICATION_OCTET_STREAM, result.headers.contentType)

        result = responseEntity.modifyResponseForCSV("fileName")
        assertEquals("attachment", result.headers.contentDisposition.type)
        assertEquals("fileName.csv", result.headers.contentDisposition.filename)
        assertEquals(MediaType.APPLICATION_OCTET_STREAM, result.headers.contentType)

        result = responseEntity.modifyResponseForCSV("fileName.csv")
        assertEquals("attachment", result.headers.contentDisposition.type)
        assertEquals("fileName.csv", result.headers.contentDisposition.filename)
        assertEquals(MediaType.APPLICATION_OCTET_STREAM, result.headers.contentType)
    }
}