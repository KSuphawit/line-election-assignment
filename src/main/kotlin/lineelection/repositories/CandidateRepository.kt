package lineelection.repositories

import lineelection.entities.Candidate
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository


@Repository
interface CandidateRepository : JpaRepository<Candidate, Long>