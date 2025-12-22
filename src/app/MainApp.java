package app;

import model.Automaton;
import parser.GraphParser;
import parser.ParseResult;
import simulation.Simulator;
import view.AEFMatrix;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

/**
 * MainApp : point d'entrée console.
 */
public class MainApp {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // 1) Lire le chemin du fichier
        System.out.print("Chemin du fichier automate.txt : ");
        String filePath = sc.nextLine().trim();

        if (filePath.isEmpty()) {
            System.out.println("Erreur : chemin vide.");
            return;
        }

        // 2) Parser
        ParseResult result;
        try {
            Path p = Paths.get(filePath);
            result = GraphParser.parse(p);
        } catch (Exception e) {
            System.out.println("Erreur lors du parsing : " + e.getMessage());
            return;
        }

        Automaton automaton = result.getAutomaton();

        // 3) Afficher warnings
        List<String> warnings = result.getWarnings();
        if (!warnings.isEmpty()) {
            System.out.println("\n--- WARNINGS ---");
            for (String w : warnings) {
                System.out.println("- " + w);
            }
        }

        // 4) Afficher l'automate (optionnel)
        System.out.println("\n--- AUTOMATE CHARGE ---");
        System.out.println("E  = " + automaton.getStates());
        System.out.println("EI = " + automaton.getInitialStates());
        System.out.println("EF = " + automaton.getFinalStates());
        System.out.println("Classes = " + automaton.getClassTable().asMap());

        // 5) Construire & afficher matrice AEF
        System.out.println("\n--- MATRICE AEF ---");
        AEFMatrix matrix = new AEFMatrix();
        matrix.build(automaton);
        matrix.print();

        // 6) Lire la séquence
        System.out.print("\nSequence a analyser : ");
        String sequence = sc.nextLine(); // peut être vide

        // 7) Simulation
        Simulator simulator = new Simulator();
        boolean accepted;
        try {
            accepted = simulator.simulate(automaton, sequence);
        } catch (Exception e) {
            System.out.println("Erreur pendant la simulation : " + e.getMessage());
            return;
        }

        // 8) Verdict
        System.out.println("\n--- VERDICT ---");
        System.out.println(accepted ? "ACCEPTE" : "REJETE");
    }
}

