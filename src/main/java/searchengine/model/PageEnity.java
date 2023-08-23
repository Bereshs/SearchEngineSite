package searchengine.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "page")
@Data
public class PageEnity {
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
}
