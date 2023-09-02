package searchengine.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.config.SitesList;
import searchengine.data.services.html.HtmlMapPage;
import searchengine.dto.statistics.SimpleResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.data.services.ParsingService;
import searchengine.data.services.StatisticsService;

import java.io.IOException;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api")
public class ApiMainController {

    private final StatisticsService statisticsService;
    private final ParsingService parsingService;
    private final SitesList sitesIndexingList;

    private final Logger logger = Logger.getLogger(ApiMainController.class.getName());

    @Autowired
    public ApiMainController(StatisticsService statisticsService, ParsingService parsingService, SitesList sitesIndexingList) {
        this.statisticsService = statisticsService;
        this.parsingService = parsingService;
        this.sitesIndexingList = sitesIndexingList;
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }


    @GetMapping("/startIndexing")
    public ResponseEntity<SimpleResponse> startIndexing() {
        logger.info("Request for start indexing. size sites=" + sitesIndexingList.getSites().size());
        if (HtmlMapPage.isIndexing()) {
            logger.info("Indexing already started");
            return ResponseEntity.ok(new SimpleResponse(false, "Индексация уже запущена"));

        }
        sitesIndexingList.getSites().forEach(site -> new Thread(() -> {
            parsingService.indexingSite(site.getUrl());
            Logger.getLogger("creating thread ").info(site.getName());
        }).start());

        return ResponseEntity.ok(new SimpleResponse(true));
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<SimpleResponse> stopIndexing() {
        logger.info("request for stop indexing service");
        if (HtmlMapPage.isIndexing()) {
            parsingService.stop();
            return ResponseEntity.ok(new SimpleResponse(true));
        }
        return ResponseEntity.ok(new SimpleResponse(false, "Индексация не запущена"));
    }

    @GetMapping("/indexPage")
    public ResponseEntity<SimpleResponse> indexPage(@RequestParam String url) throws IOException {
        logger.info("Request for index page " + url);
//        parsingService.createPageIndex(url);
        return ResponseEntity.ok(parsingService.indexPage(url));
    }

}
