package org.acme.sat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.acme.sat.constraints.ContactPlanConstraintProvider;
import org.acme.sat.domain.ContactPlan;
import org.acme.sat.domain.ContactRequest;
import org.acme.sat.domain.Visibility;
import org.optaplanner.core.api.score.ScoreManager;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.constraint.ConstraintMatch;
import org.optaplanner.core.api.score.constraint.Indictment;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.solver.SolverConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContactPlanApp {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ContactPlanApp.class);

    public static void main(String[] args) {
        SolverFactory<ContactPlan> solverFactory = SolverFactory.create(new SolverConfig()
                .withSolutionClass(ContactPlan.class)
                .withEntityClasses(ContactRequest.class)
                .withConstraintProviderClass(ContactPlanConstraintProvider.class)
                // The solver runs only for 5 seconds on this small dataset.
                // It's recommended to run for at least 5 minutes ("5m") otherwise.
                .withTerminationSpentLimit(Duration.ofSeconds(5)));

        ScoreManager<ContactPlan, HardSoftScore> scoreManager = ScoreManager.create(solverFactory);

        Instant t0 = Instant.now();
        // Load the problem
        ContactPlan problem = loadData(t0);

        // Solve the problem
        Solver<ContactPlan> solver = solverFactory.buildSolver();
        ContactPlan solution = solver.solve(problem);

        if (solution.getScore().isFeasible()) {
            LOGGER.info("SOLUTION FOUND");    
        } else {
            LOGGER.info("UNABLE TO FIND A SOLUTION");    
            LOGGER.info(scoreManager.explainScore(solution).getSummary());
        }

        print(solution, scoreManager, t0);
    }

    public static ContactPlan loadData(Instant now ) {
        List<Visibility> visibilities = new ArrayList<>(2);
        visibilities.add(new Visibility("sat1","ls1",now,now.plusSeconds(10)));
        visibilities.add(new Visibility("sat1","ls1",now.plusSeconds(5),now.plusSeconds(14)));
        visibilities.add(new Visibility("sat1","ls1",now.plusSeconds(11),now.plusSeconds(18)));
        visibilities.add(new Visibility("sat2","ls2",now.plusSeconds(10),now.plusSeconds(30)));
        //visibilities.add(new Visibility("sat2","ls2",now.plusSeconds(0),now.plusSeconds(8)));

        List<ContactRequest> contactRequests = new ArrayList<>();
        long id = 0;
        contactRequests.add(new ContactRequest(id++,"sat1",Duration.of(6, ChronoUnit.SECONDS)));
        contactRequests.add(new ContactRequest(id++,"sat1",Duration.of(4, ChronoUnit.SECONDS)));

        contactRequests.add(new ContactRequest(id++,"sat2",Duration.of(12, ChronoUnit.SECONDS)));
        contactRequests.add(new ContactRequest(id++,"sat2",Duration.of(6, ChronoUnit.SECONDS)));

        return new ContactPlan(visibilities, contactRequests);
    }

    private static void print(ContactPlan plan, ScoreManager<ContactPlan, HardSoftScore> scoreManager, Instant t0) {        
        LOGGER.info("");        
        String lb = System.getProperty("line.separator");
        Set<String> satAlreadySeen = new HashSet<>();
        StringBuilder contactPlan = new StringBuilder("@startuml"+lb);                
        Map<Object,Indictment<HardSoftScore>> indictments = scoreManager.explainScore(plan).getIndictmentMap();
        for (ContactRequest r : plan.getContactRequests()) {
            LOGGER.info(r.toString());            
            String satellite = r.getSatellite();
            if (!satAlreadySeen.contains(satellite)) {
                contactPlan.append("concise "+satellite+lb);    
                satAlreadySeen.add(satellite);
            }
            Indictment<HardSoftScore> crIndictment = indictments.get(r);            
            Visibility visibility = r.getVisibility();
            long starts = Duration.between(t0, visibility.getFrom()).getSeconds();
            contactPlan.append("@"+starts+lb);
            String color = "#lightgreen";
            String contactId = "contact "+r.getId();
            if (crIndictment != null && crIndictment.getScore().getHardScore() < 0) {    
                contactPlan.append("note top of "+satellite+" : "+getConstraintsFromIndictement(crIndictment.getConstraintMatchSet())+lb);
                color = "#red";
                contactId = "contact ?";
            }
            contactPlan.append(satellite+" is \""+contactId+"\" "+color+lb);
            contactPlan.append("@"+(starts+visibility.getDuration().getSeconds())+lb);
            contactPlan.append(satellite+" is {-}"+lb);
        }
        contactPlan.append("@enduml");
        
        try {
            Files.write(Paths.get("contact-plan.pu"), contactPlan.toString().getBytes());
        } catch (IOException e) {
            LOGGER.error("Unable to generate contact plan diagram");
            LOGGER.error(e.getMessage());
        }
    }

    private static String getConstraintsFromIndictement(Set<ConstraintMatch<HardSoftScore>> constraintMatchSet) {
        StringBuilder result = new StringBuilder();
        for (ConstraintMatch<HardSoftScore> constraintMatch : constraintMatchSet) {
            result.append(constraintMatch.getConstraintName()+"\\n");
            for (Object justification : constraintMatch.getJustificationList()) {
                result.append("\\t"+justification.toString()+"\\n");
            }
        }

        return result.toString();
    }
}
