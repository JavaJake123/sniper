package me.siansxint.sniper.common.http;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;

import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public class HttpClientSelector {

    private static final HttpClient DEFAULT_HTTP_CLIENT = HttpClients.createDefault();

    private final Deque<HttpClient> clients = new ConcurrentLinkedDeque<>();

    public HttpClientSelector(List<HttpClient> clients) {
        this.clients.addAll(clients);
    }

    public HttpClient next() {
        if (clients.isEmpty()) {
            return DEFAULT_HTTP_CLIENT;
        } else if (clients.size() == 1) {
            return clients.peek();
        } else {
            HttpClient client = clients.poll();

            if (client == null) {
                return DEFAULT_HTTP_CLIENT;
            } else {
                clients.add(client);
            }

            return client;
        }
    }

    public int size() {
        return clients.size();
    }
}