package me.siansxint.sniper.claimer.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.siansxint.sniper.claimer.MinecraftAuthentications;
import me.siansxint.sniper.common.Files;
import me.siansxint.sniper.common.Patterns;
import me.siansxint.sniper.common.http.HttpClientSelector;
import me.siansxint.sniper.common.registry.LocalTRegistry;
import me.siansxint.sniper.common.registry.TRegistry;
import net.raphimc.minecraftauth.step.java.session.StepFullJavaSession;
import net.raphimc.minecraftauth.step.msa.StepCredentialsMsaCode;
import team.unnamed.inject.AbstractModule;
import team.unnamed.inject.Module;
import team.unnamed.inject.Named;
import team.unnamed.inject.Provides;
import team.unnamed.inject.Singleton;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import static me.siansxint.sniper.claimer.MinecraftAuthentications.AUTH_CLIENT;
import static me.siansxint.sniper.claimer.MinecraftAuthentications.JAVA_CREDENTIALS_LOGIN;

public class AccountModule extends AbstractModule implements Module {

    @Provides
    @Singleton
    public TRegistry<Account> accounts(Logger logger, ObjectMapper mapper, HttpClientSelector selector, @Named("refresher") ExecutorService executor) {
        TRegistry<Account> accounts = new LocalTRegistry<>();

        File folder = new File("sessions");
        if (folder.exists()) {
            File[] files = folder.listFiles((dir, name) -> name.endsWith(".json"));
            if (files == null || files.length < 1) {
                return accounts;
            }

            for (File file : files) {
                String name = file.getName();
                if (accounts.get(name.substring(0, name.indexOf('.'))) != null) {
                    logger.warning(
                            "Account '" + name + "' is already loaded, skipping it..."
                    );
                    continue;
                }

                executor.submit(() -> {
                    try {
                        Account account = mapper.readValue(file, Account.class);
                        if (account == null) {
                            return;
                        }

                        accounts.register(MinecraftAuthentications.refreshIfNeeded(account, logger));
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, "Failed to load account from file '" + file.getName() + "'.", e);
                    }
                });
            }
        }

        File accountsFile = new File("accounts.txt");
        if (accountsFile.exists()) {
            List<String> lines;
            try {
                lines = Files.loadTextFile(accountsFile);
            } catch (IOException e) {
                logger.log(
                        Level.SEVERE,
                        "Failed to load accounts.txt!",
                        e
                );
                return accounts;
            }

            List<String> loaded = new ArrayList<>();
            if (lines.isEmpty()) {
                logger.warning("No more accounts found, probably the 'accounts.txt' file was not found or you haven't put any new accounts there!");
            } else {
                for (Iterator<String> iterator = lines.iterator(); iterator.hasNext(); ) {
                    String line = iterator.next();
                    String[] parts = Patterns.TWO_DOTS_PATTERN.split(line);
                    if (parts.length < 2) {
                        continue;
                    }

                    StepFullJavaSession.FullJavaSession session;
                    try {
                        session = JAVA_CREDENTIALS_LOGIN.getFromInput(AUTH_CLIENT, new StepCredentialsMsaCode.MsaCredentials(
                                parts[0],
                                parts[1]
                        ));
                    } catch (Exception e) {
                        logger.log(
                                Level.WARNING,
                                "An error occurred while authenticating account with email '" + parts[0] + "', it is probably using 2FA...",
                                e
                        );
                        continue;
                    }

                    accounts.register(Account.fromSession(session));
                    loaded.add(line);

                    iterator.remove();
                }
            }

            File alreadyLoadedAccountsFile = new File("already_loaded_accounts.txt");
            if (!loaded.isEmpty()) {
                try {
                    Files.writeTextFile(alreadyLoadedAccountsFile, loaded);
                } catch (IOException e) {
                    logger.log(
                            Level.WARNING,
                            "An error occurred while writing loaded accounts to '" + alreadyLoadedAccountsFile.getName() + "'.",
                            e
                    );
                }
            }

            try {
                Files.writeTextFile(accountsFile, lines);
            } catch (IOException e) {
                logger.log(
                        Level.WARNING,
                        "An error occurred while writing pendent to load accounts to '" + accountsFile.getName() + "'!",
                        e
                );
            }
        }

        return accounts;
    }
}