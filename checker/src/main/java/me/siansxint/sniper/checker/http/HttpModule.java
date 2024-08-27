package me.siansxint.sniper.checker.http;

import me.siansxint.sniper.common.Files;
import me.siansxint.sniper.common.http.HttpClientSelector;
import me.siansxint.sniper.common.Patterns;
import team.unnamed.inject.AbstractModule;
import team.unnamed.inject.Module;
import team.unnamed.inject.Provides;
import team.unnamed.inject.Singleton;

import java.io.File;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class HttpModule extends AbstractModule implements Module {

    private static final Supplier<HttpClient.Builder> BASIC_HTTP_CONFIGURATION = () -> HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5));

    @Provides
    @Singleton
    public HttpClientSelector provideSelector(Logger logger) {
        Collection<String> proxies = Files.loadTextFile(new File("proxies.txt"));

        List<HttpClient> clients = new ArrayList<>(proxies.size());
        for (String proxy : proxies) {
            String[] parts = Patterns.TWO_DOTS_PATTERN.split(proxy);
            if (parts.length < 2) {
                continue;
            }

            HttpClient.Builder builder = BASIC_HTTP_CONFIGURATION
                    .get()
                    .proxy(ProxySelector.of(new InetSocketAddress(parts[0], Integer.parseInt(parts[1]))));

            if (parts.length > 3) {
                builder.authenticator(new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(parts[2], parts[3].toCharArray());
                    }
                });
            }

            clients.add(builder.build());
        }

        if (clients.isEmpty()) {
            clients.add(BASIC_HTTP_CONFIGURATION.get().build());
        }

        logger.info("Loaded " + clients.size() + " proxies!");

        return new HttpClientSelector(clients);
    }
}