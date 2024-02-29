package lh.demo.fraud.detection.api;

import io.grpc.StatusRuntimeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ControllerExceptionHandler {

    @ExceptionHandler(StatusRuntimeException.class)
    public ResponseEntity<ErrorResponse> handleGrpcException(StatusRuntimeException exception) {
        String message = exception.getStatus().getDescription();
        ResponseEntity.BodyBuilder response =
                switch (exception.getStatus().getCode()) {
                    case OK,
                            CANCELLED,
                            DEADLINE_EXCEEDED,
                            UNKNOWN,
                            INTERNAL,
                            UNAVAILABLE,
                            DATA_LOSS,
                            ABORTED,
                            UNIMPLEMENTED -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR);
                    case INVALID_ARGUMENT, FAILED_PRECONDITION, OUT_OF_RANGE -> ResponseEntity.status(
                            HttpStatus.BAD_REQUEST);
                    case NOT_FOUND -> ResponseEntity.status(HttpStatus.NOT_FOUND);
                    case ALREADY_EXISTS -> ResponseEntity.status(HttpStatus.CONFLICT);
                    case PERMISSION_DENIED -> ResponseEntity.status(HttpStatus.FORBIDDEN);
                    case RESOURCE_EXHAUSTED -> ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS);
                    case UNAUTHENTICATED -> ResponseEntity.status(HttpStatus.UNAUTHORIZED);
                };
        return response.body(new ErrorResponse(message));
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ErrorResponse> anyException(Throwable exception) {
        String message = exception.getMessage();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(message));
    }
}
