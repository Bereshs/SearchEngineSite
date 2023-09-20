package searchengine.controllers;

import io.swagger.annotations.Api;
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
import searchengine.model.SiteEntity;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api")
@Api("All API Operations")
public class ApiMainController {

    private final StatisticsService statisticsService;
    private final ParsingService parsingService;
    private final SitesList sitesIndexingList;
    private final LemmaEntityService lemmaEntityService;
    private final PageEntityService pageEntityService;
    private final IndexEntityService indexEntityService;

    private final SiteEntityService siteEntityService;

    private final Logger logger = Logger.getLogger(ApiMainController.class.getName());

    private final SitesList sitesList;
    @Autowired
    public ApiMainController(StatisticsService statisticsService, ParsingService parsingService, SitesList sitesIndexingList, LemmaEntityService lemmaEntityService, PageEntityService pageEntityService, IndexEntityService indexEntityService, SiteEntityService siteEntityService, SitesList sitesList) {
        this.statisticsService = statisticsService;
        this.parsingService = parsingService;
        this.sitesIndexingList = sitesIndexingList;
        this.lemmaEntityService = lemmaEntityService;
        this.pageEntityService = pageEntityService;
        this.indexEntityService = indexEntityService;
        this.siteEntityService = siteEntityService;
        this.sitesList = sitesList;
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
    public ResponseEntity<SimpleResponse> indexPage(@RequestParam String url) {
        logger.info("Request for index page " + url);
        return ResponseEntity.ok(parsingService.indexPage(url));
    }

    @GetMapping("/search")
    public ResponseEntity<SearchResponse> search(String site, String query, int offset, int limit) throws IOException {
        Morphology morphology = new Morphology();
        SearchResponse response = new SearchResponse();
        morphology.createLemmasMap(query);

        sitesList.getSites().forEach(validSite -> {
            if(validSite.getUrl().equals(site) || site==null) {
                SiteEntity siteEntity = siteEntityService.getByUrl(validSite.getUrl());
                List<LemmaEntity> list = lemmaEntityService.getlemmaListByLemmaList(morphology.getLemmas(), siteEntity);
                List<IndexEntity> listIndex = getUniqueIndexEntities(list);
                float maxRating = getMaxRatingPage(listIndex);
                listIndex.forEach(indexPage -> response.getData().add(createSearchDataFromIndexPage(indexPage, maxRating, list)));
            }
        });


        Collections.sort(response.getData());
        response.setCount(response.getData().size());
        response.setData(getPageOfList(response.getData(), offset, limit));
        response.setResult(true);

        return ResponseEntity.ok(response);
    }


    public List<SearchData> getPageOfList(List<SearchData> list, Integer offset, Integer limit) {
        List<SearchData> result = new ArrayList<>();
        int startIndex = offset;
        int endIndex = startIndex + limit;
        if (endIndex > list.size()) {
            endIndex = list.size();
        }

        for (int i = startIndex; i < endIndex; i++) {
            result.add(list.get(i));
        }
        return result;
    }


    private float getMaxRatingPage(List<IndexEntity> list) {
        float maxRating = 0;
        for (IndexEntity indexEntity : list) {
            maxRating = Math.max(maxRating, indexEntity.getRating());
        }
        return maxRating;
    }


    private List<IndexEntity> getUniqueIndexEntities(List<LemmaEntity> list) {
        if(list.isEmpty()) {
            return new ArrayList<>();
        }
        List<IndexEntity> lemmaOldList = indexEntityService.findByLemma(list.get(0));
        List<IndexEntity> resultIndex = lemmaOldList;
        for (int i = 1; i < list.size(); i++) {
            List<IndexEntity> lemmaList = indexEntityService.findByLemma(list.get(i));
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
        if(m.find()) {
            return m.group(1);
        }
        return null;
    }
}
