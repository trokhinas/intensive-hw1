package ru.liga.rateprediction.cli.commands;

class ExitPredictionCliCommand implements PredictionCliCommand {
    @Override
    public void execute() {
        System.out.println("Stopping program...");
        System.exit(0);
    }
}
