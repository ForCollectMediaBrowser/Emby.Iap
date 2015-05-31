package tv.emby.iap;

import android.app.Activity;
import android.content.Context;

import com.amazon.device.iap.PurchasingService;
import com.amazon.device.iap.model.FulfillmentResult;
import com.amazon.device.iap.model.Receipt;

import java.util.HashSet;
import java.util.Set;

import tv.emby.iap.billing.PurchasingListener;

/**
 * Created by Eric on 4/10/2015.
 */
public class IabValidator {

    public static String SKU_UNLOCK = "tv.emby.embyatv.unlock";

    private String amazonUserId;
    private String amazonMarketplace;
    private String sku;
    private IResultHandler resultHandler;

    private Activity purchaseActivity;

    public IabValidator(Context context, String key, IResultHandler resultHandler) {
        //key is not used for Amazon
        this.resultHandler = resultHandler;
        PurchasingService.registerListener(context, new PurchasingListener(this));
    }

    public void setAmazonUserId(String id, String marketplace) {
        amazonUserId = id;
        amazonMarketplace = marketplace;
    }

    public void purchase(Activity activity, String sku) {
        purchaseActivity = activity;
        this.sku = sku;
        PurchasingService.purchase(sku);
    }

    public void purchaseComplete() {
        purchaseActivity.finish();
    }

    public void setResult(ResultType result) {
        resultHandler.handleResult(result);
    }

    public void checkInAppPurchase(String sku) {
        this.sku = sku;
        final Set<String> productSkus =  new HashSet();
        productSkus.add(sku);
        PurchasingService.getProductData(productSkus);
        PurchasingService.getUserData();
        PurchasingService.getPurchaseUpdates(true);
    }

    public void handleReceipt(Receipt receipt) {
        if (receipt.isCanceled()) {
            resultHandler.handleResult(ResultType.Canceled);
        } else {
            if (receipt.getSku().equals(sku)) {
                PurchasingService.notifyFulfillment(receipt.getReceiptId(), FulfillmentResult.FULFILLED);
                resultHandler.handleResult(ResultType.Success);
            } else {
                resultHandler.handleError(ErrorSeverity.Critical, ErrorType.InvalidProduct, "Invalid sku reported: "+receipt.getSku());
                PurchasingService.notifyFulfillment(receipt.getReceiptId(), FulfillmentResult.UNAVAILABLE);
            }
        }
    }

}
