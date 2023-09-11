package searchengine.dto.statistics;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "Detailed statistic about indexing")
public class DetailedStatisticsItem {
    @ApiModelProperty(value = "url site")
    private String url;
    @ApiModelProperty(value = "site name")
    private String name;
    @ApiModelProperty(value = "status index")
    private String status;
    @ApiModelProperty(value = "last change time")
    private long statusTime;
    @ApiModelProperty(value = "last error")
    private String error;
    @ApiModelProperty(value = "pages indexed")
    private int pages;
    @ApiModelProperty(value = "lemmas found")
    private int lemmas;
}
