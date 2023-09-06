package searchengine.dto.statistics;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SimpleResponse {
    boolean result;
    String error;

    public SimpleResponse(boolean result) {
        this.result = result;
    }

    public SimpleResponse(boolean result, String error) {
        this.result = result;
        this.error = error;
    }
}
