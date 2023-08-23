package searchengine.data.services.html;

import lombok.Data;
import searchengine.model.SiteEntity;

import java.util.List;

@Data
public class ListLinkPageDto {
    private List<HtmlLink> htmlLinkList;
    private SiteEntity siteEntity;
    private boolean result = false;
}
