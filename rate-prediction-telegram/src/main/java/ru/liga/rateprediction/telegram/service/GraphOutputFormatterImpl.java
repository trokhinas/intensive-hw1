package ru.liga.rateprediction.telegram.service;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import ru.liga.rateprediction.core.CurrencyType;
import ru.liga.rateprediction.core.RatePrediction;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class GraphOutputFormatterImpl implements OutputFormatter<byte[]> {
    @Override
    public byte[] format(Map<CurrencyType, List<RatePrediction>> currencyTypeListMap) {
        try {
            final JFreeChart x = ChartFactory.createTimeSeriesChart(
                    // todo может как-то передавать название?
                    "Прогноз",
                    "Дата",
                    "Курс",
                    createDataSet(currencyTypeListMap),
                    true,
                    false,
                    false
            );
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ChartUtils.writeChartAsPNG(bos, x, 1024, 768);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private XYDataset createDataSet(Map<CurrencyType, List<RatePrediction>> currencyTypeListMap) {
        final TimeSeriesCollection dataset = new TimeSeriesCollection();

        currencyTypeListMap.entrySet()
                .stream()
                .map(entry -> {
                    final TimeSeries timeSeries = new TimeSeries(entry.getKey().getCode());
                    entry.getValue().forEach(ratePrediction -> {
                        final LocalDate date = ratePrediction.getDate();
                        timeSeries.add(
                                new Day(date.getDayOfMonth(), date.getMonthValue(), date.getYear()),
                                ratePrediction.getRate()
                        );
                    });

                    return timeSeries;
                })
                .forEach(dataset::addSeries);

        return dataset;
    }
}
