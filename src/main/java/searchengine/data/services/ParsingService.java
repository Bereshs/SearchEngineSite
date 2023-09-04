package searchengine.data.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.SitesList;
import searchengine.data.services.html.HtmlMapPage;
import searchengine.dto.statistics.SimpleResponse;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.model.SiteStatus;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ParsingService {
    private final SitesList sitesList;
    private final SiteEntityService siteEntityService;
    private final PageEntityService pageEntityService;
    private final LemmaEntityService lemmaEntityService;
    private final IndexEntityService indexEntityService;

    private final Logger logger = Logger.getLogger(ParsingService.class.getName());
    private ForkJoinPool pool = new ForkJoinPool();
    //private HtmlMapPage page;

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
        if (!createPageIndex(url, false)) {
            return new SimpleResponse(false, "wrong address");
        }
        return new SimpleResponse(true);
    }

    public boolean createPageIndex(String url, boolean checkChilds) {
        String errorMessage = "Данная страница находится за пределами сайтов, указанных в конфигурационном файле";
        logger.info("Start indexing page " + url);
        HtmlMapPage.setIndexing(true);
        if (!sitesList.contains(url)) {
            logger.info(errorMessage);
            return false;
        }
        if (pool.isShutdown()) {
            pool = new ForkJoinPool();
        }
        setServicesToHtmlMapPage();

        SiteEntity site = siteEntityService.getByUrl(url);
        if (site != null) {
            PageEntity page = pageEntityService.findByPathAndSiteId(url, site.getId());
            deleteSearchIndexByPage(page);

            if (isRootPath(url, site.getUrl())) {
                deleteLemmasBySite(site);
                deleteSiteAndPageEntities(site);
            }
        }
        site = siteEntityService.getByUrlOrCreate(url);

        PageEntity mainPage = new PageEntity();
        mainPage.setSite(site);
        mainPage.setPath(url);
        HtmlMapPage mapPage = new HtmlMapPage(mainPage);

        mapPage.setCheckChilds(checkChilds);

        addAndRunPool(mapPage);


        return true;
    }

    public boolean isRootPath(String path, String rootPath) {
        return path.equals(rootPath);
    }


    public void createSiteMap(String url) {

        createPageIndex(url, true);
        logger.info("Stop indexing " + url + " found " + HtmlMapPage.getViewedLinkList().size() + " elements");
    }


    public void addAndRunPool(HtmlMapPage pageHtml) {
        pool.execute(pageHtml);

        loopPrintPoolInformation(pageHtml);
    }

    public void stop() {
        Logger.getLogger(ParsingService.class.getName()).info("Stop index " + sitesList.getSites().size() + " sites");
        HtmlMapPage.setIndexing(false);
        sitesList.getSites().forEach(site -> {
            SiteEntity siteEntity = siteEntityService.getByUrl(site.getUrl());
            if (siteEntity.getStatus().equals(SiteStatus.INDEXING)) {
                siteEntity.setLastError("Индексация остановлена пользователем");
                updateSiteEntity(siteEntity, SiteStatus.FAILED);
            }
        });

    }

    private void updateSiteEntity(SiteEntity siteEntity, SiteStatus siteStatus) {
        siteEntity.setStatus(siteStatus);
        siteEntity.setStatusTime(LocalDateTime.now());
        siteEntityService.save(siteEntity);
        logger.info(siteEntity.getStatus().toString() + " site " + siteEntity.getUrl() + " with message " + siteEntity.getLastError());
    }


    private void deleteSiteAndPageEntities(SiteEntity siteEntity) {
        List<PageEntity> pagesToDelete = pageEntityService.findBySiteId(siteEntity.getId());
        logger.info("Deleting " + pagesToDelete.size() + " available pages for " + siteEntity.getUrl());
        pageEntityService.deleteAllById(pagesToDelete.stream().map(PageEntity::getId).toList());
        logger.info("Deleting available sites");
        siteEntityService.deleteById(siteEntity.getId());
        logger.info("Preparing completed");
    }

    private void deleteLemmasBySite(SiteEntity siteEntity) {
        logger.info("Deleting lemmas by site");
        lemmaEntityService.deleteAllBySite(siteEntity);
    }

    private void deleteSearchIndexByPage(PageEntity page) {
        indexEntityService.deleteAllByPage(page);
    }

    void loopPrintPoolInformation(HtmlMapPage page) {
        do {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                logger.info(e.getMessage());
            }
            logger.info("Active threads: " + pool.getActiveThreadCount() + " task count: " + pool.getQueuedTaskCount());
        } while (!pool.isShutdown() && page.isDone() && pool.getActiveThreadCount() > 0);
    //    pool.shutdown();
    //    logger.info("ForkJoin pool shutting down");
    }

    public void setServicesToHtmlMapPage() {
        HtmlMapPage.setPageEntityService(pageEntityService);
        HtmlMapPage.setLemmaEntityService(lemmaEntityService);
        HtmlMapPage.setIndexEntityService(indexEntityService);
        HtmlMapPage.setSiteEntityService(siteEntityService);
    }

}
