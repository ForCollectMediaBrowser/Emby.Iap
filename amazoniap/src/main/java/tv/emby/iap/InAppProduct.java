package tv.emby.iap;

import android.util.Log;

import com.amazon.device.iap.model.Product;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Eric on 9/16/2015.
 */
public class InAppProduct {
    private String sku;
    private String embyFeatureCode;
    private ProductType productType;
    private String title;
    private String description;
    private String price;
    private SubscriptionPeriod period;
    private static String[] MonthlySubscriptionSkus = new String[] {"emby.supporter.monthly"};
    private static String[] LifetimeSubscriptionSkus = new String[] {"emby.supporter.lifetime"};
    private static String[] UnlockSkus = new String[] {"com.mb.android.unlock"};

    public static String getCurrentMonthlySku() { return MonthlySubscriptionSkus[0]; }
    public static String getCurrentLifetimeSku() { return LifetimeSubscriptionSkus[0]; }
    public static String getCurrentUnlockSku() { return UnlockSkus[0]; }

    public static Set<String> getCurrentSkus() {
        HashSet<String> skus = new HashSet<>();
        skus.add(getCurrentUnlockSku());
        skus.add(getCurrentMonthlySku());
        skus.add(getCurrentLifetimeSku());

        return skus;
    }

    public boolean requiresEmail() {
        return embyFeatureCode != null;
    }

    public InAppProduct(Product amazonProduct) {
        sku = amazonProduct.getSku();
        if (Arrays.asList(MonthlySubscriptionSkus).contains(sku)) {
            embyFeatureCode = "MBSClubMonthly";
            period = SubscriptionPeriod.Monthly;
        } else if (Arrays.asList(LifetimeSubscriptionSkus).contains(sku)) {
            embyFeatureCode = "MBSupporter";
        }
        Log.d("InAppProduct", "ProductType: "+ amazonProduct.getProductType());
        productType = amazonProduct.getProductType().equals("subs") ? ProductType.Subscription : ProductType.Product;
        title = amazonProduct.getTitle();
        description = amazonProduct.getDescription();
        price = amazonProduct.getPrice();
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getEmbyFeatureCode() {
        return embyFeatureCode;
    }

    public void setEmbyFeatureCode(String embyFeatureCode) {
        this.embyFeatureCode = embyFeatureCode;
    }

    public ProductType getProductType() {
        return productType;
    }

    public void setProductType(ProductType productType) {
        this.productType = productType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public SubscriptionPeriod getPeriod() {
        return period;
    }

    public void setPeriod(SubscriptionPeriod period) {
        this.period = period;
    }
}
