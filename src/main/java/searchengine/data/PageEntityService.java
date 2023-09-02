package searchengine.data;

import org.springframework.stereotype.Service;
import searchengine.data.repository.PageEntityRepository;
import searchengine.data.services.html.HtmlDocument;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;

import java.util.List;

@Service
public class PageEntityService {
    private final PageEntityRepository pageEntityRepository;

    public PageEntityService(PageEntityRepository pageEntityRepository) {
        this.pageEntityRepository = pageEntityRepository;
    }


    public PageEntity findByPathAndSiteId(String path, int siteId) {
        return pageEntityRepository.findByPathAndSiteId(path, siteId);
    }

    public void save(PageEntity page) {
        pageEntityRepository.save(page);
    }

    public List<PageEntity> findBySiteId(int id) {
        return pageEntityRepository.findBySiteId(id);
    }

    public void deleteById(int id) {
        pageEntityRepository.deleteById(id);
    }

    public void deleteAllById(Iterable<? extends Integer> pages) {
        pageEntityRepository.deleteAllById(pages);
    }

    public PageEntity getBySiteAndDocument(SiteEntity site, HtmlDocument document) {
        PageEntity page = pageEntityRepository.findByPathAndSiteId(document.getLocation(), site.getId());
        if (page == null) {
            page = new PageEntity();
            page.setSite(site);
            page.setPath(document.getLocation());
            page.setCode(document.getStatusCode());
        }
        document.removeUnUseTags();
        page.setContent(document.getHtml());
        pageEntityRepository.save(page);
        return page;
    }
}

