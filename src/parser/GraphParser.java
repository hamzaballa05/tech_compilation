package parser;

import model.Automaton;
import model.ClassTable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * GraphParser : lit un fichier automate.txt et construit un Automaton.
 */
public class GraphParser {

    // E = { ... } , EI = { ... } , EF = { ... }
    private static final Pattern SET_LINE =
            Pattern.compile("^\\s*(E|EI|EF)\\s*=\\s*\\{([^}]*)}\\s*$");

    // Headers
    private static final Pattern CLASSES_HEADER =
            Pattern.compile("^\\s*Classes\\s*=\\s*$", Pattern.CASE_INSENSITIVE);

    private static final Pattern TRANSITIONS_HEADER =
            Pattern.compile("^\\s*Transitions\\s*=\\s*$", Pattern.CASE_INSENSITIVE);

    // class line : name = def ;
    private static final Pattern CLASS_DEF =
            Pattern.compile("^\\s*([A-Za-z][A-Za-z0-9_]*)\\s*=\\s*(.+?)\\s*;\\s*$");

    // transition : A -> 'label' B ;
    private static final Pattern ARC =
            Pattern.compile("^\\s*([A-Za-z][A-Za-z0-9_]*)\\s*->\\s*'([^']+)'\\s*([A-Za-z][A-Za-z0-9_]*)\\s*;\\s*$");

    /**
     * Parse le fichier et retourne (Automaton + warnings).
     */
    public static ParseResult parse(Path path) throws IOException {
        List<String> lines = Files.readAllLines(path);

        // 1) Structures temporaires
        Set<String> E = new LinkedHashSet<>();
        Set<String> EI = new LinkedHashSet<>();
        Set<String> EF = new LinkedHashSet<>();
        ClassTable classTable = new ClassTable();
        List<String> warnings = new ArrayList<>();

        // Pour warnings : classes utilisées / labels utilisés
        Set<String> usedLabels = new LinkedHashSet<>();

        boolean inClasses = false;
        boolean inTransitions = false;

        // Buffers pour supporter des définitions multi-lignes jusqu'au ';'
        StringBuilder classBuf = new StringBuilder();
        StringBuilder transBuf = new StringBuilder();

        // 2) Lecture ligne par ligne
        for (String raw : lines) {
            String line = stripComments(raw).trim();
            if (line.isEmpty()) continue;

            // Phase avant les blocs
            if (!inClasses && !inTransitions) {

                if (CLASSES_HEADER.matcher(line).matches()) {
                    inClasses = true;
                    continue;
                }
                if (TRANSITIONS_HEADER.matcher(line).matches()) {
                    inTransitions = true;
                    continue;
                }

                // E/EI/EF
                Matcher m = SET_LINE.matcher(line);
                if (m.matches()) {
                    String tag = m.group(1);
                    String body = m.group(2);
                    Set<String> target = switch (tag) {
                        case "E" -> E;
                        case "EI" -> EI;
                        case "EF" -> EF;
                        default -> throw new IllegalArgumentException("Tag inconnu: " + tag);
                    };
                    extractSet(body, target);
                    continue;
                }

                // Ligne inconnue avant blocs : warning
                warnings.add("Ligne ignorée (hors blocs) : " + raw.trim());
                continue;
            }

            // Bloc Classes
            if (inClasses && !inTransitions) {
                // Si on rencontre "Transitions =" on bascule
                if (TRANSITIONS_HEADER.matcher(line).matches()) {
                    flushClassBuffer(classBuf, classTable, warnings);
                    inTransitions = true;
                    continue;
                }

                classBuf.append(' ').append(line).append(' ');
                flushClassBuffer(classBuf, classTable, warnings);
                continue;
            }

            // Bloc Transitions
            if (inTransitions) {
                transBuf.append(' ').append(line).append(' ');
                flushTransitionBuffer(transBuf, usedLabels, E, warnings);
            }
        }

        // 3) Gardes-fous : buffers non terminés
        if (classBuf.toString().trim().length() > 0) {
            throw new IllegalArgumentException("Définition de classe incomplète (manque ';') : " + classBuf);
        }
        if (transBuf.toString().trim().length() > 0) {
            throw new IllegalArgumentException("Transition incomplète (manque ';') : " + transBuf);
        }

        // 4) Construire Automaton
        Automaton A = new Automaton(classTable);

        // Ajouter les états déclarés
        for (String s : E) A.addState(s);
        for (String s : EI) A.addInitial(s);
        for (String s : EF) A.addFinal(s);

        // 5) Vérifications fortes (erreurs)
        if (E.isEmpty()) throw new IllegalArgumentException("E est vide");
        if (EI.isEmpty()) throw new IllegalArgumentException("EI est vide");
        if (EF.isEmpty()) throw new IllegalArgumentException("EF est vide");

        // EI ⊆ E et EF ⊆ E (ici on impose strict)
        for (String s : EI) if (!E.contains(s)) {
            throw new IllegalArgumentException("EI contient un état non déclaré dans E : " + s);
        }
        for (String s : EF) if (!E.contains(s)) {
            throw new IllegalArgumentException("EF contient un état non déclaré dans E : " + s);
        }

        // 6) Ajouter les transitions (on relit le fichier une 2e fois ? non : on a déjà validé syntaxe)
        // Astuce : on relit et re-parse uniquement les transitions, mais cette fois on les ajoute à l'automate.
        // On aurait pu les stocker temporairement, mais on garde simple et clair.

        inClasses = false;
        inTransitions = false;
        transBuf.setLength(0);

        for (String raw : lines) {
            String line = stripComments(raw).trim();
            if (line.isEmpty()) continue;

            if (!inClasses && !inTransitions) {
                if (CLASSES_HEADER.matcher(line).matches()) { inClasses = true; continue; }
                if (TRANSITIONS_HEADER.matcher(line).matches()) { inTransitions = true; continue; }
                continue;
            }

            if (inClasses && !inTransitions) {
                if (TRANSITIONS_HEADER.matcher(line).matches()) { inTransitions = true; continue; }
                continue;
            }

            if (inTransitions) {
                transBuf.append(' ').append(line).append(' ');
                int idx;
                while ((idx = transBuf.indexOf(";")) >= 0) {
                    String one = transBuf.substring(0, idx + 1).trim();
                    transBuf.delete(0, idx + 1);

                    Matcher mm = ARC.matcher(one);
                    if (!mm.matches()) {
                        throw new IllegalArgumentException("Transition invalide: " + one);
                    }
                    String from = mm.group(1);
                    String label = mm.group(2);
                    String to = mm.group(3);

                    // Ici on impose strict : from/to doivent être dans E
                    if (!E.contains(from)) throw new IllegalArgumentException("Transition depuis état hors E: " + from);
                    if (!E.contains(to)) throw new IllegalArgumentException("Transition vers état hors E: " + to);

                    A.addTransition(from, label, to);
                }
            }
        }

        // 7) Validation interne de l'automate (cohérence)
        A.validateBasics();

        // 8) Warnings supplémentaires : labels utilisés non définis dans Classes (interprétés comme littéraux)
        for (String lab : usedLabels) {
            if (!classTable.contains(lab)) {
                warnings.add("Label utilisé sans définition dans Classes (interprété comme littéral) : '" + lab + "'");
            }
        }

        // Warnings : classes définies mais jamais utilisées
        for (String cls : classTable.asMap().keySet()) {
            if (!usedLabels.contains(cls)) {
                warnings.add("Classe définie mais jamais utilisée dans Transitions : " + cls);
            }
        }

        return new ParseResult(A, warnings);
    }

    /* ===================== Helpers ===================== */

    private static void extractSet(String body, Set<String> out) {
        for (String tok : body.split(",")) {
            String t = tok.trim();
            if (!t.isEmpty()) out.add(t);
        }
    }

    private static void flushClassBuffer(StringBuilder buf, ClassTable classTable, List<String> warnings) {
        int idx;
        while ((idx = buf.indexOf(";")) >= 0) {
            String one = buf.substring(0, idx + 1).trim();
            buf.delete(0, idx + 1);
            if (one.isBlank()) continue;

            Matcher m = CLASS_DEF.matcher(one);
            if (!m.matches()) {
                warnings.add("Définition de classe ignorée (format invalide) : " + one);
                continue;
            }
            String name = m.group(1);
            String def = m.group(2);
            classTable.put(name, def);
        }
    }

    private static void flushTransitionBuffer(StringBuilder buf,
                                              Set<String> usedLabels,
                                              Set<String> E,
                                              List<String> warnings) {

        int idx;
        while ((idx = buf.indexOf(";")) >= 0) {
            String one = buf.substring(0, idx + 1).trim();
            buf.delete(0, idx + 1);
            if (one.isBlank()) continue;

            Matcher m = ARC.matcher(one);
            if (!m.matches()) {
                throw new IllegalArgumentException("Transition invalide: " + one);
            }
            String from = m.group(1);
            String label = m.group(2);
            String to = m.group(3);

            usedLabels.add(label);

            // Ici on ne jette pas encore si from/to hors E, car E peut être déclaré plus haut.
            // Mais si E est déjà connu, on peut avertir.
            if (!E.isEmpty()) {
                if (!E.contains(from)) warnings.add("Transition depuis état non déclaré dans E : " + from);
                if (!E.contains(to)) warnings.add("Transition vers état non déclaré dans E : " + to);
            }
        }
    }

    private static String stripComments(String s) {
        int i = s.indexOf("//");
        return (i >= 0) ? s.substring(0, i) : s;
    }
}

