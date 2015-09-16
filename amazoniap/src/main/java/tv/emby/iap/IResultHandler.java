package tv.emby.iap;

/**
 * Created by Eric on 5/30/2015.
 */
public interface IResultHandler<T> {
    public void onResult(T result);
    public void onError(ErrorSeverity severity, ErrorType error, String message);
}
