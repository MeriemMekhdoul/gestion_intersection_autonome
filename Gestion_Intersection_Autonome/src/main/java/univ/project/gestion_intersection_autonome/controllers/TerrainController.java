package univ.project.gestion_intersection_autonome.controllers;

import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Shape;
import univ.project.gestion_intersection_autonome.classes.*;


import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class TerrainController implements Initializable {
    @FXML
    private GridPane grilleInitiale;
    private Terrain terrain;
    private Simulation simulation;

    private Map<Vector2D, StackPane> mapStackPanes = new HashMap<>(); // stackspanes de chaque élément de la grille

    public TerrainController(Simulation simulation) { //constructeur
        this.simulation = simulation;
        terrain = simulation.getTerrain();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dessinerGrille();
    }
    public void setTerrain(Terrain terrain) {
        this.terrain = terrain;
    }

    private void dessinerGrille()
    {
        Cellule[][] grille = terrain.getGrille();
        int tailleCellule = 10; //param fixe en dehors ??

        // Vider la grille avant de dessiner si elle contient déjà des éléments
        grilleInitiale.getChildren().clear();

        // Vider la map si elle contient déjà des éléments
        mapStackPanes.clear();

        for (int i = 0; i < terrain.getLargeur(); i++)
        {
            for (int j = 0; j < terrain.getHauteur(); j++)
            {
                // Créer une StackPane pour chaque cellule
                StackPane stackPane = new StackPane();

                // Créer un rectangle pour chaque cellule
                Rectangle rect = new Rectangle(tailleCellule,tailleCellule);
                setCouleurCellule(rect, grille[i][j]);

                stackPane.getChildren().add(rect);

                grilleInitiale.add(stackPane, i, j); // Ajouter la stack pane à la position (i , j) de la grille

                Vector2D position = new Vector2D(i, j); // ajout de la stackpane dans la map
                mapStackPanes.put(position, stackPane);
            }
        }
    }

    // Définir la couleur de la cellule selon son type
    private void setCouleurCellule(Rectangle rect, Cellule cellule) {
        if (cellule.estValide()) {
            rect.setFill(Color.GREY);  // Route
            if (cellule.getTypeZone() == TypeZone.CONFLIT) {
                rect.setFill(Color.GREY);  // Zone de conflit
            } else if (cellule.getTypeZone() == TypeZone.COMMUNICATION) {
                rect.setFill(Color.LIGHTCYAN);  // Communication
            }
        } else {
            rect.setFill(Color.BLUE); // Espace vide
        }
    }

    public void updateCellule(Vector2D anciennePosition, Vector2D nouvellePosition, Shape vehiculeShape)
    {
        effacerVehicule(anciennePosition, vehiculeShape);
        dessinerVehicule(nouvellePosition, vehiculeShape);
    }

    public void dessinerVehicule(Vector2D position, Shape vehiculeShape)
    {
        StackPane cellule = mapStackPanes.get(position);

        if (cellule != null) {
            if (!cellule.getChildren().contains(vehiculeShape)) {
                cellule.getChildren().add(vehiculeShape);
            } else {
                System.out.println("Véhicule déjà présent dans la cellule : " + position); // à supprimer après vérif case occupée
            }
        }
    }

    public void effacerVehicule(Vector2D position, Shape vehiculeShape)
    {
        StackPane cellule = mapStackPanes.get(position);

        if (cellule != null) {
            cellule.getChildren().remove(vehiculeShape);
        }
    }

    public Simulation getSimulation() {
        return simulation;
    }
}
