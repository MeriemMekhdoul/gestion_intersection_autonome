package univ.project.gestion_intersection_autonome.classes;

import java.util.ArrayList;
import java.util.Random;

public class Simulation {

    private Terrain terrain;

    private ArrayList<Vehicule> vehicules;

    private Random random;

    public void Simulation () {

        terrain = new Terrain(25,25);

        vehicules = new ArrayList<>();

        random = new Random();

        genererVehiculesAleatoires(5); // Générer 5 véhicules
    }

}
