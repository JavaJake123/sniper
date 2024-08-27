package me.siansxint.sniper.checker.http;

import me.siansxint.sniper.checker.config.Configuration;
import me.siansxint.sniper.common.Files;
import me.siansxint.sniper.common.http.HttpClientSelector;
import me.siansxint.sniper.common.Patterns;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.HttpHost;
import team.unnamed.inject.AbstractModule;
import team.unnamed.inject.Module;
import team.unnamed.inject.Provides;
import team.unnamed.inject.Singleton;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class HttpModule extends AbstractModule implements Module {

    private static final Supplier<HttpClientBuilder> BASIC_HTTP_CONFIGURATION = () -> HttpClients
            .custom()
            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:129.0) Gecko/20100101 Firefox/129.0");

    @Provides
    @Singleton
    public HttpClientSelector provideSelector(Logger logger, Configuration configuration) {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(configuration.poolSize());
        connectionManager.setDefaultMaxPerRoute(configuration.poolSize());
        connectionManager.setDefaultConnectionConfig(ConnectionConfig.custom()
                .setConnectTimeout(5, TimeUnit.SECONDS)
                .setSocketTimeout(5, TimeUnit.SECONDS)
                .build());

        Collection<String> proxies = Files.loadTextFile(new File("proxies.txt"));

        List<HttpClient> clients = new ArrayList<>(proxies.size());
        for (String proxy : proxies) {
            String[] parts = Patterns.TWO_DOTS_PATTERN.split(proxy);
            if (parts.length < 2) {
                continue;
            }

            HttpClientBuilder builder = BASIC_HTTP_CONFIGURATION
                    .get()
                    .setConnectionManager(connectionManager)
                    .setProxy(new HttpHost(parts[0], Integer.parseInt(parts[1])));

            if (parts.length > 3) {
                BasicCredentialsProvider provider = new BasicCredentialsProvider();
                provider.setCredentials(new AuthScope(parts[0], Integer.parseInt(parts[1])), new UsernamePasswordCredentials(parts[2], parts[3].toCharArray()));
                builder.setDefaultCredentialsProvider(provider);
            }

            clients.add(builder.build());
        }

        if (clients.isEmpty()) {
            clients.add(BASIC_HTTP_CONFIGURATION.get().setConnectionManager(connectionManager).build());
        }

        logger.info("Loaded " + clients.size() + " proxie(s)!");

        return new HttpClientSelector(clients);
    }
}