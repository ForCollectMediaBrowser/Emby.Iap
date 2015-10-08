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
import java.util.List;
import java.util.Map;

import tv.emby.iap.billing.PurchasingListener;

/**
 * Created by Eric on 4/10/2015.
 */
public class IabValidator {

    public static String SKU_UNLOCK = "tv.emby.embyatv.unlock";

    private String amazonUserId;
    private String amazonMarketplace;
    private String sku;
    private String receiptId;
    private List<InAppProduct> products;
    private IResultHandler<PurchaseResult> purchaseHandler;
    private IResultHandler<ResultType> productHandler;
    private boolean disposed;
    private Context context;

    private Activity purchaseActivity;

    public IabValidator(Context context, String key) {
        //key is not used for Amazon
        this.context = context;
        PurchasingService.registerListener(context, new PurchasingListener(this));
        PurchasingService.getProductData(InAppProduct.getCurrentSkus(context.getPackageName()));
        PurchasingService.getUserData();
    }

    public void setAmazonUserId(String id, String marketplace) {
        amazonUserId = id;
        amazonMarketplace = marketplace;
    }

    public String getReceiptId() { return receiptId; }
    public String getAmazonUserId() { return amazonUserId; }
    public boolean isDisposed() { return disposed; }


    public void purchase(Activity activity, String sku, IResultHandler<PurchaseResult> handler) {
        purchaseActivity = activity;
        purchaseHandler = handler;
        this.sku = sku;
        PurchasingService.purchase(sku);
    }

    public void purchaseComplete() {
        purchaseActivity.finish();
    }

    public void purchaseFailed(ErrorType type) {
        if (purchaseHandler != null) purchaseHandler.onError(ErrorSeverity.Critical, type, "");
    }

    public void productQueryFailed(ErrorType type) {
        if (productHandler != null) productHandler.onError(ErrorSeverity.Critical, type, "");
    }

    public void validateProductsAsync(IResultHandler<ResultType> handler) {
        Log.d("AmazonIap", "*** validateProductsAsync");
        if (productsInitialized()) {
            handler.onResult(ResultType.Success);
        } else {
            productHandler = handler;
            PurchasingService.getProductData(InAppProduct.getCurrentSkus(context.getPackageName()));
        }
    }

    public boolean productsInitialized() { return products != null; }

    public void handleProductResponse(ProductDataResponse response) {
        Log.d("AmazonIap", "*** handleProductResponse - " + response.getRequestStatus());

        final ProductDataResponse.RequestStatus status = response.getRequestStatus();

        switch (status) {
            case SUCCESSFUL:
                 products = new ArrayList<>();
                final Map<String,Product> amazonProducts = response.getProductData();
                for (String key : amazonProducts.keySet()) {
                    Product product = amazonProducts.get(key);
                    if (!product.getTitle().contains("inactive")) products.add(new InAppProduct(product));
                }

                if (productHandler != null) productHandler.onResult(ResultType.Success);
                break;
            case FAILED:
            case NOT_SUPPORTED:
                if (productHandler != null) productHandler.onError(ErrorSeverity.Critical, ErrorType.UnableToConnectToStore, response.toString());
                break;
        }

    }

    public InAppProduct getPremiereMonthly() {
        if (!productsInitialized()) return null;

        for (InAppProduct product : products) {
            if (product.getSku().equals(InAppProduct.getCurrentMonthlySku(context.getPackageName()))) return product;
        }

        return null;
    }

    public InAppProduct getPremiereWeekly() {
        if (!productsInitialized()) return null;

        for (InAppProduct product : products) {
            if (product.getSku().equals(InAppProduct.getCurrentWeeklySku(context.getPackageName()))) return product;
        }

        return null;
    }

    public InAppProduct getPremiereLifetime() {
        if (!productsInitialized()) return null;

        for (InAppProduct product : products) {
            if (product.getSku().equals(InAppProduct.getCurrentLifetimeSku(context.getPackageName()))) return product;
        }

        return null;
    }

    public InAppProduct getUnlockProduct() {
        if (!productsInitialized()) return null;

        for (InAppProduct product : products) {
            if (product.getSku().equals(InAppProduct.getCurrentUnlockSku(context.getPackageName()))) return product;
        }

        return null;
    }

    public void checkInAppPurchase(String sku, IResultHandler<PurchaseResult> resultHandler) {
        this.sku = sku;
        this.purchaseHandler = resultHandler;
        Log.d("AmazonIap", "*** checkInAppPurchase - " + sku);
        PurchasingService.getPurchaseUpdates(true);
    }

    public void handleReceipt(Receipt receipt, boolean fulfill) {
        Log.d("AmazonIap", "*** handleReceipt - "+receipt.getSku());
        PurchaseResult result = new PurchaseResult();
        if (receipt.isCanceled()) {
            result.setResultCode(ResultType.Canceled);
            purchaseHandler.onResult(result);
        } else {
            if (receipt.getSku().equals(sku)) {
                if (fulfill) {
                    PurchasingService.notifyFulfillment(receipt.getReceiptId(), FulfillmentResult.FULFILLED);
                }
                result.setResultCode(ResultType.Success);
                result.setStoreToken(receipt.getReceiptId());
                result.setStoreId(this.amazonUserId);
                purchaseHandler.onResult(result);
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
