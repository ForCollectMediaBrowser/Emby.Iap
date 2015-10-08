package tv.emby.iap;

/**
 * Created by Eric on 10/8/2015.
 */
public class PurchaseResult {
    private ResultType resultCode;
    private String store = "Google";
    private String storeToken;
    private String storeId;

    public ResultType getResultCode() {
        return resultCode;
    }

    public void setResultCode(ResultType resultCode) {
        this.resultCode = resultCode;
    }

    public String getStore() {
        return store;
    }

    public void setStore(String store) {
        this.store = store;
    }

    public String getStoreToken() {
        return storeToken;
    }

    public void setStoreToken(String storeToken) {
        this.storeToken = storeToken;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }
}
