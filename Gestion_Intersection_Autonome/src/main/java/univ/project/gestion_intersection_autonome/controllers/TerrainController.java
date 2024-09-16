package univ.project.gestion_intersection_autonome.controllers;

import univ.project.gestion_intersection_autonome.classes.Terrain;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.net.URL;
import java.util.ResourceBundle;

public class TerrainController implements Initializable {
    @FXML
    private Pane initialGrid;
    //par la suite je vais changer la pane en un élément grid c'est plus logique et pratique

    private Terrain terrain;


    @Override
    //fonction qui s'execute lors du lancement de l'app (lancement de cette page-là donc le terrain initial)
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // Créer l'objet Terrain (il est déjà initialisé avec la grille)
        terrain = new Terrain();

        // Dessiner la grille dans le Pane
        dessinerGrille();
    }

    private void dessinerGrille(){
        char[][] grille = terrain.getGrille();
        int tailleCellule = 50;

        for (int i=0; i < terrain.getHauteur();i++){
            for (int j = 0; j < terrain.getLargeur(); j++) {
                // Créer un rectangle pour chaque cellule
                Rectangle rect = new Rectangle(j * tailleCellule, i * tailleCellule, tailleCellule, tailleCellule);

                // Définir la couleur en fonction du contenu de la cellule ('R' pour route, '.' pour espace vide)
                if (grille[i][j] == 'R') {
                    rect.setFill(Color.GRAY);  // Route en gris
                } else {
                    rect.setFill(Color.GREEN);  // Espace vide en vert
                }

                rect.setStroke(Color.BLACK);  // Bordure noire pour mieux voir les cellules
                initialGrid.getChildren().add(rect);  // Ajouter le rectangle au Pane
            }
        }

    }
}
