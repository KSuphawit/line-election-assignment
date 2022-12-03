package lineelection.services

import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import lineelection.Try
import lineelection.constants.ElectionStatus
import lineelection.constants.electionIsClosed
import lineelection.entities.*
import lineelection.models.CandidateModel
import lineelection.models.VoteModel
import lineelection.properties.WebSocketProperties
import lineelection.repositories.CandidateRepository
import lineelection.repositories.ElectionRepository
import lineelection.repositories.VoterBallotRepository
import lineelection.utilities.isValidNationalId
import lineelection.validators.VoteValidator
import org.junit.Before
import org.junit.Test
import org.springframework.data.repository.findByIdOrNull
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.test.util.ReflectionTestUtils
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class VoteServiceTest {

    @InjectMockKs
    lateinit var voteService: VoteService

    @MockK
    lateinit var candidateService: CandidateService

    @MockK
    lateinit var candidateRepository: CandidateRepository

    @MockK
    lateinit var electionRepository: ElectionRepository

    @MockK
    lateinit var simpMessagingTemplate: SimpMessagingTemplate

    @MockK
    lateinit var voterBallotRepository: VoterBallotRepository

    @MockK
    lateinit var voteValidator: VoteValidator

    @MockK
    lateinit var webSocketProperties: WebSocketProperties

    private val voteModel = VoteModel(nationalId = "nationId", candidateId = 1L)

    private val election = Election(id = 3L, status = ElectionStatus.VOTING.name)

    private val voterBallot = VoterBallot(electionId = election.id, nationalId = voteModel.nationalId)

    private val candidate = Candidate(
        id = 1L,
        name = "name",
        dob = Date(),
        bioLink = "bioLink",
        imageLink = "imageLink",
        policy = "policy",
        votedCount = 78
    )

    @Before
    fun setup() = MockKAnnotations.init(this)

    @Test
    fun testNationalIdVoteStatus() {
        val voteService = spyk<VoteService>(recordPrivateCalls = true)

        every { voteService["validateAndQueryRelatedData"](voteModel) } returns Pair(
            election,
            voterBallot
        ) andThen Pair(election, null)

        // Case not allow vote
        var result = voteService.nationalIdVoteStatus(voteModel)
        assertNotNull(result.status)
        assertFalse(result.status!!)
        verify(exactly = 1) { voteService["validateAndQueryRelatedData"](voteModel) }
        clearAllMocks(answers = false)

        // Case allow vote
        result = voteService.nationalIdVoteStatus(voteModel)
        assertNotNull(result.status)
        assertTrue(result.status!!)
        verify(exactly = 1) { voteService["validateAndQueryRelatedData"](voteModel) }
    }

    @Test
    fun testVoteCandidate() {
        mockkStatic("lineelection.entities.CandidateKt")
        val voteService = spyk<VoteService>(recordPrivateCalls = true)

        every { voteService.voteValidator } returns voteValidator
        every { voteService.candidateService } returns candidateService
        every { voteService.candidateRepository } returns candidateRepository
        every { voteService.voterBallotRepository } returns voterBallotRepository

        every { voteService["validateAndQueryRelatedData"](voteModel) } returns Pair(election, null)
        every { voteValidator.voteCandidateValidation(voteModel, election, null) } just Runs
        every { candidateService.getCandidateDetail(voteModel.candidateId!!) } returns candidate
        every { candidate.upVote() } returns candidate
        every { candidateRepository.save(candidate) } returns candidate
        every { voterBallotRepository.save(voterBallot) } returns voterBallot
        every { voteService["boardCastCandidateScore"](candidate) } returns Unit

        val result = Try.on {
            voteService.voteCandidate(voteModel)
        }
        assertTrue(result.isSuccess)
        verify(exactly = 1) { voteService["validateAndQueryRelatedData"](voteModel) }
        verify(exactly = 1) { voteValidator.voteCandidateValidation(voteModel, election, null) }
        verify(exactly = 1) { candidateService.getCandidateDetail(voteModel.candidateId!!) }
        verify(exactly = 1) { candidate.upVote() }
        verify(exactly = 1) { candidateRepository.save(candidate) }
        verify(exactly = 1) { voterBallotRepository.save(voterBallot) }
        verify(exactly = 1) { voteService["boardCastCandidateScore"](candidate) }

        unmockkAll()
    }

    @Test
    fun testBoardCastCandidateScore() {
        val candidateModel = CandidateModel(
            id = candidate.id,
            votedCount = candidate.votedCount
        )

        every { webSocketProperties.newVote } returns "new-vote"
        every {
            simpMessagingTemplate.convertAndSend(
                "new-vote",
                candidateModel
            )
        } throws Exception("Test Exception") andThen Unit

        // Case failed but function success due to try catch
        var result = Try.on {
            ReflectionTestUtils.invokeMethod<Unit>(voteService, "boardCastCandidateScore", candidate)
        }
        assertTrue(result.isSuccess)
        verify(exactly = 1) { simpMessagingTemplate.convertAndSend("new-vote", candidateModel) }
        clearAllMocks(answers = false)

        // Case Success
        result = Try.on {
            ReflectionTestUtils.invokeMethod<Unit>(voteService, "boardCastCandidateScore", candidate)
        }
        assertTrue(result.isSuccess)
        verify(exactly = 1) { simpMessagingTemplate.convertAndSend("new-vote", candidateModel) }
    }

    @Test
    fun testValidateAndQueryRelatedData() {
        mockkStatic("lineelection.utilities.CommonUtilityKt")

        every { isValidNationalId(voteModel.nationalId) } returns Unit
        every { electionRepository.findCurrentElection() } returns null andThen election.copy(status = ElectionStatus.CLOSED.name) andThen election

        // Case Election not found
        var result = Try.on {
            ReflectionTestUtils.invokeMethod<Pair<Election, VoterBallot?>>(
                voteService,
                "validateAndQueryRelatedData",
                voteModel
            )
        }
        assertTrue(result.isFailure)
        assertTrue(result.toString().contains("Election not found"))
        verify(exactly = 1) { isValidNationalId(voteModel.nationalId) }
        verify(exactly = 1) { electionRepository.findCurrentElection() }
        verify(exactly = 0) { voterBallotRepository.findByIdOrNull(any()) }
        clearAllMocks(answers = false)


        // Case Election is closed
        result = Try.on {
            ReflectionTestUtils.invokeMethod<Pair<Election, VoterBallot?>>(
                voteService,
                "validateAndQueryRelatedData",
                voteModel
            )
        }
        assertTrue(result.isFailure)
        assertTrue(result.toString().contains(electionIsClosed))
        verify(exactly = 1) { isValidNationalId(voteModel.nationalId) }
        verify(exactly = 1) { electionRepository.findCurrentElection() }
        verify(exactly = 0) { voterBallotRepository.findByIdOrNull(any()) }
        clearAllMocks(answers = false)

        // Case Success
        every {
            voterBallotRepository.findByIdOrNull(
                VoterBallotPk(
                    election.id,
                    voteModel.nationalId
                )
            )
        } returns voterBallot

        result = Try.on {
            ReflectionTestUtils.invokeMethod<Pair<Election, VoterBallot?>>(
                voteService,
                "validateAndQueryRelatedData",
                voteModel
            )
        }
        assertTrue(result.isSuccess)
        assertEquals(Pair(election, voterBallot), result.getOrThrow())
        verify(exactly = 1) { isValidNationalId(voteModel.nationalId) }
        verify(exactly = 1) { electionRepository.findCurrentElection() }
        verify(exactly = 1) { voterBallotRepository.findByIdOrNull(VoterBallotPk(election.id, voteModel.nationalId)) }

        unmockkAll()
    }
}