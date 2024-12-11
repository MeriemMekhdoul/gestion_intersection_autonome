package univ.project.gestion_intersection_autonome.controleurs;

import univ.project.gestion_intersection_autonome.classes.Message;

/**
 * Interface pour écouter les événements liés à un véhicule.
 *
 * Cette interface définit une méthode à implémenter pour gérer les messages
 * reçus par un véhicule.
 */
public interface VehiculeListener {

    /**
     * Méthode appelée lorsqu'un message est reçu par le véhicule.
     *
     * @param message Le message reçu.
     */
    void onMessageReceived(Message message);
}
