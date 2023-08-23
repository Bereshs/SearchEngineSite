package searchengine.model;

import lombok.Data;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import searchengine.config.AppConfig;

import javax.persistence.*;
import java.io.IOException;

@Entity
@Table(name = "page")
@Data
public class PageEntity implements Comparable<PageEntity> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int Id;

    @ManyToOne
    @JoinColumn(columnDefinition = "INT NOT NULL", name = "site_id")
    private SiteEntity site;

    @Column(columnDefinition = "TEXT NOT NULL")
    private String path;

    @Column(columnDefinition = "INT NOT NULL")
    private int code;

    @Column(columnDefinition = "MEDIUMTEXT NOT NULL")
    private String content;

    public String getAbsolutePath() {
        if(getPath().startsWith("http")) {
            return getPath();
        }
        String absolutePath = getSite().getUrl() + getPath();
        absolutePath = absolutePath.replaceAll("//", "/").replace(":/", "://");
        return absolutePath;
    }

    public boolean isValid() {
        String absolutePath = getAbsolutePath();
        String rootPath = getSite().getUrl();
        return absolutePath.startsWith("http")
                && !absolutePath.matches("(.+;$)|(.+:$)|(.+#$)")
                && absolutePath.contains(rootPath)
    //            && !absolutePath.equals(rootPath)
                && !path.matches("^(tel|mailto|tg):.*$")
                && !path.matches(".+[pdf|jpg]$");
    }

    public Connection.Response getConnection() throws IOException {
        String userAgent = "Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6";
        String referrer = "http://www.google.com";

        return Jsoup.connect(getAbsolutePath())
                .userAgent(userAgent)
                .referrer(referrer)
                .execute();
    }


    @Override
    public int compareTo(PageEntity o) {
        return getAbsolutePath().compareTo(o.getAbsolutePath());
    }


}
