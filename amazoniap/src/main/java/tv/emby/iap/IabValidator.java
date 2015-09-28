package tv.emby.iap;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.amazon.device.iap.PurchasingService;
import com.amazon.device.iap.model.FulfillmentResult;
import com.amazon.device.iap.model.Product;
import com.amazon.device.iap.model.ProductDataResponse;
import com.amazon.device.iap.model.Receipt;

import org.json.JSONException;
import org.json.JSONObject;

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
    private String productJson;
    private String sku;
    private String receiptId;
    private IResultHandler<ResultType> purchaseHandler;
    private IResultHandler<List<InAppProduct>> productHandler;
    private boolean disposed;

    private Activity purchaseActivity;

    public IabValidator(Context context, String key) {
        //key is not used for Amazon
        PurchasingService.registerListener(context, new PurchasingListener(this));
        PurchasingService.getUserData();
    }

    public void setAmazonUserId(String id, String marketplace) {
        amazonUserId = id;
        amazonMarketplace = marketplace;
    }

    public String getReceiptId() { return receiptId; }
    public String getAmazonUserId() { return amazonUserId; }
    public boolean isDisposed() { return disposed; }


    public void purchase(Activity activity, String productJson, IResultHandler<ResultType> handler) {
        purchaseActivity = activity;
        purchaseHandler = handler;
        this.productJson = productJson;
        try {
            JSONObject product = new JSONObject(productJson);
            sku = product.getString("sku");
        } catch (JSONException e) {
            e.printStackTrace();
            activity.finish();
            return;
        }

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
        Log.d("AmazonIap", "*** handleProductResponse - " + response.getRequestStatus());
        if (productHandler == null) return;

        final ProductDataResponse.RequestStatus status = response.getRequestStatus();

        switch (status) {
            case SUCCESSFUL:
                List<InAppProduct> products = new ArrayList<>();
                final Map<String,Product> amazonProducts = response.getProductData();
                for (String key : amazonProducts.keySet()) {
                    Product product = amazonProducts.get(key);
                    if (!product.getTitle().contains("inactive")) products.add(new InAppProduct(product));
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
        Log.d("AmazonIap", "*** checkInAppPurchase - " + sku);
        PurchasingService.getPurchaseUpdates(true);
    }

    public void handleReceipt(Receipt receipt, boolean fulfill) {
        Log.d("AmazonIap", "*** handleReceipt - "+receipt.getSku());
        if (receipt.isCanceled()) {
            purchaseHandler.onResult(ResultType.Canceled);
        } else {
            if (receipt.getSku().equals(sku)) {
                if (fulfill) {
                    PurchasingService.notifyFulfillment(receipt.getReceiptId(), FulfillmentResult.FULFILLED);
                }
                this.receiptId = receipt.getReceiptId();
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
