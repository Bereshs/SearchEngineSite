package searchengine.data.services.html;


import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import searchengine.data.repository.PageEntityRepository;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.RecursiveTask;
import java.util.logging.Logger;


public class HtmlPage extends RecursiveTask<List<HtmlLink>> {
    private final Logger logger = Logger.getLogger(HtmlPage.class.getName());
    private final List<HtmlPage> taskList;
    @Getter
    private static final Set<HtmlLink> viewedLinkList = new HashSet<>();
    private final HtmlLink linkPage;

    public HtmlPage(HtmlLink link) {
        linkPage = link;
        taskList = new ArrayList<>();
    }

    @Override
    protected List<HtmlLink> compute() {
        Set<HtmlLink> currentPageLinks = new HashSet<>();
        try {
            HtmlDocument document = new HtmlDocument(linkPage.getDocument());
            currentPageLinks = document.getHtmlLinkList();
        } catch (IOException e) {
            logger.info(linkPage.getPath()+" error "+ e.getLocalizedMessage());
            return new ArrayList<>(currentPageLinks);
        }

        if (currentPageLinks.isEmpty()) {
            return new ArrayList<>(currentPageLinks);
        }

        synchronized (viewedLinkList) {
            viewedLinkList.add(linkPage);
        }
        for (HtmlLink link : currentPageLinks) {
            if (!link.isValid()) {
                continue;
            }
            if (!viewedLinkList.contains(link)) {
                HtmlPage task = new HtmlPage(link);
                task.fork();
                taskList.add(task);
                synchronized (viewedLinkList) {
                    viewedLinkList.add(link);
                }
            }
        }
        for (HtmlPage task : taskList) {
            currentPageLinks.addAll(task.join());
        }

        return new ArrayList<>(currentPageLinks);
    }


}
