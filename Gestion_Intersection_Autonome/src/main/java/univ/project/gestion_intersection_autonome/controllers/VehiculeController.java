package univ.project.gestion_intersection_autonome.controllers;

import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import univ.project.gestion_intersection_autonome.classes.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class VehiculeController implements Runnable {
    private final Vehicule vehicule;
    private final Terrain terrain;
    private final TerrainController terrainController;
    private Vector2D anciennePosition;
    private Vector2D nouvellePosition;
    private Rectangle vehiculeRectangle; // référence du rectangle du véhicule
    private boolean entreeIntersection = true; // pour savoir si on rentre ou on sort d'une intersection
    private boolean majAffichageFaite = false;

    public VehiculeController(Vehicule vehicule, Terrain terrain, TerrainController terrainController) {
        this.vehicule = vehicule;
        this.terrain = terrain;
        this.terrainController = terrainController;
        this.vehiculeRectangle = creerRectangleVehicule(vehicule.getType());
    }

    @Override
    public void run() {
        while (!vehicule.estArrivee()) {
            deplacement();
            if (!majAffichageFaite) {
                mettreAJourGraphique();
                pauseEntreMouvements(1000);
            } else majAffichageFaite = false;
        }
        System.out.println("Le véhicule " + vehicule.getId() + " est arrivé à destination !");
    }

    public void deplacement() {
        anciennePosition = vehicule.getPosition().copy();

        // if cell1 est communication appeler gestion intersection sinon exécuter normalement
        if(estDansCommunication(anciennePosition) && entreeIntersection){
            System.out.println("je suis dans le if comm");
            //appeler gestion intersection qui va generer un tableau de déplacements,
            //si tout va bien elle va avancer jusqu'à sortir de l'intersection
            entrerIntersection();
            //colorier les cases (appel fonction dans terrainController et lui passer le tableau des deplacements
            majAffichageFaite = true; //j'ai mis à jour les rectangles
            entreeIntersection = false; //je suis sortie de l'intersection
        } else {
            System.out.println("je suis dans le else comm");
            deplacerHorsIntersection();
            //mettre à jour l'attribut "entree" pour savoir si on arrive de nouveau dans une intersection ou pas
            if (estDansCommunication(nouvellePosition)) {
                entreeIntersection = true;
                System.out.println("je m'apprête à entrer dans une nouvelle intersection");
            }
        }
    }
    private void deplacerHorsIntersection() {
        System.out.println("Déplacement hors intersection");
/** Il faut vérifier que la case ou on va ne dépasse pas le tableau **/
        List<Vector2D> cellulesPotentielles = getCellulesAutour(vehicule.getPosition());
        Vector2D positionSuivante = vehicule.seDeplacerVersDestination(cellulesPotentielles);
        vehicule.move(positionSuivante);
        // Afficher les informations de déplacement
        System.out.println("Le véhicule " + vehicule.getId() + " se déplace vers : " + vehicule.getPosition());
        mettreAJourCellules();
    }
    private void entrerIntersection() {
        //appeler les fonctions de  send et receive
        System.out.println("Entrée dans une intersection");
        List<Vector2D> deplacements = gestionIntersection();
        System.out.println("Itinéraire dans l'intersection : " + deplacements);
        avancerIntersection(deplacements);

        //puis appeler la gestion des priorités et des conflits
        //l'envoie des messages est géré içi (I guess ?)
    }
    private boolean estDansCommunication(Vector2D position) {
        return terrain.getCellule(position).getTypeZone() == TypeZone.COMMUNICATION;
    }

// Retournes les positions des cellules ou la voiture peut se déplacer en vérifiant leur accessibilité et ne pas faire marche arrière
    private List<Vector2D> getCellulesAutour(Vector2D positionActuelle) {
        List<Vector2D> cellulesPotentielles = new ArrayList<>();
        Cellule celluleActuelle = terrain.getGrille()[positionActuelle.getX()][positionActuelle.getY()];
        boolean[] directionsAutorisees = celluleActuelle.getDirectionsAutorisees();

        if (directionsAutorisees[0]) cellulesPotentielles.add(new Vector2D(positionActuelle.getX(), positionActuelle.getY() - 1));
        if (directionsAutorisees[1]) cellulesPotentielles.add(new Vector2D(positionActuelle.getX() + 1, positionActuelle.getY()));
        if (directionsAutorisees[2]) cellulesPotentielles.add(new Vector2D(positionActuelle.getX(), positionActuelle.getY() + 1));
        if (directionsAutorisees[3]) cellulesPotentielles.add(new Vector2D(positionActuelle.getX() - 1, positionActuelle.getY()));

        return cellulesPotentielles;
    }
    public List<Vector2D> gestionIntersection(){
        List<Vector2D> deplacements = new ArrayList<>();
        //tant que je ne suis pas dans une cellule de communication je continue sinon j'arrête et je renvoie mon tableau
        List<Vector2D> cellulesPotentielles = getCellulesAutour(vehicule.getPosition());
        Vector2D posSuivante = vehicule.seDeplacerVersDestination(cellulesPotentielles);
        deplacements.add(posSuivante);
        while (!estDansCommunication(posSuivante)) {
            cellulesPotentielles = getCellulesAutour(posSuivante);
            posSuivante = vehicule.seDeplacerVersDestination(cellulesPotentielles);
            deplacements.add(posSuivante);
        }
        return deplacements;
    }
    private void avancerIntersection(List<Vector2D> deplacements) {
        for (Vector2D pos : deplacements) {
            anciennePosition = vehicule.getPosition().copy();
            //check si la case de devant est occupé ou pas ; en mouvement ou pas
            vehicule.move(pos);
            // Afficher les informations de déplacement
            System.out.println("Le véhicule " + vehicule.getId() + " se déplace vers : " + vehicule.getPosition());
            mettreAJourCellules();
            mettreAJourGraphique();
            pauseEntreMouvements(1000);
        }
    }
    public void mettreAJourCellules(){
        nouvellePosition = vehicule.getPosition().copy();

        Cellule cell2 = terrain.getCellule(nouvellePosition);
        Cellule cell1 = terrain.getCellule(anciennePosition);

        cell1.setOccupee(false);
        cell1.setIdVoiture(0);
        cell2.setOccupee(true);
        cell2.setIdVoiture(vehicule.getId());
    }
    private void mettreAJourGraphique() {
        Platform.runLater(() -> terrainController.updateCellule(anciennePosition, nouvellePosition, vehiculeRectangle));
    }
    private void pauseEntreMouvements(int millisecondes) {
        try {
            Thread.sleep(millisecondes);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private Rectangle creerRectangleVehicule(TypeVehicule typeVehicule)
    {
        List<Color> listeCouleurs = Arrays.asList(Color.HOTPINK, Color.DEEPPINK, Color.ORANGE, Color.LIME, Color.MAGENTA, Color.CYAN, Color.PURPLE, Color.GOLD);
        int couleurRandom = new Random().nextInt(listeCouleurs.size());

        switch (typeVehicule)
        {
            case VOITURE -> {
                return new Rectangle(10, 10, listeCouleurs.get(couleurRandom));
            }
/*            case URGENCE -> {
                return new Rectangle(50, 50, Color.BLUE); // voir plus tard pour alterner rouge / bleu
            }
            case BUS -> {
                return new Rectangle(10, 10, Color.BLUE); // voir plus tard
            }*/
            default -> {
                return new Rectangle(10, 10, listeCouleurs.get(couleurRandom));
            }
        }
    }
}