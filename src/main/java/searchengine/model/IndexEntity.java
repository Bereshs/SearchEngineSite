package searchengine.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "search_index")
@Data
public class IndexEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @ManyToOne
    @JoinColumn(columnDefinition = "INT NOT NULL", name = "page_id")
    PageEnity page;

    @ManyToOne
    @JoinColumn(columnDefinition = "INT NOT NULL", name = "lemma_id")
    LemmaEntity lemma;

    @Column(columnDefinition = "FLOAT NOT NULL")
    float rating;
}
