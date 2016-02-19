package tv.emby.iap;

public interface ILogger {
    public void d(String tag, String message, Object... paramList);
    public void e(String tag, String message, Object... paramList);
    public void w(String tag, String message, Object... paramList);
}
