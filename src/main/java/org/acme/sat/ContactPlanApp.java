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
import java.util.Set;

import org.acme.sat.constraints.ContactPlanConstraintProvider;
import org.acme.sat.domain.ContactPlan;
import org.acme.sat.domain.ContactRequest;
import org.acme.sat.domain.Visibility;
import org.optaplanner.core.api.score.ScoreManager;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
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

        print(solution,t0);
    }

    public static ContactPlan loadData(Instant now ) {
        List<Visibility> visibilities = new ArrayList<>(2);
        visibilities.add(new Visibility("sat1","ls1",now,now.plusSeconds(10)));
        visibilities.add(new Visibility("sat1","ls1",now.plusSeconds(5),now.plusSeconds(14)));
        visibilities.add(new Visibility("sat1","ls1",now.plusSeconds(11),now.plusSeconds(18)));
        visibilities.add(new Visibility("sat2","ls2",now.plusSeconds(10),now.plusSeconds(30)));


        List<ContactRequest> contactRequests = new ArrayList<>();
        long id = 0;
        contactRequests.add(new ContactRequest(id++,"sat1",Duration.of(6, ChronoUnit.SECONDS)));
        contactRequests.add(new ContactRequest(id++,"sat1",Duration.of(4, ChronoUnit.SECONDS)));

        contactRequests.add(new ContactRequest(id++,"sat2",Duration.of(12, ChronoUnit.SECONDS)));

        return new ContactPlan(visibilities, contactRequests);
    }

    private static void print(ContactPlan plan, Instant t0) {        
        LOGGER.info("");
        String cr = System.getProperty("line.separator");
        Set<String> satAlreadySeen = new HashSet<>();
        StringBuilder contactPlan = new StringBuilder("@startuml"+cr);        
        for (ContactRequest r : plan.getContactRequests()) {
            LOGGER.info(r.toString());
            if (!satAlreadySeen.contains(r.getSatellite())) {
                contactPlan.append("concise "+r.getSatellite()+cr);    
                satAlreadySeen.add(r.getSatellite());
            }
            long starts = Duration.between(t0, r.getVisibility().getFrom()).getSeconds();
            contactPlan.append("@"+starts+cr);
            contactPlan.append(r.getSatellite()+" is V"+r.getId()+cr);
            contactPlan.append("@"+(starts+r.getVisibility().getDuration().getSeconds())+cr);
            contactPlan.append(r.getSatellite()+" is {-}"+cr);
        }
        contactPlan.append("@enduml");
        
        try {
            Files.write(Paths.get("contact-plan.pu"), contactPlan.toString().getBytes());
        } catch (IOException e) {
            LOGGER.error("Unable to generate contact plan diagram");
            LOGGER.error(e.getMessage());
        }
    }
}
