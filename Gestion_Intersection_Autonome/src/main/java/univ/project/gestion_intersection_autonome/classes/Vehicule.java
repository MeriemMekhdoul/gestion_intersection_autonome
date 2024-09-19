package univ.project.gestion_intersection_autonome.classes;

public class Vehicule
{
    // Énumérations
    public enum TypeVehicule { VOITURE, URGENCE, BUS }
    public enum Direction { NORD, SUD, EST, OUEST }

    // Données membres
    private final int id; // sécurise en empêchant toute modification
    private TypeVehicule type;
    private Vector2D position;
    private Vector2D positionDepart;
    private Vector2D positionArrivee;

    private static int idCompteur = 1; // génère un ID pour chaque véhicule

    // Constructeur par défaut
    public Vehicule() {
        this.id = idCompteur++; // incrément automatique
        this.type = TypeVehicule.VOITURE;
        this.position = new Vector2D(0,0);
        this.positionDepart = new Vector2D(0,0);
        this.positionArrivee = new Vector2D(0,0);
    }

    // Constructeur paramétré
    public Vehicule(TypeVehicule type, Vector2D position, Vector2D positionDepart, Vector2D positionArrivee) {
        this.id = idCompteur++;
        this.type = type;
        this.position = position;
        this.positionDepart = positionDepart;
        this.positionArrivee = positionArrivee;
    }

    // Méthodes
    public void move(Direction direction)
    {
        switch (direction) { // La position (0,0) se situe dans le coin supérieur gauche
            case NORD:
                position.setY(position.getY() - 1);
                break;
            case SUD:
                position.setY(position.getY() + 1);
                break;
            case EST:
                position.setX(position.getX() + 1);
                break;
            case OUEST:
                position.setX(position.getX() - 1);
                break;
        }
    }

    public void sendRequest() { }

    public void openRequest() { }


    // Getters et setters
    public int getId() {
        return id;
    }

    public TypeVehicule getType() {
        return type;
    }

    public void setType(TypeVehicule type) {
        this.type = type;
    }

    public Vector2D getPosition() {
        return position;
    }

    public void setPosition(Vector2D position) {
        this.position = position;
    }

    public Vector2D getPositionDepart() {
        return positionDepart;
    }

    public void setPositionDepart(Vector2D positionDepart) {
        this.positionDepart = positionDepart;
    }

    public Vector2D getPositionArrivee() {
        return positionArrivee;
    }

    public void setPositionArrivee(Vector2D positionArrivee) {
        this.positionArrivee = positionArrivee;
    }
}
