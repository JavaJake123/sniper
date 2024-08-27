package me.siansxint.sniper.common.http;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;

import java.util.List;
import java.util.Random;

public class HttpClientSelector {

    private static final Random RANDOM = new Random();
    private static final HttpClient DEFAULT_HTTP_CLIENT = HttpClients.createDefault();

    private final List<HttpClient> clients;

    public HttpClientSelector(List<HttpClient> clients) {
        this.clients = clients;
    }

    public HttpClient next() {
        HttpClient client = clients.size() == 1 ? clients.getFirst() : clients.get(RANDOM.nextInt(clients.size()));
        return client == null ? DEFAULT_HTTP_CLIENT : client;
    }
}