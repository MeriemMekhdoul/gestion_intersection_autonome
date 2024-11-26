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

public class MainTrafficApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {

        Simulation simulation = new Simulation();
        Terrain terrain = simulation.getTerrain();

        FXMLLoader fxmlLoader = new FXMLLoader(MainTrafficApplication.class.getResource("Terrain.fxml"));
        fxmlLoader.setControllerFactory(obj -> new TerrainController(simulation));
        Parent root = fxmlLoader.load(); // Charger l'interface

        // Récupérer l'instance de TerrainController créée par FXMLLoader
        TerrainController terrainController = fxmlLoader.getController();
        VBox vbox = terrainController.getIntersection();

        //terrain.getIntersections().get(0).setVBox(vbox);

        // Passer l'instance de TerrainController à la simulation
        simulation.setTerrainController(terrainController);

        Scene scene = new Scene(root, 610, 610);
        stage.setTitle("Mon Terrain");
        stage.setScene(scene);
        stage.show();

        simulation.lancerSimulation();



    }
        public static void main (String[]args){
            try {
                launch(args);
            } catch (Exception e) {
                e.printStackTrace();
            }


        }
    }







