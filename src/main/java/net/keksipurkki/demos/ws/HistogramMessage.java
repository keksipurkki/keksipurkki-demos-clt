package net.keksipurkki.demos.ws;

import net.keksipurkki.demos.Histogram;

import java.util.Collection;

public record HistogramMessage(Collection<Histogram> histograms) { }
