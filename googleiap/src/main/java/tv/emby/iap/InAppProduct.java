package tv.emby.iap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import tv.emby.iap.billing.SkuDetails;

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
    private static HashMap<String,String> MonthlySubscriptionSkus = new HashMap<>();
    static {
        MonthlySubscriptionSkus.put("com.mb.android","emby.supporter.monthly");
        MonthlySubscriptionSkus.put("tv.emby.embyatv","emby.supporter.atv.monthly");
    }
    private static HashMap<String,String> WeeklySubscriptionSkus = new HashMap<>();
    static {
        WeeklySubscriptionSkus.put("com.mb.android","emby.supporter.weekly");
        WeeklySubscriptionSkus.put("tv.emby.embyatv","emby.supporter.atv.weekly");
    }
    private static HashMap<String,String> LifetimeSubscriptionSkus = new HashMap<>();
    static {
        LifetimeSubscriptionSkus.put("com.mb.android","emby.supporter.lifetime");
        LifetimeSubscriptionSkus.put("tv.emby.embyatv","emby.supporter.atv.lifetime");
    }
    private static HashMap<String,String> UnlockSkus = new HashMap<>();
    static {
        UnlockSkus.put("com.mb.android","com.mb.android.unlock");
        UnlockSkus.put("tv.emby.embyatv","tv.emby.embyatv.unlock");
    }

    public static String getCurrentMonthlySku(String app) { return MonthlySubscriptionSkus.get(app); }
    public static String getCurrentWeeklySku(String app) { return WeeklySubscriptionSkus.get(app); }
    public static String getCurrentLifetimeSku(String app) { return LifetimeSubscriptionSkus.get(app); }
    public static String getCurrentUnlockSku(String app) { return UnlockSkus.get(app); }

    public static List<String> getCurrentSkus(String app) {
        List<String> skus = new ArrayList<>();
        skus.add(getCurrentUnlockSku(app));
        skus.add(getCurrentWeeklySku(app));
        skus.add(getCurrentMonthlySku(app));
        skus.add(getCurrentLifetimeSku(app));

        return skus;
    }

    public boolean requiresEmail() {
        return embyFeatureCode != null;
    }

    public InAppProduct(SkuDetails googleProduct) {
        sku = googleProduct.getSku();
        if (MonthlySubscriptionSkus.containsValue(sku)) {
            embyFeatureCode = "MBSClubMonthly";
            period = SubscriptionPeriod.Monthly;
        } else if (WeeklySubscriptionSkus.containsValue(sku)) {
            embyFeatureCode = "MBSClubWeekly";
            period = SubscriptionPeriod.Weekly;
        }  else if (LifetimeSubscriptionSkus.containsValue(sku)) {
            embyFeatureCode = "MBSupporter";
        }
        productType = googleProduct.getType().equals("subs") ? ProductType.Subscription : ProductType.Product;
        title = googleProduct.getTitle();
        description = googleProduct.getDescription();
        price = googleProduct.getPrice();
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
