package lineelection.models


import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import java.io.Serializable
import java.math.BigDecimal


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class CandidateModel(
        @JsonAlias("number")
        var id: Long? = null,
        var name: String? = null,
        var dob: String? = null,
        var bioLink: String? = null,
        var imageLink: String? = null,
        var policy: String? = null,
        var votedCount: Long? = null,
        var percentage: BigDecimal? = null
) : Serializable
