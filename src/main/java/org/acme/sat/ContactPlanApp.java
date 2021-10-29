package org.acme.sat;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.acme.sat.constraints.ContactPlanConstraintProvider;
import org.acme.sat.domain.ContactPlan;
import org.acme.sat.domain.ContactRequest;
import org.acme.sat.domain.Visibility;
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

        // Load the problem
        ContactPlan problem = loadData();

        // Solve the problem
        Solver<ContactPlan> solver = solverFactory.buildSolver();
        ContactPlan solution = solver.solve(problem);

        print(solution);

    }

    public static ContactPlan loadData() {
        Instant now = Instant.now();
        List<Visibility> visibilities = new ArrayList<>(2);
        visibilities.add(new Visibility("sat1","ls1",now,now.plusSeconds(10)));
        visibilities.add(new Visibility("sat1","ls1",now.plusSeconds(5),now.plusSeconds(14)));
        visibilities.add(new Visibility("sat2","ls1",now.plusSeconds(4),now.plusSeconds(18)));
        visibilities.add(new Visibility("sat2","ls2",now.plusSeconds(10),now.plusSeconds(30)));


        List<ContactRequest> contactRequests = new ArrayList<>();
        long id = 0;
        contactRequests.add(new ContactRequest(id++,"sat1",Duration.of(6, ChronoUnit.SECONDS)));
        contactRequests.add(new ContactRequest(id++,"sat1",Duration.of(4, ChronoUnit.SECONDS)));

        contactRequests.add(new ContactRequest(id++,"sat2",Duration.of(12, ChronoUnit.SECONDS)));

        return new ContactPlan(visibilities, contactRequests);
    }

    private static void print(ContactPlan plan) {
        LOGGER.info("");
        
        for (ContactRequest r : plan.getContactRequests()) {
            LOGGER.info(r.toString());
        }
    }
}
