package searchengine.dto.statistics;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SearchResponse {
    private boolean result;
    private int count;
    List<SearchData> data = new ArrayList<>();

}
