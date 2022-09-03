package lineelection.services


import lineelection.constants.ElectionStatus
import lineelection.constants.dataNotFound
import lineelection.constants.electionApplicationRunOutOfTime
import lineelection.entities.Candidate
import lineelection.entities.Election
import lineelection.entities.assignElection
import lineelection.entities.toCandidateModel
import lineelection.models.CandidateModel
import lineelection.repositories.CandidateRepository
import lineelection.repositories.ElectionRepository
import lineelection.utilities.DateUtility.convertStringToDate
import lineelection.utilities.validate
import lineelection.validators.CandidateValidator
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
class CandidateService {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(CandidateService::class.java)
        private val className = CandidateService::class.java.simpleName
    }

    @Autowired
    lateinit var candidateRepository: CandidateRepository

    @Autowired
    lateinit var candidateValidator: CandidateValidator

    @Autowired
    lateinit var electionRepository: ElectionRepository

    /**
     * Method for get all candidate
     */
    fun getCandidates(): List<CandidateModel> {
        return candidateRepository.findAll().toCandidateModel()
    }

    /**
     * Method for get candidate by id
     */
    fun getCandidateDetail(id: Long): Candidate {

        val existingCandidate = candidateRepository.findByIdOrNull(id)

        dataNotFound.format(Candidate::class.simpleName) validate (existingCandidate != null)

        return existingCandidate!!
    }

    /**
     * Method for create new candidate if missing required data will throw error
     *
     * @param candidateModel: Candidate Model with candidate information to create
     */
    fun createCandidate(candidateModel: CandidateModel): CandidateModel {

        candidateValidator.candidateInformationValidation(candidateModel)

        val election = getAndValidateElection()

        val newCandidate = Candidate(
                name = candidateModel.name,
                dob = convertStringToDate(candidateModel.dob),
                bioLink = candidateModel.bioLink,
                imageLink = candidateModel.imageLink,
                policy = candidateModel.policy
        ).assignElection(election)

        return candidateRepository.save(newCandidate).toCandidateModel()
    }

    /**
     * Method for update candidate information if not found existing will throw error
     * if new candidate data null or blank will use candidate old data
     *
     * @param candidateModel: Candidate Model with updating information
     */
    fun updateCandidate(candidateModel: CandidateModel): CandidateModel {

        candidateValidator.updateCandidateValidation(candidateModel)

        getAndValidateElection()

        val existingCandidate = candidateRepository.findByIdOrNull(candidateModel.id)

        dataNotFound.format(Candidate::class.simpleName) validate (existingCandidate != null)

        val updatedCandidate = existingCandidate!!.copy(
                name = if (candidateModel.name.isNullOrBlank().not()) candidateModel.name else existingCandidate.name,
                dob = if (candidateModel.dob.isNullOrBlank().not()) convertStringToDate(candidateModel.dob) else existingCandidate.dob,
                bioLink = if (candidateModel.bioLink.isNullOrBlank().not()) candidateModel.bioLink else existingCandidate.bioLink,
                imageLink = if (candidateModel.imageLink.isNullOrBlank().not()) candidateModel.imageLink else existingCandidate.imageLink,
                policy = if (candidateModel.policy.isNullOrBlank().not()) candidateModel.policy else existingCandidate.policy
        )

        logger.info("$className.updateCandidate id {}", updatedCandidate.id)

        return candidateRepository.save(updatedCandidate).toCandidateModel()
    }

    /**
     * Method for get and validate election for create/update candidate
     */
    private fun getAndValidateElection(): Election {
        val election = electionRepository.findCurrentElection()

        dataNotFound.format(Election::class.java.simpleName) validate (election != null)

        electionApplicationRunOutOfTime validate (election?.status == ElectionStatus.OPEN_APPLICATION.name)

        return election!!
    }

    /**
     * Method for delete candidate if not found existing candidate will throw error
     *
     * @param id : existing candidate id that want to remove
     */
    @Transactional
    fun deleteCandidate(id: Long) {
        val existingCandidate = candidateRepository.findByIdOrNull(id)

        dataNotFound.format(Candidate::class.simpleName) validate (existingCandidate != null)

        logger.info("$className.deleteCandidate id {}", id)

        candidateRepository.delete(existingCandidate!!)
    }
}