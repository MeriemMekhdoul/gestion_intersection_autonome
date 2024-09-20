package univ.project.gestion_intersection_autonome.controllers;

import univ.project.gestion_intersection_autonome.classes.Terrain;
import univ.project.gestion_intersection_autonome.classes.Vehicule;

public class VehiculeController {

    private Vehicule vehicule;
    private Terrain terrain;

    public VehiculeController(Vehicule v, Terrain _terrain){
        terrain = _terrain;
        vehicule = v;
    }

    public void deplacment(){
        /** choisir les cellules potentielles d'apres la grille du terrain
         * les passer a vehicule.
          **/

    }
}
