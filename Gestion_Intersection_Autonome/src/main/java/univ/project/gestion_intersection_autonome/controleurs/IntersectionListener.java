package univ.project.gestion_intersection_autonome.controleurs;

import univ.project.gestion_intersection_autonome.classes.Message;

/**
 * Interface définissant les méthodes nécessaires pour écouter les messages provenant d'autres intersections
 * ainsi que des contrôleurs de véhicules dans un système de gestion d'intersection autonome.
 * Les classes implémentant cette interface doivent définir la logique pour traiter les messages reçus
 * et réagir en conséquence.
 */
public interface IntersectionListener {

    /**
     * Méthode appelée lorsqu'un message est reçu d'une autre intersection.
     * Cette méthode est responsable de la gestion des messages envoyés par d'autres intersections.
     *
     * @param message Le message reçu d'une autre intersection.
     */
    void messageIntersection(Message message);

    /**
     * Méthode appelée lorsqu'un message est reçu d'un contrôleur de véhicule.
     * Cette méthode permet à l'intersection de traiter les messages envoyés par un contrôleur de véhicule
     * pour gérer les demandes et informations liées à la circulation.
     *
     * @param message Le message reçu d'un contrôleur de véhicule.
     */
    void onMessageReceivedFromVehiculeController(Message message);
}
