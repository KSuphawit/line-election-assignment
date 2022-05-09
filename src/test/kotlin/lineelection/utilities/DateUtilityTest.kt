package lineelection.utilities

import io.mockk.spyk
import lineelection.Try
import lineelection.utilities.DateUtility.DATE_FORMAT
import lineelection.utilities.DateUtility.DATE_TIME_FORMAT
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DateUtilityTest {

    private val defaultDateFormat = SimpleDateFormat(DATE_FORMAT, Locale.US)
    private val dateTimeFormat = SimpleDateFormat(DATE_TIME_FORMAT, Locale.US)
    private val inputDateString = "August 08, 2011"
    private val inputDateTimeString = "May 08, 2022 23:59"
    private val outputDate = defaultDateFormat.parse(inputDateString)
    private val outputDateTime = dateTimeFormat.parse(inputDateTimeString)

    @Test
    fun testConvertStringToDate() {
        val dateUtility = spyk<DateUtility>()

        // Case string is null
        var result = dateUtility.convertStringToDate(null)
        assertNull(result)

        // Case string is empty
        result = dateUtility.convertStringToDate("")
        assertNull(result)

        // Case not send date format
        result = dateUtility.convertStringToDate(inputDateString)
        assertNotNull(result)
        assertEquals(outputDate, result)

        // Case send format and correct input string
        result = dateUtility.convertStringToDate(inputDateTimeString, DATE_TIME_FORMAT)
        assertNotNull(result)
        assertEquals(outputDateTime, result)

        // Case send format and incorrect input string
        val failedResult = Try.on {
            dateUtility.convertStringToDate(inputDateString, DATE_TIME_FORMAT)
        }
        assertTrue(failedResult.isFailure)
        assertTrue(failedResult.toString().contains("August 08, 2011 is not a format MMMM dd, yyyy HH:mm"))
    }

    @Test
    fun testConvertDateToString() {
        val dateFormat = "ddMMyy"
        val dateUtility = spyk<DateUtility>()

        // Case input is null
        var result = dateUtility.convertDateToString(null)
        assertTrue(result == null)

        // Case not send date format
        result = dateUtility.convertDateToString(outputDate)
        assertEquals(inputDateString, result)

        // Case send format date
        result = dateUtility.convertDateToString(outputDate, dateFormat)
        assertEquals("080811", result)
    }
}