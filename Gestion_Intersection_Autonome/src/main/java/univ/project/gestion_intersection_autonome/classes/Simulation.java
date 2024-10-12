package univ.project.gestion_intersection_autonome.classes;

import javafx.application.Platform;
import univ.project.gestion_intersection_autonome.controllers.TerrainController;
import univ.project.gestion_intersection_autonome.controllers.VehiculeController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Simulation {

    private final Terrain terrain;
    private ArrayList<Vehicule> vehicules;
    private TerrainController terrainController;
    private ArrayList<VehiculeController> controleurs;
    private ScheduledExecutorService scheduler;
    private final int LARGEUR_TERRAIN = 50;
    private final int HAUTEUR_TERRAIN = 50;

    //constructeur par défaut
    public Simulation() {
        terrain = new Terrain(LARGEUR_TERRAIN, HAUTEUR_TERRAIN);
        vehicules = new ArrayList<>();
        controleurs = new ArrayList<>();
        scheduler = Executors.newScheduledThreadPool(1);
    }

    public Terrain getTerrain() {
        return terrain;
    }

    //Générer aléatoirement un véhicule
    public void genererVehiculeAleatoire() throws IOException {
        if (vehicules.size() >= 50) {
//            System.out.println("Limite de véhicules atteinte");
            return;
        }

        List<Vector2D> entrees = terrain.getEntrees(); // Récupérer les entrées du terrain
        List<Vector2D> sorties = terrain.getSorties(); // Récupérer les sorties du terrain

        // vérification du remplissage des entrées et sorties
        if (entrees.isEmpty() || sorties.isEmpty()) {
            System.err.println("Erreur : Aucune entrée ou sortie de disponible !");
            return;
        }

        Vector2D positionDepart = entrees.get(new Random().nextInt(entrees.size()));
        Vector2D positionArrivee = sorties.get(new Random().nextInt(sorties.size()));

        // en cas de modification des listes après lancement
        if (sorties.contains(positionDepart)) {
            System.err.println("Erreur : Position de départ ne peut pas être une sortie : " + positionDepart);
            return;
        }

        System.out.println("Départ : " + positionDepart + " | Arrivée : " + positionArrivee);

        TypeVehicule type = TypeVehicule.values()[new Random().nextInt(TypeVehicule.values().length)];

        Vehicule vehicule = new Vehicule(type, positionDepart, positionArrivee, terrain);

        Cellule cellule = terrain.getCellule(positionDepart);
        cellule.setOccupee(true);
        cellule.setIdVoiture(vehicule.getId());

        VehiculeController vehiculeController = new VehiculeController(vehicule, terrain, terrainController);
        vehicules.add(vehicule);
        controleurs.add(vehiculeController);

        Thread thread = new Thread(vehiculeController);
        thread.start();
        System.out.println("Véhicule ajouté");
    }

    // Lancer les véhicules dans des threads
    public void lancerSimulation()
    {
        scheduler.scheduleAtFixedRate(() -> {
            Platform.runLater(() -> {
                try {
                    genererVehiculeAleatoire();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }, 0, 1, TimeUnit.SECONDS); // Ajoute un véhicule toutes les secondes
    }

    // suppression du véhicule de la liste des vehicules et controlleurs
    public void supprimerVehicule(Vehicule vehicule, VehiculeController vehiculeController) {
        vehicules.remove(vehicule);
        controleurs.remove(vehiculeController);

        Platform.runLater(() -> {
            try {
                genererVehiculeAleatoire();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void setTerrainController(TerrainController terrainController) {
        this.terrainController = terrainController;
        terrainController.setTerrain(terrain);
    }
}







