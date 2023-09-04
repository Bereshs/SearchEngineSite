package searchengine.model;

import lombok.Data;

import javax.persistence.*;
import java.util.logging.Logger;

@Data
@Entity
@Table(name = "lemma")
public class LemmaEntity implements Comparable<LemmaEntity> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(columnDefinition = "INT NOT NULL", name = "site_id")
    private SiteEntity site;

    @Column(columnDefinition = "VARCHAR(255) NOT NULL")
    private String lemma;

    @Column(columnDefinition = "INT NOT NULL")
    private Integer frequency;


    @Override
    public int compareTo(LemmaEntity o) {
        return frequency.compareTo(o.frequency);
    }
}
