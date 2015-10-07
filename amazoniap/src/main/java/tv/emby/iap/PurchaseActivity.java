package tv.emby.iap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import org.json.JSONException;
import org.json.JSONObject;

public class PurchaseActivity extends Activity {

    IabValidator iabValidator;
    String productJson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unlock);

        Intent intent = getIntent();
        productJson = intent.getStringExtra("product");
        final String sku;
        JSONObject product;
        try {
            product = new JSONObject(productJson);
            sku = product.getString("sku");
        } catch (JSONException e) {
            e.printStackTrace();
            finish();
            return;
        }

        iabValidator = new IabValidator(this, "");

        iabValidator.purchase(this, sku, new IResultHandler<ResultType>() {
            @Override
            public void onResult(ResultType result) {
                switch (result) {

                    case Success:
                        Intent success = new Intent();
                        success.putExtra("product", productJson);
                        success.putExtra("storeToken", iabValidator.getReceiptId());
                        success.putExtra("storeId", iabValidator.getAmazonUserId());
                        success.putExtra("store", "Amazon");
                        setResult(RESULT_OK, success);
                        break;
                    case Canceled:
                        setResult(RESULT_CANCELED);
                        break;
                    case Failure:
                        setResult(RESULT_CANCELED);
                        break;
                }
            }

            @Override
            public void onError(ErrorSeverity severity, ErrorType error, String message) {
                // not used for amazon
            }
        });

    }
}
