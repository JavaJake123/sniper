package me.siansxint.sniper.common.http;

import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;

public interface HttpResponseHandlers {

    HttpClientResponseHandler<HttpResponse> RESPONSE_HANDLER = data -> {
        HttpEntity entity = data.getEntity();
        return new HttpResponse(
                data.getCode(),
                entity == null ? "{}" : EntityUtils.toString(entity)
        );
    };
}
