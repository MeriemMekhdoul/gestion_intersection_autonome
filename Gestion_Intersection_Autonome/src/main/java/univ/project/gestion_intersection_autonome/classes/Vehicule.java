package univ.project.gestion_intersection_autonome.classes;

import java.util.List;

public class Vehicule {
    // Données membres
    private final int id; // sécurise en empêchant toute modification
    private TypeVehicule type;  //final non ?
    private Vector2D position;
    private Vector2D positionDepart;
    private Vector2D positionArrivee;
    private static int idCompteur = 1;// génère un ID pour chaque véhicule
    private boolean enMouvement;

    // Constructeur par défaut
    public Vehicule(TypeVehicule type, Vector2D positionDepart, Vector2D positionArrivee) {
        this.id = idCompteur++; // incrément automatique
        this.type = type; //TypeVehicule.VOITURE;
        this.position = positionDepart;
        this.positionDepart = positionDepart;
        this.positionArrivee = positionArrivee;

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
    public void move(Direction direction) {
        switch (direction) { // La position (0,0) se situe dans le coin supérieur gauche
            case NORD -> {
                position.setY(position.getY() - 1);
            }
            case SUD -> {
                position.setY(position.getY() + 1);
            }
            case EST -> {
                position.setX(position.getX() + 1);
            }
            case OUEST -> {
                position.setX(position.getX() - 1);
            }
        }
    }

    // se déplacer automatiquement vers la destination
    //prendre en compte les positions possibles et se déplacer vers la plus optimale selon la destination du véhicule
    public void seDeplacerVersDestination(List<Vector2D> positionsPossibles) {
        // choisir la meilleure position à prendre pour atteindre la destination
        Vector2D posoptimale = choisirPositionOptimale(positionsPossibles);

        if (posoptimale.getX() < position.getX()) {
            move(Direction.OUEST);
        } else if (posoptimale.getX() > position.getX()) {
            move(Direction.EST);
        } else if (posoptimale.getY() < position.getY()) {
            move(Direction.NORD);
        } else if (posoptimale.getY() > position.getY()) {
            move(Direction.SUD);
        }
    }

    //methode qui determine la position par laquelle le véhicule passe selon le chemin le plus court , prend un paramètre un tableau de Vector2D contenant les positions possibles
    public Vector2D choisirPositionOptimale(List<Vector2D> positionsPossibles) {
        //position optimale initialisée par le premier Vector2D du tableau
        // valeur par défaut
        Vector2D posoptimale = positionsPossibles.get(0);
        //calculer distance entre premiere position et la position de destination, celle-ci est stockée dans la variable distanceMin
        double distanceMin = DistanceVersDestination(posoptimale);

        // Parcourir toutes les positions possibles et choisir celle qui est la plus proche a la position de destination (la distance la plus courte)
        for (Vector2D position : positionsPossibles) {
            double distance = DistanceVersDestination(position);
            if (distance < distanceMin) {
                posoptimale = position;
                distanceMin = distance;
            }
        }
        return posoptimale;
    }

    // Algorithme pour calculer la distance vers la destination ; formule de la distance euclidienne.
    private double DistanceVersDestination(Vector2D position) {
        return Math.sqrt(Math.pow(positionArrivee.getX() - position.getX(), 2) +
                Math.pow(positionArrivee.getY() - position.getY(), 2));
    }

    public boolean estArrivee() {
        return position.equals(positionArrivee);
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
