package searchengine.data.services;

import org.springframework.stereotype.Service;
import searchengine.data.repository.SiteEntityRepository;
import searchengine.data.services.html.HtmlDocument;
import searchengine.model.SiteEntity;
import searchengine.model.SiteStatus;

import java.time.LocalDateTime;

@Service
public class SiteEntityService {
    private final SiteEntityRepository siteEntityRepository;

    public SiteEntityService(SiteEntityRepository siteEntityRepository) {
        this.siteEntityRepository = siteEntityRepository;
    }

    public SiteEntity getByUrl(String url) {
        return siteEntityRepository.getByUrl(url);
    }

    public SiteEntity getByUrlAndDocument(String url, HtmlDocument document) {
        SiteEntity siteEntity = siteEntityRepository.getByUrl(document.getRootPath());
        if (siteEntity == null) {
            siteEntity = new SiteEntity();
            siteEntity.setUrl(document.getRootPath());
            siteEntity.setName(document.getTitle());
            siteEntity.setStatus(SiteStatus.INDEXING);
            save(siteEntity);
        }
        return siteEntity;
    }

    public void save(SiteEntity site) {
        site.setStatusTime(LocalDateTime.now());
        siteEntityRepository.save(site);
    }

    public void deleteById(int id) {
        siteEntityRepository.deleteById(id);
    }
}
