package org.acme.sat.domain;

import java.util.List;

import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

@PlanningSolution
public class ContactPlan {
    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "visibilityRange")
    private List<Visibility> visibilities;
    @PlanningEntityCollectionProperty
    private List<ContactRequest> contactRequests;

    @PlanningScore
    private HardSoftScore score;

    public ContactPlan() {
    }

    public ContactPlan(List<Visibility> visibilities, List<ContactRequest> contactRequests) {
        this.visibilities = visibilities;
        this.contactRequests = contactRequests;
    }

    public List<Visibility> getVisibilities() {
        return visibilities;
    }

    public List<ContactRequest> getContactRequests() {
        return contactRequests;
    }   
    
}
