package univ.project.gestion_intersection_autonome.controllers;

import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import univ.project.gestion_intersection_autonome.classes.*;


import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.net.URL;
import java.util.ResourceBundle;

public class TerrainController implements Initializable {
    @FXML
    private GridPane grilleInitiale;
    private Terrain terrain;

    public TerrainController(Simulation s) { //constructeur
        terrain = s.getTerrain();
    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dessinerGrille();
    }
    public void setTerrain(Terrain terrain) {
        this.terrain = terrain;
    }

    private void dessinerGrille() {
        Cellule[][] grille = terrain.getGrille();
        int tailleCellule = 10; //param fixe en dehors ??

        // Vider la grille avant de dessiner si elle contient déjà des éléments
        grilleInitiale.getChildren().clear();

        for (int i = 0; i < terrain.getLargeur(); i++) {
            for (int j = 0; j < terrain.getHauteur(); j++) {

                // Créer une StackPane pour chaque cellule
                StackPane stackPane = new StackPane();

                // Créer un rectangle pour chaque cellule
                Rectangle rect = new Rectangle(tailleCellule,tailleCellule);
                setCouleurCellule(rect, grille[i][j]);

                //rect.setStroke(Color.BLACK);  // Bordure noire pour mieux voir les cellules
                stackPane.getChildren().add(rect);

                grilleInitiale.add(stackPane, i, j); // Ajouter la stack pane à la position (i , j) de la grille
            }
        }
    }
    // Définir la couleur de la cellule selon son type
    private void setCouleurCellule(Rectangle rect, Cellule cellule) {
        if (cellule.estValide()) {
            rect.setFill(Color.GRAY);  // Route
            if (cellule.getTypeZone() == TypeZone.CONFLIT) {
                rect.setFill(Color.RED);  // Zone de conflit
            } else if (cellule.getTypeZone() == TypeZone.COMMUNICATION) {
                rect.setFill(Color.YELLOW);  // Communication
            }
        } else {
            rect.setFill(Color.GREEN); // Espace vide
        }
    }
    public void updateCellule(Vector2D anciennePosition, Vector2D nouvellePosition){
        /** Implémenter ici comment une case change d'état en fonction du déplacement de la voiture
         idée : on rajoute un paramètre Map<Vector2D,Stackpane> comme ça on garde pour chaque position de la grille la stackpane associée
         pour éviter de redessiner la grille à chaque déplacement de chaque voiture
         théoriquement la stackpane va s'update toute seule sur l'écran, mais à revérifier
         **/
    }
}