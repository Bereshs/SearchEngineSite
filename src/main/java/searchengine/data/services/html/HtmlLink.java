package searchengine.data.services.html;

import lombok.Data;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.IOException;
import java.util.logging.Logger;

@Data
public class HtmlLink implements Comparable<HtmlLink> {
    private String absolutePath;
    private String rootPath;
    private String relativePath;
    private Logger logger = Logger.getLogger(HtmlLink.class.getName());
    private String path;
    //TODO inject config from properties
    private String userAgentProperty = "Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6";
    private String referrerProperty = "http://www.google.com";

    public HtmlLink(String path, String rootPath) {
        this.rootPath = getPathWithHttps(rootPath);
        this.absolutePath = setAbsolute(path, rootPath);
        this.relativePath = isRelative(path) ? path : createRelativePath(path, rootPath);
        this.path = path;
    }


    private String createRelativePath(String path, String rootPath) {
        return path.length() > rootPath.length() ? path.substring(rootPath.length()) : "/" + path;
    }

    private boolean isRelative(String path) {
        return path.startsWith("/") && !path.contains("http");
    }

    private String getPathWithHttps(String path) {
        if (isHaveHttp(path)) {
            return path;
        }
        return setAbsolute(path, path);
    }

    private String setAbsolute(String path, String rootPath) {
        if (isAbsolute(path)) {
            return path;
        }
        if (!isHaveHttp(path) && rootPath.equals(path)) {
            return "https://" + path + "/";
        }
        String absolutePath = rootPath + path;
        absolutePath = absolutePath.replaceAll("//", "/").replace(":/", "://");
        return absolutePath;
    }

    private boolean isAbsolute(String testingPath) {
        return isHaveHttp(testingPath) && !testingPath.matches("(.+;$)|(.+:$)|(.+#$)");
    }

    private boolean isHaveHttp(String testingPath) {
        return testingPath.startsWith("http");
    }

    public boolean isValid() {
        return absolutePath.startsWith("http")
                && !absolutePath.matches("(.+;$)|(.+:$)|(.+#$)")
                && absolutePath.contains(rootPath)
                && !absolutePath.equals(rootPath)
                && !path.matches("^(tel|mailto|tg):.*$")
                && !path.matches(".+[pdf|jpg]$");
    }

    public Document getDocument() throws IOException {
//        logger.info("user agent =" + userAgentProperty);
//        logger.info("user referrer =" + referrerProperty);
        return Jsoup.connect(getAbsolutePath())
                .userAgent(userAgentProperty)
                .referrer(referrerProperty)
                .get();
    }

    @Override
    public int compareTo(HtmlLink o) {
        return o.getAbsolutePath().compareTo(getAbsolutePath());
    }
}
