package univ.project.gestion_intersection_autonome.classes;

import javafx.scene.paint.Color;

import java.io.IOException;
import java.util.List;

public class VehiculeUrgence extends Vehicule {
    public VehiculeUrgence(TypeVehicule type, Vector2D positionDepart, Vector2D positionArrivee, List<Vector2D> itineraire, Color couleur) throws IOException {
        super(type, positionDepart, positionArrivee, itineraire, couleur);
    }

}
