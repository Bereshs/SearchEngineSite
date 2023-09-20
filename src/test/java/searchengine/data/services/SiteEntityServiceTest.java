package searchengine.data.services;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import searchengine.data.repository.SiteEntityRepository;
import searchengine.model.SiteEntity;
import searchengine.model.SiteStatus;

import java.util.logging.Logger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@SpringBootTest
class SiteEntityServiceTest {

    private final SiteEntityService siteEntityService;

    @MockBean
    private final SiteEntityRepository siteEntityRepository;

    private final SiteEntity site;

    @Autowired
    SiteEntityServiceTest(SiteEntityService siteEntityService, SiteEntityRepository siteEntityRepository) {
        this.siteEntityService = siteEntityService;
        this.siteEntityRepository = siteEntityRepository;
        site = new SiteEntity("http://google.com");
        site.setName("google");
        site.setStatus(SiteStatus.INDEXED);
    }

    @Test
    void getByUrl() {
        Mockito.doReturn(site).when(siteEntityRepository).getByUrl(site.getUrl());
        SiteEntity siteEntity = siteEntityService.getByUrl(site.getUrl());
        Logger.getLogger("sss").info("s "+siteEntity);
        assertNotNull(siteEntity);
    }

    @Test
    void getByUrlOrCreate() {
        SiteEntity siteYandex = new SiteEntity();
        siteYandex.setUrl("http://yandex.ru");
        siteYandex.setName("yandex");
        Mockito.doReturn(siteYandex).when(siteEntityRepository).getByUrl(siteYandex.getUrl());
        SiteEntity siteEntity = siteEntityService.getByUrlOrCreate(siteYandex.getUrl());
        assertNotNull(siteEntity);
    }

    @Test
    void getRootPath() {
        String rootPth = siteEntityService.getRootPath(site.getUrl());
        assertThat(rootPth.equals(site.getUrl()));
    }
}