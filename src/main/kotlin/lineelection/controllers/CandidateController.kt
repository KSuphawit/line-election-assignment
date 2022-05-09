package lineelection.controllers

import lineelection.annotations.ResponseModelAnnotation
import lineelection.entities.toCandidateModel
import lineelection.models.CandidateModel
import lineelection.services.CandidateService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/candidates", produces = [MediaType.APPLICATION_JSON_VALUE])
class CandidateController {

    @Autowired
    lateinit var candidateService: CandidateService

    @GetMapping
    fun getCandidates(): List<CandidateModel> {
        return candidateService.getCandidates()
    }

    @GetMapping("/{candidateId}")
    fun getCandidateDetail(@PathVariable("candidateId") candidateId: Long): CandidateModel {
        return candidateService.getCandidateDetail(candidateId).toCandidateModel()
    }

    @PostMapping
    fun createCandidate(@RequestBody candidateModel: CandidateModel): CandidateModel {
        return candidateService.createCandidate(candidateModel)
    }

    @PutMapping
    fun updateCandidate(@RequestBody candidateModel: CandidateModel): CandidateModel {
        return candidateService.updateCandidate(candidateModel)
    }

    @DeleteMapping("/{candidateId}")
    @ResponseModelAnnotation
    fun deleteCandidate(@PathVariable("candidateId") candidateId: Long) {
        return candidateService.deleteCandidate(candidateId)
    }
}