package searchengine.config;

import lombok.Data;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "config")
public class AppConfig {
    @Value("userAgent")
    @Getter
    private static String userAgent;
    @Value("referrer")
    @Getter
    private static String referrer;

}
