package univ.project.gestion_intersection_autonome.classes.Enums;

/**
 * L'énumération Objetmessage définit les différents types de messages
 * qui peuvent être envoyés entre les véhicules et les intersections.
 * Chaque type de message correspond à une action ou à une information spécifique
 * dans le système de gestion de l'intersection.
 */
public enum Objetmessage {

    /**
     * Demande de passage : utilisé pour indiquer qu'un véhicule demande à traverser l'intersection.
     */
    PASSAGE,

    /**
     * Information d'arrivée dans une zone de communication : ce message informe qu'un véhicule entre dans une zone
     * de communication ou d'interaction.
     */
    INFORMATION,  // Renommer en ENTREE ?

    /**
     * Information sur le trafic dans les autres voies : ce message sert à transmettre des informations
     * sur la circulation dans d'autres voies de l'intersection ou du réseau routier.
     */
    TRAFIC,

    /**
     * Proposition de configuration de passage : envoyé lorsqu'un véhicule propose une solution ou une configuration
     * pour résoudre un conflit de passage avec un autre véhicule.
     */
    CONFIG,

    /**
     * Sortie du véhicule de l'intersection : indique qu'un véhicule quitte l'intersection.
     */
    SORTIE,

    /**
     * Entrée d'urgence dans l'intersection : ce message est utilisé lorsqu'un véhicule d'urgence entre dans l'intersection,
     * et nécessite une priorité de passage.
     */
    ENTREE,

    /**
     * Marche : utilisé pour activer ou mettre en marche un véhicule ou une partie du système.
     */
    MARCHE,

    /**
     * Arrêt : demande à un véhicule ou à un système d'arrêter une action, comme l'arrêt dans une intersection.
     */
    STOP
}
