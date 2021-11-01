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
    private static String lb = System.getProperty("line.separator");

    public static void main(String[] args) {
        SolverFactory<ContactPlan> solverFactory = SolverFactory.create(new SolverConfig()
                .withSolutionClass(ContactPlan.class)
                .withEntityClasses(ContactRequest.class)
                .withConstraintProviderClass(ContactPlanConstraintProvider.class)
                .withTerminationSpentLimit(Duration.ofSeconds(10)));

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
        generateContactRequestCSV(solution);
    }

    private static void generateContactRequestCSV(ContactPlan solution) {
        List<ContactRequest> contacts = solution.getContactRequests();
        StringBuilder content = new StringBuilder(ContactRequest.getCSVHeader()+lb);                
        for (ContactRequest cr : contacts) {            
            content.append(cr.toCSV()+lb);
        }
        saveToFile("planned-contacts.csv", content);
    }

    public static ContactPlan loadData(Instant t0 ) {
        List<Visibility> visibilities = new ArrayList<>(2);
        visibilities.add(new Visibility("sat1","ls1",t0.plusSeconds(1),t0.plusSeconds(6)));
        visibilities.add(new Visibility("sat1","ls2",t0.plusSeconds(10),t0.plusSeconds(16)));
        visibilities.add(new Visibility("sat1","ls3",t0.plusSeconds(18),t0.plusSeconds(26)));
        visibilities.add(new Visibility("sat1","ls2",t0.plusSeconds(30),t0.plusSeconds(38)));

        visibilities.add(new Visibility("sat2","ls1",t0.plusSeconds(3),t0.plusSeconds(9)));
        visibilities.add(new Visibility("sat2","ls3",t0.plusSeconds(11),t0.plusSeconds(13)));
        visibilities.add(new Visibility("sat2","ls2",t0.plusSeconds(19),t0.plusSeconds(23)));
        visibilities.add(new Visibility("sat2","ls1",t0.plusSeconds(25),t0.plusSeconds(29)));

        visibilities.add(new Visibility("sat3","ls2",t0.plusSeconds(0),t0.plusSeconds(5)));
        visibilities.add(new Visibility("sat3","ls1",t0.plusSeconds(10),t0.plusSeconds(18)));
        visibilities.add(new Visibility("sat3","ls3",t0.plusSeconds(22),t0.plusSeconds(27)));

        print(visibilities, t0);

        List<ContactRequest> contactRequests = new ArrayList<>();
        contactRequests.add(new ContactRequest("sat1",Duration.of(6, ChronoUnit.SECONDS)));
        contactRequests.add(new ContactRequest("sat1",Duration.of(6, ChronoUnit.SECONDS)));
        contactRequests.add(new ContactRequest("sat1",Duration.of(6, ChronoUnit.SECONDS)));

        contactRequests.add(new ContactRequest("sat2",Duration.of(3, ChronoUnit.SECONDS)));
        contactRequests.add(new ContactRequest("sat2",Duration.of(4, ChronoUnit.SECONDS)));
        contactRequests.add(new ContactRequest("sat2",Duration.of(4, ChronoUnit.SECONDS)));

        contactRequests.add(new ContactRequest("sat3",Duration.of(5, ChronoUnit.SECONDS)));
        contactRequests.add(new ContactRequest("sat3",Duration.of(6, ChronoUnit.SECONDS)));

        return new ContactPlan(visibilities, contactRequests);
    }

    private static void print(List<Visibility> visibilities, Instant t0) {        
        StringBuilder visibilitiesPU = new StringBuilder("@startuml"+lb);                
        Set<String> satAlreadySeen = new HashSet<>();
        for (Visibility v : visibilities) {
            String satellite = v.getSatellite();
            String antenna = v.getAntenna();
            if (!satAlreadySeen.contains(satellite)) {
                visibilitiesPU.append("concise "+satellite+lb);    
                satAlreadySeen.add(satellite);
            }
            long starts = Duration.between(t0, v.getFrom()).getSeconds();
            visibilitiesPU.append("@"+starts+lb);
            visibilitiesPU.append(satellite+" is \"visible @"+antenna+"\""+lb);
            visibilitiesPU.append("@"+(starts+v.getDuration().getSeconds())+lb);
            visibilitiesPU.append(satellite+" is {-}"+lb);
        }

        visibilitiesPU.append("@enduml");
        saveToFile("visibilities.pu", visibilitiesPU);
    }

    private static void print(ContactPlan plan, ScoreManager<ContactPlan, HardSoftScore> scoreManager, Instant t0) {        
        Set<String> satAlreadySeen = new HashSet<>();
        StringBuilder contactPlanPU = new StringBuilder("@startuml"+lb);                
        Map<Object,Indictment<HardSoftScore>> indictments = scoreManager.explainScore(plan).getIndictmentMap();
        for (ContactRequest r : plan.getContactRequests()) {
            String satellite = r.getSatellite();
            if (!satAlreadySeen.contains(satellite)) {
                contactPlanPU.append("concise "+satellite+lb);    
                satAlreadySeen.add(satellite);
            }
            Indictment<HardSoftScore> crIndictment = indictments.get(r);            
            Visibility visibility = r.getVisibility();
            long starts = Duration.between(t0, visibility.getFrom()).getSeconds();
            contactPlanPU.append("@"+starts+lb);
            String color = "#lightgreen";
            String contactId = "contact "+r.getId();
            if (crIndictment != null && crIndictment.getScore().getHardScore() < 0) {    
                contactPlanPU.append("note top of "+satellite+" : "+getConstraintsFromIndictement(crIndictment.getConstraintMatchSet())+lb);
                color = "#red";
                contactId = "contact ?";
            }
            contactPlanPU.append(satellite+" is \""+contactId+"\" "+color+lb);
            contactPlanPU.append("@"+(starts+visibility.getDuration().getSeconds())+lb);
            contactPlanPU.append(satellite+" is {-}"+lb);
        }
        contactPlanPU.append("@enduml");
        saveToFile("contact-plan.pu", contactPlanPU);
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

    private static void saveToFile(String filename, StringBuilder content){
        try {
            Files.write(Paths.get(filename), content.toString().getBytes());
        } catch (IOException e) {
            LOGGER.error("Unable to generate "+filename);
            LOGGER.error(e.getMessage());
        }
    }
}
