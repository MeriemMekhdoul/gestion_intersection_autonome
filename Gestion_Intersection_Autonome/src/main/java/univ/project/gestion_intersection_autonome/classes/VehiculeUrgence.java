package univ.project.gestion_intersection_autonome.classes;

import javafx.scene.paint.Color;
import java.io.IOException;
import java.util.List;

/**
 * Classe représentant un véhicule d'urgence dans une intersection autonome.
 *
 * Cette classe hérite de {@link Vehicule} et permet de spécifier un comportement
 * particulier pour les véhicules d'urgence, tels que les ambulances, les camions de pompiers
 * ou les voitures de police.
 */
public class VehiculeUrgence extends Vehicule {

    /**
     * Constructeur paramétré pour un véhicule d'urgence.
     *
     * @param type            Le type du véhicule (doit être de type URGENCE).
     * @param positionDepart  La position de départ du véhicule.
     * @param positionArrivee La position d'arrivée du véhicule.
     * @param itineraire      L'itinéraire complet du véhicule.
     * @param couleur         La couleur associée au véhicule.
     * @throws IOException Si une erreur d'entrée-sortie se produit.
     */
    public VehiculeUrgence(TypeVehicule type, Vector2D positionDepart, Vector2D positionArrivee, List<Vector2D> itineraire, Color couleur) throws IOException {
        super(type, positionDepart, positionArrivee, itineraire, couleur);
    }

}
