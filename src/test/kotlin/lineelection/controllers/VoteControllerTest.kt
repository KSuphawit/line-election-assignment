package lineelection.controllers

import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import lineelection.Try
import lineelection.models.NationalIdVoteStatusResponse
import lineelection.models.VoteModel
import lineelection.services.VoteService
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VoteControllerTest {

    @InjectMockKs
    lateinit var voteController: VoteController

    @MockK
    lateinit var voteService: VoteService

    private val voteModel = VoteModel()

    private val nationalIdVoteStatusResponse = NationalIdVoteStatusResponse()

    @Before
    fun setup() = MockKAnnotations.init(this)

    @Test
    fun testVoteStatus() {
        every { voteService.nationalIdVoteStatus(voteModel) } returns nationalIdVoteStatusResponse

        val result = voteController.voteStatus(voteModel)
        assertEquals(nationalIdVoteStatusResponse, result)
        verify(exactly = 1) { voteService.nationalIdVoteStatus(voteModel) }
    }

    @Test
    fun testVoteCandidate() {
        every { voteService.voteCandidate(voteModel) } just Runs

        val result = Try.on {
            voteController.voteCandidate(voteModel)
        }
        assertTrue(result.isSuccess)
        verify(exactly = 1) { voteService.voteCandidate(voteModel) }
    }
}