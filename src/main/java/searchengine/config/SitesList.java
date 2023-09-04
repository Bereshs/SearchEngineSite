package searchengine.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "indexing-settings")
public class SitesList {
    private List<Site> sites;

    public boolean contains(String url) {
        AtomicBoolean result = new AtomicBoolean(false);
        for(Site site:sites) {
            if(url.contains(site.getUrl())) {
                result.set(true);
                break;
            }
        };
        return result.get();
    }
}
