package searchengine.model;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name="site")
public class SiteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @Column(columnDefinition = "ENUM('INDEXING', 'INDEXED', 'FAILED') NOT NULL")
    SiteStatus status;

    @Column(columnDefinition = "DATETIME NOT NULL")
    LocalDateTime statusTime;

    @Column(columnDefinition = "TEXT")
    String lastError;

    @Column(columnDefinition = "VARCHAR(255) NOT NULL")
    String url;

    @Column(columnDefinition = "VARCHAR(255) NOT NULL")
    String name;

}
