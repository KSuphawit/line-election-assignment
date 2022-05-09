package lineelection.validators

import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import lineelection.Try
import lineelection.constants.alreadyVoted
import lineelection.constants.candidateIdMustNotBeNullOrEmpty
import lineelection.constants.isNotElectionVoteTime
import lineelection.entities.Election
import lineelection.entities.VoterBallot
import lineelection.entities.isVotingTime
import lineelection.models.VoteModel
import org.junit.Before
import org.junit.Test
import kotlin.test.assertTrue

class VoteValidatorTest {

    @InjectMockKs
    lateinit var voteValidator: VoteValidator

    private val voteModel = VoteModel(candidateId = 1L)

    private val election = Election()

    private val voterBallot = VoterBallot()

    @Before
    fun setup() = MockKAnnotations.init(this)

    @Test
    fun testCoteCandidateValidation() {
        mockkStatic("lineelection.entities.ElectionKt")

        // Case Candidate id / number must not be null or empty
        var result = Try.on {
            voteValidator.voteCandidateValidation(voteModel.copy(candidateId = null), election, voterBallot)
        }
        assertTrue(result.isFailure)
        assertTrue(result.toString().contains(candidateIdMustNotBeNullOrEmpty))

        // Case Is not election vote time
        every { election.isVotingTime() } returns false andThen true
        result = Try.on {
            voteValidator.voteCandidateValidation(voteModel, election, voterBallot)
        }
        assertTrue(result.isFailure)
        assertTrue(result.toString().contains(isNotElectionVoteTime))
        verify(exactly = 1) { election.isVotingTime() }
        clearAllMocks(answers = false)

        // Case Already voted
        result = Try.on {
            voteValidator.voteCandidateValidation(voteModel, election, voterBallot)
        }
        assertTrue(result.isFailure)
        assertTrue(result.toString().contains(alreadyVoted))
        verify(exactly = 1) { election.isVotingTime() }
        clearAllMocks(answers = false)

        // Case Pass
        result = Try.on {
            voteValidator.voteCandidateValidation(voteModel, election, null)
        }
        assertTrue(result.isSuccess)
        verify(exactly = 1) { election.isVotingTime() }
        clearAllMocks(answers = false)

        unmockkAll()
    }
}