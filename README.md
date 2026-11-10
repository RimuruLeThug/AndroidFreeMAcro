# Structure du projet Ciel Android

Voici la structure de base du projet Android générée.

## Fichiers de configuration (Racine)
- `build.gradle.kts` : Configuration globale.
- `settings.gradle.kts` : Modules.
- `.github/workflows/android.yml` : Pipeline CI/CD pour générer l'APK automatiquement.

## Code source (`app/src/main/java/com/ciel/app/`)
- `MainActivity.kt` : Point d'entrée de l'application (Compose).
- `CommandServerService.kt` : Serveur Ktor (Foreground Service).
- `AlarmManager.kt` : Logique de gestion des alarmes/minuteurs.
- `CielDashboard.kt` : Interface UI.

## Ressources
- `AndroidManifest.xml` : Permissions et déclarations de services.
