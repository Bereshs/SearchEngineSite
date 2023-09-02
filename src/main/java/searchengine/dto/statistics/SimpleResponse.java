package searchengine.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
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
