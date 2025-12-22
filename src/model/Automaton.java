package model;

import java.util.*;

/**
 * Représente un automate fini :
 * E, EI, EF, delta, et la table des classes.
 */
public class Automaton {

    // Ensembles d'états
    private final Set<String> states = new LinkedHashSet<>();
    private final Set<String> initialStates = new LinkedHashSet<>();
    private final Set<String> finalStates = new LinkedHashSet<>();

    /**
     * delta[from][label] = {to1, to2, ...}
     */
    private final Map<String, Map<String, Set<String>>> delta = new LinkedHashMap<>();

    // Table des classes (peut être vide mais jamais null)
    private final ClassTable classTable;

    /**
     * Constructeur : on force un ClassTable non-null.
     * Si l'utilisateur ne définit pas de classes, on passe une table vide.
     */
    public Automaton(ClassTable classTable) {
        this.classTable = (classTable == null) ? new ClassTable() : classTable;
    }

    /* =========================
     *  Méthodes d'ajout
     * ========================= */

    public void addState(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Nom d'état vide");
        }
        states.add(name.trim());
    }

    public void addInitial(String name) {
        addState(name);
        initialStates.add(name.trim());
    }

    public void addFinal(String name) {
        addState(name);
        finalStates.add(name.trim());
    }

    public void addTransition(String from, String label, String to) {
        if (from == null || from.isBlank()) throw new IllegalArgumentException("Etat source vide");
        if (label == null || label.isBlank()) throw new IllegalArgumentException("Label vide");
        if (to == null || to.isBlank()) throw new IllegalArgumentException("Etat destination vide");

        from = from.trim();
        label = label.trim();
        to = to.trim();

        // Les états doivent exister dans E : on les ajoute pour éviter incohérence pendant le parsing,
        // puis validateBasics() vérifiera si tu veux être strict.
        addState(from);
        addState(to);

        delta.computeIfAbsent(from, k -> new LinkedHashMap<>())
             .computeIfAbsent(label, k -> new LinkedHashSet<>())
             .add(to);
    }

    /* =========================
     *  Lecture (getters)
     * ========================= */

    public Set<String> getStates() {
        return Collections.unmodifiableSet(states);
    }

    public Set<String> getInitialStates() {
        return Collections.unmodifiableSet(initialStates);
    }

    public Set<String> getFinalStates() {
        return Collections.unmodifiableSet(finalStates);
    }

    public Map<String, Map<String, Set<String>>> getDelta() {
        return Collections.unmodifiableMap(delta);
    }

    public ClassTable getClassTable() {
        return classTable;
    }

    /**
     * Alphabet (ensemble des labels) extrait des transitions.
     * On le construit à partir de delta, donc l'utilisateur n'a pas besoin de déclarer T.
     */
    public List<String> alphabet() {
        LinkedHashSet<String> labels = new LinkedHashSet<>();
        for (Map<String, Set<String>> byLabel : delta.values()) {
            labels.addAll(byLabel.keySet());
        }
        return new ArrayList<>(labels);
    }

    /* =========================
     *  Validation de base
     * ========================= */

    public void validateBasics() {
        if (states.isEmpty()) throw new IllegalStateException("E vide");
        if (initialStates.isEmpty()) throw new IllegalStateException("EI vide");
        if (finalStates.isEmpty()) throw new IllegalStateException("EF vide");

        // EI ⊆ E, EF ⊆ E : déjà garanti car addInitial/addFinal appelle addState.
        // Ici on vérifie surtout les transitions.
        for (var e : delta.entrySet()) {
            String from = e.getKey();
            if (!states.contains(from)) {
                throw new IllegalStateException("Transition depuis état inconnu: " + from);
            }
            for (var labTo : e.getValue().entrySet()) {
                for (String to : labTo.getValue()) {
                    if (!states.contains(to)) {
                        throw new IllegalStateException("Transition vers état inconnu: " + to);
                    }
                }
            }
        }
    }

    @Override
    public String toString() {
        return "Automaton{E=" + states +
               ", EI=" + initialStates +
               ", EF=" + finalStates +
               ", classes=" + classTable +
               ", delta=" + delta + "}";
    }
}

