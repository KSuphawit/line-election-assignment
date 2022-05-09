package lineelection.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import lineelection.constants.Status
import org.springframework.http.HttpStatus
import java.io.Serializable


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
open class ResponseModel(
        var status: String? = Status.OK.message,
        var message: String? = null
) : Serializable

data class ErrorInfoDto(
        val httpResponseStatus: HttpStatus,
        val errorMessage: String? = null,
)