package me.siansxint.sniper.claimer;

import me.siansxint.sniper.common.Service;
import sun.misc.Signal;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Injector;

import java.util.Collection;
import java.util.Scanner;

public class ClaimerMain implements Service {

    private @Inject Collection<Service> services;

    @Override
    public void start() {
        Injector.create(new MainModule())
                .injectMembers(this);

        for (Service service : services) {
            service.start();
        }

        Signal.handle(new Signal("INT"),
                signal -> stop());

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            if (!scanner.next().equalsIgnoreCase("exit")) {
                System.out.println("If you want to exit, type 'exit'.");
                continue;
            }

            stop();
        }
    }

    @Override
    public void stop() {
        for (Service service : services) {
            service.stop();
        }

        System.exit(0);
    }

    public static void main(String[] args) {
        ClaimerMain application = new ClaimerMain();
        application.start();
    }
}