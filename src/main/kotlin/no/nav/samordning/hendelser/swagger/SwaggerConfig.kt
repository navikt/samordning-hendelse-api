package no.nav.samordning.hendelser.swagger

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import springfox.documentation.builders.PathSelectors.ant
import springfox.documentation.builders.RequestHandlerSelectors.any
import springfox.documentation.spi.DocumentationType.SWAGGER_2
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2

@Configuration
@EnableSwagger2
class SwaggerConfig {

    @Bean
    fun api() = Docket(SWAGGER_2)
            .select()
            .apis(any())
            .paths(ant("/hendelser/**"))
            .build()!!
}
