package univ.project.gestion_intersection_autonome.classes.Enums;
/**
 * Enumération représentant les différents états possibles d'un véhicule dans le système.
 * Les états permettent de définir si un véhicule est engagé dans l'intersection ou en attente.
 *
 * Les états possibles sont :
 * - ENGAGE : Le véhicule est engagé dans l'intersection et est en train de traverser.
 * - ATTENTE : Le véhicule est en attente, probablement à un stop ou dans une file d'attente.
 */
public enum EtatVehicule {

    ENGAGE,

    ATTENTE
}
