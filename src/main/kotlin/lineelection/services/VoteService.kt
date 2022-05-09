package lineelection.services

import lineelection.constants.ElectionStatus
import lineelection.constants.dataNotFound
import lineelection.constants.electionIsClosed
import lineelection.entities.*
import lineelection.models.CandidateModel
import lineelection.models.NationalIdVoteStatusResponse
import lineelection.models.VoteModel
import lineelection.properties.WebSocketProperties
import lineelection.repositories.CandidateRepository
import lineelection.repositories.ElectionRepository
import lineelection.repositories.VoterBallotRepository
import lineelection.utilities.isValidNationalId
import lineelection.utilities.validate
import lineelection.validators.VoteValidator
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
class VoteService {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(VoteService::class.java)
        private val className = VoteService::class.java.simpleName
    }

    @Autowired
    lateinit var candidateService: CandidateService

    @Autowired
    lateinit var candidateRepository: CandidateRepository

    @Autowired
    lateinit var electionRepository: ElectionRepository

    @Autowired
    lateinit var simpMessagingTemplate: SimpMessagingTemplate

    @Autowired
    lateinit var voterBallotRepository: VoterBallotRepository

    @Autowired
    lateinit var voteValidator: VoteValidator

    @Autowired
    lateinit var webSocketProperties: WebSocketProperties

    /**
     * Method for check nationalId is available for vote or not
     * if voter ballot already exist status will return false else return true
     *
     * @param voteModel
     */
    fun nationalIdVoteStatus(voteModel: VoteModel): NationalIdVoteStatusResponse {

        val (_, existingVoterBallot) = validateAndQueryRelatedData(voteModel)

        return NationalIdVoteStatusResponse(existingVoterBallot == null)
    }

    /**
     * Method for vote candidate
     * if election is close or voter already vote system will not allow voter to vote
     *
     * @param voteModel
     */
    @Transactional
    fun voteCandidate(voteModel: VoteModel) {

        val (election, existingVoterBallot) = validateAndQueryRelatedData(voteModel)

        voteValidator.voteCandidateValidation(voteModel, election, existingVoterBallot)

        val candidate = candidateService.getCandidateDetail(voteModel.candidateId!!)

        val voterBallot = VoterBallot(
                electionId = election.id,
                nationalId = voteModel.nationalId
        )

        val candidateUpdatedScore = candidateRepository.save(candidate.upVote())
        voterBallotRepository.save(voterBallot)

        boardCastCandidateScore(candidateUpdatedScore)
    }

    /**
     * Method for boardCast candidate new score
     *
     * @param candidate : candidate's updated score
     */
    private fun boardCastCandidateScore(candidate: Candidate) {
        try {
            val candidateModel = CandidateModel(id = candidate.id, votedCount = candidate.votedCount)
            simpMessagingTemplate.convertAndSend(webSocketProperties.newVote!!, candidateModel)
        } catch (e: Exception) {
            logger.error("$className.boardCastCandidateScore error ", e)
        }
    }

    /**
     * Method for validate and query related data for vote candidate or check voter status
     *
     * @param voteModel
     * @return Pair<Election, VoterBallot?> : election data and existing voter ballot
     */
    private fun validateAndQueryRelatedData(voteModel: VoteModel): Pair<Election, VoterBallot?> {

        voteModel.nationalId?.isValidNationalId()

        val election = electionRepository.findCurrentElection()

        dataNotFound.format(Election::class.simpleName) validate (election != null)

        electionIsClosed validate (election!!.status != ElectionStatus.CLOSED.name)

        val existingVoterBallot = voterBallotRepository.findByIdOrNull(VoterBallotPk(election.id, voteModel.nationalId))

        return Pair(election, existingVoterBallot)
    }
}