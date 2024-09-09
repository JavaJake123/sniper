package me.siansxint.sniper.claimer.http;

import me.siansxint.sniper.claimer.config.Configuration;
import me.siansxint.sniper.common.Files;
import me.siansxint.sniper.common.Patterns;
import me.siansxint.sniper.common.http.HttpClientSelector;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.ConnectionClosedException;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.util.TimeValue;
import team.unnamed.inject.AbstractModule;
import team.unnamed.inject.Module;
import team.unnamed.inject.Provides;
import team.unnamed.inject.Singleton;

import javax.net.ssl.SSLException;
import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.UncheckedIOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class HttpModule extends AbstractModule implements Module {

    @Provides
    @Singleton
    public HttpClientSelector provideSelector(Logger logger, Configuration configuration) {
        Collection<String> proxies;
        try {
            proxies = Files.loadTextFile(new File("proxies.txt"));
        } catch (IOException e) {
            throw new UncheckedIOException(
                    "Failed to load 'proxies.txt' file!",
                    e
            );
        }

        int size = Math.max(proxies.size(), 1);

        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(size);
        connectionManager.setDefaultMaxPerRoute(size);
        connectionManager.setDefaultConnectionConfig(ConnectionConfig.custom()
                .setConnectTimeout(5, TimeUnit.SECONDS)
                .setSocketTimeout(10, TimeUnit.SECONDS)
                .build());

        Supplier<HttpClientBuilder> basicHttpClientSupplier = () -> HttpClients
                .custom()
                .setRetryStrategy(new DefaultHttpRequestRetryStrategy(
                        configuration.maxRetries(),
                        TimeValue.ofMilliseconds(configuration.retryDelay()),
                        Arrays.asList(
                                InterruptedIOException.class,
                                UnknownHostException.class,
                                ConnectException.class,
                                ConnectionClosedException.class,
                                NoRouteToHostException.class,
                                SSLException.class),
                        Collections.emptyList()
                ) {
                    @Override
                    protected boolean handleAsIdempotent(HttpRequest request) {
                        return true;
                    }
                });

        List<HttpClient> clients = new ArrayList<>(size);
        for (String proxy : proxies) {
            if (proxy.equalsIgnoreCase("localhost")) {
                clients.add(basicHttpClientSupplier.get().setConnectionManager(connectionManager).build());
                continue;
            }

            String[] parts = Patterns.TWO_DOTS_PATTERN.split(proxy);
            if (parts.length < 2) {
                continue;
            }

            HttpHost host = new HttpHost(parts[0], Integer.parseInt(parts[1]));

            HttpClientBuilder builder = basicHttpClientSupplier
                    .get()
                    .setConnectionManager(connectionManager)
                    .setProxy(host);

            if (parts.length > 3) {
                BasicCredentialsProvider provider = new BasicCredentialsProvider();
                provider.setCredentials(new AuthScope(host), new UsernamePasswordCredentials(parts[2], parts[3].toCharArray()));
                builder.setDefaultCredentialsProvider(provider);
            }

            clients.add(builder.build());
        }

        if (clients.isEmpty()) {
            clients.add(basicHttpClientSupplier.get().setConnectionManager(connectionManager).build());
        }

        logger.info("Loaded " + clients.size() + " proxie(s)!");

        return new HttpClientSelector(clients);
    }
}