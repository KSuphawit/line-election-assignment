package lineelection.services

import lineelection.constants.*
import lineelection.entities.*
import lineelection.models.CandidateModel
import lineelection.models.ToggleModel
import lineelection.repositories.ElectionRepository
import lineelection.repositories.VoterBallotRepository
import lineelection.utilities.getFieldValue
import lineelection.utilities.validate
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service


@Service
class ElectionService {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(ElectionService::class.java)
        private val className = ElectionService::class.java.simpleName
    }

    @Autowired
    lateinit var electionRepository: ElectionRepository

    @Autowired
    lateinit var voterBallotRepository: VoterBallotRepository

    @Autowired
    lateinit var reportService: ReportService

    /**
     * Method for open/close election vote throw error if enable flag is null
     *
     * @param toggleModel: toggleModel with enable flag
     */
    fun openOrCloseVote(toggleModel: ToggleModel): ToggleModel {

        enableFlagMustNotBeNull validate (toggleModel.enable != null)

        val election = electionRepository.findCurrentElection()

        dataNotFound.format(Election::class.simpleName) validate (election != null)

        logger.info("$className.openOrCloseVoting.toggleModel.enable : {}", toggleModel.enable)

        val updatedElectionStatus = when (toggleModel.enable!!) {
            true -> openVote(election!!)
            false -> closeVote(election!!)
        }

        electionRepository.save(updatedElectionStatus)

        return toggleModel
    }

    /**
     * Method for open election vote ( validate with voting time period and election status )
     *
     * @param election : election that want to open vote
     */
    private fun openVote(election: Election): Election {

        electionIsVotingOrClosed validate (election.status == ElectionStatus.RUN_FOR_ELECTION.name)

        isNotElectionVoteTime validate election.isInVotingTime()

        return election.openVote()
    }

    /**
     * Method for close election ( validate with voting time period and election status )
     *
     * @param election : election that want to close vote
     */
    private fun closeVote(election: Election): Election {

        electionPhaseIsNotVoting validate (election.status == ElectionStatus.VOTING.name)

        itIsNotTimeToCloseTheVote validate election.isPassVotingTime()

        return election.closeVote()
    }

    /**
     * Method for close election vote
     */
    fun getElectionResult(): List<CandidateModel> {

        val election = electionRepository.findCurrentElection()

        dataNotFound.format(Election::class.simpleName) validate (election != null)

        theElectionIsNotOver validate (election!!.status == ElectionStatus.CLOSED.name)

        val totalVoted = voterBallotRepository.countByElectionId(election.id!!)

        logger.info("$className.getElectionResult.totalVoted {}", totalVoted)

        return election.candidates.toCandidateModel(totalVoted)
    }

    /**
     * Method for export election result as csv
     */
    fun exportElectionResult(): ByteArray {
        val electionResult = getElectionResult()
        val reportContent = electionResult.map { candidateModel ->
            ReportConstant.candidateElectionResultHeader.map {
                candidateModel.getFieldValue<Any>(it)?.toString() ?: DASH
            }.toTypedArray()
        }

        return reportService.generateCSVAsByte(ReportConstant.candidateElectionResultHeader, reportContent)
    }
}