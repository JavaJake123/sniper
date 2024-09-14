package me.siansxint.sniper.claimer;

import me.siansxint.sniper.claimer.account.Account;
import net.lenni0451.commons.httpclient.HttpClient;
import net.raphimc.minecraftauth.MinecraftAuth;
import net.raphimc.minecraftauth.step.AbstractStep;
import net.raphimc.minecraftauth.step.java.session.StepFullJavaSession;
import net.raphimc.minecraftauth.step.msa.StepMsaToken;

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

    static Account refreshIfNeeded(Account account, Logger logger) {
        if (account == null) {
            return null;
        }

        if (account.expireTime() > System.currentTimeMillis()) {
            return account;
        }

        StepFullJavaSession.FullJavaSession session;
        try {
            session = JAVA_CREDENTIALS_LOGIN.getFromInput(
                    AUTH_CLIENT,
                    new StepMsaToken.RefreshToken(account.refreshToken())
            );
        } catch (Exception e) {
            logger.log(
                    Level.SEVERE,
                    "Failed to refresh account '" + account.name() + "'!",
                    e
            );
            return null;
        }

        logger.info("Refreshed account '" + account.name() + "'.");
        return Account.fromSession(session);
    }
}