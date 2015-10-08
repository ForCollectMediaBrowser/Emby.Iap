package tv.emby.iap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class PurchaseActivity extends Activity {

    private IabValidator iabValidator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        final String sku = intent.getStringExtra("sku");

        iabValidator = new IabValidator(this, "");

        iabValidator.purchase(this, sku, new IResultHandler<PurchaseResult>() {
            @Override
            public void onResult(PurchaseResult result) {
                switch (result.getResultCode()) {

                    case Success:
                        setResult(RESULT_OK);
                        Intent success = new Intent();
                        success.putExtra("storeToken", result.getStoreToken());
                        success.putExtra("store", "Google");
                        setResult(RESULT_OK, success);
                        break;
                    case Canceled:
                        setResult(RESULT_CANCELED);
                        break;
                    case Failure:
                        Intent error = new Intent();
                        error.putExtra("data", result.getResultCode());
                        setResult(RESULT_CANCELED, error);
                        break;
                }
                finish();
            }

            @Override
            public void onError(ErrorSeverity severity, ErrorType error, String message) {
                Intent data = new Intent();
                data.putExtra("data", message);
                setResult(RESULT_CANCELED, data);
                finish();
            }
        });


    }

    @Override
    protected void onDestroy() {
        if (iabValidator != null) {
            iabValidator.dispose();
        }
        super.onDestroy();
    }

}
