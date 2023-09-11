package searchengine.config;

import io.swagger.annotations.Api;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.service.Contact;
@Configuration
public class SpringFoxConfig {
    @Bean
    public Docket docket() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.withClassAnnotation(Api.class))
                .paths(PathSelectors.any())
                .build()
                .apiInfo(apiInfo());
    }
    public ApiInfo apiInfo() {
        return new ApiInfo(
                "Web search engine",
                "API web search engine",
                "1.0",
                "https://t.me/bereshs",
                new Contact("API owner", "Sergey Bereshpolov", "bereshs@mail.ru").toString(),
                "api_license",
                "http:/no"
        );
    }


}
