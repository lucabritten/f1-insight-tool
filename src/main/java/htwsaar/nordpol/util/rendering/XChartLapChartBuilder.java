package htwsaar.nordpol.util.rendering;

import htwsaar.nordpol.domain.Driver;
import htwsaar.nordpol.domain.Lap;
import htwsaar.nordpol.domain.SessionReport;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.Styler;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

/**
 * Default implementation of LapChartBuilder using XChart.
 */
public class XChartLapChartBuilder implements LapChartBuilder {
    @Override
    public BufferedImage build(SessionReport report) {
        if (report.lapSeriesByDriver().isEmpty()) {
            return null;
        }

        XYChart chart = new XYChartBuilder()
                .width(900)
                .height(420)
                .title("Lap Time Comparison")
                .xAxisTitle("Lap")
                .yAxisTitle("Lap duration (s)")
                .build();

        chart.getStyler().setLegendPosition(Styler.LegendPosition.OutsideE);
        chart.getStyler().setLegendBackgroundColor(new Color(255, 255, 255, 180));
        chart.getStyler().setLegendBorderColor(Color.LIGHT_GRAY);
        chart.getStyler().setLegendFont(chart.getStyler().getLegendFont().deriveFont(9f));

        chart.getStyler().setMarkerSize(3);
        chart.getStyler().setYAxisDecimalPattern("0.000");

        double maxYValue = Float.MIN_VALUE;
        double minYValue = Float.MAX_VALUE;
        for (Map.Entry<Driver, List<Lap>> entry : report.lapSeriesByDriver().entrySet()) {
            List<Lap> laps = entry.getValue();
            if (laps == null || laps.isEmpty()) {
                continue;
            }
            List<Integer> xData = new ArrayList<>();
            List<Double> yData = new ArrayList<>();
            for (Lap lap : laps) {
                double duration = lap.lapDuration();
                if (duration <= 0 || Double.isNaN(duration) || Double.isInfinite(duration)) {
                    continue;
                }
                xData.add(lap.lapNumber());
                yData.add(duration);
            }
            Driver driver = entry.getKey();
            String seriesName = driver.lastName() + " #" + driver.driverNumber();
            chart.addSeries(seriesName, xData, yData);
            maxYValue = Math.max(maxYValue, Collections.max(yData));
            minYValue = Math.min(minYValue, Collections.min(yData));
        }
        chart.getStyler().setYAxisMax(maxYValue + 0.5);
        chart.getStyler().setYAxisMin(minYValue - 0.5);

        if (chart.getSeriesMap().isEmpty()) {
            return null;
        }
        return BitmapEncoder.getBufferedImage(chart);
    }
}
