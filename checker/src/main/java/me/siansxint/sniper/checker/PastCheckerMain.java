package me.siansxint.sniper.checker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import me.siansxint.sniper.checker.config.Configuration;
import me.siansxint.sniper.checker.model.NameDropTime;
import me.siansxint.sniper.checker.model.UsernamesBulkResponse;
import me.siansxint.sniper.common.*;
import me.siansxint.sniper.common.http.HttpClientSelector;
import me.siansxint.sniper.common.logger.LoggerFormatter;
import me.siansxint.sniper.common.storage.MongoTStorage;
import me.siansxint.sniper.common.storage.TStorage;
import me.siansxint.sniper.common.thread.NamedVirtualThreadFactory;
import net.sintaxis.codec.MongoJacksonCodecProvider;
import org.bson.codecs.configuration.CodecRegistries;

import java.io.*;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class PastCheckerMain {

    private static final AtomicInteger SUCCESSFUL_REQUESTS = new AtomicInteger(0);

    private static final List<String> NAMES = Collections.synchronizedList(new ArrayList<>());

    private static final Map<String, Long> LAST_CHECK_TIMES = Collections.synchronizedMap(new HashMap<>());
    private static final Collection<NameDropTime> DROP_TIMES = Collections.synchronizedList(new LinkedList<>());

    private static final URI USERNAME_BULK_FIND_URI = URI.create("https://api.minecraftservices.com/minecraft/profile/lookup/bulk/byname");

    private static final int CHUNK_SIZE = 10;

    private static final Logger LOGGER = Logger.getLogger(PastCheckerMain.class.getSimpleName());

    public static void main(String[] args) {
        File logs = new File("logs");
        if (!logs.exists() && !logs.mkdirs()) {
            return;
        }

        Handler fileHandler;
        try {
            fileHandler = new FileHandler("logs/" + LOGGER.getName() + ".log", true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Formatter formatter = new LoggerFormatter();

        fileHandler.setFormatter(formatter);

        Handler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(formatter);

        LOGGER.setUseParentHandlers(false);

        LOGGER.addHandler(fileHandler);
        LOGGER.addHandler(consoleHandler);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        Configuration configuration = generateConfig(mapper);

        Collection<String> proxies = Files.loadTextFile(new File("proxies.txt"));
        List<HttpClient> clients = new ArrayList<>(proxies.size());

        for (String proxy : proxies) {
            String[] parts = Patterns.TWO_DOTS_PATTERN.split(proxy);
            if (parts.length < 2) {
                continue;
            }

            HttpClient.Builder builder = HttpClient.newBuilder()
                    .connectTimeout(Duration.of(5, ChronoUnit.SECONDS))
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
            clients.add(HttpClient.newBuilder().connectTimeout(Duration.of(5, ChronoUnit.SECONDS)).build());
        }

        LOGGER.info("Loaded " + clients.size() + " proxies.");

        Collection<String> names = new HashSet<>();
        Files.loadTextFile(new File("names.txt")).forEach(name -> names.add(name.toLowerCase()));

        NAMES.addAll(names);
        if (NAMES.isEmpty()) {
            LOGGER.info("No names to check...");
            return;
        }

        MongoClient client = MongoClients.create(
                MongoClientSettings.builder()
                        .applyConnectionString(new ConnectionString(configuration.mongoUri()))
                        .codecRegistry(CodecRegistries.fromRegistries(
                                MongoClientSettings.getDefaultCodecRegistry(),
                                CodecRegistries.fromProviders(new MongoJacksonCodecProvider(mapper))
                        ))
                        .build()
        );

        TStorage<NameDropTime> dropTimes = new MongoTStorage<>(
                NameDropTime.class,
                "sniper",
                "drop-times",
                client,
                Executors.newCachedThreadPool()
        );

        ExecutorService service = Executors.newFixedThreadPool(configuration.poolSize() + 1, new NamedVirtualThreadFactory(Thread.ofVirtual().factory()));
        service.submit(() -> {
            while (!service.isTerminated()) {
                try {
                    Thread.sleep(configuration.savingInterval());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                if (DROP_TIMES.isEmpty()) {
                    continue;
                }

                Iterator<NameDropTime> iterator = DROP_TIMES.iterator();
                while (iterator.hasNext()) {
                    NameDropTime dropTime = iterator.next();
                    dropTimes.save(dropTime)
                            .whenComplete((unused, throwable) -> {
                                if (throwable != null) {
                                    LOGGER.log(
                                            Level.SEVERE,
                                            "Error saving drop time for " + dropTime.id() + ".",
                                            throwable
                                    );
                                    return;
                                }

                                LOGGER.info("Uploaded new drop-time for " + dropTime.id() + ".");
                            });
                    iterator.remove();
                }
            }
        });

        HttpClientSelector selector = new HttpClientSelector(clients);

        while (!NAMES.isEmpty()) {
            long start = System.nanoTime();
            // we cannot do more than 10 names per request
            Collection<Collection<String>> chunks = chunkNames();
            LOGGER.info("Chunks to process: " + chunks.size());

            CountDownLatch latch = new CountDownLatch(chunks.size());

            for (Collection<String> chunk : chunks) {
                CompletableFuture.supplyAsync(() -> processChunk(selector, chunk, configuration, mapper), service)
                        .whenComplete((available, throwable) -> {
                            if (throwable != null || available.isEmpty()) {
                                return;
                            }

                            Instant now = Instant.now();

                            for (String name : available) {
                                NAMES.remove(name);

                                Long epoch = LAST_CHECK_TIMES.remove(name);

                                DROP_TIMES.add(new NameDropTime(name, Instant.ofEpochMilli(epoch == null ? now.toEpochMilli() : epoch), now));

                                LOGGER.info("\u001B[32mNew name detected as available: " + name + ", at: " + Instant.now().toString() + "\u001B[0m");
                            }

                            latch.countDown();
                        });
            }

            try {
                latch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            LOGGER.info("Process " + chunks.size() + " chunks took " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start) + "ms.");
        }

        service.shutdown();
    }

    private static Configuration generateConfig(ObjectMapper mapper) {
        File config = new File("config.json");
        try {
            return mapper.readValue(config, Configuration.class);
        } catch (IOException e) {
            LOGGER.log(
                    Level.WARNING,
                    "An error occurred while reading configuration file, using default one...",
                    e
            );

            Configuration configuration = new Configuration(
                    3,
                    5000,
                    20000,
                    "mongodb://localhost:27017"
            );
            try (Writer writer = new BufferedWriter(new FileWriter(config))) {
                mapper.writeValue(writer, configuration);
            } catch (IOException ex) {
                LOGGER.log(
                        Level.WARNING,
                        "An error occurred while saving default configuration file, trying again...",
                        e
                );
            }

            return configuration;
        }
    }

    private static Collection<String> processChunk(HttpClientSelector selector,
                                                   Collection<String> chunk,
                                                   Configuration configuration,
                                                   ObjectMapper mapper) {
        while (true) {
            HttpClient client = selector.next();
            HttpRequest request;
            try {
                request = HttpRequest.newBuilder(USERNAME_BULK_FIND_URI)
                        .header("Content-Type", "application/json")
                        .header("User-Agent", "test")
                        .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(chunk)))
                        .build();
            } catch (JsonProcessingException e) {
                LOGGER.log(
                        Level.WARNING,
                        "An error occurred while building request body...",
                        e
                );
                continue;
            }

            HttpResponse<String> response;
            try {
                response = client.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );
            } catch (IOException | InterruptedException e) {
                LOGGER.log(
                        Level.WARNING,
                        "An error occurred while sending name check request...",
                        e
                );
                continue;
            }

            if (response.statusCode() == 200) {
                long now = Instant.now().toEpochMilli();

                List<UsernamesBulkResponse> responses;
                try {
                    responses = mapper.readValue(response.body(), new TypeReference<>() {
                    });
                } catch (JsonProcessingException e) {
                    LOGGER.log(
                            Level.WARNING,
                            "An error occurred while reading request body...",
                            e
                    );
                    continue;
                }

                Collection<String> taken = responses.stream()
                        .map(usernamesBulkResponse -> usernamesBulkResponse.name().toLowerCase())
                        .collect(Collectors.toSet());

                for (String name : taken) {
                    LAST_CHECK_TIMES.put(name, now);
                }

                LOGGER.info("Got a 200 response from name check request. Total count: " + SUCCESSFUL_REQUESTS.incrementAndGet());

                return chunk.stream()
                        .filter(s -> !taken.contains(s))
                        .collect(Collectors.toSet());
            } else if (response.statusCode() == 429) {
                LOGGER.info("Got rate-limited. Waiting " + configuration.rateLimitDelay() + "ms.");
                try {
                    TimeUnit.MILLISECONDS.sleep(configuration.rateLimitDelay());
                } catch (InterruptedException e) {
                    LOGGER.log(
                            Level.WARNING,
                            "An error occurred while preventing being rate-limited...",
                            e
                    );
                }
            }
        }
    }

    private static Collection<Collection<String>> chunkNames() {
        List<String> randomized = new ArrayList<>(NAMES);
        Collections.shuffle(randomized); // cant explain why, but this should get better results

        Collection<Collection<String>> chunks = new ArrayList<>(CHUNK_SIZE);
        for (int i = 0; i < NAMES.size(); i += CHUNK_SIZE) {
            Collection<String> chunk = new ArrayList<>(randomized.subList(i, Math.min(NAMES.size(), i + CHUNK_SIZE)));
            chunks.add(chunk);
        }

        return chunks;
    }
}