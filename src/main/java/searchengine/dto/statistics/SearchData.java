package searchengine.dto.statistics;

import lombok.Data;

@Data
public class SearchData implements Comparable<Object> {
    String site;
    String siteName;
    String uri;
    String title;
    String snippet;
    double relevance;

    @Override
    public int compareTo(Object o) {
        return Double.compare(((SearchData) o).getRelevance(), getRelevance());
    }
}
