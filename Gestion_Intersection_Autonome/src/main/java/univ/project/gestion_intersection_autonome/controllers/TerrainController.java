package univ.project.gestion_intersection_autonome.controllers;

import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Shape;
import univ.project.gestion_intersection_autonome.classes.*;


import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.net.URL;
import java.util.*;

public class TerrainController implements Initializable {
    @FXML
    private GridPane grilleInitiale;
    private Terrain terrain;
    private Simulation simulation;
    public final int TAILLE_CELLULE = 15;

    private Map<Vector2D, StackPane> mapStackPanes = new HashMap<>(); // stackspanes de chaque élément de la grille
    private Map<Vector2D, ArrayList<Rectangle>> rectanglesItineraires = new HashMap<>();

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
                Rectangle rectBackground = new Rectangle(TAILLE_CELLULE,TAILLE_CELLULE);
                setCouleurCellule(rectBackground, grille[i][j]);

                stackPane.getChildren().add(rectBackground);

                // Créer un rectangle pour l'itinéraire
                if (grille[i][j].getTypeZone() == TypeZone.CONFLIT || grille[i][j].getTypeZone() == TypeZone.COMMUNICATION) {
                    Rectangle rectItineraire = new Rectangle(TAILLE_CELLULE, TAILLE_CELLULE);
                    rectItineraire.setFill(Color.TRANSPARENT);
                    rectItineraire.setUserData("rectItineraire");
                    stackPane.getChildren().add(rectItineraire);
                }

                grilleInitiale.add(stackPane, i, j); // Ajouter la stack pane à la position (i , j) de la grille

                Vector2D position = new Vector2D(i, j); // ajout de la stackpane dans la map
                mapStackPanes.put(position, stackPane);
            }
        }
    }

    // Permet de dessiner l'itinéraire des véhicules lors de la traversée d'une intersection
    public synchronized void dessinerItineraire(ArrayList<Vector2D> itineraire, Vehicule vehicule)
    {
        for (Vector2D position : itineraire) // parcourt de l'itinéraire
        {
            Cellule cellule = terrain.getCellule(position);

            // affichage uniquement sur les zones de communication et conflit
            if (cellule != null && (cellule.getTypeZone() == TypeZone.COMMUNICATION || cellule.getTypeZone() == TypeZone.CONFLIT))
            {
                StackPane stackPane = mapStackPanes.get(position);

                if (stackPane != null)
                {
                    Rectangle rectItineraire = new Rectangle(TAILLE_CELLULE, TAILLE_CELLULE);
                    rectItineraire.setFill(vehicule.getCouleur().deriveColor(0, 1.0, 1.0, 0.5)); // couleur transparente
                    rectItineraire.setUserData("rectItineraire_" + vehicule.getId()); // lié à l'id de la voiture pour éviter les conflits

                    // il faut créer un rectangle pour chaque véhicule pour pouvoir les superposer
                    stackPane.getChildren().add(rectItineraire);

                    // verifie si une liste de rectangles existe déjà pour la position donnée dans la map rectanglesItineraires
                    // si ce n'est pas le cas, elle crée une nouvelle arraylist
                    // ajout le rectItineraire à la liste associée à cette position
                    rectanglesItineraires.computeIfAbsent(position, k -> new ArrayList<>()).add(rectItineraire);
                }
            }
        }
    }

    // synchronized evite les pb de concurrence entre les threads
    public synchronized void effacerItineraire(Vehicule vehicule, Vector2D position)
    {
        List<Rectangle> listeRectangles = rectanglesItineraires.get(position);

        if (listeRectangles != null)
        {
            Iterator<Rectangle> iterator = listeRectangles.iterator(); // permet de parcourir la liste

            while (iterator.hasNext())
            {
                Rectangle rect = iterator.next();

                if (rect.getUserData().equals("rectItineraire_" + vehicule.getId()))
                {
                    StackPane stackPane = mapStackPanes.get(position);

                    if (stackPane != null) {
                        stackPane.getChildren().remove(rect);
                    }
                    iterator.remove();
                    break;
                }
            }
            if (listeRectangles.isEmpty()) {
                rectanglesItineraires.remove(position);
            }
        }
    }


    // Définir la couleur de la cellule selon son type
    private void setCouleurCellule(Rectangle rect, Cellule cellule) {
        if (cellule.estValide()) {
            rect.setFill(Color.DARKSLATEGRAY);  // Route
            if (cellule.getTypeZone() == TypeZone.CONFLIT) {
                rect.setFill(Color.BLACK);  // Zone de conflit
            } else if (cellule.getTypeZone() == TypeZone.COMMUNICATION) {
                rect.setFill(Color.BLACK);  // Communication
            }
        } else {
            rect.setFill(Color.FORESTGREEN); // Espace vide
        }
    }

    public void updateCellule(Vector2D anciennePosition, Vector2D nouvellePosition, Shape vehiculeShape)
    {
        if (!anciennePosition.equals(nouvellePosition)) {
            effacerVehicule(anciennePosition, vehiculeShape);
        }
        dessinerVehicule(nouvellePosition, vehiculeShape);
    }


    public void dessinerVehicule(Vector2D position, Shape vehiculeShape)
    {
        StackPane cellule = mapStackPanes.get(position);

        if (cellule != null) {
            if (!cellule.getChildren().contains(vehiculeShape))
            {
                // gestion couleur véhicule d'urgence
                if (vehiculeShape.getFill() == Color.BLUE) {
                    vehiculeShape.setFill(Color.RED);
                }
                else if (vehiculeShape.getFill() == Color.RED){
                    vehiculeShape.setFill(Color.BLUE);
                }

                cellule.getChildren().add(vehiculeShape);
                //System.out.println("Véhicule dessiné à la cellule : " + position);
            } else {
                //System.out.println("Véhicule déjà présent dans la cellule : " + position); // à supprimer après vérif case occupée
            }
        }

        //System.out.println("Le véhicule a été dessiné");
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
