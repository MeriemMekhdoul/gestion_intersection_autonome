package univ.project.gestion_intersection_autonome.classes;

import javafx.application.Platform;
import univ.project.gestion_intersection_autonome.controllers.TerrainController;
import univ.project.gestion_intersection_autonome.controllers.VehiculeController;

import javafx.scene.paint.Color;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * La classe Simulation gère l'exécution de la simulation de l'intersection,
 * y compris la création de véhicules, leur gestion dans des threads et leur mouvement
 * dans un terrain modélisé par un objet Terrain.
 */
public class Simulation {

    private final Terrain terrain;
    private ArrayList<Vehicule> vehicules;
    private TerrainController terrainController;
    private ArrayList<VehiculeController> controleurs;
    private ScheduledExecutorService scheduler;
    private final int LARGEUR_TERRAIN = 20;
    private final int HAUTEUR_TERRAIN = 20;
    private final int LIMITE_VEHICULES = 20;

    /**
     * Constructeur par défaut de la simulation.
     * Initialise le terrain, les listes de véhicules et contrôleurs, et le planificateur.
     */
    public Simulation() {
        terrain = new Terrain(LARGEUR_TERRAIN, HAUTEUR_TERRAIN);
        vehicules = new ArrayList<>();
        controleurs = new ArrayList<>();
        scheduler = Executors.newScheduledThreadPool(1);
    }

    /**
     * Retourne l'objet Terrain associé à cette simulation.
     * @return Le terrain de la simulation.
     */
    public Terrain getTerrain() {
        return terrain;
    }

    /**
     * Génère aléatoirement un véhicule et l'ajoute à la simulation.
     * Cette méthode vérifie les entrées et sorties disponibles et génère un véhicule
     * avec un itinéraire valide basé sur l'algorithme A*.
     * @throws IOException Si une erreur se produit lors de la génération du véhicule.
     */
    public void genererVehiculeAleatoire() throws IOException {
        if (vehicules.size() >= LIMITE_VEHICULES) {
            return;  // Limite de véhicules atteinte, on ne génère pas de nouveaux véhicules.
        }

        boolean voieGauche = new Random().nextBoolean(); // Choisir une voie aléatoire (gauche ou droite)

        // Récupérer les entrées et sorties du terrain
        List<Vector2D> entrees = voieGauche ? terrain.getEntreesVoieGauche() : terrain.getEntreesVoieDroite();
        List<Vector2D> sorties = voieGauche ? terrain.getSortiesVoieGauche() : terrain.getSortiesVoieDroite();

        // Vérification si les entrées ou sorties sont vides
        if (entrees.isEmpty() || sorties.isEmpty()) {
            System.err.println("Erreur : Aucune entrée ou sortie disponible !");
            return;
        }

        // Sélectionner une position de départ aléatoire parmi les entrées
        Vector2D positionDepart = entrees.get(new Random().nextInt(entrees.size()));

        ArrayList<Vector2D> sortiesPossibles = new ArrayList<>();

        // Filtrer les sorties qui ne sont pas directement à côté de l'entrée
        for (Vector2D sortie : sorties) {
            if (!isSideBySide(positionDepart, sortie)) {
                sortiesPossibles.add(sortie);
            }
        }

        if (sortiesPossibles.isEmpty()) {
            System.err.println("Aucune sortie possible pour l'entrée choisie");
            return;
        }

        // Sélectionner une sortie possible
        Vector2D positionArrivee = sortiesPossibles.get(new Random().nextInt(sortiesPossibles.size()));

        // Générer un type de véhicule aléatoire et sa couleur
        int random = new Random().nextInt(20) + 1;
        TypeVehicule type = TypeVehicule.VOITURE;
        Color couleur = Vehicule.genererCouleurAleatoire();

        if (random == 10) {
            type = TypeVehicule.URGENCE;
            couleur = Color.WHITE;  // Si c'est un véhicule d'urgence, on lui attribue une couleur spéciale.
        }

        // Calculer l'itinéraire avec l'algorithme A*
        AStar aStar = new AStar(terrain);
        List<Vector2D> itineraire = aStar.trouverChemin(positionDepart, positionArrivee);

        if (itineraire.isEmpty()) {
            throw new IllegalStateException("Aucun chemin trouvé de " + positionDepart + " à " + positionArrivee);
        }

        // Créer le véhicule et le contrôleur de véhicule
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

        // Ajouter le véhicule et son contrôleur à la simulation
        vehicules.add(vehicule);
        controleurs.add(vehiculeController);

        // Démarrer le contrôleur du véhicule dans un nouveau thread
        Thread thread = new Thread(vehiculeController);
        thread.start();
    }

    /**
     * Lance la simulation en générant des véhicules à intervalles réguliers.
     * Cette méthode utilise un planificateur pour générer un véhicule toutes les secondes.
     */
    public void lancerSimulation() {
        scheduler.scheduleAtFixedRate(() -> {
            Platform.runLater(() -> {
                try {
                    genererVehiculeAleatoire();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }, 0, VehiculeController.VITESSE_SIMULATION_MS, TimeUnit.MILLISECONDS); // Ajoute un véhicule à chaque intervalle défini.
    }

    /**
     * Supprime un véhicule et son contrôleur de la simulation.
     * @param vehicule Le véhicule à supprimer.
     * @param vehiculeController Le contrôleur du véhicule à supprimer.
     */
    public void supprimerVehicule(Vehicule vehicule, VehiculeController vehiculeController) {
        vehicules.remove(vehicule);
        controleurs.remove(vehiculeController);

        // Regénérer un véhicule après suppression
        Platform.runLater(() -> {
            try {
                genererVehiculeAleatoire();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Définit le contrôleur de terrain pour cette simulation.
     * @param terrainController Le contrôleur de terrain à associer.
     */
    public void setTerrainController(TerrainController terrainController) {
        this.terrainController = terrainController;
        terrainController.setTerrain(terrain);
    }

    /**
     * Vérifie si deux positions sont à proximité l'une de l'autre, c'est-à-dire si leur distance est inférieure ou égale à 3 unités.
     * @param pos1 La première position à comparer.
     * @param pos2 La deuxième position à comparer.
     * @return true si les positions sont considérées comme à côté, sinon false.
     */
    private boolean isSideBySide(Vector2D pos1, Vector2D pos2) {
        return pos1.distance(pos2) <= 3;
    }
}
