package univ.project.gestion_intersection_autonome.classes;

/**
 * Enumération des types de zones dans une intersection autonome.
 *
 * Ces zones définissent les différentes parties fonctionnelles de l'intersection.
 */
public enum TypeZone
{
    /**
     * Zone de conflit, où plusieurs véhicules peuvent se croiser.
     */
    CONFLIT,

    /**
     * Zone de communication, utilisée pour l'échange d'informations entre véhicules.
     */
    COMMUNICATION,

    /**
     * Zone de route, correspondant aux voies normales de circulation.
     */
    ROUTE
}
