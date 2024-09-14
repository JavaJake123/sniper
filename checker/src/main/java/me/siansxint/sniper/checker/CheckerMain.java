package me.siansxint.sniper.checker;

import me.siansxint.sniper.common.Service;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Injector;

import java.util.Collection;
import java.util.Scanner;

public class CheckerMain implements Service {

    private @Inject Collection<Service> services;

    @Override
    public void start() {
        Injector.create(new MainModule())
                .injectMembers(this);

        for (Service service : services) {
            service.start();
        }

        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            if (!scanner.next().equalsIgnoreCase("exit")) {
                System.out.println("If you want to exit, type 'exit'.");
                continue;
            }

            stop();
            System.exit(0);
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
    }
}