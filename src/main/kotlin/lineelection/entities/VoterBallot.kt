package lineelection.entities

import org.hibernate.annotations.CreationTimestamp
import java.io.Serializable
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "voter_ballot")
@IdClass(VoterBallotPk::class)
data class VoterBallot(
        @Id
        var electionId: Long? = null,
        @Id
        var nationalId: String? = null,
        @CreationTimestamp
        @Column(name = "voted_date", updatable = false)
        var votedDate: Date? = null,
) : Serializable


@Embeddable
data class VoterBallotPk(
        var electionId: Long? = null,
        var nationalId: String? = null,
) : Serializable
