package me.siansxint.sniper.checker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.siansxint.sniper.checker.config.Configuration;
import me.siansxint.sniper.checker.model.LastCheck;
import me.siansxint.sniper.common.NameDropTime;
import me.siansxint.sniper.checker.model.UsernamesBulkResponse;
import me.siansxint.sniper.common.ConsoleColors;
import me.siansxint.sniper.common.HttpResponse;
import me.siansxint.sniper.common.registry.TRegistry;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicHeader;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@SuppressWarnings("BusyWait")
public class ChunkProcessorTask implements Runnable {

    private static final URI USERNAME_BULK_FIND_URI = URI.create("https://api.mojang.com/profiles/minecraft");
    private static final Header[] DEFAULT_HEADERS = new Header[]{
            new BasicHeader("Accept", ContentType.APPLICATION_JSON.toString()),
            new BasicHeader("Content-Type", ContentType.APPLICATION_JSON.toString()),
            new BasicHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:129.0) Gecko/20100101 Firefox/129.0"),
    };

    public static final AtomicInteger SUCCESSFUL_REQUESTS = new AtomicInteger(0);

    private final Collection<String> chunk;
    private final CountDownLatch latch;

    private final HttpClient client;
    private final ObjectMapper mapper;

    private final Logger logger;

    private final TRegistry<NameDropTime> dropTimes;
    private final LastCheckCachedStorage lastChecks;

    private final Configuration configuration;

    private final Instant startedAt;

    public ChunkProcessorTask(
            Collection<String> chunk,
            CountDownLatch latch,
            HttpClient client,
            ObjectMapper mapper,
            Logger logger,
            TRegistry<NameDropTime> dropTimes,
            LastCheckCachedStorage lastChecks,
            Configuration configuration,
            Instant startedAt
    ) {
        this.chunk = chunk;
        this.latch = latch;
        this.client = client;
        this.mapper = mapper;
        this.logger = logger;
        this.dropTimes = dropTimes;
        this.lastChecks = lastChecks;
        this.configuration = configuration;
        this.startedAt = startedAt;
    }

    @Override
    public void run() {
        HttpPost request = new HttpPost(USERNAME_BULK_FIND_URI);

        request.setHeaders(DEFAULT_HEADERS);
        try {
            request.setEntity(new StringEntity(mapper.writeValueAsString(chunk), ContentType.APPLICATION_JSON));
        } catch (JsonProcessingException e) {
            logger.log(
                    Level.WARNING,
                    "An error occurred while serializing chunk data...",
                    e
            );
        }

        while (true) {
            try {
                Thread.sleep(1000); // we have 600 requests each 10 minutes

                HttpResponse response = client.execute(
                        request,
                        data -> {
                            HttpEntity entity = data.getEntity();
                            return new HttpResponse(
                                    data.getCode(),
                                    entity == null ? "{}" : EntityUtils.toString(entity)
                            );
                        }
                );

                if (response == null || response.result() == null) {
                    logger.warning("Got an unexpected response, trying again...");
                    continue;
                }

                if (response.status() == HttpStatus.SC_FORBIDDEN) {
                    logger.warning("Seems like the proxy of this HTTP client got forbidden...");
                    continue;
                }

                if (response.status() == HttpStatus.SC_TOO_MANY_REQUESTS) {
                    logger.warning("Got rate-limited, waiting " + configuration.rateLimitedDelay() + "ms!");
                    Thread.sleep(configuration.rateLimitedDelay());
                    continue;
                }

                Instant now = Instant.now();

                List<UsernamesBulkResponse> responses = mapper.readValue(response.result(), new TypeReference<>() {
                });

                Collection<String> taken = responses.stream()
                        .map(usernamesBulkResponse -> usernamesBulkResponse.name().toLowerCase())
                        .collect(Collectors.toSet());

                for (String name : taken) {
                    lastChecks.register(new LastCheck(
                            name,
                            now.toEpochMilli()
                    ));
                }

                chunk.stream()
                        .filter(s -> !taken.contains(s))
                        .forEach(name -> {
                            LastCheck lastCheck = lastChecks.remove(name);
                            dropTimes.register(new NameDropTime(
                                    name,
                                    lastCheck == null
                                            ?
                                            startedAt.plus(37, ChronoUnit.DAYS).toEpochMilli()
                                            :
                                            Instant.ofEpochMilli(lastCheck.when()).plus(37, ChronoUnit.DAYS).toEpochMilli(),
                                    now.plus(37, ChronoUnit.DAYS).toEpochMilli()
                            ));

                            logger.info(ConsoleColors.resetting(
                                    ConsoleColors.GREEN,
                                    "New name detected as available: " + name + " at: " + now)
                            );
                        });

                latch.countDown();
                logger.info("Got a 200 response from name check request. Total count: " + SUCCESSFUL_REQUESTS.incrementAndGet());

                break;
            } catch (IOException e) {
                logger.log(
                        Level.WARNING,
                        "An error occurred while executing name check request, trying again...",
                        e
                );
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}