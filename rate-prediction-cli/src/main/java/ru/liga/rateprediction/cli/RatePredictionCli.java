package ru.liga.rateprediction.cli;

import ru.liga.rateprediction.cli.commands.PredictionCliCommand;
import ru.liga.rateprediction.cli.commands.PredictionCliCommandParser;
import ru.liga.rateprediction.core.RatePredictionFacade;

import java.util.Scanner;

public class RatePredictionCli {
    public static void start() {
        final Scanner scanner = new Scanner(System.in);
        final PredictionCliCommandParser predictionCliCommandParser = new PredictionCliCommandParser(
                new RatePredictionFacade()
        );

        predictionCliCommandParser.executeHelp();
        while (true) {
            System.out.print("Input your command: ");
            final String input = scanner.nextLine();

            final PredictionCliCommand command = predictionCliCommandParser.parse(input);
            try {
                command.execute();
            } catch (Exception e) {
                System.out.printf("Something went wrong during execution of \"%s\": %s\n", input, e.getMessage());
            }
        }
    }
}
