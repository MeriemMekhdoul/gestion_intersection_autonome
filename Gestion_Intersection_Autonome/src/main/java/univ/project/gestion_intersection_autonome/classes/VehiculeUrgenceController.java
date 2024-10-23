package univ.project.gestion_intersection_autonome.classes;

import javafx.application.Platform;
import univ.project.gestion_intersection_autonome.controllers.TerrainController;
import univ.project.gestion_intersection_autonome.controllers.VehiculeController;

import java.security.MessageDigestSpi;
import java.util.List;
public class VehiculeUrgenceController extends VehiculeController {

    private boolean signalEnvoye = false;

    public VehiculeUrgenceController(VehiculeUrgence vehicule, Terrain terrain, TerrainController terrainController) {
        super(vehicule, terrain, terrainController); // Appel du constructeur parent
    }

    @Override
    public void run() {
        List<Vector2D> itineraire = vehicule.getItineraire();

        for (int i = 1; i < itineraire.size(); i++) {
            // Vérifier le trafic avant d'atteindre une intersection
            if (estProcheIntersection(itineraire.get(i)) && !signalEnvoye) {
                verifierEtatTrafic();
            }

            anciennePosition = vehicule.getPosition().copy();
            nouvellePosition = itineraire.get(i);
            deplacement();

            // Mettre à jour l'index dans le cas du déplacement dans l'intersection
            i = itineraire.indexOf(nouvellePosition);
        }

        // Logique de fin de déplacement (libérer la cellule, etc.)
        super.finDeplacement();
    }

    @Override
    public void deplacement() {
        if (estDansCommunication(anciennePosition) && entreeIntersection) {
            System.out.println("je suis dans une zone de communication");
            entrerIntersection();
            entreeIntersection = false; //je suis sortie de l'intersection
            signalEnvoye = false;
        } else {
            System.out.println("je suis en zone normale");
            deplacerHorsIntersection();
            //mettre à jour l'attribut "entree" pour savoir si on arrive de nouveau dans une intersection ou pas
            if (estDansCommunication(nouvellePosition)) {
                System.out.println("je m'apprête à entrer dans une nouvelle intersection");
                entreeIntersection = true;
            }
        }
    }

    /**
     * Vérifie si le véhicule est proche d'une intersection.
     */
    private boolean estProcheIntersection(Vector2D position) {
        // Logique pour déterminer si la position est proche d'une intersection
        return terrain.estProcheIntersection(position);
    }

    /**
     * Vérifie l'état du trafic sur la voie actuelle.
     * Si un embouteillage est détecté, envoie un signal à l'intersection.
     */
    private void verifierEtatTrafic() {
        // Parcourir la voie et vérifier si des véhicules sont bloqués
        Intersection intersection = terrain.getIntersection(vehicule.getPosition());

        boolean embouteillageDetecte = detecterEmbouteillageSurVoie(intersection);

        if (embouteillageDetecte) {
            intersection.addVehiculeControllerListener(this);
            addIntersectionListener(intersection);

            System.out.println("Embouteillage détecté par le véhicule d'urgence " + vehicule.getId());
            envoyerSignalUrgence();
        }
    }

    /**
     * Détecte un embouteillage sur la voie du véhicule.
     */
    private boolean detecterEmbouteillageSurVoie(Intersection intersection) {
        // Logique de détection d'embouteillage (vérification des véhicules lents ou arrêtés sur la voie)
        // Retourne true si un embouteillage est détecté, false sinon
        return intersection.verifierEmbouteillage(vehicule.getPosition());
    }

    /**
     * Envoie un signal à l'intersection pour indiquer la priorité du véhicule d'urgence.
     */
    private void envoyerSignalUrgence() {
        Message message = new Message();
        message.setObjet(Objetmessage.PASSAGE); // Message de type URGENCE
        message.setv1(vehicule);

        sendMessageToIntersections(message);
        System.out.println("Signal d'urgence envoyé à l'intersection par le véhicule " + vehicule.getId());
        signalEnvoye = true;
    }
}
