# Ciel Android Agent

Petite appli Android qui remplace MacroDroid pour les **minuteurs** et **alarmes** pilotés par Ciel (Hermes).
Elle ouvre un serveur HTTP local (port 8081) protégé par un jeton, et commande l'appli Horloge du téléphone.

## Installation
1. Télécharger `app-debug.apk` : onglet **Actions** > dernier build vert > section **Artifacts**.
2. L'installer, l'ouvrir, puis :
   - « Démarrer le serveur » (une notification permanente apparaît) ;
   - « Autoriser la superposition » (obligatoire : sans ça Android bloque l'Horloge quand l'appli est en arrière-plan) ;
   - « Ne pas optimiser la batterie » ;
   - « Copier adresse + jeton » pour configurer Ciel.
3. « Tester : minuteur 10 s » vérifie que l'Horloge répond.

## API
Jeton : en-tête `Authorization: Bearer <jeton>` **ou** paramètre `token=<jeton>`.

| Route | Paramètres | Exemple |
|---|---|---|
| `GET /status` | aucun (pas de jeton) | `/status` |
| `GET /minuteur` | `secondes`, `minutes`, `heures` (additionnés, 1 s à 24 h), `message` | `/minuteur?minutes=10&message=Pates` |
| `GET /alarme` | `heure` (0-23), `minute` (0-59), `message` | `/alarme?heure=7&minute=30` |

Réponse JSON : `{"ok": true|false, "message": "..."}`. Codes : 200, 400 (paramètres), 401 (jeton), 404 (route), 500 (Horloge).
Les noms de routes/paramètres sont insensibles à la casse (`/MINUTEUR?SECONDES=60` marche aussi, comme les macros MacroDroid).

## Build
- Versions figées ensemble dans `gradle/libs.versions.toml` : AGP 8.7.3, Kotlin 2.1.0 (+ plugin compose), Gradle 8.10 (wrapper), JDK 17, compileSdk 35.
- Local (Windows) : `gradlew.bat testDebugUnitTest assembleDebug` avec `JAVA_HOME` = un JDK 17 ou 21 et `local.properties` contenant `sdk.dir`.
- CI : `.github/workflows/android.yml` lance les tests puis publie l'APK à chaque push sur `main`.

## Code
- `CielCommandProcessor.kt` : routes, validation, jeton (pur Kotlin, testé).
- `MiniServeurHttp.kt` : serveur HTTP minimal sans dépendance (testé en vrai sur un port libre).
- `CommandServerService.kt` : service au premier plan qui héberge le serveur.
- `Horloge.kt` : intents `SET_TIMER` / `SET_ALARM` de l'Horloge.
- `Reglages.kt` : jeton aléatoire, port, relance au démarrage (`DemarrageReceiver.kt`).
