package searchengine.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;

import java.util.List;

@Repository
public interface SiteEntityRepository extends JpaRepository<SiteEntity, Integer> {
    public SiteEntity getByUrl(String url);

}
