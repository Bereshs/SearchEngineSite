package searchengine.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import searchengine.config.Site;
import searchengine.model.LemmaEntity;
import searchengine.model.SiteEntity;

import java.util.List;

public interface LemmaEntityRepository extends JpaRepository<LemmaEntity, Integer> {
    List<LemmaEntity> getLemmaEntitiesBySite(SiteEntity siteEntity);

    LemmaEntity getLemmaEntityByLemmaAndSite(String lemma,  SiteEntity site);


    long countAllBySite(SiteEntity site);

    List<LemmaEntity> getAllByLemmaOrderByFrequencyAsc(String lemma);

    void deleteAllBySite(SiteEntity site);


    LemmaEntity findByLemma(String lemma);

    LemmaEntity findByLemmaAndSite(String lemma, SiteEntity site);
}
