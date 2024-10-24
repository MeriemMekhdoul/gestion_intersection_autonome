package univ.project.gestion_intersection_autonome.classes;

import javafx.scene.paint.Color;
import univ.project.gestion_intersection_autonome.controllers.VehiculeController;

import java.io.IOException;
import java.util.ArrayList;

public class VehiculePolice extends Vehicule {


    public VehiculePolice(TypeVehicule type, Vector2D positionDepart, Vector2D positionArrivee, ArrayList<Vector2D> itineraire, Color couleur) throws IOException {
        super(type, positionDepart, positionArrivee, itineraire, couleur);
    }

    public void passagePolice (Intersection intersection, VehiculeController vehiculeController) {

        // Ajouter l'intersection comme écouteur au véhicule controller
        vehiculeController.addListener((VehiculeControllerListener) intersection); // Ajout du listener

        // Récupérer les véhicules en attente dans l'intersection
        ArrayList<Vehicule>vehiculesEnAttente=intersection.getVehiculesEnAttente();

        // Vérifier si le nombre de véhicules en attente dépasse 3
        if(vehiculesEnAttente.size()>3) {

            Message message= new Message(this,"Laissez passer le véhicule de police ");

            intersection.sendMessageintersection(message); // Notifie l'intersection et les VC
        }
        else {
            System.out.println("Le véhicule de police passe normalement.");
        }


    }




}
