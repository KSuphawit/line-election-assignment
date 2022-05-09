package lineelection.validators

import lineelection.constants.alreadyVoted
import lineelection.constants.candidateIdMustNotBeNullOrEmpty
import lineelection.constants.isNotElectionVoteTime
import lineelection.entities.Election
import lineelection.entities.VoterBallot
import lineelection.entities.isVotingTime
import lineelection.models.VoteModel
import lineelection.utilities.validate
import org.springframework.stereotype.Service


@Service
class VoteValidator {

    /**
     * Method for validate vote candidate related data
     */
    fun voteCandidateValidation(voteModel: VoteModel, election: Election, existingVoterBallot: VoterBallot?) {
        candidateIdMustNotBeNullOrEmpty validate (voteModel.candidateId != null)
        isNotElectionVoteTime validate election.isVotingTime()
        alreadyVoted validate (existingVoterBallot == null)
    }
}