package simulation;

import model.Automaton;
import model.ClassTable;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Simulator : exécute une séquence sur un automate.
 * Retourne true si accepté, false sinon.
 */
public class Simulator {

    /**
     * Simule une séquence d'entrée sur l'automate.
     *
     * @param a        automate
     * @param sequence chaîne entrée (ex: "+36548")
     * @return true si accepté, false si rejeté
     */
    public boolean simulate(Automaton a, String sequence) {
        if (a == null) throw new IllegalArgumentException("Automaton null");
        if (sequence == null) sequence = "";

        // Ensemble des états courants (on commence par EI)
        Set<String> current = new LinkedHashSet<>(a.getInitialStates());

        // Cas séquence vide : accepté seulement si current intersecte EF
        if (sequence.isEmpty()) {
            return intersects(current, a.getFinalStates());
        }

        Map<String, Map<String, Set<String>>> delta = a.getDelta();
        ClassTable ct = a.getClassTable();

        // Lecture caractère par caractère (utile pour ch/let etc.)
        for (int i = 0; i < sequence.length(); i++) {
            char c = sequence.charAt(i);

            // Calcul des nouveaux états atteignables après lecture de c
            Set<String> next = new LinkedHashSet<>();

            for (String state : current) {
                Map<String, Set<String>> byLabel = delta.get(state);
                if (byLabel == null) continue;

                for (var entry : byLabel.entrySet()) {
                    String label = entry.getKey();
                    Set<String> targets = entry.getValue();

                    if (matchesLabel(ct, label, c)) {
                        next.addAll(targets);
                    }
                }
            }

            // Aucun état atteint -> rejet
            if (next.isEmpty()) return false;

            // Mise à jour des états courants
            current = next;
        }

        // Fin de lecture : accepté si un état courant est final
        return intersects(current, a.getFinalStates());
    }

    /* ===================== Helpers ===================== */

    private boolean matchesLabel(ClassTable ct, String label, char c) {
        // 1) Si label est une classe déclarée : regex/definition
        var defOpt = ct.get(label);
        if (defOpt.isPresent()) {
            String def = defOpt.get().trim();

            // Cas def = "'+'" ou "' '" etc. (littéral entre quotes)
            if (def.length() >= 2 && def.startsWith("'") && def.endsWith("'")) {
                String literal = def.substring(1, def.length() - 1);
                return literal.length() == 1 && literal.charAt(0) == c;
            }

            // Sinon on considère def comme une regex (ex: [0-9])
            return String.valueOf(c).matches(def);
        }

        // 2) Sinon : label littéral exact (ex: "+", "-", etc.)
        return label.length() == 1 && label.charAt(0) == c;
    }

    private boolean intersects(Set<String> a, Set<String> b) {
        for (String x : a) {
            if (b.contains(x)) return true;
        }
        return false;
    }
}

