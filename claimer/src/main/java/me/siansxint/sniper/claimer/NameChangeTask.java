package me.siansxint.sniper.claimer;

import me.siansxint.sniper.claimer.account.Account;
import me.siansxint.sniper.common.ConsoleColors;
import me.siansxint.sniper.common.http.HttpResponse;
import me.siansxint.sniper.common.NameDropTime;
import me.siansxint.sniper.common.ReadableTimes;
import me.siansxint.sniper.common.http.HttpClientSelector;
import me.siansxint.sniper.common.http.HttpResponseHandlers;
import me.siansxint.sniper.common.registry.TRegistry;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.message.BasicHeader;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("BusyWait")
public class NameChangeTask implements Runnable {

    private static final String USERNAME_CHANGE_URI = "https://api.minecraftservices.com/minecraft/profile/name/%s";

    private final TRegistry<Account> accounts;
    private final HttpClientSelector selector;

    private final NameDropTime dropTime;

    private final CountDownLatch latch;
    private final Logger logger;

    public NameChangeTask(
            TRegistry<Account> accounts,
            HttpClientSelector selector,
            NameDropTime dropTime,
            CountDownLatch latch,
            Logger logger
    ) {
        this.accounts = accounts;
        this.dropTime = dropTime;
        this.selector = selector;
        this.latch = latch;
        this.logger = logger;
    }

    @Override
    public void run() {
        Instant start = Instant.ofEpochMilli(dropTime.from());
        Instant end = Instant.ofEpochMilli(dropTime.to());

        if (Instant.now().isAfter(end)) {
            logger.info("Drop-time interval already ended!");
            return;
        }

        while (Instant.now().isBefore(start)) {
            long from = Duration.between(Instant.now(), start).toMillis();
            logger.info("Starting claim of name '" + dropTime.id() + "' in " + ReadableTimes.durationToHumanTime(from) + ".");

            try {
                Thread.sleep(Math.min(
                        TimeUnit.HOURS.toMillis(1),
                        from
                ));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        main:
        while (Instant.now().isBefore(end)) {
            for (Account account : accounts) {
                HttpClient client = selector.next();

                HttpPut request = new HttpPut(String.format(USERNAME_CHANGE_URI, dropTime.id()));
                request.setHeaders(
                        new BasicHeader("Authorization", "Bearer " + account.accessToken()),
                        new BasicHeader("Accept", ContentType.APPLICATION_JSON.toString()),
                        new BasicHeader("Content-Type", ContentType.APPLICATION_JSON.toString()),
                        new BasicHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:129.0) Gecko/20100101 Firefox/129.0")
                );

                try {
                    HttpResponse response = client.execute(
                            request,
                            HttpResponseHandlers.RESPONSE_HANDLER
                    );

                    if (response == null || response.result() == null) {
                        logger.warning("Got an unexpected response, trying again...");
                        continue;
                    }

                    switch (response.status()) {
                        case 400 -> {
                            logger.warning("Name is invalid, longer than 16 characters or contains characters other than (a-zA-Z0-9_).");
                            break main;
                        }
                        case 403 ->
                                logger.info("Name '" + dropTime.id() + "' is unavailable (Either taken or has not become available), trying again...");
                        case 401 -> {
                            logger.warning("Unauthorized (Bearer token expired or is not correct).");
                            continue; // try using another account
                        }
                        case 429 -> logger.warning("Got rate-limited, trying again...");
                        case 500 -> {
                            logger.warning("Server returned an error.");
                            continue; // is not our fault, we could try again ig
                        }
                        case 200 -> {
                            logger.info(ConsoleColors.resetting(ConsoleColors.GREEN, "Sniped name '" + dropTime.id() + "' on account previous named '" + account.id() + "'!"));
                            break main;
                        }
                        default -> logger.info("Got an unexpected response status: " + response.status());
                    }

                    Thread.sleep(
                            10000 / accounts.size()
                    );
                } catch (IOException e) {
                    logger.log(
                            Level.WARNING,
                            "An error occurred while executing name change request, trying again...",
                            e
                    );
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        // there's no need to remove the drop-time from the database now
        logger.info("Drop-time interval for '" + dropTime.id() + "' ended, moving on...");

        latch.countDown();
    }
}