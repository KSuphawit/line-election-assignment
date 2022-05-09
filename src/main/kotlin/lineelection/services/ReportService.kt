package lineelection.services

import com.opencsv.CSVWriter
import lineelection.constants.cannotExportReportFile
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.io.OutputStreamWriter


@Service
class ReportService {

    /**
     * Method for generate CSV file as byte array
     *
     * @param header : csv header,
     * @param content: csv line content
     */
    fun generateCSVAsByte(header: List<String>, content: List<Array<String>>): ByteArray {
        return try {
            val byteArrayOutputStream = ByteArrayOutputStream()
            val outputStreamWriter = OutputStreamWriter(byteArrayOutputStream)
            val writer = CSVWriter(outputStreamWriter)

            writer.writeNext(header.toTypedArray())
            writer.writeAll(content)

            outputStreamWriter.flush()
            byteArrayOutputStream.toByteArray()
        } catch (e: Exception) {
            throw IllegalStateException(cannotExportReportFile)
        }
    }
}