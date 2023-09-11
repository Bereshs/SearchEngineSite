package searchengine.dto.statistics;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel(description = "statistic response")
public class StatisticsResponse {
    private boolean result;
    private StatisticsData statistics;
}
