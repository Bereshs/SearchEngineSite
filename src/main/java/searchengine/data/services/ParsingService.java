package searchengine.data.services;

import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.SitesList;
import searchengine.data.services.html.HtmlLink;
import searchengine.data.services.html.HtmlPage;
import searchengine.model.PageEnity;
import searchengine.model.SiteEntity;
import searchengine.model.SiteStatus;
import searchengine.data.repository.PageEntityRepository;
import searchengine.data.repository.SiteEntityRepository;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static java.util.Objects.isNull;

@Service
public class ParsingService {
    private final SitesList sitesList;
    private final SiteEntityRepository siteEntityRepository;
    private final PageEntityRepository pageEntityRepository;
    private final Logger logger = Logger.getLogger(ParsingService.class.getName());
    private final ForkJoinPool pool = new ForkJoinPool();
    private HtmlPage page;

    @Autowired
    public ParsingService(SitesList sitesList, SiteEntityRepository siteEntityRepository, PageEntityRepository pageEntityRepository) {
        this.sitesList = sitesList;
        this.siteEntityRepository = siteEntityRepository;
        this.pageEntityRepository = pageEntityRepository;
    }

    public void indexingSite(String url) {
        createMapSite(url);
    }

    public void createMapSite(String url) {
        logger.info("Start indexing "+url);
        HtmlLink link = new HtmlLink(url, url);
        SiteEntity siteEntity = siteEntityRepository.getByUrl(link.getRootPath());
        if (!isNull(siteEntity)) {
            siteEntityRepository.deleteAllById(Collections.singleton(siteEntity.getId()));
        }
        siteEntity = new SiteEntity(link.getRootPath(),
                SiteStatus.INDEXING,
                "unNamed");
        siteEntityRepository.save(siteEntity);
        page = new HtmlPage(link);
        pool.execute(page);
        do {
            logger.info("Active threads: " + pool.getActiveThreadCount() + " task count: " + pool.getQueuedTaskCount());
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                logger.info(e.getMessage());
            }
        } while (!page.isDone());
        pool.shutdown();

        List<HtmlLink> list = page.join();
        logger.info("Stop indexing "+url +" found "+list.size()+" elements");
        List<PageEnity> pageEnityList = new ArrayList<>();
        for(HtmlLink linkPage: list) {
            pageEnityList.add(createPageEntityFromLink(linkPage, siteEntity));
        }
        logger.info("Compiled database");
        pageEntityRepository.saveAll(pageEnityList);
        logger.info("Saved database");
    }

    public  PageEnity createPageEntityFromLink(HtmlLink link, SiteEntity siteEntity) {
        PageEnity pageEnity = new PageEnity();
        pageEnity.setSite(siteEntity);
        pageEnity.setPath(link.getRelativePath());
        pageEnity.setContent("body");
        return pageEnity;
    }

    public void stop() {
        Logger.getLogger(ParsingService.class.getName()).info("Stop index " + sitesList.getSites().size() + " sites");
    }

    public void updateSiteEntity(SiteEntity siteEntity) {
        siteEntityRepository.save(siteEntity);
        logger.info(siteEntity.getStatus().toString() + " site " + siteEntity.getUrl() + " with message " + siteEntity.getLastError());
    }

    public void setFailedAndUpdateSiteEntity(SiteEntity siteEntity, String message) {
        siteEntity.setLastError(message);
        siteEntity.setStatus(SiteStatus.FAILED);
        updateSiteEntity(siteEntity);
    }
}
