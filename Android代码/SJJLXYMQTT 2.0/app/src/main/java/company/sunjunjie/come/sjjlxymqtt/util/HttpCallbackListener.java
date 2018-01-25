package company.sunjunjie.come.sjjlxymqtt.util;

/**
 * Created by sunjunjie on 2018/1/12.
 */

public interface HttpCallbackListener {
    void onFinish(String response);
    void onError(Exception e);
}
