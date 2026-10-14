# tech_compilation : automate fini (Java)

Projet Java de **lecture et de simulation d'un automate fini**, réalisé pendant la formation d'ingénieur (cours de compilation).

## Contenu

- `src/parser/` : lecture de la description d'un automate depuis un fichier (`GraphParser`, `ParseResult`)
- `src/model/` : modèle de l'automate (`Automaton`, `Transition`, `ClassTable`)
- `src/simulation/` : simulation (`Simulator`)
- `src/view/` : affichage sous forme de matrice (`AEFMatrix`)
- `src/app/MainApp.java` : point d'entrée
- `data/automate.txt` : exemple d'automate

## Exécution

Prérequis : JDK 21 (version configurée dans le projet).

```bash
javac -d bin $(find src -name "*.java")
java -cp bin app.MainApp
```

## Auteur

**Hamza Balla** : étudiant ingénieur à l'INSEA (Data and Software Engineering).
