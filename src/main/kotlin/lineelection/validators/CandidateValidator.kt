package lineelection.validators


import lineelection.constants.*
import lineelection.models.CandidateModel
import lineelection.utilities.validate
import org.springframework.stereotype.Service


@Service
class CandidateValidator {

    /**
     * Method for validate candidate information for create candidate
     */
    fun candidateInformationValidation(candidateModel: CandidateModel) {
        candidateNameMustNotBeNullOrEmpty validate (candidateModel.name.isNullOrBlank().not())
        candidateBiographyLinkMustNotBeNullOrEmpty validate (candidateModel.bioLink.isNullOrBlank().not())
        candidateImageLinkMustNotBeNullOrEmpty validate (candidateModel.imageLink.isNullOrBlank().not())
        candidatePolicyMustNotBeNullOrEmpty validate (candidateModel.policy.isNullOrBlank().not())
    }

    /**
     * Method for validate candidate information for update candidate
     */
    fun updateCandidateValidation(candidateModel: CandidateModel) {
        candidateIdMustNotBeNullOrEmpty validate (candidateModel.id != null)
    }

}