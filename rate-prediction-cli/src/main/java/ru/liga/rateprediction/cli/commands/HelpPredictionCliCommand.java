package ru.liga.rateprediction.cli.commands;

import ru.liga.rateprediction.cli.CliCommand;
import ru.liga.rateprediction.cli.PredictionRange;
import ru.liga.rateprediction.core.CurrencyType;

import java.util.Arrays;

class HelpPredictionCliCommand implements PredictionCliCommand {
    private static final String HELP_TEXT =
            String.format(
                    """
                            Input your command in format: (command) [currency_type] [prediction_range]. For example "rate TRY tomorrow" or "rate USD week".
                                        
                            Supported commands: %s.
                            Supported currencies: %s.
                            Supported ranges: %s.
                            Good luck!
                            """,
                    Arrays.toString(CliCommand.values()),
                    Arrays.toString(CurrencyType.values()),
                    Arrays.toString(PredictionRange.values())
            );

    @Override
    public void execute() {
        System.out.println(HELP_TEXT);
    }
}
