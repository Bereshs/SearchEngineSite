package searchengine.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import searchengine.dto.statistics.SimpleResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Logger;

@ControllerAdvice
public class ExceptionHandlerController {

    @ExceptionHandler(IOException.class)
    public ResponseEntity<SimpleResponse> handleIOException(Exception exception) {
        return allException(exception);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<SimpleResponse> handleRuntimeException(Exception exception) {
        return allException(exception);
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<SimpleResponse> handleSQLException(Exception exception) {
        return allException(exception);
    }

    public ResponseEntity<SimpleResponse> allException(Exception exception) {
        SimpleResponse response = new SimpleResponse(false);
        response.setError(exception.getMessage());
        exception.printStackTrace();
        return ResponseEntity.ok(response);

    }

}
