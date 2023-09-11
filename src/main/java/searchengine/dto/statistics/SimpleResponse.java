package searchengine.dto.statistics;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "simple result")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SimpleResponse {
    boolean result;
    @ApiModelProperty(value = "error text, if present")
    String error;

    public SimpleResponse(boolean result) {
        this.result = result;
    }

    public SimpleResponse(boolean result, String error) {
        this.result = result;
        this.error = error;
    }
}
