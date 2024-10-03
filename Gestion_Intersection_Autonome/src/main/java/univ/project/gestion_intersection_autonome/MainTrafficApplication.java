package univ.project.gestion_intersection_autonome;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import univ.project.gestion_intersection_autonome.classes.*;
import univ.project.gestion_intersection_autonome.controllers.TerrainController;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.io.*;
import java.net.*;




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

        simulation.genererVehiculesAleatoires(5);
        simulation.lancerSimulation();
        /* Socket s = new Socket("9090", 0);
        Vehicule v1 = new Vehicule(TypeVehicule.VOITURE,new Vector2D(1,2), new Vector2D(4,3),new Vector2D(5,6),s);
        v1.setId(1);
        Vehicule v3 = new Vehicule(TypeVehicule.VOITURE,new Vector2D(0,2), new Vector2D(7,8),new Vector2D(9,10),s);
        Vehicule v4 = new Vehicule(TypeVehicule.VOITURE,new Vector2D(3,2), new Vector2D(10,11),new Vector2D(12,13),s);
        v1.setId(2);
        v1.setId(3);
        ArrayList<Vehicule> v2 = new ArrayList<>();
        v2.add(v3);
        v2.add(v4);
        Objetmessage objet = Objetmessage.valueOf("PASSAGE");
        Vector2D d = new Vector2D(15,17);
        Vector2D a = new Vector2D(18,20);
        ArrayList<Vector2D> itineraire= new ArrayList<>();
        itineraire.add(d);
        itineraire.add(a);

        Message message = new Message(5, v1,v2,objet,itineraire);
        Vehicule.sendMessage(message,v1,v2 );
        Vehicule.receiveMessage(message,v2); */


    }

    public static void main(String[] args) {
        launch();
    }
}
