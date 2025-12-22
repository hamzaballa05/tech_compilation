package parser;

import java.util.Collections;
import java.util.List;
import model.Automaton;

/**
 * Résultat du parsing :
 * - l'automate construit
 * - une liste de warnings (problèmes non bloquants)
 */
public class ParseResult {

    private final Automaton automaton;
    private final List<String> warnings;

    public ParseResult(Automaton automaton, List<String> warnings) {
        if (automaton == null) {
            throw new IllegalArgumentException("Automaton null");
        }
        this.automaton = automaton;
        this.warnings = (warnings == null) ? List.of() : List.copyOf(warnings);
    }

    public Automaton getAutomaton() {
        return automaton;
    }

    public List<String> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }

    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }
}

