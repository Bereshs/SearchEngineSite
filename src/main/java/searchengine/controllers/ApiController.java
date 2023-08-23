package searchengine.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import searchengine.config.SitesList;
import searchengine.dto.statistics.SimpleResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.data.services.ParsingService;
import searchengine.data.services.StatisticsService;

import java.util.logging.Logger;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final StatisticsService statisticsService;
    private final ParsingService parsingService;
    private final SitesList sitesIndexingList;

    @Autowired
    public ApiController(StatisticsService statisticsService, ParsingService parsingService, SitesList sitesIndexingList) {
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
        sitesIndexingList.getSites().forEach(site -> {
            new Thread(()->{
                parsingService.indexingSite(site.getUrl());
                Logger.getLogger("creating thread ").info(site.getName());
            }).start();
        });

        return ResponseEntity.ok(new SimpleResponse(true));
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<SimpleResponse> stopIndexing() {
        parsingService.stop();

        return ResponseEntity.ok(new SimpleResponse(true));
    }
}
