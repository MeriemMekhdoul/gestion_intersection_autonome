package univ.project.gestion_intersection_autonome.classes;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Simulation {


    private Terrain terrain;

    private ArrayList<Vehicule> vehicules;

    public Simulation () { //constructeur par défaut

        terrain = new Terrain(25,25);

        vehicules = new ArrayList<>();


        genererVehiculesAleatoires(5); // Générer 5 véhicules
    }
    //Générer aléatoirement des véhicules
    public void genererVehiculesAleatoires(int nombre){
        List<int[]> entrees = terrain.getEntrees(); // Récupérer les entrées du terrain

        for (int i = 0; i < nombre; i++) {
            // Récupérer une entrée
            int[] entree = entrees.get(i % entrees.size());
            Random random ;
            random = new Random();
            Vehicule.TypeVehicule type = Vehicule.TypeVehicule.values()[random.nextInt(Vehicule.TypeVehicule.values().length)];
            Vector2D positionDepart = new Vector2D(entree[0], entree[1]); // Position de départ à l'entrée
            Vector2D positionArrivee = new Vector2D(random.nextInt(terrain.getLargeur()), random.nextInt(terrain.getHauteur()));

            Vehicule vehicule = new Vehicule(type,  positionDepart, positionArrivee);
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

public static void main(String[] args) {
                Simulation simulation = new Simulation();
                simulation.lancerSimulation(); // Lancer la simulation
            }
        }






