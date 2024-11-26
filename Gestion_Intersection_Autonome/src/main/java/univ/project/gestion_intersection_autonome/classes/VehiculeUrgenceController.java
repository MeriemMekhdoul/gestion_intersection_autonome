package univ.project.gestion_intersection_autonome.classes;

import univ.project.gestion_intersection_autonome.controllers.TerrainController;
import univ.project.gestion_intersection_autonome.controllers.VehiculeController;

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
            if (estProcheIntersection(vehicule.getPosition().copy()) && !signalEnvoye && entreeIntersection) {
                //System.out.println("JE SUIS PROCHE D'UNE INTERSECTION : " + vehicule.getId());
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
            entrerIntersection();
            entreeIntersection = false; //je suis sortie de l'intersection
            signalEnvoye = false;
            //envoyer un signal de sortie
        } else {
            deplacerHorsIntersection();
            if (estDansCommunication(nouvellePosition)) {
                entreeIntersection = true;
            }
        }
    }

    @Override
    protected void avancerIntersection(List<Vector2D> deplacements) {
        super.avancerIntersection(deplacements);
        //à la sortie de l'intersection, on débloque les voies ..
        if(signalEnvoye)
            envoyerSignalUrgence(Objetmessage.SORTIE);
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
        Intersection intersection = terrain.getIntersectionPlusProche(vehicule.getPosition().copy());

        boolean embouteillageDetecte = detecterEmbouteillageSurVoie(intersection);

        if (embouteillageDetecte) {
            intersection.addVehiculeControllerListener(this);
            addIntersectionListener(intersection);

            System.out.println("Embouteillage détecté par le véhicule d'urgence " + vehicule.getId());
            envoyerSignalUrgence(Objetmessage.PASSAGE);
        }
    }

    /**
     * Détecte un embouteillage sur la voie du véhicule.
     */
    private boolean detecterEmbouteillageSurVoie(Intersection intersection) {
        return intersection.verifierEmbouteillage(vehicule.getPosition().copy());
    }

    /**
     * Envoie un signal à l'intersection pour indiquer la priorité du véhicule d'urgence.
     */
    private void envoyerSignalUrgence(Objetmessage objet) {
        Message message = new Message();
        message.setObjet(objet); // Message de type URGENCE
        message.setv1(vehicule);
        message.setEntreeUrgence(vehicule.getPosition().copy());

        System.out.println("Signal d'urgence envoyé à l'intersection par le véhicule " + vehicule.getId());
        sendMessageToIntersections(message);
        signalEnvoye = true;
    }
}
