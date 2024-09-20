package univ.project.gestion_intersection_autonome.controllers;

import javafx.beans.binding.Bindings;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import univ.project.gestion_intersection_autonome.classes.Cellule;
import univ.project.gestion_intersection_autonome.classes.Simulation;
import univ.project.gestion_intersection_autonome.classes.Terrain;


import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import univ.project.gestion_intersection_autonome.classes.TypeZone;

import java.net.URL;
import java.util.ResourceBundle;

public class TerrainController implements Initializable {
    @FXML
    private GridPane grilleInitiale;
    private Terrain terrain;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        dessinerGrille();
    }

    public TerrainController(Simulation s) { //constructeur
        terrain = s.getTerrain();
    }

    public void setTerrain(Terrain terrain) {
        this.terrain = terrain;
    }

    public Terrain getTerrain() {
        return terrain;
    }

    private void dessinerGrille() {
        Cellule[][] grille_c = terrain.getGrille();
        int tailleCellule = 10; //param fixe en dehors ??

        // Vider la grille avant de dessiner si elle contient déjà des éléments
        grilleInitiale.getChildren().clear();

        for (int i = 0; i < terrain.getHauteur(); i++) {
            for (int j = 0; j < terrain.getLargeur(); j++) {

                // Créer une StackPane pour chaque cellule
                StackPane stackPane = new StackPane();

                // Créer un rectangle pour chaque cellule
                Rectangle rect = new Rectangle(tailleCellule,tailleCellule);

                // Définir la couleur du rectangle en fonction du contenu de la cellule
                if (grille_c[i][j].estValide()) {
                    rect.setFill(Color.GRAY);  // Route en gris
                    if (grille_c[i][j].getTypeZone() == TypeZone.CONFLIT) {
                        rect.setFill(Color.RED);
                    } else if (grille_c[i][j].getTypeZone() == TypeZone.COMMUNICATION) {
                        rect.setFill(Color.YELLOW);  // Intersection en jaune
                    }
                } else {
                    rect.setFill(Color.GREEN); // Espace vide en vert
                }

                //rect.setStroke(Color.BLACK);  // Bordure noire pour mieux voir les cellules
                stackPane.getChildren().add(rect);

                grilleInitiale.add(stackPane, j, i); // Ajouter la stack pane à la position (i , j) de la grille
            }
        }
    }
}