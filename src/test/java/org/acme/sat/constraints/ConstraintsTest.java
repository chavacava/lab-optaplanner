package org.acme.sat.constraints;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.acme.sat.domain.ContactPlan;
import org.acme.sat.domain.ContactRequest;
import org.acme.sat.domain.Visibility;
import org.junit.jupiter.api.Test;
import org.optaplanner.test.api.score.stream.ConstraintVerifier;

public class ConstraintsTest {
    private ConstraintVerifier<ContactPlanConstraintProvider,ContactPlan> constraintVerifier = ConstraintVerifier.build(new ContactPlanConstraintProvider(), ContactPlan.class, ContactRequest.class);

    @Test
    public void visibilityConflict(){
        Instant now = Instant.now();
        Visibility visibility= new Visibility("sat1","ls1",now,now.plusSeconds(10));
        ContactRequest cr1 = new ContactRequest(1L,"sat1",Duration.of(6, ChronoUnit.SECONDS));
        cr1.setVisibility(visibility);
        ContactRequest cr2 = new ContactRequest(2L,"sat1",Duration.of(8, ChronoUnit.SECONDS));
        cr2.setVisibility(visibility);      
        constraintVerifier.verifyThat(ContactPlanConstraintProvider::visibilityConflict).given(cr1,cr2).penalizesBy(1);

        ContactRequest cr3 = new ContactRequest(3L,"sat1",Duration.of(7, ChronoUnit.SECONDS));
        constraintVerifier.verifyThat(ContactPlanConstraintProvider::visibilityConflict).given(cr1,cr3).penalizesBy(0);

        Visibility otherVisibility= new Visibility("sat1","ls1",now,now.plusSeconds(10));
        cr3.setVisibility(otherVisibility);
        constraintVerifier.verifyThat(ContactPlanConstraintProvider::visibilityConflict).given(cr1,cr3).penalizesBy(0);
    }

    @Test
    public void visibilityTooShort(){
        Instant now = Instant.now();
        Visibility visibility= new Visibility("sat1","ls1",now,now.plusSeconds(10));
        ContactRequest cr1 = new ContactRequest(1L,"sat1",Duration.of(11, ChronoUnit.SECONDS));
        cr1.setVisibility(visibility);               
        constraintVerifier.verifyThat(ContactPlanConstraintProvider::visibilityTooShort).given(cr1).penalizesBy(1);
        
        ContactRequest cr2 = new ContactRequest(3L,"sat1",Duration.of(10, ChronoUnit.SECONDS));
        cr2.setVisibility(visibility);
        constraintVerifier.verifyThat(ContactPlanConstraintProvider::visibilityTooShort).given(cr2).penalizesBy(0);
        
    }

    @Test
    public void visibilityForOtherSat(){
        Instant now = Instant.now();
        Visibility visibility= new Visibility("sat1","ls1",now,now.plusSeconds(10));
        ContactRequest cr1 = new ContactRequest(1L,"sat2",Duration.of(5, ChronoUnit.SECONDS));
        cr1.setVisibility(visibility);               
        constraintVerifier.verifyThat(ContactPlanConstraintProvider::visibilityForOtherSat).given(cr1).penalizesBy(1);
        
        ContactRequest cr2 = new ContactRequest(3L,"sat1",Duration.of(5, ChronoUnit.SECONDS));
        cr2.setVisibility(visibility);
        constraintVerifier.verifyThat(ContactPlanConstraintProvider::visibilityForOtherSat).given(cr2).penalizesBy(0);        
    }

    @Test
    public void antennaConflict(){
        Instant now = Instant.now();
        Visibility v1 = new Visibility("sat1","ls1",now,now.plusSeconds(10));
        Visibility v2= new Visibility("sat1","ls1",now.plusSeconds(2),now.plusSeconds(5));
        Visibility v3= new Visibility("sat1","ls1",now.plusSeconds(9),now.plusSeconds(5));
        /* Visibilities
           0----*----|----*----|
        v1 //////////
        v2   /////
        v3          ////// 
        */
        ContactRequest cr1 = new ContactRequest(1L,"sat1",Duration.of(1, ChronoUnit.SECONDS));
        ContactRequest cr2 = new ContactRequest(2L,"sat1",Duration.of(1, ChronoUnit.SECONDS));
        
        cr1.setVisibility(v1);
        cr2.setVisibility(v2);      
        constraintVerifier.verifyThat(ContactPlanConstraintProvider::antennaConflict).given(cr1,cr2).penalizesBy(1);

        cr1.setVisibility(v2);
        cr2.setVisibility(v1);
        constraintVerifier.verifyThat(ContactPlanConstraintProvider::antennaConflict).given(cr1,cr2).penalizesBy(1);

        cr1.setVisibility(v1);
        cr2.setVisibility(v3);
        constraintVerifier.verifyThat(ContactPlanConstraintProvider::antennaConflict).given(cr1,cr2).penalizesBy(1);

        cr1.setVisibility(v3);
        cr2.setVisibility(v1);
        constraintVerifier.verifyThat(ContactPlanConstraintProvider::antennaConflict).given(cr1,cr2).penalizesBy(1);

        cr1.setVisibility(v2);
        cr2.setVisibility(v3);
        constraintVerifier.verifyThat(ContactPlanConstraintProvider::antennaConflict).given(cr1,cr2).penalizesBy(0);

        // Must not fail if antennas !=
        v1.setAntenna(v2.getAntenna()+"dif");
        cr1.setVisibility(v1);
        cr2.setVisibility(v2);      
        constraintVerifier.verifyThat(ContactPlanConstraintProvider::antennaConflict).given(cr1,cr2).penalizesBy(0);
    }

}

