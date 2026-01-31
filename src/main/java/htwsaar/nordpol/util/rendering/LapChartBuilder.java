package htwsaar.nordpol.util.rendering;

import htwsaar.nordpol.domain.SessionReport;

import java.awt.image.BufferedImage;

public interface LapChartBuilder {
    BufferedImage build(SessionReport report);
}
