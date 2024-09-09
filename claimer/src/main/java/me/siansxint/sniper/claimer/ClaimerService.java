package me.siansxint.sniper.claimer;

import com.mongodb.client.model.Sorts;
import me.siansxint.sniper.claimer.account.Account;
import me.siansxint.sniper.common.NameDropTime;
import me.siansxint.sniper.common.Service;
import me.siansxint.sniper.common.http.HttpClientSelector;
import me.siansxint.sniper.common.registry.TRegistry;
import me.siansxint.sniper.common.storage.MongoTStorage;
import me.siansxint.sniper.common.storage.TStorage;
import team.unnamed.inject.Inject;

import java.time.Instant;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class ClaimerService implements Service {

    private @Inject TRegistry<Account> accounts;
    private @Inject TStorage<NameDropTime> dropTimes;

    private @Inject HttpClientSelector selector;

    private @Inject Logger logger;

    private @Inject ExecutorService service;

    @Override
    public void start() {
        Thread.ofVirtual()
                .name("NameChangerService")
                .start(() -> {
                    while (true) {
                        try {
                            if (!(dropTimes instanceof MongoTStorage<NameDropTime> storage)) {
                                break;
                            }

                            Collection<NameDropTime> dropTimes = storage.findAllSorting(Sorts.ascending("from")).join();
                            if (dropTimes.isEmpty()) {
                                TimeUnit.HOURS.sleep(1);
                                continue;
                            }

                            CountDownLatch latch = new CountDownLatch(dropTimes.size());

                            for (NameDropTime dropTime : dropTimes) {
                                Instant start = Instant.ofEpochMilli(dropTime.from());
                                if (start.isBefore(Instant.now())) { // no longer available for claiming
                                    this.dropTimes.delete(dropTime.id());
                                    continue;
                                }

                                service.submit(new NameChangeTask(
                                        accounts,
                                        dropTime,
                                        selector,
                                        latch,
                                        logger
                                ));
                            }

                            logger.info("Submitted " + dropTimes.size() + " name claims.");

                            latch.await();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt(); // reset thread status
                        }
                    }
                });
    }
}