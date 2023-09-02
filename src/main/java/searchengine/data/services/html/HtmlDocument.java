package searchengine.data.services.html;

import lombok.Getter;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;

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
    public HtmlDocument(String url) throws IOException {
        Connection.Response connection = getConnection(url);
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
        return document.html();
    }
    public String getText() {
        return document.text();
    }

    public String getLocation() {return document.location();}

    public void  removeUnUseTags() {
        document.select("style").remove();
        document.select("script").remove();
        document.select("noscript").remove();
    }
    public String getRootPath() {
        Pattern pattern = Pattern.compile("(https?://.*?/)");
        Matcher matcher = pattern.matcher(document.location());
        if (matcher.find()) {
            return matcher.group(0);
        }
        return null;
    }

    private Connection.Response getConnection(String absolutePath) throws IOException {
        String userAgent = "Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6";
        String referrer = "http://www.google.com";

        return Jsoup.connect(absolutePath)
                .userAgent(userAgent)
                .referrer(referrer)
                .execute();
    }

}
