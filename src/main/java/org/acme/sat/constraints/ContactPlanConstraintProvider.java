package org.acme.sat.constraints;

import java.time.Instant;

import org.acme.sat.domain.ContactRequest;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.core.api.score.stream.Joiners;

public class ContactPlanConstraintProvider implements ConstraintProvider {
    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                // Hard constraints
                visibilityConflict(constraintFactory),
                visibilityTooShort(constraintFactory),
                visibilityForOtherSat(constraintFactory),
                satConflict(constraintFactory),
                antennaConflict(constraintFactory)    
        };
    }

    // Must not assign the same visibility to more than one contact request
    Constraint visibilityConflict(ConstraintFactory constraintFactory) {
        return constraintFactory
                // Select each pair of 2 different ContactRequests ...
                .fromUniquePair(ContactRequest.class,
                        // ... in the same visibility                        
                        Joiners.equal(ContactRequest::getVisibility)
                )                        
                // ... and penalize each pair with a hard weight.
                .penalize("Visibility conflict", HardSoftScore.ONE_HARD);
    }

    // Must not assign a visibility with a duration shorter than required by the contact request
    Constraint visibilityTooShort(ConstraintFactory constraintFactory) {
        return constraintFactory
                // Select contacts ...
                .from(ContactRequest.class)
                        // ... with visibilities shorter than the requerid duration ...
                        .filter(cr -> cr.getVisibility() != null && cr.getDuration().compareTo(cr.getVisibility().getDuration()) > 0)                        
                // ... and penalize them with a hard weight.
                .penalize("Visibility too short", HardSoftScore.ONE_HARD);
    }

    // Must not assign a visibility for sat X to a contact request for sat Y
    Constraint visibilityForOtherSat(ConstraintFactory constraintFactory) {
        return constraintFactory
                // Select contacts ...
                .from(ContactRequest.class)
                        // ... with visibilities not matching the requested sat 
                        .filter(cr -> cr.getVisibility() != null && !(cr.getSatellite().equals(cr.getVisibility().getSatellite())))
                // ... and penalize them with a hard weight.
                .penalize("Visibility does not match sat", HardSoftScore.ONE_HARD);
    }

    // Can not assign overlapping visibilities for a sat
    Constraint satConflict(ConstraintFactory constraintFactory) {
        return constraintFactory
                // Select each pair of 2 different ContactRequests ...
                .fromUniquePair(ContactRequest.class,
                        // ... in the same satellite                        
                        Joiners.equal(ContactRequest::getSatellite)
                ).filter((cr1,cr2) -> 
                        // ... have a visibility allocated ...
                        cr1.getVisibility() != null && cr2.getVisibility() != null
                        // ... and the visibilities' periods overlap
                        && overlappingPeriods(cr1.getVisibility().getFrom(), cr1.getVisibility().getTo(), cr2.getVisibility().getFrom(), cr2.getVisibility().getTo())
                )
                // ... and penalize each pair with a hard weight.
                .penalize("Satellite conflict", HardSoftScore.ONE_HARD);
    }

    // Can not use the same antenna to stablish more than one contact
    Constraint antennaConflict(ConstraintFactory constraintFactory) {
        return constraintFactory
                /// Select each pair of 2 different ContactRequests ...
                .fromUniquePair(ContactRequest.class)
                // ... select those that ...
                .filter((cr1, cr2) -> 
                        // ... have a visibility allocated ...
                        cr1.getVisibility() != null && cr2.getVisibility() != null
                        // ... and visibilities are on the same antenna ...
                        && cr1.getVisibility().getAntenna().equals(cr2.getVisibility().getAntenna())
                        // ... and the visibilities' periods overlap
                        && overlappingPeriods(cr1.getVisibility().getFrom(), cr1.getVisibility().getTo(), cr2.getVisibility().getFrom(), cr2.getVisibility().getTo())
                )
        // ... and penalize each pair with a hard weight.
        .penalize("Antenna conflict", HardSoftScore.ONE_HARD);
    }

    static boolean overlappingPeriods(Instant fromLeft, Instant toLeft, Instant fromRight, Instant toRight) {
        return fromLeft.isBefore(fromRight) && fromRight.isBefore(toLeft)
                || fromRight.isBefore(fromLeft) && fromLeft.isBefore(toRight);
    }
}
