package me.siansxint.sniper.common.http;

import java.net.http.HttpClient;
import java.util.List;
import java.util.Random;

public class HttpClientSelector {

    private static final Random RANDOM = new Random();
    private static final HttpClient DEFAULT_CLIENT = HttpClient.newHttpClient();

    private final List<HttpClient> clients;

    public HttpClientSelector(List<HttpClient> clients) {
        this.clients = clients;
    }

    public synchronized HttpClient next() {
        HttpClient client = clients.size() == 1 ? clients.getFirst() : clients.get(RANDOM.nextInt(clients.size()));
        if (client == null) {
            return DEFAULT_CLIENT;
        }

        return client;
    }
}