package me.siansxint.sniper.claimer.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.siansxint.sniper.claimer.MinecraftAuthentications;
import me.siansxint.sniper.common.Service;
import me.siansxint.sniper.common.registry.TRegistry;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Named;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class AccountService implements Service {

    private @Inject TRegistry<Account> accounts;

    private @Inject Logger logger;

    private @Inject ObjectMapper mapper;

    @Inject
    @Named("refresher")
    private ExecutorService executor;
    private @Inject ScheduledExecutorService scheduledExecutor;

    @Override
    public void start() {
        scheduledExecutor.scheduleWithFixedDelay(
                () -> {
                    List<CompletableFuture<Void>> futures = new ArrayList<>();

                    for (Account account : accounts) {
                        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                            Account refreshed = MinecraftAuthentications.refreshIfNeeded(account, logger);
                            if (refreshed != null) {
                                accounts.register(refreshed);
                                return;
                            }

                            logger.warning("Failed to refresh account '" + account.id() + "'. Removing account...");
                            accounts.remove(account.id());
                        }, executor);

                        futures.add(future);
                    }

                    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                            .join();
                    logger.info("Refreshed all needing accounts!");
                },
                5,
                5,
                TimeUnit.MINUTES
        );
    }

    @Override
    public void stop() {
        File folder = new File("sessions");
        if (!folder.exists() && !folder.mkdirs()) {
            return;
        }

        for (Account account : accounts) {
            File file = new File(folder, account.id() + ".json");
            try (Writer writer = new BufferedWriter(new FileWriter(file))) {
                mapper.writeValue(writer, account);
            } catch (IOException e) {
                System.err.println("An error occurred while saving session '" + account.id() + "'. Exception message: " + e.getMessage());
            }
        }

        System.out.println("Saved all sessions!");
    }
}