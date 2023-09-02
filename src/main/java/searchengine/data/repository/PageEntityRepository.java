package searchengine.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.PageEntity;

import java.util.List;

@Repository
public interface PageEntityRepository extends JpaRepository<PageEntity, Integer> {
    List<PageEntity> findBySiteId(int siteId);

    PageEntity findByPathAndSiteId(String path, int siteId);

//    void deleteAll(List<PageEntity> pages);
}
