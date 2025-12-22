package model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Table des classes de symboles.
 * Exemple : "ch" -> "[0-9]", "let" -> "[a-zA-Z]"
 */
public class ClassTable {

    // defs : nomClasse -> définition
    private final Map<String, String> defs = new LinkedHashMap<>();

    /**
     * Ajoute ou remplace une définition.
     */
    public void put(String name, String def) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Nom de classe vide");
        }
        if (def == null || def.isBlank()) {
            throw new IllegalArgumentException("Définition vide pour la classe: " + name);
        }
        defs.put(name, def.trim());
    }

    /**
     * Vérifie si une classe existe.
     */
    public boolean contains(String name) {
        return defs.containsKey(name);
    }

    /**
     * Récupère une définition si elle existe.
     */
    public Optional<String> get(String name) {
        return Optional.ofNullable(defs.get(name));
    }

    /**
     * Expose une vue non modifiable de la table.
     */
    public Map<String, String> asMap() {
        return Collections.unmodifiableMap(defs);
    }

    /**
     * Nombre de classes enregistrées.
     */
    public int size() {
        return defs.size();
    }

    /**
     * Pour affichage/debug.
     */
    @Override
    public String toString() {
        return defs.toString();
    }
}

