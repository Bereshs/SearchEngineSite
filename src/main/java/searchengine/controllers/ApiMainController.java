package searchengine.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.config.SitesList;
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

import javax.persistence.criteria.CriteriaBuilder;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
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
    private final IndexEntityService indexEntityService;

    private final Logger logger = Logger.getLogger(ApiMainController.class.getName());

    @Autowired
    public ApiMainController(StatisticsService statisticsService, ParsingService parsingService, SitesList sitesIndexingList, LemmaEntityService lemmaEntityService, PageEntityService pageEntityService, IndexEntityService indexEntityService) {
        this.statisticsService = statisticsService;
        this.parsingService = parsingService;
        this.sitesIndexingList = sitesIndexingList;
        this.lemmaEntityService = lemmaEntityService;
        this.pageEntityService = pageEntityService;
        this.indexEntityService = indexEntityService;
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
        morphology.createLemmasMap(query);

        List<LemmaEntity> list = lemmaEntityService.getlemmaListByLemmaList(morphology.getLemmas());
        List<IndexEntity> listIndex = getUniqueIndexEntities(list);

        logger.info("ssss");
        listIndex.forEach(page -> {
            logger.info(page.getPage().getPath() + " rating =" + page.getRating());
        });


        float maxRating = getMaxRatingPage(listIndex);


        listIndex.forEach(indexPage -> {
            response.getData().add(createSearchDataFromIndexPage(indexPage, maxRating, list));
        });
        Collections.sort(response.getData());

        response.setCount(response.getData().size());
        response.setResult(true);
        return ResponseEntity.ok(response);
    }


    private float getMaxRatingPage(List<IndexEntity> list) {
        float maxRating = 0;
        for (IndexEntity indexEntity : list) {
            maxRating = Math.max(maxRating, indexEntity.getRating());
        }
        return maxRating;
    }


    private List<IndexEntity> getUniqueIndexEntities(List<LemmaEntity> list) {
        List<IndexEntity> lemmaOldList = indexEntityService.findByLemma(list.get(0));//indlemmaEntityService.getAllByLemma(list.get(0).getLemma());
        List<IndexEntity> resultIndex = new ArrayList<>();
        for (int i = 1; i < list.size(); i++) {
            List<IndexEntity> lemmaList = indexEntityService.findByLemma(list.get(i));//indlemmaEntityService.getAllByLemma(list.get(0).getLemma());
            resultIndex = compareLemmaEntityList(lemmaOldList, lemmaList);
            lemmaOldList = resultIndex;
        }

        return resultIndex;
    }

    private List<IndexEntity> compareLemmaEntityList(List<IndexEntity> list1, List<IndexEntity> list2) {
        List<IndexEntity> list3 = new ArrayList<>();
        for (IndexEntity indexLemma1 : list1) {
            for (IndexEntity indexLemma2 : list2) {
                addToIndexList(indexLemma1, indexLemma2, list3);
            }
        }


        return list3;
    }

    private void addToIndexList(IndexEntity indexLemma1, IndexEntity indexLemma2, List<IndexEntity> list3) {
        if ((indexLemma1.getPage().getPath()).equals(indexLemma2.getPage().getPath())) {
            if (list3.contains(indexLemma1)) {
                int index = list3.indexOf(indexLemma1);
                float rating = list3.get(index).getRating();
                list3.get(index).setRating(rating + indexLemma1.getRating());
            } else {
                list3.add(indexLemma1);
            }
        }

    }


    private SearchData createSearchDataFromIndexPage(IndexEntity indexPage, float maxRating, List<LemmaEntity> list) {

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
        searchData.setRelevance(indexPage.getRating() / maxRating);
        searchData.setSnippet(paragraph.getParagraph(list));

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
