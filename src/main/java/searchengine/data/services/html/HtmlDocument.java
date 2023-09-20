package searchengine.data.services.html;

import lombok.Getter;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import searchengine.config.ConnectionConfig;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class HtmlDocument {
    private final Document document;

    @Getter
    private final int statusCode;

    public HtmlDocument(String url, ConnectionConfig config) throws IOException {
        Connection.Response connection = getConnection(url, config);
        this.statusCode = connection.statusCode();
        this.document = connection.parse();
        if(statusCode>399 && statusCode<499) {
            try {
                Thread.sleep(500);
                connection =getConnection(url, config);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public Set<PageEntity> getChildPageList(SiteEntity siteEntity) {
        Set<PageEntity> pageList = new HashSet<>();
        Elements elements;
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

    public String getLocation() {
        String location = document.location();
        if (location.contains(getRootPath())) {
            int startPath = location.indexOf(getRootPath()) + getRootPath().length();
            String path = location.substring(startPath);
            return path.startsWith("/") ? path : "/" + path;
        }
        return location;


    }

    public void removeUnUseTags() {
        document.select("style").remove();
        document.select("script").remove();
        document.select("noscript").remove();
    }

    public String getRootPath() {
        String location = document.location();
        if (!location.endsWith("/")) {
            location = location + "/";
        }
        Pattern pattern = Pattern.compile("(https?://.*?/)");
        Matcher matcher = pattern.matcher(location);
        if (matcher.find()) {
            String result = matcher.group(0);
            return result.substring(0, result.length() - 1);
        }
        return null;
    }

    private Connection.Response getConnection(String absolutePath, ConnectionConfig config) throws IOException {
        return Jsoup.connect(absolutePath)
                .userAgent(config.getUserAgent())
                .referrer(config.getReferrer())
                .execute();
    }

}
