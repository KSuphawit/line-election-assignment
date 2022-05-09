package lineelection.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component


@Component
@ConfigurationProperties("web-socket")
data class WebSocketProperties(
        var endpoint: String? = null,
        var newVote: String? = null
)
