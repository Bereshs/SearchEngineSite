package searchengine.model;

import lombok.Data;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import javax.persistence.*;
import java.io.IOException;

@Entity
@Table(name = "page")
//@Table(name = "page", indexes = {@Index(columnList = "path", name = "path_index")})
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

    public PageEntity() {
        path = "";
    }

    public String getAbsolutePath() {
        if (getPath().startsWith("http")) {
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
                && !path.matches(".+[pdf|jpg]$")
                && !path.contains("#");
    }




    @Override
    public int compareTo(PageEntity o) {
        return getAbsolutePath().compareTo(o.getAbsolutePath());
    }


}
