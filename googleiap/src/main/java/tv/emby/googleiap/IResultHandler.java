package tv.emby.googleiap;

/**
 * Created by Eric on 5/30/2015.
 */
public interface IResultHandler {
    public void handleResult(ResultType result);
    public void handleError(ErrorSeverity severity, ErrorType error, String message);
}
