package searchengine.model;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "lemma")
public class LemmaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @ManyToOne
    @JoinColumn(columnDefinition = "INT NOT NULL", name = "site_id")
    SiteEntity site;

    @Column(columnDefinition = "VARCHAR(255) NOT NULL")
    String lemma;

    @Column(columnDefinition = "INT NOT NULL")
    int frequency;




}
