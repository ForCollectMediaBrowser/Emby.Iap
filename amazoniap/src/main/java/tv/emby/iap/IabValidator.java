package tv.emby.iap;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.amazon.device.iap.PurchasingService;
import com.amazon.device.iap.model.FulfillmentResult;
import com.amazon.device.iap.model.Product;
import com.amazon.device.iap.model.ProductDataResponse;
import com.amazon.device.iap.model.Receipt;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
    private IResultHandler<List<InAppProduct>> productHandler;
    private boolean disposed;

    private Activity purchaseActivity;

    public IabValidator(Context context, String key) {
        //key is not used for Amazon
        PurchasingService.registerListener(context, new PurchasingListener(this));
    }

    public void setAmazonUserId(String id, String marketplace) {
        amazonUserId = id;
        amazonMarketplace = marketplace;
    }

    public boolean isDisposed() { return disposed; }

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

    public void getAvailableProductsAsync(final IResultHandler<List<InAppProduct>> resultHandler) {
        Log.d("AmazonIap", "*** getAvailableProductsAsync");
        productHandler = resultHandler;
        PurchasingService.getProductData(InAppProduct.getCurrentSkus());
    }

    public void handleProductResponse(ProductDataResponse response) {
        Log.d("AmazonIap", "*** handleProductResponse - "+response.getRequestStatus());
        if (productHandler == null) return;

        final ProductDataResponse.RequestStatus status = response.getRequestStatus();

        switch (status) {
            case SUCCESSFUL:
                List<InAppProduct> products = new ArrayList<>();
                final Map<String,Product> amazonProducts = response.getProductData();
                for (String key : amazonProducts.keySet()) {
                    products.add(new InAppProduct(amazonProducts.get(key)));
                }
                productHandler.onResult(products);
                break;
            case FAILED:
            case NOT_SUPPORTED:
                productHandler.onError(ErrorSeverity.Critical, ErrorType.UnableToConnectToStore, response.toString());
                break;
        }

    }


    public void checkInAppPurchase(String sku, IResultHandler<ResultType> resultHandler) {
        this.sku = sku;
        this.purchaseHandler = resultHandler;
        Log.d("AmazonIap", "*** checkInAppPurchase - "+sku);
        final Set<String> productSkus =  new HashSet();
        productSkus.add(sku);
        PurchasingService.getProductData(productSkus);
        PurchasingService.getUserData();
        PurchasingService.getPurchaseUpdates(true);
    }

    public void handleReceipt(Receipt receipt, boolean fulfill) {
        Log.d("AmazonIap", "*** handleReceipt - "+receipt.getSku());
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

    public void dispose() {
        //not needed for Amazon
    }

}
