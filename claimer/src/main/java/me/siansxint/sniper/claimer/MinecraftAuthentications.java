package me.siansxint.sniper.claimer;

import me.siansxint.sniper.claimer.account.Account;
import me.siansxint.sniper.common.registry.TRegistry;
import net.lenni0451.commons.httpclient.HttpClient;
import net.raphimc.minecraftauth.MinecraftAuth;
import net.raphimc.minecraftauth.step.AbstractStep;
import net.raphimc.minecraftauth.step.java.StepMCProfile;
import net.raphimc.minecraftauth.step.java.StepMCToken;
import net.raphimc.minecraftauth.step.java.session.StepFullJavaSession;
import net.raphimc.minecraftauth.step.msa.StepMsaToken;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

public interface MinecraftAuthentications {

    AbstractStep<?, StepFullJavaSession.FullJavaSession> JAVA_CREDENTIALS_LOGIN = MinecraftAuth.builder()
            .withClientId("00000000402b5328")
            .withScope("service::user.auth.xboxlive.com::MBI_SSL")
            .credentials()
            .withDeviceToken("Win32")
            .sisuTitleAuthentication("rp://api.minecraftservices.com/")
            .buildMinecraftJavaProfileStep(false);
    HttpClient AUTH_CLIENT = MinecraftAuth.createHttpClient();

    static void refresh(TRegistry<Account> registry, HttpClient authClient, Logger logger) {
        Collection<Account> accounts = registry.copy().values();

        registry.clear();
        for (Account account : accounts) {
            StepFullJavaSession.FullJavaSession session;
            try {
                session = JAVA_CREDENTIALS_LOGIN.getFromInput(
                        authClient,
                        new StepMsaToken.RefreshToken(account.refreshToken())
                );
            } catch (Exception e) {
                logger.log(
                        Level.WARNING,
                        "An error occurred while refreshing session for '" + account.name() + "'."
                );
                continue;
            }

            StepMCProfile.MCProfile profile = session.getMcProfile();
            StepMCToken.MCToken mc = profile.getMcToken();
            StepMsaToken.MsaToken msa = mc.getXblXsts().getInitialXblSession().getMsaToken();

            registry.register(new Account(
                    profile.getId().toString(),
                    profile.getName(),
                    mc.getAccessToken(),
                    msa.getRefreshToken(),
                    mc.getExpireTimeMs()
            ));
        }

        logger.info("Refreshed all accounts!");
    }
}