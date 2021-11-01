package org.acme.sat.domain;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.UUID;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.lookup.PlanningId;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PlanningEntity
public class ContactRequest {
    private static final Logger LOGGER = LoggerFactory.getLogger(ContactRequest.class);

    @PlanningId
    private String id;
    private String satellite;
    private Duration duration;
    @PlanningVariable(valueRangeProviderRefs = "visibilityRange")
    private Visibility visibility;

    public ContactRequest() {
    }
    
    public ContactRequest(String satellite, Duration duration) {
        this.id = getPseudoUniqueId();
        this.satellite = satellite;
        this.duration = duration;
    }

    public ContactRequest(String csvLine) {
        String[] parts = csvLine.split(","); 
        if (parts.length<3) {
            LOGGER.error("Malformed CVS line for ContactRequest: "+csvLine);
            System.exit(1);
        }
        this.id = parts[0];
        this.satellite = parts[2];
        try {
        this.duration = Duration.parse(parts[1]);
        } catch (DateTimeParseException e) {
            LOGGER.error("unable to parse duration from CVS line: "+parts[1]);
            System.exit(1);
        }
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getSatellite() {
        return satellite;
    }
    public void setSatellite(String satellite) {
        this.satellite = satellite;
    }
    public Duration getDuration() {
        return duration;
    }
    public void setDuration(Duration duration) {
        this.duration = duration;
    }
    public Visibility getVisibility() {
        return visibility;
    }
    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    private String getPseudoUniqueId() {
        return UUID.randomUUID().toString().substring(0, 5);
    }

    @Override
    public String toString() {
        return "ContactRequest [duration=" + duration + ", id=" + id + ", satellite=" + satellite + ", "
                + visibility + "]";
    }    

    /**
     * @param pin generates the CSV entry with this ContactRequest pinned.
     */
    public String toCSV(boolean pin) {
        return id + "," + duration + "," + satellite;
    }
}
