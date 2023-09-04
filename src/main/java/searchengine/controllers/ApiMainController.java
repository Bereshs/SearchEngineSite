package searchengine.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.config.SitesList;
import searchengine.data.repository.IndexEntityRepository;
import searchengine.data.services.*;
import searchengine.data.services.html.HtmlMapPage;
import searchengine.data.services.html.Morphology;
import searchengine.data.services.html.Paragraph;
import searchengine.dto.statistics.SearchData;
import searchengine.dto.statistics.SearchResponse;
import searchengine.dto.statistics.SimpleResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api")
public class ApiMainController {

    private final StatisticsService statisticsService;
    private final ParsingService parsingService;
    private final SitesList sitesIndexingList;
    private final LemmaEntityService lemmaEntityService;
    private final PageEntityService pageEntityService;
    private final IndexEntityRepository indexEntityRepository;

    private final Logger logger = Logger.getLogger(ApiMainController.class.getName());

    @Autowired
    public ApiMainController(StatisticsService statisticsService, ParsingService parsingService, SitesList sitesIndexingList, LemmaEntityService lemmaEntityService, PageEntityService pageEntityService, IndexEntityRepository indexEntityRepository) {
        this.statisticsService = statisticsService;
        this.parsingService = parsingService;
        this.sitesIndexingList = sitesIndexingList;
        this.lemmaEntityService = lemmaEntityService;
        this.pageEntityService = pageEntityService;
        this.indexEntityRepository = indexEntityRepository;
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
            logger.info("Creating thred for " + site.getName());
            parsingService.indexingSite(site.getUrl());
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

    @PostMapping("/indexPage")
    public ResponseEntity<SimpleResponse> indexPage(@RequestParam String url) throws IOException {
        logger.info("Request for index page " + url);
//        parsingService.createPageIndex(url);
        return ResponseEntity.ok(parsingService.indexPage(url));
    }

    @GetMapping("/search")
    public ResponseEntity<SearchResponse> search(String query, int offset, int limit) throws IOException {
        Morphology morphology = new Morphology();
        SearchResponse response = new SearchResponse();
        morphology.createLemmasList(query).forEach(lemma -> {
            List<LemmaEntity> lemmaEntityList = lemmaEntityService.getAllByLemma(lemma);
            lemmaEntityList.forEach(lemmaEntity -> {
                List<IndexEntity> indexEntity = indexEntityRepository.findByLemma(lemmaEntity);
                indexEntity.forEach(indexPage -> {
                    response.getData().add(createSearchDataFromIndexPage(indexPage, lemma));
                });
            });
            Collections.sort(response.getData());
        });

        response.setCount(response.getData().size());
        response.setResult(true);
        return ResponseEntity.ok(response);
    }

    private SearchData createSearchDataFromIndexPage(IndexEntity indexPage, String lemma) {
        PageEntity pageEntity = pageEntityService.findById(indexPage.getPage().getId());
        String pageContent = pageEntity.getContent()
                .replaceAll("<[^>]*>|\\n", "")
                .replaceAll("\\s+", " ");
        SearchData searchData = new SearchData();
        Paragraph paragraph = new Paragraph(pageContent);
        searchData.setSite(pageEntity.getSite().getUrl());
        searchData.setSiteName(pageEntity.getSite().getName());
        searchData.setUri(pageEntity.getPath());
        searchData.setTitle(getTitleFromHtml(pageEntity.getContent()));
        searchData.setRelevance(indexPage.getRating());
        searchData.setSnippet(paragraph.getParagraph(lemma));

        return searchData;
    }

    private String getTitleFromHtml(String html) {
        Pattern p = Pattern.compile("<title>(.*?)</title>");
        Matcher m = p.matcher(html);
        while (m.find()) {
            return m.group(1);
        }
        return null;
    }
}
