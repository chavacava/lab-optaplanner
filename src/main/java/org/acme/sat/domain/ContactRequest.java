package org.acme.sat.domain;

import java.time.Duration;
import java.util.UUID;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.entity.PlanningPin;
import org.optaplanner.core.api.domain.lookup.PlanningId;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

@PlanningEntity
public class ContactRequest {
    @PlanningId
    private String id;
    private String satellite;
    private Duration duration;
    @PlanningVariable(valueRangeProviderRefs = "visibilityRange")
    private Visibility visibility;
    private boolean pinned;

    public ContactRequest() {
    }
    
    public ContactRequest(String satellite, Duration duration) {
        this.id = getPseudoUniqueId();
        this.satellite = satellite;
        this.duration = duration;
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

    @PlanningPin
    public boolean isPinned() {
        return pinned;
    }

    private String getPseudoUniqueId() {
        return UUID.randomUUID().toString().substring(0, 5);
    }

    @Override
    public String toString() {
        return "ContactRequest [duration=" + duration + ", id=" + id + ", satellite=" + satellite + ", "
                + visibility + "]";
    }    

    public String toCSV() {
        return id + "," + duration + "," + satellite;
    }

    public static String getCSVHeader() {
        return "id,duration,satellite";
    }
}
