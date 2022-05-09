package lineelection.services

import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.InjectMockKs
import lineelection.Try
import lineelection.constants.ReportConstant.candidateElectionResultHeader
import org.junit.Before
import org.junit.Test
import kotlin.test.assertTrue

class ReportServiceTest {

    @InjectMockKs
    lateinit var reportService: ReportService

    @Before
    fun setup() = MockKAnnotations.init(this)

    @Test
    fun testGenerateCSVAsByte() {
        val content = listOf(arrayOf("1", "name", "1", "25"))

        val result = Try.on {
            reportService.generateCSVAsByte(candidateElectionResultHeader, content)
        }
        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().isNotEmpty())
    }
}