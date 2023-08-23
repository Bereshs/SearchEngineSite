package searchengine.data.services.html;


import lombok.Getter;
import lombok.Setter;
import searchengine.data.repository.PageEntityRepository;
import searchengine.model.PageEntity;

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
    private static PageEntityRepository pageRepository;

    public HtmlMapPage(PageEntity mainPage) {
        this.mainPage = mainPage;
    }

    @Override
    protected Integer compute() {
        Set<PageEntity> currentPageLinks = new HashSet<>();
        PageEntity getPage = pageRepository.findByPathAndSiteId(mainPage.getPath(), mainPage.getSite().getId());
        if (getPage != null || !mainPage.isValid()) {
            return 0;
        }

        synchronized (viewedLinkList) {
            viewedLinkList.add(mainPage.getAbsolutePath());
        }

        try {
            HtmlDocument document = new HtmlDocument(mainPage.getConnection());
            currentPageLinks = document.getChildPageList(mainPage.getSite());
            PageEntity pageToSave = new PageEntity();
            mainPage.setContent(document.getHtml());
            mainPage.setCode(document.getStatusCode());
            pageRepository.save(mainPage);
            mainPage=null;
            document=null;
        } catch (IOException e) {
            logger.info(mainPage.getPath() + " error " + e.getLocalizedMessage());
            mainPage.setCode(999);
            mainPage.setContent(e.getLocalizedMessage());
            pageRepository.save(mainPage);
            mainPage=null;
            return 0;
        }

        if (currentPageLinks.isEmpty()) {
            return 0;
        }

        for (PageEntity link : currentPageLinks) {
            if (!link.isValid()) {
                continue;
            }
            if (!viewedLinkList.contains(link.getAbsolutePath())) {
                HtmlMapPage task = new HtmlMapPage(link);
                if(!link.isValid()) {
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
        currentPageLinks=null;
        int result = 0;
        for (HtmlMapPage task : taskList) {
            result += task.join();
        }
        System.gc();
        return result;
    }

}
