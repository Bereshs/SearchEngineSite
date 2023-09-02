package searchengine.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import searchengine.model.LemmaEntity;
import searchengine.model.SiteEntity;

import java.util.List;

public interface LemmaEntityRepository extends JpaRepository<LemmaEntity, Integer> {
    List<LemmaEntity> getLemmaEntitiesBySite(SiteEntity siteEntity);

    LemmaEntity getLemmaEntityByLemmaAndSite(String lemma,  SiteEntity site);


}
