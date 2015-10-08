package tv.emby.iap.billing;

import android.util.Log;

import com.amazon.device.iap.PurchasingService;
import com.amazon.device.iap.model.ProductDataResponse;
import com.amazon.device.iap.model.PurchaseResponse;
import com.amazon.device.iap.model.PurchaseUpdatesResponse;
import com.amazon.device.iap.model.Receipt;
import com.amazon.device.iap.model.UserDataResponse;

import org.json.JSONException;

import tv.emby.iap.ErrorType;
import tv.emby.iap.IabValidator;
import tv.emby.iap.ResultType;

/**
 * Implementation of {@link com.amazon.device.iap.PurchasingListener} that listens to Amazon
 * InAppPurchase SDK's events, and call {@link tv.emby.iap.IabValidator} to handle the
 * purchase business logic.
 */
public class PurchasingListener implements com.amazon.device.iap.PurchasingListener {

    private final IabValidator iapManager;

    public PurchasingListener(final IabValidator iapManager) {
        this.iapManager = iapManager;
    }

    /**
     * This is the callback for {@link com.amazon.device.iap.PurchasingService#getUserData}. For
     * successful case, get the current user from {@link com.amazon.device.iap.model.UserDataResponse} and
     * call {@link tv.emby.iap.IabValidator#setAmazonUserId} method to load the Amazon
     * user and related purchase information
     * 
     * @param response
     */
    @Override
    public void onUserDataResponse(final UserDataResponse response) {

        final UserDataResponse.RequestStatus status = response.getRequestStatus();
        switch (status) {
        case SUCCESSFUL:
            iapManager.setAmazonUserId(response.getUserData().getUserId(), response.getUserData().getMarketplace());
            break;

        case FAILED:
        case NOT_SUPPORTED:
            iapManager.setAmazonUserId(null, null);
            break;
        }
    }

    /**
     * This is the callback for {@link com.amazon.device.iap.PurchasingService#getProductData}.
     *
     */
    @Override
    public void onProductDataResponse(final ProductDataResponse response) {
        iapManager.handleProductResponse(response);
    }

    /**
     * This is the callback for {@link com.amazon.device.iap.PurchasingService#getPurchaseUpdates}.
     * 
     * We will receive Consumable receipts from this callback if the consumable
     * receipts are not marked as "FULFILLED" in Amazon Appstore. So for every
     * single Consumable receipts in the response, we need to call
     * {@link tv.emby.iap.IabValidator#handleReceipt} to fulfill the purchase.
     * 
     */
    @Override
    public void onPurchaseUpdatesResponse(final PurchaseUpdatesResponse response) {
        try {
            Log.d("AmazonIap", "*** purchaseUpdatesResponse - " + response.toJSON());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final PurchaseUpdatesResponse.RequestStatus status = response.getRequestStatus();
        switch (status) {
        case SUCCESSFUL:
            iapManager.setAmazonUserId(response.getUserData().getUserId(), response.getUserData().getMarketplace());
            if (response.getReceipts().size() == 0) {
                iapManager.productQueryFailed(ErrorType.InvalidProduct);
            } else {
                for (final Receipt receipt : response.getReceipts()) {
                    iapManager.handleReceipt(receipt, false);
                }
                if (response.hasMore()) {
                    PurchasingService.getPurchaseUpdates(true);
                }
            }
            break;
        case FAILED:
        case NOT_SUPPORTED:
            iapManager.productQueryFailed(ErrorType.General);
            break;
        }

    }

    /**
     * This is the callback for {@link com.amazon.device.iap.PurchasingService#purchase}. For each
     * time the application sends a purchase request
     * {@link com.amazon.device.iap.PurchasingService#purchase}, Amazon Appstore will call this
     * callback when the purchase request is completed. If the RequestStatus is
     * Successful or AlreadyPurchased then application needs to call
     * {@link tv.emby.iap.IabValidator#handleReceipt} to handle the purchase
     * fulfillment. If the RequestStatus is INVALID_SKU, NOT_SUPPORTED, or
     * FAILED, notify corresponding method of {@link tv.emby.iap.IabValidator} .
     */
    @Override
    public void onPurchaseResponse(final PurchaseResponse response) {
        final PurchaseResponse.RequestStatus status = response.getRequestStatus();

        switch (status) {
            case SUCCESSFUL:
            case ALREADY_PURCHASED:
                final Receipt receipt = response.getReceipt();
                iapManager.setAmazonUserId(response.getUserData().getUserId(), response.getUserData().getMarketplace());
                iapManager.handleReceipt(receipt, true);
                break;
            case INVALID_SKU:
                iapManager.purchaseFailed(ErrorType.InvalidProduct);
                break;
            case FAILED:
            case NOT_SUPPORTED:
                iapManager.purchaseFailed(ErrorType.UnableToCompletePurchase);
                break;
        }
        iapManager.purchaseComplete();

    }

}
