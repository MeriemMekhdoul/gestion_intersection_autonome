package univ.project.gestion_intersection_autonome.classes;

import univ.project.gestion_intersection_autonome.controllers.TerrainController;
import univ.project.gestion_intersection_autonome.controllers.VehiculeController;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Simulation {

    private final Terrain terrain;
    private ArrayList<Vehicule> vehicules;
    private TerrainController terrainController;
    private ArrayList<VehiculeController> controleurs;

    //constructeur par défaut
    public Simulation() {
        terrain = new Terrain(25, 25);
        vehicules = new ArrayList<>();
        controleurs = new ArrayList<>();
    }

    public Terrain getTerrain() {
        return terrain;
    }


    //Générer aléatoirement des véhicules
    public void genererVehiculesAleatoires(int nombre) {
        List<Vector2D> entrees = terrain.getEntrees(); // Récupérer les entrées du terrain
        List<Vector2D> sorties = terrain.getSorties(); // Récupérer les sorties du terrain

        for (int i = 0; i < nombre; i++) {
            // Récupérer une entrée et une sortie
            Vector2D positionDepart = entrees.get(i);
            Vector2D positionArrivee = sorties.get(sorties.size() - i - 1);

            System.out.println("posD" + positionDepart );
            System.out.println("posA" + positionArrivee);

            Random random = new Random();
            TypeVehicule type = TypeVehicule.values()[random.nextInt(TypeVehicule.values().length)];

            Vehicule vehicule = new Vehicule(type, positionDepart, positionArrivee);

            Cellule cell = terrain.getCellule(positionDepart); //l'objet n'est pas nécessaire ?
            cell.setOccupee(true);
            cell.setIdVoiture(vehicule.getId());

            VehiculeController vehiculeController = new VehiculeController(vehicule, terrain, terrainController);
            vehicules.add(vehicule);
            controleurs.add(vehiculeController);
        }
    }

    // Lancer les véhicules dans des threads
    public void lancerSimulation() {
        ArrayList<Thread> threads = new ArrayList<>();

        for (VehiculeController controller : controleurs) {
            Thread thread = new Thread(controller);
            threads.add(thread);
            thread.start(); // Lancer chaque contrôleur dans un thread
        }

    }

    public void setTerrainController(TerrainController terrainController) {
        this.terrainController = terrainController;
        terrainController.setTerrain(terrain);
    }
}







