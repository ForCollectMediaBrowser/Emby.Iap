package tv.emby.iap;


import android.content.Context;

import tv.emby.iap.billing.IabHelper;
import tv.emby.iap.billing.IabResult;
import tv.emby.iap.billing.Inventory;

/**
 * Created by Eric on 4/10/2015.
 */
public class IabValidator {

    private IabHelper iabHelper;
    private IResultHandler resultHandler;
    private String sku;

    public IabValidator(Context context, String key, IResultHandler resultHandler) {
        this.resultHandler = resultHandler;
        iabHelper = new IabHelper(context, key);
    }

    public void checkInAppPurchase(String sku) {
        this.sku = sku;
        iabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            @Override
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    //Failed to connect to Google Play
                    resultHandler.handleError(ErrorSeverity.Critical, ErrorType.UnableToConnectToStore, result.getMessage());

                } else {
                    iabHelper.queryInventoryAsync(mGotInventoryListener);
                }
            }
        });

    }

    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            if (result.isFailure()) {
                resultHandler.handleError(ErrorSeverity.Critical, ErrorType.UnableToValidatePurchase, result.getMessage());
            }
            else {
                // set our indicator of paid status
                resultHandler.handleResult(inventory.hasPurchase(sku) ? ResultType.Success : ResultType.Failure);
            }

            // no longer need connection to Google
            dispose();
        }
    };

    public void dispose() {
        if (iabHelper != null) iabHelper.dispose();
    }

}
