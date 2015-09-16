package tv.emby.iap;

import java.util.List;

/**
 * Created by Eric on 9/16/2015.
 */
public interface IProductsResultHandler {
    public void onResult(List<InAppProduct> products);
    public void onError(ErrorType error);

}
