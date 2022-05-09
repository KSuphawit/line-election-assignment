package lineelection.services

import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import lineelection.Try
import lineelection.constants.ElectionStatus
import lineelection.constants.electionApplicationRunOutOfTime
import lineelection.entities.Candidate
import lineelection.entities.Election
import lineelection.entities.assignElection
import lineelection.entities.toCandidateModel
import lineelection.models.CandidateModel
import lineelection.repositories.CandidateRepository
import lineelection.repositories.ElectionRepository
import lineelection.utilities.DateUtility
import lineelection.validators.CandidateValidator
import org.junit.Before
import org.junit.Test
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.util.ReflectionTestUtils
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CandidateServiceTest {

    @InjectMockKs
    lateinit var candidateService: CandidateService

    @MockK
    lateinit var candidateRepository: CandidateRepository

    @MockK
    lateinit var candidateValidator: CandidateValidator

    @MockK
    lateinit var electionRepository: ElectionRepository

    private val candidateModel = CandidateModel(
            1L,
            name = "name",
            dob = "dob",
            bioLink = "bioLink",
            imageLink = "imageLink",
            policy = "policy"
    )

    private val candidate = Candidate(
            1L,
            name = "name",
            dob = Date(),
            bioLink = "bioLink",
            imageLink = "imageLink",
            policy = "policy"
    )

    private val election = Election(id = 3L)

    @Before
    fun setup() = MockKAnnotations.init(this)

    @Test
    fun testGetCandidates() {
        mockkStatic("lineelection.entities.CandidateKt")

        every { candidateRepository.findAll() } returns listOf(candidate)
        every { listOf(candidate).toCandidateModel() } returns listOf(candidateModel)

        val result = candidateService.getCandidates()
        assertEquals(listOf(candidateModel), result)
        verify(exactly = 1) { candidateRepository.findAll() }
        verify(exactly = 1) { listOf(candidate).toCandidateModel() }

        unmockkAll()
    }

    @Test
    fun testGetCandidateDetail() {
        val id = 1L

        // Case Candidate not found
        every { candidateRepository.findByIdOrNull(id) } returns null

        var result = Try.on {
            candidateService.getCandidateDetail(id)
        }
        assertTrue(result.isFailure)
        assertTrue(result.toString().contains("Candidate not found"))
        verify(exactly = 1) { candidateRepository.findByIdOrNull(id) }
        clearAllMocks(answers = false)

        // Case Success
        every { candidateRepository.findByIdOrNull(id) } returns candidate

        result = Try.on {
            candidateService.getCandidateDetail(id)
        }
        assertTrue(result.isSuccess)
        assertEquals(candidate, result.getOrThrow())
        verify(exactly = 1) { candidateRepository.findByIdOrNull(id) }
    }

    @Test
    fun testCreateCandidate() {
        mockkObject(DateUtility)
        mockkStatic("lineelection.entities.CandidateKt")

        val candidateService = spyk<CandidateService>(recordPrivateCalls = true)
        val date = Date()

        every { candidateService.candidateValidator } returns candidateValidator
        every { candidateService.candidateRepository } returns candidateRepository

        every { candidateValidator.candidateInformationValidation(candidateModel) } just Runs
        every { candidateService["getAndValidateElection"]() } returns election
        every { DateUtility.convertStringToDate(candidateModel.dob) } returns date
        every {
            match<Candidate> {
                it.name == candidateModel.name &&
                        it.dob == date &&
                        it.bioLink == candidateModel.bioLink &&
                        it.imageLink == candidateModel.imageLink &&
                        it.policy == candidateModel.policy
            }.assignElection(election)
        } returns candidate
        every { candidateRepository.save(candidate) } returns candidate
        every { candidate.toCandidateModel() } returns candidateModel

        val result = candidateService.createCandidate(candidateModel)
        assertEquals(candidateModel, result)
        verify(exactly = 1) { candidateValidator.candidateInformationValidation(candidateModel) }
        verify(exactly = 1) { candidateService["getAndValidateElection"]() }
        verify(exactly = 1) { DateUtility.convertStringToDate(candidateModel.dob) }
        verify(exactly = 1) {
            match<Candidate> {
                it.name == candidateModel.name &&
                        it.dob == date &&
                        it.bioLink == candidateModel.bioLink &&
                        it.imageLink == candidateModel.imageLink &&
                        it.policy == candidateModel.policy
            }.assignElection(election)
        }
        verify(exactly = 1) { candidateRepository.save(candidate) }
        verify(exactly = 1) { candidate.toCandidateModel() }

        unmockkAll()
    }

    @Test
    fun testUpdateCandidate() {
        mockkObject(DateUtility)
        mockkStatic("lineelection.entities.CandidateKt")

        val candidateService = spyk<CandidateService>(recordPrivateCalls = true)
        val candidateModel = candidateModel.copy(
                name = null,
                dob = null,
                imageLink = "new-image-link",
                policy = "  "
        )
        val updatedCandidate = candidate.copy(imageLink = candidateModel.imageLink)

        every { candidateService.candidateValidator } returns candidateValidator
        every { candidateService.candidateRepository } returns candidateRepository

        every { candidateValidator.updateCandidateValidation(candidateModel) } just Runs
        every { candidateService["getAndValidateElection"]() } returns election
        every { candidateRepository.findByIdOrNull(candidateModel.id) } returns null

        // Case Candidate not found
        var result = Try.on {
            candidateService.updateCandidate(candidateModel)
        }
        assertTrue(result.isFailure)
        assertTrue(result.toString().contains("Candidate not found"))
        verify(exactly = 1) { candidateValidator.updateCandidateValidation(candidateModel) }
        verify(exactly = 1) { candidateService["getAndValidateElection"]() }
        verify(exactly = 1) { candidateRepository.findByIdOrNull(candidateModel.id) }
        verify(exactly = 0) { candidateRepository.save(any()) }
        verify(exactly = 0) { any<Candidate>().toCandidateModel() }
        clearAllMocks(answers = false)

        // Case Success
        every { candidateRepository.save(updatedCandidate) } returns updatedCandidate
        every { updatedCandidate.toCandidateModel() } returns candidateModel
        every { candidateRepository.findByIdOrNull(candidateModel.id) } returns candidate

        result = Try.on {
            candidateService.updateCandidate(candidateModel)
        }
        assertTrue(result.isSuccess)
        assertEquals(candidateModel, result.getOrThrow())
        verify(exactly = 1) { candidateValidator.updateCandidateValidation(candidateModel) }
        verify(exactly = 1) { candidateService["getAndValidateElection"]() }
        verify(exactly = 1) { candidateRepository.findByIdOrNull(candidateModel.id) }
        verify(exactly = 1) { candidateRepository.save(updatedCandidate) }
        verify(exactly = 1) { updatedCandidate.toCandidateModel() }

        unmockkAll()
    }

    @Test
    fun testGetAndValidateElection() {
        val election = Election(status = ElectionStatus.RUN_FOR_ELECTION.name)

        every { electionRepository.findCurrentElection() } returns null andThen election.copy(status = ElectionStatus.VOTING.name) andThen election

        // Case Election not found
        var result = Try.on {
            ReflectionTestUtils.invokeMethod<Election>(candidateService, "getAndValidateElection")
        }
        assertTrue(result.isFailure)
        assertTrue(result.toString().contains("Election not found"))
        verify(exactly = 1) { electionRepository.findCurrentElection() }
        clearAllMocks(answers = false)

        // Case Election application run out of time
        result = Try.on {
            ReflectionTestUtils.invokeMethod<Election>(candidateService, "getAndValidateElection")
        }
        assertTrue(result.isFailure)
        assertTrue(result.toString().contains(electionApplicationRunOutOfTime))
        verify(exactly = 1) { electionRepository.findCurrentElection() }
        clearAllMocks(answers = false)

        // Case Success
        result = Try.on {
            ReflectionTestUtils.invokeMethod<Election>(candidateService, "getAndValidateElection")
        }
        assertTrue(result.isSuccess)
        assertEquals(election, result.getOrThrow())
        verify(exactly = 1) { electionRepository.findCurrentElection() }
    }

    @Test
    fun testDeleteCandidate() {
        val id = 1L

        // Case Candidate not found
        every { candidateRepository.findByIdOrNull(id) } returns null

        var result = Try.on {
            candidateService.deleteCandidate(id)
        }
        assertTrue(result.isFailure)
        assertTrue(result.toString().contains("Candidate not found"))
        verify(exactly = 1) { candidateRepository.findByIdOrNull(id) }
        verify(exactly = 0) { candidateRepository.delete(any()) }
        clearAllMocks(answers = false)

        // Case Success
        every { candidateRepository.findByIdOrNull(id) } returns candidate
        every { candidateRepository.delete(candidate) } returns Unit

        result = Try.on {
            candidateService.deleteCandidate(id)
        }
        assertTrue(result.isSuccess)
        verify(exactly = 1) { candidateRepository.findByIdOrNull(id) }
        verify(exactly = 1) { candidateRepository.delete(candidate) }
    }

}