package me.siansxint.sniper.checker.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.siansxint.sniper.checker.Requests;
import me.siansxint.sniper.checker.TCachedStorage;
import me.siansxint.sniper.checker.config.Configuration;
import me.siansxint.sniper.checker.model.LastCheck;
import me.siansxint.sniper.checker.model.NameDropTime;
import me.siansxint.sniper.checker.model.UsernamesBulkResponse;
import me.siansxint.sniper.common.ConsoleColors;
import me.siansxint.sniper.common.Service;
import me.siansxint.sniper.common.http.HttpClientSelector;
import me.siansxint.sniper.common.registry.TRegistry;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Named;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ChunkedNameCheckerService implements Service {

    private static final URI USERNAME_BULK_FIND_URI = URI.create("https://api.minecraftservices.com/minecraft/profile/lookup/bulk/byname");
    private static final int CHUNK_SIZE = 10;
    private static final AtomicInteger SUCCESSFUL_REQUESTS = new AtomicInteger(0);

    private @Inject HttpClientSelector httpClientSelector;

    private @Inject TRegistry<NameDropTime> dropTimes;
    private @Inject TRegistry<LastCheck> lastChecks;
    private @Inject TCachedStorage<LastCheck> lastChecksStorage;
    private @Inject List<String> names;

    private @Inject
    @Named("checker") ExecutorService checkerService;

    private @Inject Configuration configuration;
    private @Inject ObjectMapper mapper;

    private @Inject Logger logger;

    private Thread thread;

    @Override
    public void start() {
        this.thread = Thread.ofVirtual()
                .name("Chunked Name Checker Service")
                .start(
                        () -> {
                            while (!checkerService.isTerminated()) {
                                Collection<Collection<String>> chunks = chunkNames();
                                logger.info("Chunks to process: " + chunks.size());

                                CountDownLatch latch = new CountDownLatch(chunks.size());

                                for (Collection<String> chunk : chunks) {
                                    CompletableFuture.supplyAsync(
                                                    () -> processChunk(
                                                            httpClientSelector,
                                                            chunk,
                                                            configuration,
                                                            mapper
                                                    ), checkerService
                                            )
                                            .whenComplete((available, throwable) -> {
                                                latch.countDown();

                                                if (throwable != null || available.isEmpty()) {
                                                    return;
                                                }

                                                Instant now = Instant.now();

                                                for (String name : available) {
                                                    LastCheck check = lastChecksStorage.remove(name);
                                                    dropTimes.register(new NameDropTime(
                                                            name,
                                                            Instant.ofEpochMilli(
                                                                    check == null
                                                                            ?
                                                                            now.toEpochMilli()
                                                                            :
                                                                            check.when().toEpochMilli()
                                                            ),
                                                            now
                                                    ));

                                                    logger.info(ConsoleColors.resetting(
                                                            ConsoleColors.GREEN,
                                                            "New name detected as available: " + name + ", at: " + now.toString())
                                                    );
                                                }
                                            });
                                }

                                try {
                                    latch.await();
                                } catch (InterruptedException e) {
                                    logger.log(
                                            Level.WARNING,
                                            "An error occurred while waiting for chunks processing...",
                                            e
                                    );
                                }

                                logger.info(ConsoleColors.resetting(ConsoleColors.PURPLE, "Finished processing " + chunks.size() + " chunks!"));
                                SUCCESSFUL_REQUESTS.set(0);
                            }
                        }
                );
    }

    @Override
    public void stop() {
        if (thread != null) {
            thread.interrupt();
        }
    }

    private Collection<String> processChunk(HttpClientSelector selector,
                                            Collection<String> chunk,
                                            Configuration configuration,
                                            ObjectMapper mapper) {
        while (true) {
            Requests.HttpResponse response;
            try {
                response = Requests.post(
                        new StringEntity(mapper.writeValueAsString(chunk), ContentType.APPLICATION_JSON),
                        selector,
                        USERNAME_BULK_FIND_URI,
                        logger
                );
            } catch (JsonProcessingException e) {
                logger.log(
                        Level.WARNING,
                        "An error occurred while serializing chunk data...",
                        e
                );
                continue;
            }

            if (response == null) {
                logger.warning("Got no HTTP response...");
                continue;
            }

            if (response.status() == 200) {
                Instant now = Instant.now();

                List<UsernamesBulkResponse> responses;
                try {
                    responses = mapper.readValue(response.body(), new TypeReference<>() {
                    });
                } catch (JsonProcessingException e) {
                    logger.log(
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
                    lastChecks.register(new LastCheck(name, now));
                }

                logger.info("Got a 200 response from name check request. Total count: " + SUCCESSFUL_REQUESTS.incrementAndGet());

                return chunk.stream()
                        .filter(s -> !taken.contains(s))
                        .collect(Collectors.toSet());
            } else if (response.status() == 429) {
                logger.info("Got rate-limited. Waiting " + configuration.rateLimitDelay() + "ms.");
                try {
                    TimeUnit.MILLISECONDS.sleep(configuration.rateLimitDelay());
                } catch (InterruptedException e) {
                    logger.log(
                            Level.WARNING,
                            "An error occurred while preventing being rate-limited...",
                            e
                    );
                }
            }
        }
    }

    private Collection<Collection<String>> chunkNames() {
        List<String> randomized = new ArrayList<>(names);
        Collections.shuffle(randomized); // cant explain why, but this should get better results

        Collection<Collection<String>> chunks = new ArrayList<>(CHUNK_SIZE);
        for (int i = 0; i < randomized.size(); i += CHUNK_SIZE) {
            Collection<String> chunk = new ArrayList<>(randomized.subList(i, Math.min(randomized.size(), i + CHUNK_SIZE)));
            chunks.add(chunk);
        }

        return chunks;
    }
}