package tv.emby.iap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class UnlockActivity extends Activity {

    IabValidator iabValidator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unlock);

        Intent intent = getIntent();
        final String sku = intent.getStringExtra("sku");
        iabValidator = new IabValidator(this, "");

        iabValidator.purchase(this, sku, new IResultHandler<ResultType>() {
            @Override
            public void onResult(ResultType result) {
                // not used for amazon
            }

            @Override
            public void onError(ErrorSeverity severity, ErrorType error, String message) {
                // not used for amazon
            }
        });

    }
}
