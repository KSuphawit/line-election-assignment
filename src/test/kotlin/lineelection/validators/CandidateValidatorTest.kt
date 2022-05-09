package lineelection.validators

import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.InjectMockKs
import lineelection.Try
import lineelection.constants.*
import lineelection.models.CandidateModel
import org.junit.Before
import org.junit.Test
import kotlin.test.assertTrue

class CandidateValidatorTest {

    @InjectMockKs
    lateinit var candidateValidator: CandidateValidator

    private val candidateModel = CandidateModel(
            1L,
            name = "name",
            bioLink = "bioLink",
            imageLink = "imageLink",
            policy = "policy"
    )

    @Before
    fun setup() = MockKAnnotations.init(this)

    @Test
    fun testCandidateInformationValidation() {
        // Case Candidate name must not be null or empty
        var result = Try.on {
            candidateValidator.candidateInformationValidation(candidateModel.copy(name = null))
        }
        assertTrue(result.isFailure)
        assertTrue(result.toString().contains(candidateNameMustNotBeNullOrEmpty))

        // Case Candidate biography link must not be null or empty
        result = Try.on {
            candidateValidator.candidateInformationValidation(candidateModel.copy(bioLink = null))
        }
        assertTrue(result.isFailure)
        assertTrue(result.toString().contains(candidateBiographyLinkMustNotBeNullOrEmpty))

        // Case Candidate image link must not be null or empty
        result = Try.on {
            candidateValidator.candidateInformationValidation(candidateModel.copy(imageLink = null))
        }
        assertTrue(result.isFailure)
        assertTrue(result.toString().contains(candidateImageLinkMustNotBeNullOrEmpty))

        // Case Candidate policy must not be null or empty
        result = Try.on {
            candidateValidator.candidateInformationValidation(candidateModel.copy(policy = null))
        }
        assertTrue(result.isFailure)
        assertTrue(result.toString().contains(candidatePolicyMustNotBeNullOrEmpty))

        // Case Pass
        result = Try.on {
            candidateValidator.candidateInformationValidation(candidateModel)
        }
        assertTrue(result.isSuccess)
    }

    @Test
    fun testUpdateCandidateValidation() {
        // Case Candidate id / number must not be null or empty
        var result = Try.on {
            candidateValidator.updateCandidateValidation(candidateModel.copy(id = null))
        }
        assertTrue(result.isFailure)
        assertTrue(result.toString().contains(candidateIdMustNotBeNullOrEmpty))

        // Case Pass
        result = Try.on {
            candidateValidator.updateCandidateValidation(candidateModel)
        }
        assertTrue(result.isSuccess)
    }
}