package lineelection.entities

import io.mockk.*
import lineelection.models.CandidateModel
import lineelection.utilities.DateUtility
import org.junit.Test
import java.math.BigDecimal
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CandidateTest {

    private val candidate = Candidate(
            id = 1L,
            name = "name",
            dob = Date(),
            bioLink = "bioLink",
            imageLink = "imageLink",
            policy = "policy",
            votedCount = 78
    )

    private val election = Election(id = 3L)

    private val candidateModel = CandidateModel()

    @Test
    fun testToCandidateModel() {
        mockkObject(DateUtility)

        every { DateUtility.convertDateToString(candidate.dob) } returns "DOB"

        // Case not send totalVoted
        var result = candidate.toCandidateModel()
        assertEquals(candidate.id, result.id)
        assertEquals(candidate.name, result.name)
        assertEquals("DOB", result.dob)
        assertEquals(candidate.bioLink, result.bioLink)
        assertEquals(candidate.imageLink, result.imageLink)
        assertEquals(candidate.policy, result.policy)
        assertEquals(candidate.votedCount, result.votedCount)
        assertTrue(result.percentage == null)

        // Case send totalVoted
        result = candidate.toCandidateModel(100L)
        assertEquals(candidate.id, result.id)
        assertEquals(candidate.name, result.name)
        assertEquals("DOB", result.dob)
        assertEquals(candidate.bioLink, result.bioLink)
        assertEquals(candidate.imageLink, result.imageLink)
        assertEquals(candidate.policy, result.policy)
        assertEquals(candidate.votedCount, result.votedCount)
        assertEquals(BigDecimal(78), result.percentage)

        // Case send totalVoted but equal 0
        result = candidate.toCandidateModel(0L)
        assertEquals(candidate.id, result.id)
        assertEquals(candidate.name, result.name)
        assertEquals("DOB", result.dob)
        assertEquals(candidate.bioLink, result.bioLink)
        assertEquals(candidate.imageLink, result.imageLink)
        assertEquals(candidate.policy, result.policy)
        assertEquals(candidate.votedCount, result.votedCount)
        assertEquals(BigDecimal.ZERO, result.percentage)

        // Case send totalVoted but votedCount is null
        result = candidate.copy(votedCount = null).toCandidateModel(100L)
        assertEquals(candidate.id, result.id)
        assertEquals(candidate.name, result.name)
        assertEquals("DOB", result.dob)
        assertEquals(candidate.bioLink, result.bioLink)
        assertEquals(candidate.imageLink, result.imageLink)
        assertEquals(candidate.policy, result.policy)
        assertTrue(result.votedCount == null)
        assertEquals(BigDecimal.ZERO, result.percentage)

        unmockkAll()
    }

    @Test
    fun testListCandidateToListCandidateModel() {
        mockkStatic("lineelection.entities.CandidateKt")

        // Case not send totalVoted
        every { candidate.toCandidateModel() } returns candidateModel

        var result = listOf(candidate).toCandidateModel()
        assertEquals(1, result.size)
        assertEquals(candidateModel, result.first())
        verify(exactly = 1) { candidate.toCandidateModel() }

        // Case send totalVoted
        every { candidate.toCandidateModel(100L) } returns candidateModel.copy(5L)

        result = listOf(candidate).toCandidateModel(100L)
        assertEquals(1, result.size)
        assertEquals(candidateModel.copy(5L), result.first())
        verify(exactly = 1) { candidate.toCandidateModel(100L) }

        unmockkAll()
    }

    @Test
    fun testUpVote() {
        val result = candidate.upVote()
        assertEquals(79L, result.votedCount)
    }

    @Test
    fun testAssignElection() {
        val result = candidate.assignElection(election)
        assertEquals(election.id, result.electionId)
    }
}