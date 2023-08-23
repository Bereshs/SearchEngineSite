package searchengine.data.services.html;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class HtmlDocument {
    private final Document document;
    private Logger logger = Logger.getLogger(HtmlDocument.class.getName());

    public HtmlDocument(Document document) {
        this.document = document;
    }

    public Set<HtmlLink> getHtmlLinkList() {
        Set<HtmlLink> linkList = new HashSet<>();
        Elements elements = new Elements();
        elements = document.select("a[href]");
        elements.forEach(element -> {
            HtmlLink link = new HtmlLink(element.attr("href"), getRootPath());
            if (link.isValid()) {
                linkList.add(link);
            }
        });
        return linkList;
    }

    public String getBody() {
        return document.body().html();
    }

    public String getTitle() {
        return document.title();
    }

    public String getHtml() {
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
