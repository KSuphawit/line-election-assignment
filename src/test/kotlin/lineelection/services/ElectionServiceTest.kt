package lineelection.services

import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import lineelection.Try
import lineelection.constants.*
import lineelection.entities.*
import lineelection.models.CandidateModel
import lineelection.models.ToggleModel
import lineelection.repositories.ElectionRepository
import lineelection.repositories.VoterBallotRepository
import org.junit.Before
import org.junit.Test
import org.springframework.test.util.ReflectionTestUtils
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ElectionServiceTest {

    @InjectMockKs
    lateinit var electionService: ElectionService

    @MockK
    lateinit var electionRepository: ElectionRepository

    @MockK
    lateinit var voterBallotRepository: VoterBallotRepository

    @MockK
    lateinit var reportService: ReportService

    private val election = Election(id = 3L, status = ElectionStatus.CLOSED.name)

    private val candidateModel = CandidateModel(
            1L,
            name = "name",
            dob = "dob",
            bioLink = "bioLink",
            imageLink = "imageLink",
            policy = "policy",
            votedCount = 5,
            percentage = BigDecimal.TEN
    )

    private val toggleModel = ToggleModel(enable = true)

    @Before
    fun setup() = MockKAnnotations.init(this)

    @Test
    fun testOpenOrCloseVote() {
        val electionService = spyk<ElectionService>(recordPrivateCalls = true)

        every { electionService.electionRepository } returns electionRepository

        // Case Enable flag must not be null
        var result = Try.on {
            electionService.openOrCloseVote(toggleModel.copy(enable = null))
        }
        assertTrue(result.isFailure)
        assertTrue(result.toString().contains(enableFlagMustNotBeNull))
        verify(exactly = 0) { electionRepository.findCurrentElection() }
        verify(exactly = 0) { electionService["openVote"](any<Election>()) }
        verify(exactly = 0) { electionService["closeVote"](any<Election>()) }
        verify(exactly = 0) { electionRepository.save(any()) }

        // Case Election not found
        every { electionRepository.findCurrentElection() } returns null andThen election

        result = Try.on {
            electionService.openOrCloseVote(toggleModel)
        }
        assertTrue(result.isFailure)
        assertTrue(result.toString().contains("Election not found"))
        verify(exactly = 1) { electionRepository.findCurrentElection() }
        verify(exactly = 0) { electionService["openVote"](any<Election>()) }
        verify(exactly = 0) { electionService["closeVote"](any<Election>()) }
        verify(exactly = 0) { electionRepository.save(any()) }
        clearAllMocks(answers = false)

        // Case openVote
        every { electionService["openVote"](election) } returns election
        every { electionRepository.save(election) } returns election

        result = Try.on {
            electionService.openOrCloseVote(toggleModel)
        }
        assertTrue(result.isSuccess)
        assertEquals(toggleModel, result.getOrThrow())
        verify(exactly = 1) { electionRepository.findCurrentElection() }
        verify(exactly = 1) { electionService["openVote"](any<Election>()) }
        verify(exactly = 0) { electionService["closeVote"](any<Election>()) }
        verify(exactly = 1) { electionRepository.save(any()) }
        clearAllMocks(answers = false)

        // Case closeVote
        every { electionService["closeVote"](election) } returns election

        result = Try.on {
            electionService.openOrCloseVote(toggleModel.copy(enable = false))
        }
        assertTrue(result.isSuccess)
        assertEquals(toggleModel.copy(enable = false), result.getOrThrow())
        verify(exactly = 1) { electionRepository.findCurrentElection() }
        verify(exactly = 0) { electionService["openVote"](any<Election>()) }
        verify(exactly = 1) { electionService["closeVote"](any<Election>()) }
        verify(exactly = 1) { electionRepository.save(any()) }
    }

    @Test
    fun testOpenVote() {
        mockkStatic("lineelection.entities.ElectionKt")
        val election = election.copy(status = ElectionStatus.OPEN_APPLICATION.name)

        // Case Election is voting or closed
        var result = Try.on {
            ReflectionTestUtils.invokeMethod<Election>(electionService, "openVote", election.copy(status = ElectionStatus.CLOSED.name))
        }
        assertTrue(result.isFailure)
        assertTrue(result.toString().contains(electionIsVotingOrClosed))
        verify(exactly = 0) { election.isInVotingTime() }
        verify(exactly = 0) { election.openVote() }

        // Case Cannot close
        every { election.isInVotingTime() } returns false andThen true

        result = Try.on {
            ReflectionTestUtils.invokeMethod<Election>(electionService, "openVote", election.copy(status = ElectionStatus.OPEN_APPLICATION.name))
        }
        assertTrue(result.isFailure)
        assertTrue(result.toString().contains(isNotElectionVoteTime))
        verify(exactly = 1) { election.isInVotingTime() }
        verify(exactly = 0) { election.openVote() }
        clearAllMocks(answers = false)

        // Case Open vote success
        every { election.openVote() } returns election.copy(status = ElectionStatus.VOTING.name)

        result = Try.on {
            ReflectionTestUtils.invokeMethod<Election>(electionService, "openVote", election.copy(status = ElectionStatus.OPEN_APPLICATION.name))
        }
        assertTrue(result.isSuccess)
        assertEquals(election.copy(status = ElectionStatus.VOTING.name), result.getOrThrow())
        verify(exactly = 1) { election.isInVotingTime() }
        verify(exactly = 1) { election.openVote() }

        unmockkAll()
    }

    @Test
    fun testCloseVote() {
        mockkStatic("lineelection.entities.ElectionKt")
        val election = election.copy(status = ElectionStatus.VOTING.name)

        // Case Election phase is not a voting
        var result = Try.on {
            ReflectionTestUtils.invokeMethod<Election>(electionService, "closeVote", election.copy(status = ElectionStatus.CLOSED.name))
        }
        assertTrue(result.isFailure)
        assertTrue(result.toString().contains(electionPhaseIsNotVoting))
        verify(exactly = 0) { election.isPassVotingTime() }
        verify(exactly = 0) { election.closeVote() }

        // Case It's not time to close the vote.
        every { election.isPassVotingTime() } returns false andThen true

        result = Try.on {
            ReflectionTestUtils.invokeMethod<Election>(electionService, "closeVote", election)
        }
        assertTrue(result.isFailure)
        assertTrue(result.toString().contains(itIsNotTimeToCloseTheVote))
        verify(exactly = 1) { election.isPassVotingTime() }
        verify(exactly = 0) { election.closeVote() }
        clearAllMocks(answers = false)

        // Case Close vote success
        every { election.closeVote() } returns election.copy(status = ElectionStatus.CLOSED.name)

        result = Try.on {
            ReflectionTestUtils.invokeMethod<Election>(electionService, "closeVote", election)
        }
        assertTrue(result.isSuccess)
        assertEquals(election.copy(status = ElectionStatus.CLOSED.name), result.getOrThrow())
        verify(exactly = 1) { election.isPassVotingTime() }
        verify(exactly = 1) { election.closeVote() }

        unmockkAll()
    }

    @Test
    fun testGetElectionResult() {
        mockkStatic("lineelection.entities.CandidateKt")
        val totalVoted = 30L

        every { electionRepository.findCurrentElection() } returns null andThen election.copy(status = ElectionStatus.VOTING.name) andThen election

        // Case Election not found
        var result = Try.on {
            electionService.getElectionResult()
        }
        assertTrue(result.isFailure)
        assertTrue(result.toString().contains("Election not found"))
        verify(exactly = 1) { electionRepository.findCurrentElection() }
        verify(exactly = 0) { voterBallotRepository.countByElectionId(any()) }
        verify(exactly = 0) { election.candidates.toCandidateModel(any()) }
        clearAllMocks(answers = false)

        // Case The election is not over.
        result = Try.on {
            electionService.getElectionResult()
        }
        assertTrue(result.isFailure)
        assertTrue(result.toString().contains(theElectionIsNotOver))
        verify(exactly = 1) { electionRepository.findCurrentElection() }
        verify(exactly = 0) { voterBallotRepository.countByElectionId(any()) }
        verify(exactly = 0) { election.candidates.toCandidateModel(any()) }
        clearAllMocks(answers = false)

        // Case return election result success
        every { voterBallotRepository.countByElectionId(election.id!!) } returns totalVoted
        every { election.candidates.toCandidateModel(totalVoted) } returns listOf(candidateModel)

        result = Try.on {
            electionService.getElectionResult()
        }
        println(result.toString())
        assertTrue(result.isSuccess)
        assertEquals(listOf(candidateModel), result.getOrThrow())
        verify(exactly = 1) { electionRepository.findCurrentElection() }
        verify(exactly = 1) { voterBallotRepository.countByElectionId(election.id!!) }
        verify(exactly = 1) { election.candidates.toCandidateModel(totalVoted) }

        unmockkAll()
    }

    @Test
    fun testExportElectionResult() {
        val electionService = spyk<ElectionService>()
        val electionResult = listOf(candidateModel)
        val fileData = ByteArray(10)

        every { electionService.reportService } returns reportService
        every { electionService.getElectionResult() } returns electionResult
        every { reportService.generateCSVAsByte(ReportConstant.candidateElectionResultHeader, match { it.size == electionResult.size }) } returns fileData

        val result = electionService.exportElectionResult()
        assertEquals(fileData, result)
        verify(exactly = 1) { reportService.generateCSVAsByte(ReportConstant.candidateElectionResultHeader, match { it.size == electionResult.size }) }
    }
}