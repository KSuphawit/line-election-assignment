package lineelection.controllers

import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import lineelection.Try
import lineelection.entities.Candidate
import lineelection.entities.toCandidateModel
import lineelection.models.CandidateModel
import lineelection.services.CandidateService
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CandidateControllerTest {

    @InjectMockKs
    lateinit var candidateController: CandidateController

    @MockK
    lateinit var candidateService: CandidateService

    private val candidateModel = CandidateModel()

    private val candidate = Candidate()

    @Before
    fun setup() = MockKAnnotations.init(this)

    @Test
    fun testGetCandidates() {
        every { candidateService.getCandidates() } returns listOf(candidateModel)

        val result = candidateController.getCandidates()
        assertEquals(listOf(candidateModel), result)
        verify(exactly = 1) { candidateService.getCandidates() }
    }

    @Test
    fun testGetCandidateDetail() {
        mockkStatic("lineelection.entities.CandidateKt")

        every { candidateService.getCandidateDetail(1L) } returns candidate
        every { candidate.toCandidateModel() } returns candidateModel

        val result = candidateController.getCandidateDetail(1L)
        assertEquals(candidateModel, result)
        verify(exactly = 1) { candidateService.getCandidateDetail(1L) }
        verify(exactly = 1) { candidate.toCandidateModel() }

        unmockkAll()
    }

    @Test
    fun testCreateCandidate() {
        every { candidateService.createCandidate(candidateModel) } returns candidateModel

        val result = candidateController.createCandidate(candidateModel)
        assertEquals(candidateModel, result)
        verify(exactly = 1) { candidateService.createCandidate(candidateModel) }
    }

    @Test
    fun testUpdateCandidate() {
        every { candidateService.updateCandidate(candidateModel) } returns candidateModel

        val result = candidateController.updateCandidate(candidateModel)
        assertEquals(candidateModel, result)
        verify(exactly = 1) { candidateService.updateCandidate(candidateModel) }
    }

    @Test
    fun testDeleteCandidate() {
        every { candidateService.deleteCandidate(1L) } just Runs

        val result = Try.on {
            candidateController.deleteCandidate(1L)
        }
        assertTrue(result.isSuccess)
        verify(exactly = 1) { candidateService.deleteCandidate(1L) }
    }
}