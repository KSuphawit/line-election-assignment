package lineelection.controllers

import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import lineelection.constants.ReportConstant.candidateElectionResultFilename
import lineelection.models.CandidateModel
import lineelection.models.ToggleModel
import lineelection.services.ElectionService
import lineelection.utilities.ResponseEntityUtility
import lineelection.utilities.ResponseEntityUtility.modifyResponseForCSV
import org.junit.Before
import org.junit.Test
import org.springframework.http.ResponseEntity
import kotlin.test.assertEquals

class ElectionControllerTest {

    @InjectMockKs
    lateinit var electionController: ElectionController

    @MockK
    lateinit var electionService: ElectionService

    private val toggleModel = ToggleModel()

    private val candidateModel = CandidateModel()

    @Before
    fun setup() = MockKAnnotations.init(this)

    @Test
    fun testOpenOrCloseVoting() {
        every { electionService.openOrCloseVote(toggleModel) } returns toggleModel

        val result = electionService.openOrCloseVote(toggleModel)
        assertEquals(toggleModel, result)
        verify(exactly = 1) { electionService.openOrCloseVote(toggleModel) }
    }

    @Test
    fun testGetElectionResult() {
        every { electionService.getElectionResult() } returns listOf(candidateModel)

        val result = electionService.getElectionResult()
        assertEquals(listOf(candidateModel), result)
        verify(exactly = 1) { electionService.getElectionResult() }
    }

    @Test
    fun testExportElectionResult() {
        mockkObject(ResponseEntityUtility)
        val fileData = ByteArray(100)
        val expectedResult = ResponseEntity.ok().body(fileData)

        every { electionService.exportElectionResult() } returns fileData
        every { any<ResponseEntity<ByteArray>>().modifyResponseForCSV(candidateElectionResultFilename) } returns expectedResult

        val result = electionController.exportElectionResult()
        assertEquals(expectedResult, result)
        verify(exactly = 1) { electionService.exportElectionResult() }
        verify(exactly = 1) { any<ResponseEntity<ByteArray>>().modifyResponseForCSV(candidateElectionResultFilename) }

        unmockkAll()
    }
}