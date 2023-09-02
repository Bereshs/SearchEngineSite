package searchengine.data.services.html;


import lombok.Getter;
import lombok.Setter;
import searchengine.config.SitesList;
import searchengine.data.LemmaEntityService;
import searchengine.data.PageEntityService;
import searchengine.data.services.IndexEntityService;
import searchengine.data.services.Morphology;
import searchengine.data.services.SiteEntityService;
import searchengine.dto.statistics.SimpleResponse;
import searchengine.model.*;

import javax.swing.text.Document;
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
    private String url;

    public HtmlMapPage(PageEntity mainPage) {
        this.mainPage = mainPage;
        indexing = true;
    }

    @Override
    protected Integer compute() {
        if (!indexing) {
            return 0;
        }

        PageEntity getPage = pageEntityService.findByPathAndSiteId(mainPage.getPath(), mainPage.getSite().getId());
        if (getPage != null || !mainPage.isValid()) {
            return 0;
        }

        synchronized (viewedLinkList) {
            viewedLinkList.add(mainPage.getAbsolutePath());
        }

        try {
            HtmlDocument document = new HtmlDocument(mainPage.getAbsolutePath());
            Set<PageEntity> currentPageLinks = document.getChildPageList(mainPage.getSite());
            document.removeUnUseTags();
            mainPage.setContent(document.getHtml());
            mainPage.setCode(document.getStatusCode());
            pageEntityService.save(mainPage);
            addToTaskList(currentPageLinks);
    //        pageLemmasAnalizing(document.getText(), mainPage);
        } catch (IOException e) {
            logger.info(mainPage.getPath() + " error " + e.getLocalizedMessage());
            mainPage.setCode(999);
            mainPage.setContent(e.getLocalizedMessage());
            pageEntityService.save(mainPage);
            return 0;
        }


        int result = 0;
        for (HtmlMapPage task : taskList) {
            result += task.join();
        }
//        System.gc();
        return result;
    }

    private void addToTaskList(Set<PageEntity> currentPageLinks) {
        for (PageEntity link : currentPageLinks) {
            if (!link.isValid()) {
                continue;
            }
            if (!viewedLinkList.contains(link.getAbsolutePath())) {
                HtmlMapPage task = new HtmlMapPage(link);
                if (!link.isValid()) {
                    continue;
                }
                task.setMainPage(link);
                task.fork();
                taskList.add(task);
                synchronized (viewedLinkList) {
                    viewedLinkList.add(link.getAbsolutePath());
                }
            }
        }

    }

    public HtmlDocument createPageIndex(String url) {
        logger.info("Start indexing page " + url);
        try {
            HtmlDocument document = new HtmlDocument(url);
            Morphology morphology = new Morphology();
            morphology.createLemmasMap(document.getText());

            logger.info("created " + morphology.getLemmas().size() + " lemmas  on " + url);
            SiteEntity site = siteEntityService.getByUrlAndDocument(url, document);
            PageEntity page = pageEntityService.getBySiteAndDocument(site, document);

            lemmaEntityService.getLemmaEntitiesBySite(site);
            List<LemmaEntity> lemmaEntities = lemmaEntityService.saveLemmasFromList(morphology.getLemmas(), site);
            logger.info("Saving indexes structure for page "+url);
            indexEntityService.saveIndexFromList(lemmaEntities, page);
            site.setStatus(SiteStatus.INDEXED);
            siteEntityService.save(site);
            logger.info("Stop indexing page "+url);
            return document;
        } catch (IOException e) {
            String errorMessage = "Error indexing page "+mainPage.getPath() + " has  error " + e.getLocalizedMessage();
            logger.info(errorMessage);
            mainPage.setCode(999);
            mainPage.setContent(e.getLocalizedMessage());
            pageEntityService.save(mainPage);
            return null;
        }
    }
}
