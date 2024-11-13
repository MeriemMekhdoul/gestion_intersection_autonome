package univ.project.gestion_intersection_autonome.classes;

public class Cellule {
    private int idVoiture;
    private TypeZone typeZone ;
    private boolean valide ;
    private boolean occupee ;
    private final boolean[] directionsAutorisees = new boolean[8]; // N, E, S, O

    // Constructeur par défaut
    public Cellule() {
        this.idVoiture = 0 ;
        this.typeZone = null;
        this.valide = false;
        this.occupee = false;
    }

    // Constructeur avec paramètres
    public Cellule(int idVoiture, TypeZone typeZone, boolean valide, boolean occupee) {
        this.idVoiture = idVoiture;
        this.typeZone = typeZone;
        this.valide = valide;
        this.occupee = occupee;
    }

    // Getters et Setters

    public int getIdVoiture() {
        return idVoiture;
    }

    public void setIdVoiture(int idVoiture) {
        this.idVoiture = idVoiture;
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

    public void setOccupee(boolean occupee) {
        this.occupee = occupee;
    }

    // Méthode pour définir les directions autorisées
     public void setDirectionsAutorisees(boolean nord, boolean est, boolean sud, boolean ouest , boolean nordouest, boolean nordest, boolean sudouest, boolean sudest ) {
        directionsAutorisees[0] = nord;
        directionsAutorisees[1] = est;
        directionsAutorisees[2] = sud;
        directionsAutorisees[3] = ouest;
        directionsAutorisees[4] = nordouest;
        directionsAutorisees[5] = nordest;
        directionsAutorisees[6] = sudouest ;
        directionsAutorisees[7] = sudest ;

    }

    public boolean[] getDirectionsAutorisees() {
        return directionsAutorisees;
    }
    public boolean isDirectionAutorisee(Direction d){
        switch(d) {
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
                return  directionsAutorisees[4];
            }
            case NORDEST -> {
                return  directionsAutorisees[5];
            }
            case SUDOUEST -> {
                return  directionsAutorisees[6];
            }
            case SUDEST -> {
                return  directionsAutorisees[7];
            }

            default -> {
                return false;
            }
        }
    }
}






