package univ.project.gestion_intersection_autonome.classes;

/**
 * Représente une cellule dans le terrain du système de gestion des intersections autonomes.
 * Chaque cellule peut contenir un véhicule et possède des propriétés pour la gestion du trafic.
 */
public class Cellule {
    private int idVoiture; // Identifiant du véhicule occupant la cellule
    private Vehicule vehicule; // Référence au véhicule présent dans la cellule
    private TypeZone typeZone; // Type de zone (ex. conflit, communication)
    private boolean valide; // Indique si la cellule est une zone valide pour un véhicule
    private boolean occupee; // Indique si la cellule est occupée
    private final boolean[] directionsAutorisees = new boolean[8]; // Directions autorisées : N, E, S, O, etc.

    /**
     * Constructeur par défaut initialisant les attributs à leurs valeurs par défaut.
     */
    public Cellule() {
        this.idVoiture = 0;
        this.typeZone = null;
        this.valide = false;
        this.occupee = false;
    }

    /**
     * Constructeur avec paramètres pour initialiser une cellule.
     *
     * @param idVoiture Identifiant du véhicule occupant la cellule.
     * @param typeZone Type de zone (ex. conflit, communication).
     * @param valide Indique si la cellule est valide.
     * @param occupee Indique si la cellule est occupée.
     */
    public Cellule(int idVoiture, TypeZone typeZone, boolean valide, boolean occupee) {
        this.idVoiture = idVoiture;
        this.typeZone = typeZone;
        this.valide = valide;
        this.occupee = occupee;
    }

    // Getters et setters

    public int getIdVoiture() {
        return idVoiture;
    }

    public void setIdVoiture(int idVoiture) {
        this.idVoiture = idVoiture;
    }

    public Vehicule getVehicule() {
        return vehicule;
    }

    public void setVehicule(Vehicule vehicule) {
        this.vehicule = vehicule;
    }

    public TypeZone getTypeZone() {
        return typeZone;
    }

    public void setTypeZone(TypeZone typeZone) {
        this.typeZone = typeZone;
    }

    public boolean estValide() {
        return valide;
    }

    public void estValide(boolean valide) {
        this.valide = valide;
    }

    public boolean estOccupee() {
        return occupee;
    }

    /**
     * Définit si la cellule est occupée.
     * La méthode est synchronisée pour garantir la cohérence dans un contexte multithread.
     *
     * @param occupee `true` si la cellule est occupée, `false` sinon.
     */
    synchronized public void setOccupee(boolean occupee) {
        this.occupee = occupee;
    }

    /**
     * Définit les directions autorisées depuis cette cellule.
     *
     * @param nord Direction nord.
     * @param est Direction est.
     * @param sud Direction sud.
     * @param ouest Direction ouest.
     * @param nordouest Direction nord-ouest.
     * @param nordest Direction nord-est.
     * @param sudouest Direction sud-ouest.
     * @param sudest Direction sud-est.
     */
    public void setDirectionsAutorisees(boolean nord, boolean est, boolean sud, boolean ouest,
                                        boolean nordouest, boolean nordest, boolean sudouest, boolean sudest) {
        directionsAutorisees[0] = nord;
        directionsAutorisees[1] = est;
        directionsAutorisees[2] = sud;
        directionsAutorisees[3] = ouest;
        directionsAutorisees[4] = nordouest;
        directionsAutorisees[5] = nordest;
        directionsAutorisees[6] = sudouest;
        directionsAutorisees[7] = sudest;
    }

    /**
     * Retourne les directions autorisées depuis cette cellule.
     *
     * @return Un tableau de booléens indiquant les directions autorisées.
     */
    public boolean[] getDirectionsAutorisees() {
        return directionsAutorisees;
    }

    /**
     * Vérifie si une direction donnée est autorisée.
     *
     * @param d La direction à vérifier.
     * @return `true` si la direction est autorisée, `false` sinon.
     */
    public boolean isDirectionAutorisee(Direction d) {
        switch (d) {
            case NORD -> {
                return directionsAutorisees[0];
            }
            case EST -> {
                return directionsAutorisees[1];
            }
            case SUD -> {
                return directionsAutorisees[2];
            }
            case OUEST -> {
                return directionsAutorisees[3];
            }
            case NORDOUEST -> {
                return directionsAutorisees[4];
            }
            case NORDEST -> {
                return directionsAutorisees[5];
            }
            case SUDOUEST -> {
                return directionsAutorisees[6];
            }
            case SUDEST -> {
                return directionsAutorisees[7];
            }
            default -> {
                return false;
            }
        }
    }

    /**
     * Vérifie si la cellule contient un véhicule.
     *
     * @return `true` si un véhicule est présent, `false` sinon.
     */
    public boolean contientVehicule() {
        return vehicule != null;
    }
}
