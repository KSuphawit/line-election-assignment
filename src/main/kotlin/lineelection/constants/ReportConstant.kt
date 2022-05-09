package lineelection.constants

import lineelection.models.CandidateModel

object ReportConstant {

    const val candidateElectionResultFilename = "candidate_election_report${CSV_EXTENSION}"

    val candidateElectionResultHeader = listOf(
            CandidateModel::id.name,
            CandidateModel::name.name,
            CandidateModel::votedCount.name,
            CandidateModel::percentage.name
    )
}