package me.siansxint.sniper.checker;

import me.siansxint.sniper.common.Service;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Injector;

import java.util.Collection;

public class CheckerMain implements Service {

    private @Inject Collection<Service> services;

    @Override
    public void start() {
        Injector.create(new MainModule())
                .injectMembers(this);

        for (Service service : services) {
            service.start();
        }
    }

    @Override
    public void stop() {
        for (Service service : services) {
            service.stop();
        }
    }

    public static void main(String[] args) {
        CheckerMain application = new CheckerMain();

        application.start();
        Runtime.getRuntime().addShutdownHook(new Thread(application::stop));
    }
}