package univ.project.gestion_intersection_autonome.classes;

import univ.project.gestion_intersection_autonome.controllers.TerrainController;

import java.util.ArrayList;
import java.util.Random;

public class Simulation {

    private Terrain terrain;

    private TerrainController terrainController;

    private ArrayList<Vehicule> vehicules;

    private Random random;

        public Simulation () {

            terrain = new Terrain(25,25);

            vehicules = new ArrayList<>();

            random = new Random();

        //genererVehiculesAleatoires(5); // Générer 5 véhicules
    }

    public Terrain getTerrain(){
        return terrain;
    }

    public void setTerrainController(TerrainController terrainController) {
        this.terrainController = terrainController;
        terrainController.setTerrain(terrain);
    }
}

