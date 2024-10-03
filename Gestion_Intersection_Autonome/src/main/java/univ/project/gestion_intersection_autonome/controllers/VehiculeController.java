package univ.project.gestion_intersection_autonome.controllers;

import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
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
    private Shape vehiculeShape; // référence de la forme du véhicule

    public VehiculeController(Vehicule vehicule, Terrain terrain, TerrainController terrainController) {
        this.vehicule = vehicule;
        this.terrain = terrain;
        this.terrainController = terrainController;
        this.vehiculeShape = creerVehiculeShape(vehicule.getType());
    }

    public void deplacement()
    {
        anciennePosition = vehicule.getPosition().copy(); // on stocke une copie de l'objet sinon cela passe une référence

        Cellule cell1 = terrain.getCellule(anciennePosition);
        cell1.setOccupee(false);
        cell1.setIdVoiture(0);

        List<Vector2D> cellulesPotentielles = getCellulesAutour();
        vehicule.seDeplacerVersDestination(cellulesPotentielles);

        nouvellePosition = vehicule.getPosition().copy();

        Cellule cell2 = terrain.getCellule(nouvellePosition);
        cell2.setOccupee(true);
        cell2.setIdVoiture(vehicule.getId());

        // Afficher les informations de déplacement
        System.out.println("Le véhicule " + vehicule.getId() + " se déplace en " + vehicule.getPosition() + " vers " + vehicule.getPositionArrivee());
    }

// Retournes les positions des cellules ou la voiture peut se déplacer en vérifiant leur accessibilité et ne pas faire marche arrière
    public List<Vector2D> getCellulesAutour(){
        Vector2D positionActuelle = vehicule.getPosition();
        List<Vector2D> cellulesPotentielles = new ArrayList<>();

        // Récupérer la cellule actuelle
        Cellule celluleActuelle = terrain.getGrille()[positionActuelle.getX()][positionActuelle.getY()];
        boolean[] directionsAutorisees = celluleActuelle.getDirectionsAutorisees();

        // Remplir les cellules potentielles en fonction des directions autorisées
        if (directionsAutorisees[0] && positionActuelle.getY() - 1 >= 0) {
            cellulesPotentielles.add(new Vector2D(positionActuelle.getX(), positionActuelle.getY() - 1));
        }
        if (directionsAutorisees[1] && positionActuelle.getX() + 1 < terrain.getLargeur()) {
            cellulesPotentielles.add(new Vector2D(positionActuelle.getX() + 1, positionActuelle.getY()));
        }
        if (directionsAutorisees[2] && positionActuelle.getY() + 1 < terrain.getHauteur()) {
            cellulesPotentielles.add(new Vector2D(positionActuelle.getX(), positionActuelle.getY() + 1));
        }
        if (directionsAutorisees[3] && positionActuelle.getX() - 1 >= 0) {
            cellulesPotentielles.add(new Vector2D(positionActuelle.getX() - 1, positionActuelle.getY()));
        }

        return cellulesPotentielles;
    }

    @Override
    public void run()
    {
        while (!vehicule.estArrivee())
        {
            Platform.runLater(() -> { // runLater permet la maj de la partie graphique sur le thread principal
                terrainController.updateCellule(anciennePosition,nouvellePosition, vehiculeShape);
            });

            deplacement(); // voir si impact avant ou après l'affichage de la cellule

            try {
                Thread.sleep(500); // Pause entre chaque mouvement
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Le véhicule " + vehicule.getId() + " est arrivé à destination !");

        Platform.runLater(() -> {
            terrainController.effacerVehicule(nouvellePosition, vehiculeShape);
            System.out.println("Véhicule " + vehicule.getId() + " effacé");
            terrainController.getSimulation().supprimerVehicule(vehicule, this);
            System.out.println("Véhicule " + vehicule.getId() + " supprimé");
        });
    }

    private Shape creerVehiculeShape(TypeVehicule typeVehicule)
    {
        List<Color> listeCouleurs = Arrays.asList(Color.HOTPINK, Color.DEEPPINK, Color.ORANGE, Color.LIME, Color.MAGENTA, Color.CYAN, Color.PURPLE, Color.GOLD);
        int couleurRandom = new Random().nextInt(listeCouleurs.size());

        switch (typeVehicule)
        {
            case VOITURE -> {
                return new Circle(5, listeCouleurs.get(couleurRandom));
            }
/*            case URGENCE -> {
                return new Rectangle(10, 10, Color.BLUE); // voir plus tard pour alterner rouge / bleu
            }
            case BUS -> {
                return new Rectangle(10, 10, Color.BLUE); // voir plus tard
            }*/
            default -> {
                return new Circle(5, listeCouleurs.get(couleurRandom));
            }
        }
    }
}
