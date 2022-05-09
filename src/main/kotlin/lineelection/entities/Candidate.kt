package lineelection.entities

import lineelection.models.CandidateModel
import lineelection.utilities.DateUtility.convertDateToString
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.io.Serializable
import java.util.*
import javax.persistence.*


@Entity
@Table(name = "candidate")
data class Candidate(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long? = null,
        var name: String? = null,
        var dob: Date? = null,
        var bioLink: String? = null,
        var imageLink: String? = null,
        var policy: String? = null,
        var votedCount: Long? = 0L,

        @Column(name = "election_id")
        val electionId: Long? = null,

        @CreationTimestamp
        @Column(name = "created_date", updatable = false)
        var createdDate: Date? = null,

        @UpdateTimestamp
        @Column(name = "updated_date", insertable = false)
        var updatedDate: Date? = null
) : Serializable {
    @ManyToOne
    @JoinColumn(name = "election_id", insertable = false, updatable = false)
    var election: Election? = null
}


/**
 * Method for convert Candidate to CandidateModel
 */
fun Candidate.toCandidateModel(totalVoted: Long? = null): CandidateModel {

    val percentage = totalVoted?.let {
        if (it == 0L) 0 else this.votedCount?.times(100)?.div(it) ?: 0L
    }

    return CandidateModel(
            id = this.id,
            name = this.name,
            dob = convertDateToString(this.dob),
            bioLink = this.bioLink,
            imageLink = this.imageLink,
            policy = this.policy,
            votedCount = this.votedCount,
            percentage = percentage?.toBigDecimal()
    )
}

/**
 * Method for convert list Candidate to list CandidateModel
 */
fun List<Candidate>.toCandidateModel(totalVoted: Long? = null): List<CandidateModel> {
    return this.map { it.toCandidateModel(totalVoted) }
}

/**
 * Method for upVote candidate
 */
fun Candidate.upVote(): Candidate {
    return this.copy(votedCount = this.votedCount?.plus(1L))
}

/**
 * Method for assign election id to candidate
 *
 * @param election
 */
fun Candidate.assignElection(election: Election): Candidate {
    return this.copy(electionId = election.id)
}

