package org.acme.sat.domain;

import java.time.Duration;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.lookup.PlanningId;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

@PlanningEntity
public class ContactRequest {
    @PlanningId
    private Long id;
    private String satellite;
    private Duration duration;
    @PlanningVariable(valueRangeProviderRefs = "visibilityRange")
    private Visibility visibility;
    
    public ContactRequest() {
    }

    
    public ContactRequest(Long id, String satellite, Duration duration) {
        this.id = id;
        this.satellite = satellite;
        this.duration = duration;
    }


    public Long getId() {
        return id;
    }
    public void setId(Long id) {
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

    @Override
    public String toString() {
        return "ContactRequest [duration=" + duration + ", id=" + id + ", satellite=" + satellite + ", "
                + visibility + "]";
    }    
}
