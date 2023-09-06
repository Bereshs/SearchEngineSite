package searchengine.data.services.html;


import lombok.Getter;
import lombok.Setter;
import searchengine.data.services.LemmaEntityService;
import searchengine.data.services.PageEntityService;
import searchengine.data.services.IndexEntityService;
import searchengine.data.services.SiteEntityService;
import searchengine.model.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.RecursiveTask;
import java.util.logging.Logger;

public class HtmlMapPage extends RecursiveTask<Integer> {
    private final Logger logger = Logger.getLogger(HtmlMapPage.class.getName());
    private final List<HtmlMapPage> taskList = new ArrayList<>();
    @Getter
    private static final List<String> viewedLinkList = new ArrayList<>();
    @Setter
    private PageEntity mainPage;
    @Setter
    private static PageEntityService pageEntityService;
    @Setter
    private static LemmaEntityService lemmaEntityService;
    @Setter
    private static IndexEntityService indexEntityService;
    @Setter
    private static SiteEntityService siteEntityService;
    @Getter
    @Setter
    private static boolean indexing;

    @Setter
    private boolean checkChilds;

    public HtmlMapPage(PageEntity mainPage) {
        this.mainPage = mainPage;
        checkChilds = true;
    }

    @Override
    protected Integer compute() {
        if (!isIndexing()) {
            return 0;
        }

        PageEntity getPage = pageEntityService.findByPathAndSiteId(mainPage.getPath(), mainPage.getSite().getId());

        if (getPage != null || !mainPage.isValid()) {
            return 0;
        }

        synchronized (viewedLinkList) {
            viewedLinkList.add(mainPage.getAbsolutePath());
        }

        HtmlDocument document = createPageIndex(mainPage.getAbsolutePath());
        if (!checkChilds) {
            setIndexing(false);
            return 0;
        }

        Set<PageEntity> currentPageLinks = document.getChildPageList(mainPage.getSite());
        if (currentPageLinks.isEmpty()) {
            return 0;
        }

        addToTaskList(currentPageLinks);

        int result = 0;
        for (HtmlMapPage task : taskList) {
            result += task.join();
        }
        return result;
    }

    private void addToTaskList(Set<PageEntity> currentPageLinks) {
        for (PageEntity link : currentPageLinks) {
            if (!link.isValid()) {
                continue;
            }
            if (viewedLinkContains(link.getAbsolutePath())) {
                HtmlMapPage task = new HtmlMapPage(link);
                task.setMainPage(link);
                task.fork();
                taskList.add(task);
                synchronized (viewedLinkList) {
                    viewedLinkList.add(link.getAbsolutePath());
                }
            }
        }

    }

    private boolean viewedLinkContains(String link) {
        String newLink = link + "/";
        if (link.endsWith("/")) {
            newLink = link.substring(0, link.length() - 1);
        }
        return !viewedLinkList.contains(link) && !viewedLinkList.contains(newLink);
    }

    public HtmlDocument createPageIndex(String url) {
        if (!isIndexing()) {
            return null;
        }
        logger.info("Start indexing page " + url);
        try {
            HtmlDocument document = new HtmlDocument(url);
            Morphology morphology = new Morphology();
            SiteEntity site = siteEntityService.getByDocument(document);
            siteEntityService.saveStatusSite(site, SiteStatus.INDEXING);
            morphology.createLemmasMap(document.getText());

            PageEntity page = pageEntityService.getBySiteAndDocument(site, document);
            HashMap<String, Integer> lemmasMap = morphology.getLemmas();
            List<LemmaEntity> lemmaEntities = lemmaEntityService.saveLemmasFromList(lemmasMap, site);
            indexEntityService.saveIndexFromList(lemmaEntities, page, lemmasMap);
            siteEntityService.saveStatusSite(site, SiteStatus.INDEXED);

            logger.info("Stop indexing page " + url);
            return document;

        } catch (IOException e) {
            String errorMessage = "Error indexing page " + mainPage.getPath() + " has  error " + e.getLocalizedMessage();
            siteEntityService.saveSiteError(url, errorMessage);
            pageEntityService.savePageError(mainPage, errorMessage);
            logger.info(errorMessage);
            return null;
        }
    }
}
