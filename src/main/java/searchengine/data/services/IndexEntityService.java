package searchengine.data.services;

import org.springframework.stereotype.Service;
import searchengine.data.repository.IndexEntityRepository;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Service
public class IndexEntityService {
    private final IndexEntityRepository indexEntityRepository;

    public IndexEntityService(IndexEntityRepository indexEntityRepository) {
        this.indexEntityRepository = indexEntityRepository;
    }

    private void save(IndexEntity indexEntity) {
        indexEntityRepository.save(indexEntity);
    }

    public void deleteAllByPage(PageEntity page) {
        indexEntityRepository.deleteAllByPage(page);
    }

    public void saveAll(List<IndexEntity> indexEntityList) {
        indexEntityRepository.saveAll(indexEntityList);
    }

    public void saveIndexFromList(List<LemmaEntity> lemmaEntities, PageEntity page) {
        List<IndexEntity> indexEntities = new ArrayList<>();
        lemmaEntities.forEach(lemmaEntity -> {
            IndexEntity indexEntity = new IndexEntity();
            indexEntity.setRating(lemmaEntity.getFrequency());
            indexEntity.setPage(page);
            indexEntity.setLemma(lemmaEntity);
            indexEntities.add(indexEntity);
        });
        saveAll(indexEntities);
    }
}
