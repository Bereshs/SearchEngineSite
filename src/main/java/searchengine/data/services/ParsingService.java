package searchengine.data.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.SitesList;
import searchengine.data.LemmaEntityService;
import searchengine.data.PageEntityService;
import searchengine.data.services.html.HtmlDocument;
import searchengine.data.services.html.HtmlMapPage;
import searchengine.dto.statistics.SimpleResponse;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.model.SiteStatus;

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
    private final SiteEntityService siteEntityService;
    private final PageEntityService pageEntityService;
    private final LemmaEntityService lemmaEntityService;
    private final IndexEntityService indexEntityService;

    private final Logger logger = Logger.getLogger(ParsingService.class.getName());
    private final ForkJoinPool pool = new ForkJoinPool();
    private HtmlMapPage page;

    @Autowired
    public ParsingService(SitesList sitesList, SiteEntityService siteEntityService, PageEntityService pageEntityService, LemmaEntityService lemmaEntityService, IndexEntityService indexEntityService) {
        this.sitesList = sitesList;
        this.siteEntityService = siteEntityService;
        this.pageEntityService = pageEntityService;
        this.lemmaEntityService = lemmaEntityService;
        this.indexEntityService = indexEntityService;
    }

    public void indexingSite(String url) {

        createSiteMap(url);

    }

    public SimpleResponse indexPage(String url) {
        HtmlDocument document =createPageIndex(url);
        if(document==null) {
            return  new SimpleResponse(false, "wrong address");
        }
        return new SimpleResponse(true);
    }

    public HtmlDocument createPageIndex(String url) {
        String errorMessage = "Данная страница находится за пределами сайтов, указанных в конфигурационном файле";
        logger.info("Start indexing page " + url);
        if (sitesList.contains(url)) {
            logger.info(errorMessage);
            return null;
        }
        setServicesToHtmlMapPage();

        PageEntity mainPage = new PageEntity();
        HtmlMapPage mapPage = new HtmlMapPage(mainPage);

        return mapPage.createPageIndex(mainPage.getAbsolutePath());
    }


    public void createSiteMap(String url) {
        setServicesToHtmlMapPage();
        logger.info("Start creating site map " + url);
        SiteEntity siteEntity = siteEntityService.getByUrl(url);

        siteEntity = new SiteEntity(url);
        updateSiteEntity(siteEntity, SiteStatus.INDEXING);

        PageEntity mainPageEntity = new PageEntity();
        mainPageEntity.setSite(siteEntity);

        try {
            HtmlDocument document = new HtmlDocument(mainPageEntity.getAbsolutePath());
            siteEntity.setName(document.getTitle());
            updateSiteEntity(siteEntity, SiteStatus.INDEXING);

        } catch (IOException ex) {
            siteEntity.setLastError(ex.getMessage());
            updateSiteEntity(siteEntity, SiteStatus.FAILED);
            logger.info("Stopped indexing site " + mainPageEntity.getSite().getUrl() + " with error");
            return;
        }

        page = new HtmlMapPage(mainPageEntity);
        pool.execute(page);

        loopPrintPoolInformation();

        pool.shutdown();

        if (!HtmlMapPage.isIndexing()) {
            siteEntity.setLastError("Индексация остановлена пользователем");
            updateSiteEntity(siteEntity, SiteStatus.FAILED);
            return;
        }
        updateSiteEntity(siteEntity, SiteStatus.INDEXED);
        logger.info("Stop indexing " + url + " found " + HtmlMapPage.getViewedLinkList().size() + " elements");
    }


    public void stop() {
        Logger.getLogger(ParsingService.class.getName()).info("Stop index " + sitesList.getSites().size() + " sites");
        HtmlMapPage.setIndexing(false);
    }

    private void updateSiteEntity(SiteEntity siteEntity, SiteStatus siteStatus) {
        siteEntity.setStatus(siteStatus);
        siteEntity.setStatusTime(LocalDateTime.now());
        siteEntityService.save(siteEntity);
        logger.info(siteEntity.getStatus().toString() + " site " + siteEntity.getUrl() + " with message " + siteEntity.getLastError());
    }

    private void deleteSiteAndPageEntitiesBySite(SiteEntity siteEntity) {
        List<PageEntity> pagesToDelete = pageEntityService.findBySiteId(siteEntity.getId());

        logger.info("Deleting " + pagesToDelete.size() + " available lemmas indexes for " + siteEntity.getUrl());
        pagesToDelete.forEach(indexEntityService::deleteAllByPage);

        logger.info("Deleting " + pagesToDelete.size() + " available pages for " + siteEntity.getUrl());
        pageEntityService.deleteAllById(pagesToDelete.stream().map(PageEntity::getId).toList());
        logger.info("Deleting available sites");
        siteEntityService.deleteById(siteEntity.getId());
        logger.info("Preparing completed");
    }

    private void deleteLemmasIndexesBySite(SiteEntity siteEntity) {
        logger.info("Getting lemmas list ");
        List<LemmaEntity> lemmaToDelete = lemmaEntityService.getLemmaEntitiesBySite(siteEntity);
        logger.info("Deleting" + lemmaToDelete.size() + " lemmas");
        lemmaEntityService.deleteAllById(lemmaToDelete.stream().map(LemmaEntity::getId).toList());
    }

    void deleteOldDataSite(SiteEntity siteEntity) {
        if (!isNull(siteEntity)) {
            deleteSiteAndPageEntitiesBySite(siteEntity);
            deleteLemmasIndexesBySite(siteEntity);
        }
    }

    void loopPrintPoolInformation() {
        do {
            logger.info("Active threads: " + pool.getActiveThreadCount() + " task count: " + pool.getQueuedTaskCount());
            try {
                TimeUnit.SECONDS.sleep(60);
            } catch (InterruptedException e) {
                logger.info(e.getMessage());
            }
        } while (!page.isDone());
    }

    public void setServicesToHtmlMapPage() {
        HtmlMapPage.setPageEntityService(pageEntityService);
        HtmlMapPage.setLemmaEntityService(lemmaEntityService);
        HtmlMapPage.setIndexEntityService(indexEntityService);
        HtmlMapPage.setSiteEntityService(siteEntityService);
    }

}
