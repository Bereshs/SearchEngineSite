package searchengine.data.services;

import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.data.repository.LemmaEntityRepository;
import searchengine.model.LemmaEntity;
import searchengine.model.SiteEntity;

import java.util.*;
import java.util.logging.Logger;

@Service
public class LemmaEntityService {
    private final LemmaEntityRepository lemmaEntityRepository;

    public LemmaEntityService(LemmaEntityRepository lemmaEntityRepository) {
        this.lemmaEntityRepository = lemmaEntityRepository;
    }

    public LemmaEntity getLemmaBySite(String lemma, SiteEntity site) {
        return lemmaEntityRepository.getLemmaEntityByLemmaAndSite(lemma, site);
    }

    public void saveAll(Iterable<LemmaEntity> lemmas) {
        lemmaEntityRepository.saveAll(lemmas);
    }

    public List<LemmaEntity> getLemmaEntitiesBySite(SiteEntity site) {
        return lemmaEntityRepository.getLemmaEntitiesBySite(site);
    }

    public void deleteAllBySite (SiteEntity site) {
        lemmaEntityRepository.deleteAllBySite(site);
    }

    public LemmaEntity getByLemmaAndSite(String word, SiteEntity site) {
        LemmaEntity lemma = lemmaEntityRepository.getLemmaEntityByLemmaAndSite(word, site);
        if (lemma == null) {
            lemma = new LemmaEntity();
            lemma.setSite(site);
            lemma.setLemma(word);
            lemma.setFrequency(0);
        }
        int frequency = lemma.getFrequency();
        lemma.setFrequency(frequency);

        lemmaEntityRepository.save(lemma);
        return lemma;
    }


    public List<LemmaEntity> saveLemmasFromList(HashMap<String, Integer> lemmasList, SiteEntity site) {
        List<LemmaEntity> newList = new ArrayList<>();
        lemmasList.forEach((key, value) -> {
            LemmaEntity lemma = getByLemmaAndSite(key, site);
            int frequency = lemma.getFrequency() + 1;
            lemma.setFrequency(frequency);
            newList.add(lemma);
        });
        saveAll(newList);
        return newList;
    }

    public long getCount() {
        return lemmaEntityRepository.count();
    }


    public List<LemmaEntity> getAllByLemma(String lemma) {
        return lemmaEntityRepository.getAllByLemmaOrderByFrequencyAsc(lemma);
    }


    public List<LemmaEntity> getlemmaListByLemmaList(HashMap<String, Integer> lemmaList) {
        List<LemmaEntity> lemmaEntityListFromDb = new ArrayList<>();
        lemmaList.forEach((lemma,value) -> {
            LemmaEntity lemmaEntity = lemmaEntityRepository.findByLemma(lemma);
            if(lemmaEntity!=null) {
                lemmaEntityListFromDb.add(lemmaEntity);
            }
        });
        Collections.sort(lemmaEntityListFromDb);
        return lemmaEntityListFromDb;
    }
}
