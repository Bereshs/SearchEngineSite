package searchengine.dto.statistics;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "search result data object")
public class SearchData implements Comparable<Object> {
    @ApiModelProperty(value = "site")
    String site;
    @ApiModelProperty(value = "site name")
    String siteName;
    @ApiModelProperty(value = "site url")
    String uri;
    @ApiModelProperty(value = "site title")
    String title;
    @ApiModelProperty(value="snippet")
    String snippet;
    @ApiModelProperty(value="relevance")
    double relevance;

    @Override
    public int compareTo(Object o) {
        return Double.compare(((SearchData) o).getRelevance(), getRelevance());
    }
}
