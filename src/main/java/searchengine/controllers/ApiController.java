package searchengine.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import searchengine.dto.statistics.SimpleResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.data.services.ParsingService;
import searchengine.data.services.StatisticsService;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final StatisticsService statisticsService;
    private final ParsingService parsingService;

    @Autowired
    public ApiController(StatisticsService statisticsService, ParsingService parsingService) {
        this.statisticsService = statisticsService;
        this.parsingService = parsingService;
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }


    @GetMapping("/startIndexing")
    public ResponseEntity<SimpleResponse> startIndexing() {
        parsingService.indexingSite("playback.ru");

        return ResponseEntity.ok(new SimpleResponse(true));
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<SimpleResponse> stopIndexing() {
        parsingService.stop();

        return ResponseEntity.ok(new SimpleResponse(true));
    }
}
