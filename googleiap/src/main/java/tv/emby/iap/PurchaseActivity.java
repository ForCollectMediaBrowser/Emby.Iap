package tv.emby.iap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import java.util.UUID;

import tv.emby.googleiap.R;
import tv.emby.iap.billing.IabHelper;
import tv.emby.iap.billing.IabResult;
import tv.emby.iap.billing.Purchase;

public class PurchaseActivity extends Activity {

    private IabHelper iabHelper;
    private String sku;
    private Activity activity;
    private String check;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String key = intent.getStringExtra("googleKey");
        sku = intent.getStringExtra("sku");

        activity = this;
        iabHelper = new IabHelper(this, key);

        iabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            @Override
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    //Failed to connect to Google Play
                    Toast.makeText(getApplicationContext(), "Error connecting to Google Play Store.  Please try later.", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
                check = UUID.randomUUID().toString();
                if (InAppProduct.isSubscription(sku)) {
                    iabHelper.launchSubscriptionPurchaseFlow(activity, sku, 1000, new IabHelper.OnIabPurchaseFinishedListener() {
                        @Override
                        public void onIabPurchaseFinished(IabResult result, Purchase info) {
                            processPurchaseResult(result, info);
                        }
                    }, check);
                } else {
                    iabHelper.launchPurchaseFlow(activity, sku, 1000, new IabHelper.OnIabPurchaseFinishedListener() {
                        @Override
                        public void onIabPurchaseFinished(IabResult result, Purchase info) {
                            processPurchaseResult(result, info);
                        }


                    }, check);
                }
            }
        });

    }

    private void processPurchaseResult(IabResult result, Purchase info) {
        if (!result.isSuccess()) {
            if (!result.getMessage().contains("-1005")) { // make sure it isn't just user cancelled
                Toast.makeText(getApplicationContext(), "Error completing purchase.  Please try later.", Toast.LENGTH_LONG).show();
            }

            setResult(RESULT_CANCELED);
        } else {
            Intent success = new Intent();
            success.putExtra("storeToken", info.getToken());
            success.putExtra("store", "Google");
            setResult(RESULT_OK, success);
        }
        activity.finish();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (iabHelper == null) return;

        // Pass on the activity result to the helper for handling
        if (!iabHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves
            super.onActivityResult(requestCode, resultCode, data);
        }

    }

    @Override
    protected void onDestroy() {
        if (iabHelper != null) {
            iabHelper.dispose();
        }
        super.onDestroy();
    }

}
