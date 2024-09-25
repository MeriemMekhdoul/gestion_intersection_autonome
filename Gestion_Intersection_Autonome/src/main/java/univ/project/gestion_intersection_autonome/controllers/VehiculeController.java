package univ.project.gestion_intersection_autonome.controllers;

import javafx.application.Platform;
import univ.project.gestion_intersection_autonome.classes.*;

import java.util.ArrayList;
import java.util.List;

public class VehiculeController implements Runnable {
    private final Vehicule vehicule;
    private final Terrain terrain;
    private final TerrainController terrainController;
    private Vector2D anciennePosition;
    private Vector2D nouvellePosition;

    public VehiculeController(Vehicule vehicule, Terrain terrain, TerrainController terrainController) {
        this.vehicule = vehicule;
        this.terrain = terrain;
        this.terrainController = terrainController;
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
        System.out.println("Le véhicule " + vehicule.getId() + " se déplace vers : " + vehicule.getPosition());
    }

// Retournes les positions des cellules ou la voiture peut se déplacer en vérifiant leur accessibilité et ne pas faire marche arrière
    public List<Vector2D> getCellulesAutour(){
        Vector2D positionActuelle = vehicule.getPosition();
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

    @Override
    public void run()
    {
        while (!vehicule.estArrivee())
        {
            deplacement();

            Platform.runLater(() -> { // runLater permet la maj de la partie graphique sur le thread principal
                terrainController.updateCellule(anciennePosition,nouvellePosition);
            });

            try {
                Thread.sleep(1000); // Pause entre chaque mouvement
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Le véhicule " + vehicule.getId() + " est arrivé à destination !");
    }
}
