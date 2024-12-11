package univ.project.gestion_intersection_autonome.controleurs;

import univ.project.gestion_intersection_autonome.classes.Message;

/**
 * Interface pour écouter les événements provenant du contrôleur de véhicules.
 *
 * Cette interface définit les méthodes à implémenter pour gérer les messages
 * envoyés ou reçus par le contrôleur des véhicules.
 */
public interface VehiculeControllerListener {

    /**
     * Méthode appelée lorsqu'un message est envoyé par le contrôleur de véhicules.
     *
     * @param message Le message envoyé.
     */
    void messageVc(Message message);

    /**
     * Méthode appelée lorsqu'un message est reçu depuis l'intersection.
     *
     * @param message Le message reçu.
     */
    void onMessageReceivedFromIntersection(Message message);
}
