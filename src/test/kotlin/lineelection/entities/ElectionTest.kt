package lineelection.entities

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import lineelection.constants.ElectionStatus
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ElectionTest {

    private val election = Election(
            id = 1L,
            startVotingDate = Date(),
            endVotingDate = Date()
    )

    @Test
    fun testIsVotingTime() {
        mockkStatic("lineelection.entities.ElectionKt")

        // Case status == VOTING && in voting time
        every { any<Election>().isInVotingTime() } returns true

        var result = election.copy(status = ElectionStatus.VOTING.name).isVotingTime()
        assertTrue(result)

        // Case status != VOTING && in voting time
        result = election.copy(status = ElectionStatus.RUN_FOR_ELECTION.name).isVotingTime()
        assertFalse(result)

        // Case status == VOTING && not in voting time
        every { any<Election>().isInVotingTime() } returns false

        result = election.copy(status = ElectionStatus.VOTING.name).isVotingTime()
        assertFalse(result)

        // Case status != VOTING && not in voting time
        result = election.copy(status = ElectionStatus.RUN_FOR_ELECTION.name).isVotingTime()
        assertFalse(result)

        unmockkAll()
    }

    @Test
    fun testIsInVotingTime() {
        val calendarYesterday = Calendar.getInstance()
        calendarYesterday.time = Date()
        calendarYesterday.add(Calendar.DATE, -1)

        val calendarTomorrow = Calendar.getInstance()
        calendarTomorrow.time = Date()
        calendarTomorrow.add(Calendar.DATE, 1)

        val dateYesterday = calendarYesterday.time
        val dateTomorrow = calendarTomorrow.time

        // Case not in voting time due to startVotingDate > currentDate
        var result = election.copy(startVotingDate = dateTomorrow, endVotingDate = dateTomorrow).isInVotingTime()
        assertFalse(result)

        // Case not in voting time due to pass voting time
        result = election.copy(startVotingDate = dateYesterday, endVotingDate = dateYesterday).isInVotingTime()
        assertFalse(result)

        // Case not in voting time due to startVotingDate is null
        result = election.copy(startVotingDate = null, endVotingDate = dateYesterday).isInVotingTime()
        assertFalse(result)

        // Case not in voting time due to endVotingDate is null
        result = election.copy(startVotingDate = dateYesterday, endVotingDate = null).isInVotingTime()
        assertFalse(result)

        // Case not in voting time due to startVotingDate and endVotingDate is null
        result = election.copy(startVotingDate = null, endVotingDate = null).isInVotingTime()
        assertFalse(result)

        // Case in voting time
        result = election.copy(startVotingDate = dateYesterday, endVotingDate = dateTomorrow).isInVotingTime()
        assertTrue(result)
    }

    @Test
    fun testIsPassVotingTime() {
        val calendarYesterday = Calendar.getInstance()
        calendarYesterday.time = Date()
        calendarYesterday.add(Calendar.DATE, -1)

        val calendarTomorrow = Calendar.getInstance()
        calendarTomorrow.time = Date()
        calendarTomorrow.add(Calendar.DATE, 1)

        val dateYesterday = calendarYesterday.time
        val dateTomorrow = calendarTomorrow.time

        // Case not pass voting time due to in voting time
        var result = election.copy(startVotingDate = dateYesterday, endVotingDate = dateTomorrow).isPassVotingTime()
        assertFalse(result)

        // Case not pass voting time due to before voting time
        result = election.copy(startVotingDate = dateTomorrow, endVotingDate = dateTomorrow).isPassVotingTime()
        assertFalse(result)

        // Case not pass voting time due to startVotingDate is null
        result = election.copy(startVotingDate = null, endVotingDate = dateTomorrow).isPassVotingTime()
        assertFalse(result)

        // Case not pass voting time due to endVotingDate is null
        result = election.copy(startVotingDate = dateTomorrow, endVotingDate = null).isPassVotingTime()
        assertFalse(result)

        // Case not pass voting time due to startVotingDate and endVotingDate is null
        result = election.copy(startVotingDate = null, endVotingDate = null).isPassVotingTime()
        assertFalse(result)

        // Case pass voting time
        result = election.copy(startVotingDate = dateYesterday, endVotingDate = dateYesterday).isPassVotingTime()
        assertTrue(result)
    }

    @Test
    fun testOpenVote() {
        val result = election.openVote()
        assertEquals(ElectionStatus.VOTING.name, result.status)
    }

    @Test
    fun testCloseVote() {
        val result = election.closeVote()
        assertEquals(ElectionStatus.CLOSED.name, result.status)
    }
}