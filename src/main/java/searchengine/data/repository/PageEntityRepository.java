package searchengine.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.PageEnity;

@Repository
public interface PageEntityRepository extends JpaRepository<PageEnity, Integer> {
}
