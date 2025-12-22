package view;

import model.Automaton;

import java.util.*;

/**
 * AEFMatrix : représentation tabulaire de l'automate.
 * Lignes = états, colonnes = symboles (labels), cellules = ensemble des états atteignables.
 */
public class AEFMatrix {

    private List<String> states;   // ordre stable pour l'affichage
    private List<String> symbols;  // alphabet (labels)
    private String[][] matrix;     // cellule texte (ex: "{B,C}" ou "-")

    public void build(Automaton a) {
        // 1) états
        states = new ArrayList<>(a.getStates());

        // 2) symboles = alphabet déduit des transitions
        symbols = new ArrayList<>(a.alphabet());

        // 3) init matrice
        matrix = new String[states.size()][symbols.size()];
        for (int i = 0; i < states.size(); i++) {
            Arrays.fill(matrix[i], "-");
        }

        // 4) remplir à partir de delta
        // delta : Map<String, Map<String, Set<String>>>
        var delta = a.getDelta();

        for (int i = 0; i < states.size(); i++) {
            String from = states.get(i);
            Map<String, Set<String>> byLabel = delta.get(from);
            if (byLabel == null) continue;

            for (int j = 0; j < symbols.size(); j++) {
                String label = symbols.get(j);
                Set<String> targets = byLabel.get(label);
                if (targets == null || targets.isEmpty()) continue;

                matrix[i][j] = toSetString(targets);
            }
        }
    }

    public void print() {
        if (states == null || symbols == null || matrix == null) {
            System.out.println("(AEFMatrix vide) appelez build() avant print().");
            return;
        }

        // header
        System.out.printf("%-8s", "");
        for (String sym : symbols) {
            System.out.printf("%-12s", sym);
        }
        System.out.println();

        // rows
        for (int i = 0; i < states.size(); i++) {
            System.out.printf("%-8s", states.get(i));
            for (int j = 0; j < symbols.size(); j++) {
                System.out.printf("%-12s", matrix[i][j]);
            }
            System.out.println();
        }
    }

    private String toSetString(Set<String> set) {
        // affichage stable
        List<String> list = new ArrayList<>(set);
        Collections.sort(list);
        return "{" + String.join(",", list) + "}";
    }
}
