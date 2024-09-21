package univ.project.gestion_intersection_autonome.classes;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Vehicule implements Runnable {
/** Rajouter la fonction qui prend les 3 positions possibles et choisir la direction optimale parmi ces choix et move la voiture **/
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
        this.type = type; //TypeVehicule.VOITURE;
        this.position = positionDepart;
        this.positionDepart = positionDepart; //new Vector2D(0,0);
        this.positionArrivee = positionArrivee; //new Vector2D(new Random().nextInt(26),new Random().nextInt(26));
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
/*
    public void boucleDeplacement(Terrain terrain){
        boolean valide = false;
        List<Direction> contraintes = new ArrayList<>();

        while(!valide){
            Direction D = seDeplacerVersDestination(contraintes);
            valide = validerDirection(D, terrain);
            if (!valide){
                contraintes.add(D);
            } else {
                move(D);
                valide = true;
            }
        }
    }

    public boolean validerDirection(Direction D, Terrain terrain){
        Vector2D posPotentielle = new Vector2D();
        switch (D) { // La position (0,0) se situe dans le coin supérieur gauche
            case NORD:
                posPotentielle.setY(position.getY() - 1);
                break;
            case SUD:
                posPotentielle.setY(position.getY() + 1);
                break;
            case EST:
                posPotentielle.setX(position.getX() + 1);
                break;
            case OUEST:
                posPotentielle.setX(position.getX() - 1);
                break;
        }
        if (posPotentielle.getX()< terrain.getLargeur() && posPotentielle.getX()> 0 ){
            if (posPotentielle.getY() < terrain.getHauteur() && posPotentielle.getY() > 0 ) {
                if(terrain.getGrille()[posPotentielle.getY()][posPotentielle.getX()].estValide())
                    return  true;
            }
        }
        return false;
    }
    */

    // se déplacer automatiquement vers la destination
    public void seDeplacerVersDestination()

    {
        if (position.getX() < positionArrivee.getX()) {
            move(Direction.EST);
        } else if (position.getX() > positionArrivee.getX()) {
            move(Direction.OUEST);
        } else if (position.getY() < positionArrivee.getY()) {
            move(Direction.SUD);
        } else if (position.getY() > positionArrivee.getY()) {
            move(Direction.NORD);
        }
    }

    @Override
    public void run()
    {
        while (!position.equals(positionArrivee))
        {
            /* Terrain terrain = new Terrain(25,25);
            Vector2D posD = new Vector2D(terrain.get_entrees().get(3).getX(),terrain.get_entrees().get(3).getY());
            positionDepart = posD; */
            //boucleDeplacement(terrain);

            System.out.println("Véhicule " + id + " de type " + type + " se déplace en direction de " + positionArrivee + ". Position actuelle : " + position);
            seDeplacerVersDestination();
//            System.out.println("Véhicule " + id + " de type " + type + " se déplace en direction " + direction + " vers la position " + position);

            try {
                Thread.sleep(1000); // Pause entre chaque mouvement
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Véhicule " + id + " est arrivé à destination en " + positionArrivee);
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
