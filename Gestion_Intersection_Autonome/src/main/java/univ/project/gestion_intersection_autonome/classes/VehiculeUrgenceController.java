package univ.project.gestion_intersection_autonome.classes;

import univ.project.gestion_intersection_autonome.controllers.TerrainController;
import univ.project.gestion_intersection_autonome.controllers.VehiculeController;

import java.util.List;

/**
 * Contrôleur pour gérer les véhicules d'urgence dans une intersection autonome.
 *
 * Ce contrôleur étend {@link VehiculeController} et ajoute une logique spécifique
 * pour la gestion des priorités et des signaux d'urgence lorsqu'un véhicule d'urgence
 * approche ou traverse une intersection.
 */
public class VehiculeUrgenceController extends VehiculeController {

    /** Indique si un signal d'urgence a déjà été envoyé. */
    private boolean signalEnvoye = false;

    /**
     * Constructeur du contrôleur de véhicules d'urgence.
     *
     * @param vehicule          Le véhicule d'urgence associé à ce contrôleur.
     * @param terrain           Le terrain sur lequel se déplace le véhicule.
     * @param terrainController Le contrôleur du terrain.
     */
    public VehiculeUrgenceController(VehiculeUrgence vehicule, Terrain terrain, TerrainController terrainController) {
        super(vehicule, terrain, terrainController); // Appel du constructeur parent
    }

    /**
     * Méthode principale exécutée lors du déplacement du véhicule d'urgence.
     * Gère l'ensemble des étapes du déplacement, y compris l'entrée et la sortie
     * des intersections.
     */
    @Override
    public void run() {
        List<Vector2D> itineraire = vehicule.getItineraire();

        for (int i = 1; i < itineraire.size(); i++) {
            // Vérifier le trafic avant d'atteindre une intersection
            if (estProcheIntersection(vehicule.getPosition().copy()) && !signalEnvoye && entreeIntersection) {
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

    /**
     * Gère le déplacement du véhicule d'urgence, incluant l'entrée et la sortie
     * des intersections.
     */
    @Override
    public void deplacement() {
        if (estDansCommunication(anciennePosition) && entreeIntersection) {
            entrerIntersection();
            entreeIntersection = false; // Le véhicule est sorti de l'intersection
            signalEnvoye = false;
        } else {
            deplacerHorsIntersection();
            if (estDansCommunication(nouvellePosition)) {
                entreeIntersection = true;
            }
        }
    }

    /**
     * Gère les déplacements dans une intersection en débloquant les voies si
     * le véhicule d'urgence est sorti.
     *
     * @param deplacements Liste des déplacements prévus dans l'intersection.
     */
    @Override
    protected void avancerIntersection(List<Vector2D> deplacements) {
        super.avancerIntersection(deplacements);

        // Débloquer les voies à la sortie de l'intersection
        if (signalEnvoye) {
            envoyerSignalUrgence(Objetmessage.SORTIE);
        }
    }

    /**
     * Vérifie si le véhicule est proche d'une intersection.
     *
     * @param position La position actuelle du véhicule.
     * @return {@code true} si le véhicule est proche d'une intersection, sinon {@code false}.
     */
    private boolean estProcheIntersection(Vector2D position) {
        return terrain.estProcheIntersection(position);
    }

    /**
     * Vérifie l'état du trafic sur la voie actuelle. Si un embouteillage est détecté,
     * envoie un signal à l'intersection pour indiquer la priorité du véhicule d'urgence.
     */
    private void verifierEtatTrafic() {
        Intersection intersection = terrain.getIntersectionPlusProche(vehicule.getPosition().copy());

        boolean embouteillageDetecte = detecterEmbouteillageSurVoie(intersection);

        if (embouteillageDetecte) {
            intersection.addVehiculeControllerListener(this);
            addIntersectionListener(intersection);

//            System.out.println("Embouteillage détecté par le véhicule d'urgence " + vehicule.getId());
            envoyerSignalUrgence(Objetmessage.PASSAGE);
        }
    }

    /**
     * Détecte un embouteillage sur la voie actuelle du véhicule.
     *
     * @param intersection L'intersection à analyser.
     * @return {@code true} si un embouteillage est détecté, sinon {@code false}.
     */
    private boolean detecterEmbouteillageSurVoie(Intersection intersection) {
        return intersection.verifierEmbouteillage(vehicule.getPosition().copy());
    }

    /**
     * Envoie un signal à l'intersection pour indiquer la priorité du véhicule d'urgence.
     *
     * @param objet Le type de message envoyé (par exemple, PASSAGE ou SORTIE).
     */
    private void envoyerSignalUrgence(Objetmessage objet) {
        Message message = new Message();
        message.setObjet(objet); // Message de type URGENCE
        message.setv1(vehicule);
        message.setEntreeUrgence(vehicule.getPosition().copy());

//        System.out.println("Signal d'urgence envoyé à l'intersection par le véhicule " + vehicule.getId());
        sendMessageToIntersections(message);
        signalEnvoye = true;
    }
}