package searchengine.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "page")
@Data
public class PageEnity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int Id;

    @ManyToOne
    @JoinColumn(columnDefinition = "INT NOT NULL", name = "site_id")
    SiteEntity site;

    @Column(columnDefinition = "TEXT NOT NULL")
    String path;

    @Column(columnDefinition = "INT NOT NULL")
    int code;

    @Column(columnDefinition = "MEDIUMTEXT NOT NULL")
    String content;
}
