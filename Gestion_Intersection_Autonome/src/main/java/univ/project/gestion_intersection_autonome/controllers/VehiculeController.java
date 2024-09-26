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
    private boolean entree = true; // pour savoir si on rentre ou on sort d'une intersection
    private boolean done = false;

    public VehiculeController(Vehicule vehicule, Terrain terrain, TerrainController terrainController) {
        this.vehicule = vehicule;
        this.terrain = terrain;
        this.terrainController = terrainController;
        this.vehiculeRectangle = creerRectangleVehicule(vehicule.getType());
    }
    public void deplacement()
    {
        anciennePosition = vehicule.getPosition().copy(); // on stocke une copie de l'objet sinon cela passe une référence

        // if cell1 est communication appeler gestion intersection sinon exécuter normalement
        if((terrain.getCellule(anciennePosition).getTypeZone() == TypeZone.COMMUNICATION) && entree){
            System.out.println("je suis dans le if comm");
            //appeler gestion intersection qui va generer un tableau de déplacements,
            List<Vector2D> deplacements = gestionIntersection();
            System.out.println("-------------- ");
            System.out.println("itinéraire dans l'intersection ");
            for (Vector2D d : deplacements ) {
                System.out.println("pos = " + d);
            }
            System.out.println("-------------- ");
            //colorier les cases (appel fonction dans terrainController et lui passer le tableau des deplacements
            /* ********* */
            //puis appeler la gestion des priorités et des conflits

            //si tout va bien elle va avancer jusqu'à sortir de l'intersection
            avancerIntersection(deplacements);
            done = true; //j'ai mis à jour les rectangles
            entree = false; //je suis sortie de l'intersection
            //l'envoie des messages est géré içi (I guess ?)

        } else {
            System.out.println("je suis dans le else comm");
            List<Vector2D> cellulesPotentielles = getCellulesAutour(vehicule.getPosition());
            Vector2D positionSuivante = vehicule.seDeplacerVersDestination(cellulesPotentielles);
            vehicule.move(positionSuivante);
            updateCellule();
            // Afficher les informations de déplacement
            System.out.println("Le véhicule " + vehicule.getId() + " se déplace vers : " + vehicule.getPosition());

            //mettre à jour l'attribut "entree" pour savoir si on arrive de nouveau dans une intersection ou pas
            if (terrain.getCellule(nouvellePosition).getTypeZone() == TypeZone.COMMUNICATION) {
                entree = true;
                System.out.println("je m'apprête à entrer dans une nouvelle intersection");
            }
        }
    }

// Retournes les positions des cellules ou la voiture peut se déplacer en vérifiant leur accessibilité et ne pas faire marche arrière
    public List<Vector2D> getCellulesAutour(Vector2D positionActuelle){
        //Vector2D positionActuelle = vehicule.getPosition();
        List<Vector2D> cellulesPotentielles = new ArrayList<>();

        // Récupérer la cellule actuelle
        Cellule celluleActuelle = terrain.getGrille()[positionActuelle.getX()][positionActuelle.getY()];
        boolean[] directionsAutorisees = celluleActuelle.getDirectionsAutorisees();

        // Remplir les cellules potentielles en fonction des directions autorisées
        if (directionsAutorisees[0]) {
            cellulesPotentielles.add(new Vector2D(positionActuelle.getX(), positionActuelle.getY() - 1));
        }
        if (directionsAutorisees[1]) {
            cellulesPotentielles.add(new Vector2D(positionActuelle.getX() + 1, positionActuelle.getY()));
        }
        if (directionsAutorisees[2]) {
            cellulesPotentielles.add(new Vector2D(positionActuelle.getX(), positionActuelle.getY() + 1));
        }
        if (directionsAutorisees[3]) {
            cellulesPotentielles.add(new Vector2D(positionActuelle.getX() - 1, positionActuelle.getY()));
        }

        return cellulesPotentielles;
    }

    public List<Vector2D> gestionIntersection(){
        List<Vector2D> deplacements = new ArrayList<>();
        //tant que je ne suis pas dans une cellule de communication je continue sinon j'arrête et je renvoie mon tableau
        List<Vector2D> cellulesPotentielles = getCellulesAutour(vehicule.getPosition());
        Vector2D posSuivante = vehicule.seDeplacerVersDestination(cellulesPotentielles);
        deplacements.add(posSuivante);
        while (terrain.getCellule(posSuivante).getTypeZone() != TypeZone.COMMUNICATION) {
            cellulesPotentielles = getCellulesAutour(posSuivante);
            posSuivante = vehicule.seDeplacerVersDestination(cellulesPotentielles);
            deplacements.add(posSuivante);
        }
        return deplacements;
    }

    public void avancerIntersection(List<Vector2D> deplacements){
        for (Vector2D pos :deplacements) {
            anciennePosition = vehicule.getPosition().copy();
            vehicule.move(pos);
            // Afficher les informations de déplacement
            System.out.println("Le véhicule " + vehicule.getId() + " se déplace vers : " + vehicule.getPosition());
            updateCellule();

            Platform.runLater(() -> { // runLater permet la maj de la partie graphique sur le thread principal
                terrainController.updateCellule(anciennePosition,nouvellePosition, vehiculeRectangle);
            });

            try {
                Thread.sleep(1000); // Pause entre chaque mouvement
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    public void updateCellule(){

        nouvellePosition = vehicule.getPosition().copy();

        Cellule cell2 = terrain.getCellule(nouvellePosition);
        Cellule cell1 = terrain.getCellule(anciennePosition);

        cell1.setOccupee(false);
        cell1.setIdVoiture(0);
        cell2.setOccupee(true);
        cell2.setIdVoiture(vehicule.getId());
    }
    @Override
    public void run() {
        while (!vehicule.estArrivee()) {
            deplacement();
            if (!done){
                Platform.runLater(() -> { // runLater permet la maj de la partie graphique sur le thread principal
                    terrainController.updateCellule(anciennePosition,nouvellePosition, vehiculeRectangle);
                });

                try {
                    Thread.sleep(1000); // Pause entre chaque mouvement
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else done = false;
        }
        System.out.println("Le véhicule " + vehicule.getId() + " est arrivé à destination !");
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