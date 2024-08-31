package me.siansxint.sniper.checker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.siansxint.sniper.checker.ChunkProcessorTask;
import me.siansxint.sniper.checker.LastCheckCachedStorage;
import me.siansxint.sniper.checker.config.Configuration;
import me.siansxint.sniper.checker.model.NameDropTime;
import me.siansxint.sniper.common.ConsoleColors;
import me.siansxint.sniper.common.Service;
import me.siansxint.sniper.common.http.HttpClientSelector;
import me.siansxint.sniper.common.registry.TRegistry;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Named;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChunkedNameCheckerService implements Service {

    private static final int CHUNK_SIZE = 10;

    private @Inject HttpClientSelector httpClientSelector;

    private @Inject TRegistry<NameDropTime> dropTimesRegistry;
    private @Inject LastCheckCachedStorage lastChecks;
    private @Inject List<String> names;

    @Inject
    @Named("checker")
    private ExecutorService checkerService;

    private @Inject ObjectMapper mapper;
    private @Inject Configuration configuration;

    private @Inject Logger logger;

    @Override
    public void start() {
        Thread.ofVirtual()
                .name("ChunkedNameCheckerService")
                .start(() -> {
                    while (!names.isEmpty()) {
                        Collection<Collection<String>> chunks = chunkNames();
                        logger.info("Chunks to process: " + chunks.size());

                        CountDownLatch latch = new CountDownLatch(chunks.size());

                        Instant startedAt = Instant.now();
                        long start = System.nanoTime();

                        for (Collection<String> chunk : chunks) {
                            checkerService.submit(new ChunkProcessorTask(
                                    chunk,
                                    latch,
                                    httpClientSelector.next(),
                                    mapper,
                                    logger,
                                    dropTimesRegistry,
                                    lastChecks,
                                    configuration,
                                    startedAt
                            ));
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

                        logger.info(ConsoleColors.resetting(ConsoleColors.PURPLE, "Finished processing " + chunks.size() + " chunks! Took " + TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - start) + " seconds."));
                        ChunkProcessorTask.SUCCESSFUL_REQUESTS.set(0);
                    }
                });
    }

    private Collection<Collection<String>> chunkNames() {
        List<String> randomized = new ArrayList<>(names);

        Collection<Collection<String>> chunks = new ArrayList<>(CHUNK_SIZE);
        for (int i = 0; i < randomized.size(); i += CHUNK_SIZE) {
            chunks.add(new ArrayList<>(randomized.subList(i, Math.min(randomized.size(), i + CHUNK_SIZE))));
        }

        return chunks;
    }
}