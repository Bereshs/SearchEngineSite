package searchengine.data.services.html;

import lombok.Getter;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;

import javax.print.Doc;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class HtmlDocument {
    private final Document document;

    @Getter
    private final int statusCode;
    public HtmlDocument(Connection.Response connection) throws IOException {
        this.statusCode = connection.statusCode();
        this.document = connection.parse();
    }
    public Set<PageEntity> getChildPageList(SiteEntity siteEntity) throws IOException {
        Set<PageEntity> pageList = new HashSet<>();
        Elements elements = new Elements();
        elements = document.select("a[href]");
        elements.forEach(element -> {
            PageEntity page = new PageEntity();
            page.setPath(element.attr("href"));
            page.setContent(getHtml());
            page.setSite(siteEntity);
            page.setCode(getStatusCode());
            if (page.isValid()) {
                pageList.add(page);
            }
        });
        return pageList;
    }

    public String getTitle() {
        return document.title();
    }
    public String getHtml() {
        document.select("style").remove();
        document.select("script").remove();
        document.select("noscript").remove();
        return document.html();
    }

    public String getRootPath() {
        Pattern pattern = Pattern.compile("(https?://.*?/)");
        Matcher matcher = pattern.matcher(document.location());
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }


}
