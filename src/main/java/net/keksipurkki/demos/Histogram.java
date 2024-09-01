package net.keksipurkki.demos;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static java.math.RoundingMode.HALF_DOWN;

@Slf4j
public class Histogram {

    private final BigDecimal min;
    private final BigDecimal max;
    private final BigDecimal delta;
    private final int[] counts;
    private final int id;

    public Histogram(int id, int numBins, double min, double max) {
        Assert.isTrue(min < max, "Min < Max!");
        Assert.isTrue(numBins > 0, "numBins must be a positive number");
        this.id = id;
        this.counts = new int[numBins]; // zeros
        this.min = new BigDecimal(min).setScale(2, HALF_DOWN);
        this.max = new BigDecimal(max).setScale(2, HALF_DOWN);
        this.delta = this.max.subtract(this.min);
    }

    public void record(Double value) {
        if (value > max.doubleValue() || value < min.doubleValue()) {
            log.warn("Ignoring value {}. Out of range", value);
            return;
        }
        counts[bin(value)]++;
    }

    private int bin(double value) {
        return (int) Math.floor(counts.length * Math.abs(value / delta.doubleValue()));
    }

    @JsonValue
    List<Bin> getHistogram() {
        var bins = new ArrayList<Bin>();

        var binWidth = new BigDecimal(this.delta.doubleValue() / counts.length);

        for (var i = 0; i < counts.length; i++) {
            var offset = new BigDecimal(i * binWidth.doubleValue());
            var binMin = min.add(offset);
            var binMax = binMin.add(binWidth);
            bins.add(new Bin(binMin, binMax, counts[i]));
        }

        return bins;
    }

    public String name() {
        return "Histogram #%d".formatted(id);
    }

    record Bin(BigDecimal min, BigDecimal max, int count) {
    }

}
