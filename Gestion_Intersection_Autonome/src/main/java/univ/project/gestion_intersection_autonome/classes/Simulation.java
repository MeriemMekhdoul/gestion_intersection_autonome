package univ.project.gestion_intersection_autonome.classes;

import javafx.application.Platform;
import univ.project.gestion_intersection_autonome.controllers.TerrainController;
import univ.project.gestion_intersection_autonome.controllers.VehiculeController;

import javafx.scene.paint.Color;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;
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
    private final int LIMITE_VEHICULES = 1;


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

        boolean voieGauche = new Random().nextBoolean(); // voie aléatoire

        List<Vector2D> entrees = voieGauche ? terrain.getEntreesVoieGauche() : terrain.getEntreesVoieDroite(); // Récupérer les entrées du terrain
        List<Vector2D> sorties = voieGauche ? terrain.getSortiesVoieGauche() : terrain.getSortiesVoieDroite(); // Récupérer les sorties du terrain

        // vérification du remplissage des entrées et sorties
        if (entrees.isEmpty() || sorties.isEmpty()) {
            System.err.println("Erreur : Aucune entrée ou sortie de disponible !");
            return;
        }

        Vector2D positionDepart = entrees.get(new Random().nextInt(entrees.size()));

        ArrayList<Vector2D> sortiesPossibles = new ArrayList<>();

        // on trie les sorties qui ne sont pas à côté de l'entrée
        for (Vector2D sortie : sorties) {
            if (!estSurMemeLigne(positionDepart, sortie)) {
                sortiesPossibles.add(sortie);
            }
        }

        if (sortiesPossibles.isEmpty()) {
            System.err.println("Aucune sortie possible pour l'entrée choisie");
            return;
        }

        Vector2D positionArrivee = sortiesPossibles.get(new Random().nextInt(sortiesPossibles.size()));

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

    // verifie si deux positions sont considérées comme à côté
    private boolean estSurMemeLigne(Vector2D pos1, Vector2D pos2) {
        return pos1.getX() == pos2.getX() || pos1.getY() == pos2.getY();
    }

}







