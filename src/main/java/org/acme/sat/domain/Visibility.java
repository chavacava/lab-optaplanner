package org.acme.sat.domain;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Model of a satellite visibility.
 */
public class Visibility {
    private static final Logger LOGGER = LoggerFactory.getLogger(Visibility.class);
    private static final int CSV_PARTS = 5;

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

    public Visibility(String csvLine) {
        String[] parts = csvLine.split(","); 
        if (parts.length<CSV_PARTS) {
            LOGGER.error("Malformed CVS line for Visibility: "+csvLine);
            System.exit(1);
        }
        this.id = parts[0];
        this.satellite = parts[1];
        this.antenna = parts[2];        
        try {
            this.from = Instant.parse(parts[3]);
            this.to = Instant.parse(parts[4]);
        } catch (DateTimeParseException e) {
            LOGGER.error("unable to parse instant from CVS line: "+e.toString());
            System.exit(1);
        }
        this.duration = Duration.between(this.from, this.to);
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
