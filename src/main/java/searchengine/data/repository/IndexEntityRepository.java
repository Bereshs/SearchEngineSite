package searchengine.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;

import java.util.List;

public interface IndexEntityRepository extends JpaRepository<IndexEntity, Integer> {
    void deleteAllByPage(PageEntity page);

    List<IndexEntity> findByLemma(LemmaEntity lemma);

}
