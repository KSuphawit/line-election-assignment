package lineelection.controllers

import lineelection.annotations.ResponseModelAnnotation
import lineelection.models.NationalIdVoteStatusResponse
import lineelection.models.VoteModel
import lineelection.services.VoteService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/vote", produces = [MediaType.APPLICATION_JSON_VALUE])
class VoteController {

    @Autowired
    lateinit var voteService: VoteService

    @PostMapping("/status")
    fun voteStatus(@RequestBody voteModel: VoteModel): NationalIdVoteStatusResponse {
        return voteService.nationalIdVoteStatus(voteModel)
    }

    @PostMapping
    @ResponseModelAnnotation
    fun voteCandidate(@RequestBody voteModel: VoteModel) {
        return voteService.voteCandidate(voteModel)
    }
}