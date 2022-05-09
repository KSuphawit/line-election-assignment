package lineelection.repositories

import lineelection.entities.VoterBallot
import lineelection.entities.VoterBallotPk
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface VoterBallotRepository : JpaRepository<VoterBallot, VoterBallotPk> {

    @Query(nativeQuery = true, value = "SELECT COUNT(*) FROM voter_ballot WHERE election_id = :electionId")
    fun countByElectionId(electionId: Long): Long
}