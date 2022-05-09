package lineelection.entities

import lineelection.constants.ElectionStatus
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.io.Serializable
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "election")
data class Election(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long? = null,
        var startVotingDate: Date? = null,
        var endVotingDate: Date? = null,

        @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER, mappedBy = "election", orphanRemoval = true)
        var candidates: MutableList<Candidate> = mutableListOf(),

        var status: String? = ElectionStatus.RUN_FOR_ELECTION.name,

        @CreationTimestamp
        @Column(name = "created_date", updatable = false)
        var createdDate: Date? = null,

        @UpdateTimestamp
        @Column(name = "updated_date", insertable = false)
        var updatedDate: Date? = null,
) : Serializable


/**
 * Method for check is election voting time
 */
fun Election.isVotingTime(): Boolean {
    return this.status == ElectionStatus.VOTING.name && this.isInVotingTime()
}

/**
 * Method for check current date is in period voting time
 */
fun Election.isInVotingTime(): Boolean {
    return (this.startVotingDate?.before(Date()) ?: false) && (this.endVotingDate?.after(Date()) ?: false)
}

/**
 * Method for check current date is pass period voting time
 */
fun Election.isPassVotingTime(): Boolean {
    return (this.startVotingDate?.before(Date()) ?: false) && (this.endVotingDate?.before(Date()) ?: false)
}

/**
 * Method for change election status to voting
 */
fun Election.openVote(): Election {
    return this.copy(status = ElectionStatus.VOTING.name)
}

/**
 * Method for change election status to closed
 */
fun Election.closeVote(): Election {
    return this.copy(status = ElectionStatus.CLOSED.name)
}