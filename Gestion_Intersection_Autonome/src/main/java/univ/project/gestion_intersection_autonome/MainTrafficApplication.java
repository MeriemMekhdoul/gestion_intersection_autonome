package univ.project.gestion_intersection_autonome;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import univ.project.gestion_intersection_autonome.classes.Simulation;
import univ.project.gestion_intersection_autonome.controleurs.TerrainController;

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

        Simulation simulation = new Simulation();

        FXMLLoader fxmlLoader = new FXMLLoader(MainTrafficApplication.class.getResource("Terrain.fxml"));
        fxmlLoader.setControllerFactory(obj -> new TerrainController(simulation));
        Parent root = fxmlLoader.load();

        TerrainController terrainController = fxmlLoader.getController();
        simulation.setTerrainController(terrainController);

        Scene scene = new Scene(root, 610, 610);
        stage.setTitle("Mon Terrain");
        stage.setScene(scene);
        stage.show();

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
            e.printStackTrace();
        }
    }
}
