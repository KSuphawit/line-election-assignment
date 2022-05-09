package lineelection.configuration

import lineelection.properties.WebSocketProperties
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer


@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig : WebSocketMessageBrokerConfigurer {

    @Autowired
    lateinit var webSocketProperties: WebSocketProperties

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint(webSocketProperties.endpoint).withSockJS()
    }
}