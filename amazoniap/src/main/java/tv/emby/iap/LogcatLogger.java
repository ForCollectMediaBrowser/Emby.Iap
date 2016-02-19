package tv.emby.iap;

import android.util.Log;

public class LogcatLogger implements ILogger {
    @Override
    public void d(String tag, String message, Object... paramList) {
        Log.d(tag, String.format(message, paramList));
    }

    @Override
    public void e(String tag, String message, Object... paramList) {
        Log.e(tag, String.format(message, paramList));
    }

    @Override
    public void w(String tag, String message, Object... paramList) {
        Log.w(tag, String.format(message, paramList));
    }
}
