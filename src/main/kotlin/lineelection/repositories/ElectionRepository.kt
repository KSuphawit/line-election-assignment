package lineelection.repositories

import lineelection.entities.Election
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository


@Repository
interface ElectionRepository : JpaRepository<Election, Long> {

    @Query(nativeQuery = true, value = "SELECT TOP(1) * FROM election ORDER BY id DESC")
    fun findCurrentElection(): Election?
}