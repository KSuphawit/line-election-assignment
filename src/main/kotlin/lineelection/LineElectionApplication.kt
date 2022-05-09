package lineelection

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder

@SpringBootApplication
class LineElectionApplication {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(LineElectionApplication::class.java)

        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplicationBuilder(LineElectionApplication::class.java)
                    .web(WebApplicationType.SERVLET)
                    .run(*args)
            logger.info("******************* SPRING BOOT STARTED ************************")
        }
    }
}