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
    private IResultHandler<ResultType> purchaseHandler;

    private Activity purchaseActivity;

    public IabValidator(Context context, String key) {
        //key is not used for Amazon
        PurchasingService.registerListener(context, new PurchasingListener(this));
    }

    public void setAmazonUserId(String id, String marketplace) {
        amazonUserId = id;
        amazonMarketplace = marketplace;
    }

    public void purchase(Activity activity, String sku, IResultHandler<ResultType> handler) {
        purchaseActivity = activity;
        purchaseHandler = handler;
        this.sku = sku;
        PurchasingService.purchase(sku);
    }

    public void purchaseComplete() {
        purchaseActivity.finish();
    }

    public void setResult(ResultType result) {
        purchaseHandler.onResult(result);
    }

    public void checkInAppPurchase(String sku, IResultHandler<ResultType> resultHandler) {
        this.sku = sku;
        this.purchaseHandler = resultHandler;
        final Set<String> productSkus =  new HashSet();
        productSkus.add(sku);
        PurchasingService.getProductData(productSkus);
        PurchasingService.getUserData();
        PurchasingService.getPurchaseUpdates(true);
    }

    public void handleReceipt(Receipt receipt, boolean fulfill) {
        if (receipt.isCanceled()) {
            purchaseHandler.onResult(ResultType.Canceled);
        } else {
            if (receipt.getSku().equals(sku)) {
                if (fulfill) PurchasingService.notifyFulfillment(receipt.getReceiptId(), FulfillmentResult.FULFILLED);
                purchaseHandler.onResult(ResultType.Success);
            } else {
                purchaseHandler.onError(ErrorSeverity.Critical, ErrorType.InvalidProduct, "Invalid sku reported: " + receipt.getSku());
                PurchasingService.notifyFulfillment(receipt.getReceiptId(), FulfillmentResult.UNAVAILABLE);
            }
        }
    }

}
