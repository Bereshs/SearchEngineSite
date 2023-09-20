package searchengine.data.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class IndexEntityServiceTest {
    @MockBean
    private final IndexEntityService indexEntityService;
    @MockBean
    private final PageEntityService pageEntityService;
    @MockBean
    private final LemmaEntityService lemmaEntityService;
    @MockBean
    private final SiteEntityService siteEntityService;
    private final PageEntity page;
    private final LemmaEntity lemma;

    @Autowired
    IndexEntityServiceTest(IndexEntityService indexEntityService, PageEntityService pageEntityService, LemmaEntityService lemmaEntityService, SiteEntityService siteEntityService) {
        this.indexEntityService = indexEntityService;
        this.pageEntityService = pageEntityService;
        this.lemmaEntityService = lemmaEntityService;
        this.siteEntityService = siteEntityService;
        SiteEntity site = new SiteEntity();
        site.setName("noname");
        site.setUrl("http://loclhost");

        page = new PageEntity();
        page.setSite(new SiteEntity());
        page.setCode(200);
        page.setContent("Хорошо в деревне летом");
        page.setPath("http://localhost");

        lemma = new LemmaEntity();
        lemma.setLemma("Хорошо");
        lemma.setSite(site);
        lemma.setFrequency(1);

        siteEntityService.save(site);
        pageEntityService.save(page);
        lemmaEntityService.save(lemma);

    }


    @Test
    void saveAllTest() {
        List<IndexEntity> list = new ArrayList<>();
        IndexEntity indexEntity = new IndexEntity();
        indexEntity.setLemma(new LemmaEntity());
        indexEntity.setRating((float) (Math.random() * 20));
        indexEntity.setPage(page);
        list.add(indexEntity);
        indexEntityService.saveAll(list);

    }

    @Test
    void findByLemmaTest() {
        List<IndexEntity> list = indexEntityService.findByLemma(lemma);
        list.forEach(indexEntity -> assertThat(indexEntity.getLemma().getLemma().equals(lemma.getLemma())));
    }

    @Test
    void deleteAllByPageTest() {
        indexEntityService.deleteAllByPage(page);
        assertThat(indexEntityService.findByLemma(lemma).isEmpty());
    }

}