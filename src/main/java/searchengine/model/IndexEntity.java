package searchengine.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "search_index")
@Data
public class IndexEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(columnDefinition = "INT NOT NULL", name = "page_id")
    private PageEntity page;

    @ManyToOne
    @JoinColumn(columnDefinition = "INT NOT NULL", name = "lemma_id")
    private LemmaEntity lemma;

    @Column(columnDefinition = "FLOAT NOT NULL")
    private float rating;
}
