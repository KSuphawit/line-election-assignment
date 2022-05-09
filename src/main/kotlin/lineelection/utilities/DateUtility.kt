package lineelection.utilities

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object DateUtility {

    const val DATE_FORMAT = "MMMM dd, yyyy"
    const val DATE_TIME_FORMAT = "MMMM dd, yyyy HH:mm"

    /**
     * Method for convert string date to date
     *
     * @param strDate: string date
     * @param format: string date format
     */
    fun convertStringToDate(strDate: String?, format: String = DATE_FORMAT): Date? {

        if (strDate.isNullOrEmpty()) return null

        val simpleDateFormat = SimpleDateFormat(format, Locale.US)

        return try {
            simpleDateFormat.parse(strDate)
        } catch (e: ParseException) {
            throw IllegalArgumentException("$strDate is not a format $format")
        }
    }

    /**
     * Method for date to string date
     *
     * @param date: date
     * @param format: format that want to convert
     */
    fun convertDateToString(date: Date?, format: String = DATE_FORMAT): String? {
        if (date == null) return null

        val simpleDateFormat = SimpleDateFormat(format, Locale.US)

        return simpleDateFormat.format(date)
    }
}