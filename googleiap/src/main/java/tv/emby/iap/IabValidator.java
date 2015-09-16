package tv.emby.iap;


import android.content.Context;

import java.util.List;

import tv.emby.iap.billing.IabHelper;
import tv.emby.iap.billing.IabResult;
import tv.emby.iap.billing.Inventory;

/**
 * Created by Eric on 4/10/2015.
 */
public class IabValidator {

    private IabHelper iabHelper;
    private IResultHandler<ResultType> purchaseCheckHandler;
    private String sku;
    private boolean initialized;
    private String message;

    public IabValidator(Context context, String key) {
        iabHelper = new IabHelper(context, key);
        iabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            @Override
            public void onIabSetupFinished(IabResult result) {
                initialized = result.isSuccess();
                message = result.getMessage();
            }
        });
    }

    public void checkInAppPurchase(String sku, IResultHandler<ResultType> resultHandler) {
        if (!initialized) {
            resultHandler.onError(ErrorSeverity.Critical, ErrorType.UnableToConnectToStore, message);
        } else {
            this.sku = sku;
            this.purchaseCheckHandler = resultHandler;

            iabHelper.queryInventoryAsync(mPurchaseCheckListener);
        }

    }

    IabHelper.QueryInventoryFinishedListener mPurchaseCheckListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            if (result.isFailure()) {
                purchaseCheckHandler.onError(ErrorSeverity.Critical, ErrorType.UnableToValidatePurchase, result.getMessage());
            }
            else {
                // set our indicator of paid status
                purchaseCheckHandler.onResult(inventory.hasPurchase(sku) ? ResultType.Success : ResultType.Failure);
            }

        }
    };

    public void getAvailableProductsAsync(final IResultHandler<List<InAppProduct>> handler) {
        if (!initialized) {
            handler.onError(ErrorSeverity.Critical, ErrorType.UnableToConnectToStore, message);
        } else {

        }
    }

    public void dispose() {
        if (iabHelper != null) iabHelper.dispose();
    }

}
