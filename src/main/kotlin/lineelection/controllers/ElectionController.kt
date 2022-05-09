package lineelection.controllers

import lineelection.constants.ReportConstant
import lineelection.models.CandidateModel
import lineelection.models.ToggleModel
import lineelection.services.ElectionService
import lineelection.utilities.ResponseEntityUtility.modifyResponseForCSV
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/election", produces = [MediaType.APPLICATION_JSON_VALUE])
class ElectionController {

    @Autowired
    lateinit var electionService: ElectionService

    @PostMapping("/toggle")
    fun openOrCloseVote(@RequestBody toggleModel: ToggleModel): ToggleModel {
        return electionService.openOrCloseVote(toggleModel)
    }

    @GetMapping("/result")
    fun getElectionResult(): List<CandidateModel> {
        return electionService.getElectionResult()
    }

    @GetMapping("/export")
    fun exportElectionResult(): ResponseEntity<ByteArray> {
        val fileData = electionService.exportElectionResult()
        return ResponseEntity.ok().body(fileData).modifyResponseForCSV(ReportConstant.candidateElectionResultFilename)
    }
}