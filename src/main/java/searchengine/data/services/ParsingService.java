package searchengine.data.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.SitesList;
import searchengine.data.services.html.HtmlDocument;
import searchengine.data.services.html.HtmlMapPage;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.model.SiteStatus;
import searchengine.data.repository.PageEntityRepository;
import searchengine.data.repository.SiteEntityRepository;

import java.io.IOException;
import java.time.LocalDateTime;
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
    private HtmlMapPage page;

    @Autowired
    public ParsingService(SitesList sitesList, SiteEntityRepository siteEntityRepository, PageEntityRepository pageEntityRepository) {
        this.sitesList = sitesList;
        this.siteEntityRepository = siteEntityRepository;
        this.pageEntityRepository = pageEntityRepository;
    }

    public void indexingSite(String url) {

        createSiteMap(url);

    }

    public void createSiteMap(String url) {
        HtmlMapPage.setPageRepository(pageEntityRepository);
        logger.info("Start indexing " + url);
        SiteEntity siteEntity = siteEntityRepository.getByUrl(url);
        if (!isNull(siteEntity)) {
            List<PageEntity> pagesToDelete = pageEntityRepository.findBySiteId(siteEntity.getId());
            logger.info("Deleting " + pagesToDelete.size() + " available pages for "+siteEntity.getUrl());
            pageEntityRepository.deleteAllById(pagesToDelete.stream().map(PageEntity::getId).toList());
            logger.info("Deleting available sites");
            siteEntityRepository.deleteById(siteEntity.getId());
            logger.info("Preparing completed");
        }

        siteEntity = new SiteEntity(url,
                SiteStatus.INDEXING,
                "unNamed");
        updateSiteEntity(siteEntity);

        PageEntity mainPageEntity = new PageEntity();
        mainPageEntity.setSite(siteEntity);
        mainPageEntity.setPath("");

        try {
            HtmlDocument document = new HtmlDocument(mainPageEntity.getConnection());
            siteEntity.setName(document.getTitle());
            updateSiteEntity(siteEntity);

        } catch (IOException ex) {
            siteEntity.setLastError(ex.getMessage());
            siteEntity.setStatus(SiteStatus.FAILED);
            updateSiteEntity(siteEntity);
            logger.info("Stopped indexing site" + mainPageEntity.getSite().getUrl() + " with error");
            return;
        }

        page = new HtmlMapPage(mainPageEntity);
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


        siteEntity.setStatus(SiteStatus.INDEXED);
        updateSiteEntity(siteEntity);
    //    int size = page.join();
        logger.info("Stop indexing " + url + " found " + HtmlMapPage.getViewedLinkList().size() + " elements");
        logger.info("Saved database");
    }


    public void stop() {
        Logger.getLogger(ParsingService.class.getName()).info("Stop index " + sitesList.getSites().size() + " sites");
    }

    public void updateSiteEntity(SiteEntity siteEntity) {
        siteEntity.setStatusTime(LocalDateTime.now());
        siteEntityRepository.save(siteEntity);
        logger.info(siteEntity.getStatus().toString() + " site " + siteEntity.getUrl() + " with message " + siteEntity.getLastError());
    }

}
