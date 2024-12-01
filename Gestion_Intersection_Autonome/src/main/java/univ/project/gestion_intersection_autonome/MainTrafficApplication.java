package univ.project.gestion_intersection_autonome;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import univ.project.gestion_intersection_autonome.classes.*;
import univ.project.gestion_intersection_autonome.controllers.TerrainController;
import univ.project.gestion_intersection_autonome.controllers.VehiculeController;

import java.io.IOException;

/**
 * Classe principale de l'application JavaFX pour la gestion d'intersections autonomes.
 * Cette classe initialise l'interface graphique et lance la simulation.
 */
public class MainTrafficApplication extends Application {

    /**
     * Point d'entrée de l'application JavaFX. Cette méthode est appelée lors du démarrage de l'application.
     * Elle charge l'interface utilisateur, initiale la simulation et démarre le processus de simulation.
     *
     * @param stage Le stage JavaFX (fenêtre) dans lequel l'application sera affichée.
     * @throws IOException Si le fichier FXML ne peut pas être chargé correctement.
     */
    @Override
    public void start(Stage stage) throws IOException {

        // Création de l'instance de la simulation
        Simulation simulation = new Simulation();
        Terrain terrain = simulation.getTerrain();

        // Chargement du fichier FXML pour l'interface graphique de la simulation (terrain)
        FXMLLoader fxmlLoader = new FXMLLoader(MainTrafficApplication.class.getResource("Terrain.fxml"));

        // Définition du contrôleur à utiliser pour la vue
        fxmlLoader.setControllerFactory(obj -> new TerrainController(simulation));

        // Chargement du fichier FXML et création de l'interface
        Parent root = fxmlLoader.load();

        // Récupération du contrôleur TerrainController de l'interface chargée
        TerrainController terrainController = fxmlLoader.getController();

        // (Optionnel) Passer l'instance de TerrainController à la simulation si nécessaire
        simulation.setTerrainController(terrainController);

        // Création de la scène JavaFX en utilisant le root (contenu de l'interface)
        Scene scene = new Scene(root, 610, 610);

        // Définition du titre de la fenêtre
        stage.setTitle("Mon Terrain");

        // Ajout de la scène à la fenêtre
        stage.setScene(scene);

        // Affichage de la fenêtre
        stage.show();

        // Lancement de la simulation
        simulation.lancerSimulation();
    }

    /**
     * Méthode main qui sert de point d'entrée pour l'application.
     * Elle lance l'application JavaFX et gère les erreurs potentielles lors du lancement.
     *
     * @param args Arguments de la ligne de commande (non utilisés ici).
     */
    public static void main(String[] args) {
        try {
            // Démarre l'application JavaFX
            launch(args);
        } catch (Exception e) {
            // Affiche l'exception si le lancement échoue
            e.printStackTrace();
        }
    }
}
