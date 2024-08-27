package me.siansxint.sniper.checker;

import me.siansxint.sniper.common.http.HttpClientSelector;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicHeader;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Requests {

    private static final Header[] DEFAULT_HEADERS = new Header[]{
            new BasicHeader("Accept", "application/json"),
            new BasicHeader("Content-Type", "application/json"),
    };

    public static HttpResponse post(HttpEntity body, HttpClientSelector selector, URI uri, Logger logger) {
        HttpPost request = new HttpPost(uri);

        request.setHeaders(DEFAULT_HEADERS);
        request.setEntity(body);

        try {
            return selector.next().execute(request, data -> new HttpResponse(data.getCode(), EntityUtils.toString(data.getEntity())));
        } catch (IOException e) {
            logger.log(
                    Level.WARNING,
                    "An exception occurred while executing HTTP request...",
                    e
            );

            return null;
        }
    }

    public record HttpResponse(int status, String body) { }
}