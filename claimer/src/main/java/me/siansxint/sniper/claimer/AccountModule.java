package me.siansxint.sniper.claimer;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.siansxint.sniper.claimer.account.Account;
import me.siansxint.sniper.common.Files;
import me.siansxint.sniper.common.Patterns;
import me.siansxint.sniper.common.registry.LocalTRegistry;
import me.siansxint.sniper.common.registry.TRegistry;
import net.raphimc.minecraftauth.step.java.StepMCProfile;
import net.raphimc.minecraftauth.step.java.StepMCToken;
import net.raphimc.minecraftauth.step.java.session.StepFullJavaSession;
import net.raphimc.minecraftauth.step.msa.StepCredentialsMsaCode;
import net.raphimc.minecraftauth.step.msa.StepMsaToken;
import team.unnamed.inject.AbstractModule;
import team.unnamed.inject.Module;
import team.unnamed.inject.Provides;
import team.unnamed.inject.Singleton;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import static me.siansxint.sniper.claimer.MinecraftAuthentications.AUTH_CLIENT;
import static me.siansxint.sniper.claimer.MinecraftAuthentications.JAVA_CREDENTIALS_LOGIN;

public class AccountModule extends AbstractModule implements Module {

    @Provides
    @Singleton
    public TRegistry<Account> accounts(Logger logger, ObjectMapper mapper) {
        TRegistry<Account> accounts = new LocalTRegistry<>();

        File folder = new File("sessions");
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".json"));

        if (folder.exists() && files != null && files.length > 0) {
            for (File file : files) {
                try (Reader reader = new BufferedReader(new FileReader(file))) {
                    Account account = mapper.readValue(reader, Account.class);
                    if (account == null) {
                        continue;
                    }

                    StepFullJavaSession.FullJavaSession session = JAVA_CREDENTIALS_LOGIN.getFromInput(
                            AUTH_CLIENT,
                            new StepMsaToken.RefreshToken(account.refreshToken())
                    );

                    StepMCProfile.MCProfile profile = session.getMcProfile();
                    StepMCToken.MCToken mc = profile.getMcToken();
                    StepMsaToken.MsaToken msa = mc.getXblXsts().getInitialXblSession().getMsaToken();

                    accounts.register(new Account(
                            profile.getId().toString(),
                            profile.getName(),
                            mc.getAccessToken(),
                            msa.getRefreshToken(),
                            mc.getExpireTimeMs()
                    ));
                } catch (Exception e) {
                    logger.log(
                            Level.WARNING,
                            "An error occurred while reading account file '" + file.getName() + "'.",
                            e
                    );
                }
            }
        } else {
            Collection<String> lines;
            try {
                lines = Files.loadTextFile(new File("accounts.txt"));
            } catch (IOException e) {
                throw new UncheckedIOException(
                        "Failed to load 'accounts.txt' file!",
                        e
                );
            }

            if (lines.isEmpty()) {
                logger.warning("No accounts found, probably the 'accounts.txt' file was not found or you haven't put accounts there!");
            } else {
                for (String line : lines) {
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

                    StepMCProfile.MCProfile profile = session.getMcProfile();
                    StepMCToken.MCToken mc = session.getMcProfile().getMcToken();
                    StepMsaToken.MsaToken msa = mc.getXblXsts().getInitialXblSession().getMsaToken();

                    accounts.register(new Account(
                            profile.getId().toString(),
                            profile.getName(),
                            mc.getAccessToken(),
                            msa.getRefreshToken(),
                            mc.getExpireTimeMs()
                    ));
                }
            }
        }

        for (Account account : accounts) {
            File file = new File(folder, account.id() + ".json");
            if (!file.exists()) {
                logger.warning("File cannot be created for " + account.id() + " session.");
                continue;
            }

            try (Writer writer = new BufferedWriter(new FileWriter(file))) {
                mapper.writeValue(writer, account);
            } catch (IOException e) {
                logger.log(
                        Level.WARNING,
                        "An error occurred while saving session '" + account.id() + "'.",
                        e
                );
            }
        }

        return accounts;
    }
}