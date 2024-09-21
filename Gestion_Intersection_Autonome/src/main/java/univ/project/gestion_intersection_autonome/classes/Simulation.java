package univ.project.gestion_intersection_autonome.classes;

import univ.project.gestion_intersection_autonome.controllers.TerrainController;
import univ.project.gestion_intersection_autonome.controllers.VehiculeController;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Simulation {

    private Terrain terrain;
    private ArrayList<Vehicule> vehicules;
    private TerrainController terrainController;

    //constructeur par défaut
    public Simulation() {
        terrain = new Terrain(25, 25);
        vehicules = new ArrayList<>();
    }

    public Terrain getTerrain() {
        return terrain;
    }

//    public List<Vehicule> getVehicules() {
//        return vehicules;
//    }

    //Générer aléatoirement des véhicules
    public void genererVehiculesAleatoires(int nombre) {
        List<Vector2D> entrees = terrain.getEntrees(); // Récupérer les entrées du terrain
        List<Vector2D> sorties = terrain.getSorties(); // Récupérer les sorties du terrain

        for (int i = 0; i < nombre; i++) {
            // Récupérer une entrée et une sortie
            Vector2D positionDepart = entrees.get(i % entrees.size());
            Vector2D positionArrivee = sorties.get(i % sorties.size());

            Random random = new Random();
            TypeVehicule type = TypeVehicule.values()[random.nextInt(TypeVehicule.values().length)];

            Vehicule vehicule = new Vehicule(type, positionDepart, positionArrivee);
            VehiculeController vehiculeController = new VehiculeController(vehicule, terrain);
            vehicules.add(vehicule);
        }
    }

    // Lancer les véhicules dans des threads
    public void lancerSimulation() {
        ArrayList<Thread> threads = new ArrayList<>();

        for (Vehicule vehicule : vehicules) {
            Thread thread = new Thread(vehicule);
            threads.add(thread);
            thread.start(); // Lancer chaque véhicule dans un thread
        }

    }

    public void setTerrainController(TerrainController terrainController) {
        this.terrainController = terrainController;
        terrainController.setTerrain(terrain);
    }
}







