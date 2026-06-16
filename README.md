# Quest Manager — TaskQuest

**Projet Java B1 · Ynov Campus Rennes · 2025-2026**
Arthur PINEAU

---

## C'est quoi ?

TaskQuest c'est un gestionnaire de tâches sous forme de jeu RPG. L'idée c'est de transformer les trucs qu'on doit faire au quotidien en quêtes. Quand on termine une quête, on gagne de l'XP, on monte de niveau, et on débloque des titres au fil de la progression.

Les titres : **Novice → Apprenti → Développeur → Vétéran → Architecte → Légende**

---

## Fonctionnalités

- Créer des quêtes (Unique ou Quotidienne) avec titre, description et récompense XP
- Afficher et filtrer la liste par statut : À faire / En cours / Terminées
- Passer une quête en "En cours" puis la terminer (l'XP est attribuée automatiquement)
- Supprimer une quête
- Profil joueur : nom, niveau, titre, barre de progression XP, avatar qui change selon le niveau
- Étoiles de difficulté sur chaque quête (basées sur l'XP)
- Les quêtes quotidiennes se réinitialisent automatiquement au prochain lancement du jour suivant
- Sauvegarde automatique en JSON à chaque action, rechargement au démarrage

---

## Comment lancer

### Avec IntelliJ (le plus simple)

1. Ouvrir IntelliJ → File → Open → sélectionner le dossier `Quest_Manager_PINEAU_Arthur`
2. Attendre que Maven télécharge Gson (automatique)
3. Lancer `com.taskquest.Main`

> Le fichier `lib/gson-2.10.1.jar` est déjà inclus dans le projet.

### Avec Maven (si installé)

```bash
mvn clean package
java -jar target/taskquest.jar
```

---

## Architecture

J'ai suivi une architecture MVC avec 5 packages distincts :

```
src/main/java/com/taskquest/
├── Main.java
├── model/         → logique métier pure (Player, Quest, DailyQuest, OneTimeQuest...)
├── controller/    → coordination entre vue et données (QuestController, PlayerController)
├── repository/    → lecture/écriture JSON (QuestRepository, PlayerRepository)
├── view/          → interface Swing (MainWindow, QuestPanel, PlayerPanel, QuestFormDialog)
└── exception/     → exceptions métier personnalisées
```

La règle stricte : **la vue ne touche jamais au repository**. Tout passe par le contrôleur.

---

## Choix techniques

**Interface : Swing**
J'ai choisi Swing parce que c'est intégré au JDK et que je voulais pas rajouter de dépendances inutiles. JavaFX était une option mais la config avec OpenJFX m'a semblé plus risquée pour un premier projet GUI.

**Persistance : JSON avec Gson**
J'ai utilisé Gson (Google) pour la sérialisation. La gestion de la désérialisation polymorphe de la classe abstraite `Quest` était le point le plus technique : j'ai utilisé un champ discriminant `"type"` dans le JSON et une désérialisation manuelle dans le repository.

**Données stockées dans `~/.taskquest/data/`**
Le dossier personnel de l'utilisateur, donc ça marche sur n'importe quelle machine sans configuration.

---

## Dépendances

- **Gson 2.10.1** (Google) — sérialisation JSON
- Java 17 minimum (switch expressions, pattern matching instanceof)
- Swing (inclus dans le JDK)

Aucune autre dépendance.

---

## Sécurité

- Validation de toutes les entrées côté code avant traitement (titre vide, XP hors bornes...)
- Longueur maximale des champs (titre : 100 chars, description : 500)
- XP récompense entre 1 et 1000
- Intégrité du fichier JSON vérifiée au chargement, avec gestion des fichiers corrompus
- Pas de requêtes SQL donc pas d'injection possible (JSON pur)

---

## Ce qui aurait pu être mieux

- Ajouter des animations lors de la montée de niveau (pour l'instant c'est juste un popup)
- Un historique des quêtes terminées avec date
- Pouvoir éditer une quête après création
- Des sons RPG (mais ça aurait ajouté des dépendances)

---

*Ynov Campus Rennes · Bachelor Informatique B1 · S2 2025-2026*
