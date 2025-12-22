package model;

/**
 * Représente une transition du graphe :
 * from --label--> to
 */
public class Transition {

    // Attributs (données de la transition)
    private final String from;
    private final String label;
    private final String to;

    /**
     * Constructeur : crée une transition complète.
     */
    public Transition(String from, String label, String to) {
        this.from = from;
        this.label = label;
        this.to = to;
    }

    // Getters : accès en lecture (sans modifier l'objet)
    public String getFrom() {
        return from;
    }

    public String getLabel() {
        return label;
    }

    public String getTo() {
        return to;
    }

    /**
     * Utile pour afficher la transition facilement (debug).
     */
    @Override
    public String toString() {
        return from + " --'" + label + "'--> " + to;
    }
}

