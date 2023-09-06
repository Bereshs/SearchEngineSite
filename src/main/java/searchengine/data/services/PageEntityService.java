package searchengine.data.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import searchengine.data.repository.PageEntityRepository;
import searchengine.data.services.html.HtmlDocument;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;

import javax.persistence.criteria.CriteriaBuilder;
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

    public long getCount() {
        return pageEntityRepository.count();
    }

    public long getCountBySite(SiteEntity site) {
        return pageEntityRepository.countAllBySite(site);
    }

    public PageEntity findById(int id) {return pageEntityRepository.findById(id);}

}

