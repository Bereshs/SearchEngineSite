package searchengine.data.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.data.repository.LemmaEntityRepository;
import searchengine.data.repository.PageEntityRepository;
import searchengine.data.repository.SiteEntityRepository;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.SiteEntity;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final Random random = new Random();
    private final SitesList sites;

    private final PageEntityRepository pageEntityRepository;
    private final LemmaEntityRepository lemmaEntityRepository;
    private final SiteEntityRepository siteEntityRepository;


    @Override
    public StatisticsResponse getStatistics() {
        String[] statuses = {"INDEXED", "FAILED", "INDEXING"};
        String[] errors = {
                "Ошибка индексации: главная страница сайта не доступна",
                "Ошибка индексации: сайт не доступен",
                ""
        };

        TotalStatistics total = new TotalStatistics();
        total.setSites((int) siteEntityRepository.count());
        total.setIndexing(true);

        List<DetailedStatisticsItem> detailed = new ArrayList<>();
        List<Site> sitesList = sites.getSites();
        for (int i = 0; i < sitesList.size(); i++) {
            Site site = sitesList.get(i);
            DetailedStatisticsItem item = getDetailedItem(site);
            if (item == null) {
                continue;
            }
            total.setPages(total.getPages() + item.getPages());
            total.setLemmas(total.getLemmas() + item.getLemmas());
            detailed.add(item);
        }

        StatisticsResponse response = new StatisticsResponse();
        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailed);
        response.setStatistics(data);
        response.setResult(true);
        return response;
    }

    private DetailedStatisticsItem getDetailedItem(Site site) {
        DetailedStatisticsItem item = new DetailedStatisticsItem();
        item.setName(site.getName());
        item.setUrl(site.getUrl());
        SiteEntity siteEntity = siteEntityRepository.getByUrl(site.getUrl());
        if (siteEntity == null) {
            siteEntity = siteEntityRepository.getByUrl(site.getUrl() + "/");
        }
        if (siteEntity == null) {
            return null;
        }

        int pages = (int) pageEntityRepository.countAllBySite(siteEntity);
        int lemmas = (int) lemmaEntityRepository.countAllBySite(siteEntity);
        item.setPages(pages);
        item.setLemmas(lemmas);
        item.setStatus(siteEntity.getStatus().toString());
        item.setError(siteEntity.getLastError() == null ? "" : siteEntity.getLastError());
        item.setStatusTime(siteEntity.getStatusTime().toInstant(ZoneOffset.ofTotalSeconds(10800)).toEpochMilli());
        return item;
    }
}
