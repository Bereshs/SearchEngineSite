package searchengine.dto.statistics;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@ApiModel(description = "search result object")
public class SearchResponse {
    @ApiModelProperty(value = "search result")
    private boolean result;
    @ApiModelProperty(value = "count found results")
    private int count;
    List<SearchData> data = new ArrayList<>();

}
