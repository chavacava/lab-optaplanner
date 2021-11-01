package org.acme.sat.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Model of a satellite visibility.
 */
public class Visibility {
    private String satellite;
    private String antenna;
    private Instant from;
    private Instant to;
    private Duration duration;
    private String id;

    
    public Visibility(String satellite, String antenna, Instant from, Instant to) {
        this.satellite = satellite;
        this.antenna = antenna;
        this.from = from;
        this.to = to;
        this.duration = Duration.between(from, to);
        this.id = UUID.randomUUID().toString();
    }


    public String getSatellite() {
        return satellite;
    }


    public void setSatellite(String satellite) {
        this.satellite = satellite;
    }


    public String getAntenna() {
        return antenna;
    }


    public void setAntenna(String antenna) {
        this.antenna = antenna;
    }


    public Instant getFrom() {
        return from;
    }


    public void setFrom(Instant from) {
        this.from = from;
    }


    public Instant getTo() {
        return to;
    }


    public void setTo(Instant to) {
        this.to = to;
    }


    public Duration getDuration() {
        return duration;
    }

    
    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Visibility [antenna=" + antenna + ", duration=" + duration + ", satellite=" + satellite + "]";
    }
    
    
}
