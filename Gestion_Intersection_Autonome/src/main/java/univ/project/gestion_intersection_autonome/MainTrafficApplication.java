package univ.project.gestion_intersection_autonome;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import univ.project.gestion_intersection_autonome.classes.*;
import univ.project.gestion_intersection_autonome.controllers.TerrainController;
import univ.project.gestion_intersection_autonome.controllers.VehiculeController;

import java.io.IOException;
import java.net.ServerSocket;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;

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
        // Passer l'instance de TerrainController à la simulation
        simulation.setTerrainController(terrainController);

       /*Scene scene = new Scene(root, 525, 525);
        stage.setTitle("Mon Terrain");
        stage.setScene(scene);
        stage.show();

        simulation.lancerSimulation(); */
   /*    // Création des véhicules
        Vehicule v1 = new Vehicule(TypeVehicule.VOITURE, new Vector2D(3, 4), new Vector2D(4, 5));
        Vehicule v2 = new Vehicule(TypeVehicule.VOITURE, new Vector2D(6, 7), new Vector2D(7, 8));
        Vehicule v3 = new Vehicule(TypeVehicule.VOITURE, new Vector2D(5, 5), new Vector2D(6, 6));

// Ajoutez v2 et v3 comme écouteurs pour v1 (pour recevoir les messages de v1)
        v1.addListener(v2);
        v1.addListener(v3);

// Envoi d'un message depuis v1
        Vector2D vec1 = new Vector2D(1, 1);
        Vector2D vec2 = new Vector2D(2, 2);
        ArrayList<Vector2D> itineraire = new ArrayList<>();
        itineraire.add(vec1);
        itineraire.add(vec2);

// Liste des destinataires (recipients)
        ArrayList<Vehicule> recipients = new ArrayList<>();
        recipients.add(v2); // Destinataire
        recipients.add(v3); // Destinataire

// Création du message
        Message message = new Message(1, v1, recipients, Objetmessage.PASSAGE, itineraire);

// Envoi du message
        v1.sendMessage(message);

    }

*/ }
    public static void main(String[] args) {
        try {
            launch(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
            // Création d'une liste de messages reçus (à ajuster selon ta classe Message)
        ArrayList<Message> messagesReçus = new ArrayList<>();

            // Création de quelques itinéraires (à ajuster selon ta classe Vector2D)
        ArrayList<Vector2D> itineraire1 = new ArrayList<>();
        itineraire1.add(new Vector2D(1, 1));
        itineraire1.add(new Vector2D(2, 2));

        ArrayList<Vector2D> itineraire2 = new ArrayList<>();
        itineraire2.add(new Vector2D(2, 2)); // Conflit avec le véhicule 1
        itineraire2.add(new Vector2D(3, 3));

        ArrayList<Vector2D> itineraire3 = new ArrayList<>();
        itineraire3.add(new Vector2D(4, 4)); // Aucun conflit

        // Création des véhicules
        Vehicule vehicule1 = new Vehicule(itineraire1);
        Vehicule vehicule2 = new Vehicule(itineraire2);
        Vehicule vehicule3 = new Vehicule(itineraire3);

        ArrayList<Vehicule> vehiculesEngages = new ArrayList<>();
        vehiculesEngages.add(vehicule2);
        vehiculesEngages.add(vehicule3);



            // Appel de la méthode calculs
            int tempsTotalAttente = VehiculeController.calculs(messagesReçus, itineraire1, vehiculesEngages);

            // Affichage du temps d'attente total
            System.out.println("Temps total d'attente : " + tempsTotalAttente);
        }

    }






