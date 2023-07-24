package no.nav.samordning.hendelser.feed

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor

@Configuration
class ControllerConfig {

    @Bean
    fun methodValidationPostProcessor() = MethodValidationPostProcessor()
}
