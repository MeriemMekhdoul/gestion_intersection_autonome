package univ.project.gestion_intersection_autonome;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import univ.project.gestion_intersection_autonome.classes.Simulation;
import univ.project.gestion_intersection_autonome.controllers.TerrainController;

import java.io.IOException;

public class MainTrafficApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {

        Simulation simulation = new Simulation();

        FXMLLoader fxmlLoader = new FXMLLoader(MainTrafficApplication.class.getResource("Terrain.fxml"));
        fxmlLoader.setControllerFactory(obj -> new TerrainController(simulation));
        Parent root = fxmlLoader.load(); // Charger l'interface

        // Récupérer l'instance de TerrainController créée par FXMLLoader
        TerrainController terrainController = fxmlLoader.getController();
        // Passer l'instance de TerrainController à la simulation
        simulation.setTerrainController(terrainController);

        Scene scene = new Scene(root, 400, 300);
        stage.setTitle("Mon Terrain");
        stage.setScene(scene);
        stage.show();

        simulation.lancerSimulation();
    }

    public static void main(String[] args) {
        launch();
    }
}
