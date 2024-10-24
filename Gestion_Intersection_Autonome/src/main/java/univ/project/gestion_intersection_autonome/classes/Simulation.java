package univ.project.gestion_intersection_autonome.classes;

import javafx.application.Platform;
import univ.project.gestion_intersection_autonome.controllers.TerrainController;
import univ.project.gestion_intersection_autonome.controllers.VehiculeController;

import javafx.scene.paint.Color;

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
    private final int LARGEUR_TERRAIN = 40;
    private final int HAUTEUR_TERRAIN = 40;
    private final int LIMITE_VEHICULES = 50;


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
    public void genererVehiculeAleatoire() throws IOException
    {
        if (vehicules.size() >= LIMITE_VEHICULES) {
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

        int indexEntree = new Random().nextInt(entrees.size());
        int indexSortie = new Random().nextInt(sorties.size());

        while (indexEntree == indexSortie) {
            indexSortie = new Random().nextInt(sorties.size());
        }

        Vector2D positionDepart = entrees.get(indexEntree);
        Vector2D positionArrivee = sorties.get(indexSortie);

        // en cas de modification des listes après lancement
        if (sorties.contains(positionDepart)) {
            System.err.println("Erreur : Position de départ ne peut pas être une sortie : " + positionDepart);
            return;
        }

        System.out.println("Départ : " + positionDepart + " | Arrivée : " + positionArrivee);

        // génération du type de véhicule
        int random = new Random().nextInt(20) + 1;
        TypeVehicule type = TypeVehicule.VOITURE;

        Color couleur = Vehicule.genererCouleurAleatoire();

        if (random == 1) {
            type = TypeVehicule.URGENCE;
            couleur = Color.WHITE;
        }

        // calcul de l'itinéraire
        AStar aStar = new AStar(terrain);
        List<Vector2D> itineraire = aStar.trouverChemin(positionDepart, positionArrivee);

        if (itineraire.isEmpty()) {
            throw new IllegalStateException("Aucun chemin trouvé de " + positionDepart + " à " + positionArrivee);
        }

        Vehicule vehicule;
        if (type == TypeVehicule.URGENCE)
            vehicule = new VehiculeUrgence(type, positionDepart, positionArrivee, itineraire, couleur);
        else
            vehicule = new Vehicule(type, positionDepart, positionArrivee, itineraire, couleur);

        VehiculeController vehiculeController;
        if (type == TypeVehicule.URGENCE)
            vehiculeController = new VehiculeUrgenceController((VehiculeUrgence) vehicule, terrain, terrainController);
        else
            vehiculeController = new VehiculeController(vehicule, terrain, terrainController);
        vehicules.add(vehicule);
        controleurs.add(vehiculeController);

        Thread thread = new Thread(vehiculeController);
        thread.start();
        //System.out.println("Véhicule ajouté à la position " + vehicule.getPosition());
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
        // voir pour modifier afin de récupérer la constante de véhicule controller
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







