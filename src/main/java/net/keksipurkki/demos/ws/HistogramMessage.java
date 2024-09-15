package net.keksipurkki.demos.ws;

import lombok.Value;
import net.keksipurkki.demos.Histogram;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.math.RoundingMode.HALF_DOWN;

@Value
public class HistogramMessage {

    List<List<Bin>> histograms;

    private HistogramMessage(List<List<Bin>> histograms) {
        this.histograms = histograms;
    }

    public static HistogramMessage from(Collection<Histogram> histograms) {

        var data = new ArrayList<List<Bin>>();

        for (var histogram : histograms) {
            data.add(bins(histogram));
        }

        return new HistogramMessage(data);

    }

    private static List<Bin> bins(Histogram histogram) {
        var bins = new ArrayList<Bin>();
        var counts = histogram.counts();
        var binWidth = new BigDecimal(histogram.delta().doubleValue() / counts.length);

        for (var i = 0; i < counts.length; i++) {
            var offset = new BigDecimal(i * binWidth.doubleValue()).setScale(2, HALF_DOWN);
            var binMin = histogram.min().add(offset);
            var binMax = binMin.add(binWidth);
            bins.add(new Bin(binMin, binMax, counts[i]));
        }

        return bins;
    }

    record Bin(BigDecimal min, BigDecimal max, int count) {
    }

}
