package programminglife.model.exception;

/**
 * Created by toinehartman on 04/05/2017.
 */
public class UnknownTypeException extends RuntimeException {
    public UnknownTypeException(String message) {
        super(message);
    }
}