package tv.emby.iap;


import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import tv.emby.iap.billing.IabHelper;
import tv.emby.iap.billing.IabResult;
import tv.emby.iap.billing.Inventory;
import tv.emby.iap.billing.SkuDetails;

/**
 * Created by Eric on 4/10/2015.
 */
public class IabValidator {

    private IabHelper iabHelper;
    private IResultHandler<ResultType> purchaseCheckHandler;
    private String sku;
    private List<InAppProduct> products;
    private boolean initialized;
    private String message;
    private boolean disposed;
    private Context context;

    public IabValidator(Context context, String key) {
        this.context = context;
        iabHelper = new IabHelper(context, key);
    }

    public boolean isDisposed() { return disposed; }

    public void checkInAppPurchase(final String sku, final IResultHandler<ResultType> resultHandler) {
        if (!initialized) {
            iabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
                @Override
                public void onIabSetupFinished(IabResult result) {
                    initialized = result.isSuccess();
                    if (initialized) {
                        checkPurchaseInternal(sku, resultHandler);
                    } else {
                        message = result.getMessage();
                        resultHandler.onError(ErrorSeverity.Critical, ErrorType.UnableToConnectToStore, message);
                    }

                }
            });
        } else {
            checkPurchaseInternal(sku, resultHandler);
        }

    }

    private void checkPurchaseInternal(String sku, IResultHandler<ResultType> resultHandler) {
        this.sku = sku;
        this.purchaseCheckHandler = resultHandler;

        iabHelper.queryInventoryAsync(mPurchaseCheckListener);

    }

    IabHelper.QueryInventoryFinishedListener mPurchaseCheckListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            if (result.isFailure()) {
                purchaseCheckHandler.onError(ErrorSeverity.Critical, ErrorType.UnableToValidatePurchase, result.getMessage());
            }
            else {
                // call handler
                purchaseCheckHandler.onResult(inventory.hasPurchase(sku) ? ResultType.Success : ResultType.Failure);
            }

        }
    };

    public void validateProductsAsync(final IResultHandler<ResultType> resultHandler) {
        if (!initialized) {
            iabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
                @Override
                public void onIabSetupFinished(IabResult result) {
                    initialized = result.isSuccess();
                    if (initialized) {
                        getProductsInternal(resultHandler);
                    } else {
                        message = result.getMessage();
                        resultHandler.onError(ErrorSeverity.Critical, ErrorType.UnableToConnectToStore, message);
                    }

                }
            });
        } else {
            getProductsInternal(resultHandler);
        }

    }

    private void getProductsInternal(final IResultHandler<ResultType> handler) {
        iabHelper.queryInventoryAsync(true, InAppProduct.getCurrentSkus(context.getPackageName()), new IabHelper.QueryInventoryFinishedListener() {
            @Override
            public void onQueryInventoryFinished(IabResult result, Inventory inv) {
                if (result.isFailure()) {
                    handler.onError(ErrorSeverity.Critical, ErrorType.General, result.getMessage());
                } else {
                    // Build our list of products to return
                    products = new ArrayList<>();
                    for (SkuDetails googleProduct : inv.getAllProducts()) {
                        if (!googleProduct.getTitle().contains("inactive")) products.add(new InAppProduct(googleProduct));
                    }

                    handler.onResult(ResultType.Success);
                }
            }
        });

    }

    public boolean productsInitialized() { return products != null; }

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

    public void dispose() {
        if (iabHelper != null) iabHelper.dispose();
        disposed = true;
    }

}
