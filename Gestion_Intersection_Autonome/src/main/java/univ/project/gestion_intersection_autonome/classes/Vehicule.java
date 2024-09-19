package univ.project.gestion_intersection_autonome.classes;

import java.util.Random;

public class Vehicule implements Runnable {

    // Données membres
    private final int id; // sécurise en empêchant toute modification
    private TypeVehicule type;
    private Vector2D position;
    private Vector2D positionDepart;
    private Vector2D positionArrivee;

    private static int idCompteur = 1;// génère un ID pour chaque véhicule
    private boolean enMouvement;

    // Constructeur par défaut
    public Vehicule(TypeVehicule type, Vector2D positionDepart, Vector2D positionArrivee) {
        this.id = idCompteur++; // incrément automatique
        this.type = TypeVehicule.VOITURE;
        this.position = new Vector2D(1,1);
        this.positionDepart = new Vector2D(1,1);
        this.positionArrivee = new Vector2D(1,1);
    }

    // Constructeur paramétré
    public Vehicule(TypeVehicule type, Vector2D position, Vector2D positionDepart, Vector2D positionArrivee) {
        this.id = idCompteur++;
        this.type = type;
        this.position = position;
        this.positionDepart = positionDepart;
        this.positionArrivee = positionArrivee;
        this.enMouvement = true;
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
    //lancer le thread

    @Override
    public void run() {

        Random random = new Random();

        Direction direction = Direction.values()[random.nextInt(Direction.values().length)]; //direction aléatoire

        move(direction); //avancer le véhicule

        System.out.println("Véhicule" + id + "de type" + type + "se déplacer en direction" + direction + "vers" + position); //afficher la position actuelle du véhicule

        try {
            Thread.sleep(1000); // Pause entre chaque mouvement
        } catch (InterruptedException e) {
            e.printStackTrace();
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
