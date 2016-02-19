package tv.emby.iap;

import android.util.Log;

import com.amazon.device.iap.model.Product;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
    private static HashMap<String,String> MonthlySubscriptionSkus = new HashMap<>();
    static {
        MonthlySubscriptionSkus.put("com.mb.android","emby.supporter.monthly");
        MonthlySubscriptionSkus.put("tv.emby.embyatv","emby.premiere.atv.monthly");
        MonthlySubscriptionSkus.put("com.emby.mobile","emby.premiere.monthly");
    }
    private static HashMap<String,String> ParentSubscriptionSkus = new HashMap<>();
    static {
        MonthlySubscriptionSkus.put("com.mb.android","emby.supporter");
        MonthlySubscriptionSkus.put("tv.emby.embyatv","emby.premiere.atv");
        MonthlySubscriptionSkus.put("com.emby.mobile","emby.premiere");
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
        LifetimeSubscriptionSkus.put("com.emby.mobile","emby.premiere.lifetime");
    }
    private static HashMap<String,String> UnlockSkus = new HashMap<>();
    static {
        UnlockSkus.put("com.mb.android","com.mb.android.unlock");
        UnlockSkus.put("tv.emby.embyatv","tv.emby.embyatv.unlock");
        UnlockSkus.put("com.emby.mobile","com.emby.mobile.unlock");
    }

    public static String getCurrentMonthlySku(String app) { return MonthlySubscriptionSkus.get(app); }
    public static String getCurrentSubscriptionSku(String app) { return ParentSubscriptionSkus.get(app); }
    public static String getCurrentWeeklySku(String app) { return WeeklySubscriptionSkus.get(app); }
    public static String getCurrentLifetimeSku(String app) { return LifetimeSubscriptionSkus.get(app); }
    public static String getCurrentUnlockSku(String app) { return UnlockSkus.get(app); }

    public static Set<String> getCurrentSkus(String app) {
        HashSet<String> skus = new HashSet<>();
        skus.add(getCurrentUnlockSku(app));
        skus.add(getCurrentMonthlySku(app));
        skus.add(getCurrentLifetimeSku(app));

        return skus;
    }

    public boolean requiresEmail() {
        return embyFeatureCode != null;
    }

    public static boolean isSubscription(String sku) {
        return WeeklySubscriptionSkus.values().contains(sku) || MonthlySubscriptionSkus.values().contains(sku);
    }

    public InAppProduct(Product amazonProduct, ILogger logger) {
        sku = amazonProduct.getSku();
        if (MonthlySubscriptionSkus.values().contains(sku)) {
            embyFeatureCode = "MBSClubMonthly";
            period = SubscriptionPeriod.Monthly;
        } else if (WeeklySubscriptionSkus.values().contains(sku)) {
            embyFeatureCode = "MBSClubWeekly";
            period = SubscriptionPeriod.Weekly;
        } else if (LifetimeSubscriptionSkus.values().contains(sku)) {
            embyFeatureCode = "MBSupporter";
        }
        logger.d("InAppProduct", "ProductType: "+ amazonProduct.getProductType());
        productType = amazonProduct.getProductType().equals(com.amazon.device.iap.model.ProductType.SUBSCRIPTION) ? ProductType.Subscription : ProductType.Product;
        title = amazonProduct.getTitle();
        description = amazonProduct.getDescription();
        price = amazonProduct.getPrice();
    }

    public String getSku() {
        return sku;
    }

    public String getEmbyFeatureCode() {
        return embyFeatureCode;
    }

    public ProductType getProductType() {
        return productType;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getPrice() {
        return price;
    }

    public SubscriptionPeriod getPeriod() {
        return period;
    }

}
