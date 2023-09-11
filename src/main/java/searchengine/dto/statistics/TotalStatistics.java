package searchengine.dto.statistics;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "statistics site indexing")
public class TotalStatistics {
    @ApiModelProperty(value = "total sites indexing")
    private int sites;
    @ApiModelProperty(value = "total pages indexing")
    private int pages;
    @ApiModelProperty(value = "total lemmas indexing")
    private int lemmas;
    @ApiModelProperty(value = "status indexing")
    private boolean indexing;
}
