package univ.project.gestion_intersection_autonome.controleurs;

import javafx.animation.TranslateTransition;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import univ.project.gestion_intersection_autonome.classes.Simulation;
import univ.project.gestion_intersection_autonome.classes.Agents.Vehicule;
import univ.project.gestion_intersection_autonome.classes.Enums.Direction;
import univ.project.gestion_intersection_autonome.classes.Enums.TypeZone;
import univ.project.gestion_intersection_autonome.classes.Terrain.Cellule;
import univ.project.gestion_intersection_autonome.classes.Terrain.Terrain;
import univ.project.gestion_intersection_autonome.classes.Terrain.Vector2D;

import java.net.URL;
import java.util.*;

/**
 * Contrôleur pour gérer l'affichage et les interactions avec le terrain et les véhicules
 * dans une simulation d'intersection autonome.
 *
 * Cette classe fournit des fonctionnalités pour dessiner la grille, gérer les itinéraires des véhicules
 * et mettre à jour leurs positions sur le terrain.
 */
public class TerrainController implements Initializable {

    /** Grille initiale représentant le terrain. */
    @FXML
    private GridPane grilleInitiale;

    /** Pane contenant les formes représentant les véhicules. */
    @FXML
    Pane vehiclePane;

    /** VBox représentant l'intersection. */
    @FXML
    private VBox intersection;

    /** Terrain simulé. */
    private Terrain terrain;

    /** Simulation en cours. */
    private Simulation simulation;

    /** Taille de chaque cellule de la grille. */
    public final int TAILLE_CELLULE = 20;

    /** Map associant les positions à leurs StackPane correspondants dans la grille. */
    private Map<Vector2D, StackPane> mapStackPanes = new HashMap<>();

    /** Map associant les positions aux rectangles représentant les itinéraires des véhicules. */
    private Map<Vector2D, ArrayList<Rectangle>> rectanglesItineraires = new HashMap<>();

    private static final Image intersectionImage = new Image(TerrainController.class.getResource("/images/terrain/intersection.jpg").toExternalForm());
    private static final Image roadImage = new Image(TerrainController.class.getResource("/images/terrain/road.jpg").toExternalForm());
    private static final Image grassImage = new Image(TerrainController.class.getResource("/images/terrain/grass.jpg").toExternalForm());


    /**
     * Constructeur du contrôleur de terrain.
     *
     * @param simulation La simulation associée au terrain.
     */
    public TerrainController(Simulation simulation) {
        this.simulation = simulation;
        this.terrain = simulation.getTerrain();
    }

    /**
     * Retourne le VBox représentant l'intersection.
     *
     * @return Le VBox de l'intersection.
     */
    public VBox getIntersection() {
        return intersection;
    }

    /**
     * Initialise le contrôleur. Appelé automatiquement par JavaFX après le chargement de la scène.
     *
     * @param url            L'URL de localisation.
     * @param resourceBundle Les ressources associées.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dessinerGrille();
    }

    /**
     * Définit le terrain à utiliser dans le contrôleur.
     *
     * @param terrain Le terrain à utiliser.
     */
    public void setTerrain(Terrain terrain) {
        this.terrain = terrain;
    }

    /**
     * Dessine la grille représentant le terrain.
     * La couleur de chaque cellule est définie en fonction de son type (route, zone de conflit, communication, etc.).
     */
    private void dessinerGrille() {
        Cellule[][] grille = terrain.getGrille();
        grilleInitiale.getChildren().clear();
        mapStackPanes.clear();

        for (int i = 0; i < terrain.getLargeur(); i++) {
            for (int j = 0; j < terrain.getHauteur(); j++) {
                StackPane stackPane = new StackPane();
                Rectangle rectBackground = new Rectangle(TAILLE_CELLULE, TAILLE_CELLULE);
                setFondCellule(rectBackground, grille[i][j]);
                stackPane.getChildren().add(rectBackground);

                if (grille[i][j].getTypeZone() == TypeZone.CONFLIT || grille[i][j].getTypeZone() == TypeZone.COMMUNICATION) {
                    Rectangle rectItineraire = new Rectangle(TAILLE_CELLULE, TAILLE_CELLULE);
                    rectItineraire.setFill(Color.TRANSPARENT);
                    rectItineraire.setUserData("rectItineraire");
                    stackPane.getChildren().add(rectItineraire);
                }

                grilleInitiale.add(stackPane, i, j);
                mapStackPanes.put(new Vector2D(i, j), stackPane);
            }
        }
    }

    /**
     * Dessine l'itinéraire d'un véhicule sur le terrain.
     *
     * @param itineraire L'itinéraire à dessiner.
     * @param vehicule   Le véhicule associé à l'itinéraire.
     */
    public synchronized void dessinerItineraire(ArrayList<Vector2D> itineraire, Vehicule vehicule) {
        for (Vector2D position : itineraire) {
            Cellule cellule = terrain.getCellule(position);
            if (cellule != null && (cellule.getTypeZone() == TypeZone.COMMUNICATION || cellule.getTypeZone() == TypeZone.CONFLIT)) {
                StackPane stackPane = mapStackPanes.get(position);
                if (stackPane != null) {
                    Rectangle rectItineraire = new Rectangle(TAILLE_CELLULE / 4, TAILLE_CELLULE / 4);
                    rectItineraire.setFill(vehicule.getCouleur().deriveColor(0, 1.0, 1.0, 0.5));
                    rectItineraire.setUserData("rectItineraire_" + vehicule.getId());
                    stackPane.getChildren().add(rectItineraire);
                    rectanglesItineraires.computeIfAbsent(position, k -> new ArrayList<>()).add(rectItineraire);
                }
            }
        }
    }

    /**
     * Efface l'itinéraire d'un véhicule à une position donnée.
     *
     * @param vehicule Le véhicule dont l'itinéraire doit être effacé.
     * @param position La position où l'itinéraire doit être supprimé.
     */
    public synchronized void effacerItineraire(Vehicule vehicule, Vector2D position) {
        List<Rectangle> listeRectangles = rectanglesItineraires.get(position);
        if (listeRectangles != null) {
            Iterator<Rectangle> iterator = listeRectangles.iterator();
            while (iterator.hasNext()) {
                Rectangle rect = iterator.next();
                if (rect.getUserData().equals("rectItineraire_" + vehicule.getId())) {
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

    /**
     * Définit le fond d'une cellule en fonction de son type.
     *
     * @param rect    Le rectangle représentant la cellule.
     * @param cellule La cellule associée.
     */
    private void setFondCellule(Rectangle rect, Cellule cellule)
    {
        if (cellule.estValide())
        {
            rect.setFill(new ImagePattern(roadImage));

            if (cellule.getTypeZone() == TypeZone.CONFLIT || cellule.getTypeZone() == TypeZone.COMMUNICATION) {
                rect.setFill(new ImagePattern(intersectionImage));
            }
        } else {
            rect.setFill(new ImagePattern(grassImage));
        }
    }


    /**
     * Met à jour la position d'un véhicule sur la grille.
     *
     * @param anciennePosition La position précédente du véhicule.
     * @param nouvellePosition La nouvelle position du véhicule.
     * @param vehiculeShape    La forme représentant le véhicule.
     */
    public void updateCellule(Vector2D anciennePosition, Vector2D nouvellePosition, Shape vehiculeShape) {
        if (!anciennePosition.equals(nouvellePosition)) {
            effacerVehicule(anciennePosition, vehiculeShape);
        }
        dessinerVehicule(nouvellePosition, vehiculeShape);
    }

    /**
     * Dessine un véhicule à une position donnée.
     *
     * @param position      La position où dessiner le véhicule.
     * @param vehiculeShape La forme représentant le véhicule.
     */
    public void dessinerVehicule(Vector2D position, Shape vehiculeShape) {
        StackPane cellule = mapStackPanes.get(position);
        if (cellule != null && !cellule.getChildren().contains(vehiculeShape)) {
            cellule.getChildren().add(vehiculeShape);
        }
    }

    /**
     * Efface un véhicule de la grille à une position donnée.
     *
     * @param position      La position où effacer le véhicule.
     * @param vehiculeShape La forme représentant le véhicule.
     */
    public void effacerVehicule(Vector2D position, Shape vehiculeShape) {
        StackPane cellule = mapStackPanes.get(position);
        if (cellule != null) {
            cellule.getChildren().remove(vehiculeShape);
        }
    }

    /**
     * Retourne la simulation associée.
     *
     * @return La simulation.
     */
    public Simulation getSimulation() {
        return simulation;
    }

    /**
     * Anime le déplacement d'un véhicule entre deux positions sur la grille.
     *
     * Cette méthode utilise une transition de type `TranslateTransition` pour animer
     * le déplacement d'une forme représentant un véhicule.
     *
     * @param vehiculeShape    La forme représentant le véhicule à animer.
     * @param anciennePosition La position initiale du véhicule.
     * @param nouvellePosition La position finale du véhicule.
     * @param dureeMs          La durée de l'animation en millisecondes.
     */
    public void animerDeplacementVehicule(Shape vehiculeShape, Vector2D anciennePosition, Vector2D nouvellePosition, int dureeMs)
    {
        double centerX = nouvellePosition.getX() * TAILLE_CELLULE + TAILLE_CELLULE / 2;
        double centerY = nouvellePosition.getY() * TAILLE_CELLULE + TAILLE_CELLULE / 2;

        // calcul rotation
        Direction direction = getDirection(anciennePosition, nouvellePosition);
        int rotation = getRotationFromDirection(direction);

        // centrage du véhicule
        double vehiculeWidth = ((Rectangle) vehiculeShape).getWidth();
        double vehiculeHeight = ((Rectangle) vehiculeShape).getHeight();
        double offsetX = centerX - vehiculeWidth / 2;
        double offsetY = centerY - vehiculeHeight / 2;

        // rotation
        vehiculeShape.getTransforms().clear();
        vehiculeShape.getTransforms().add(new Rotate(rotation, vehiculeWidth / 2, vehiculeHeight / 2));

        // animation
        TranslateTransition transition = new TranslateTransition(Duration.millis(dureeMs), vehiculeShape);
        transition.setToX(offsetX);
        transition.setToY(offsetY);

        transition.play();
    }


    public Direction getDirection(Vector2D anciennePosition, Vector2D nouvellePosition)
    {
        Direction direction = Direction.NORD;

        // fixe la position de départ
        if (anciennePosition.equals(nouvellePosition))
        {
            int x = nouvellePosition.getX();
            int y = nouvellePosition.getY();

            if (y == 0) {
                direction = Direction.SUD;
            }
            else if (x == 0) {
                direction = Direction.EST;
            }
        }

        int deltaX = nouvellePosition.getX() - anciennePosition.getX();
        int deltaY = nouvellePosition.getY() - anciennePosition.getY();

        if (deltaX == 0 && deltaY == 1) {
            direction = Direction.SUD;
        }
        else if (deltaX == 0 && deltaY == -1) {
            direction = Direction.NORD;
        }
        else if (deltaX == 1 && deltaY == 0) {
            direction = Direction.EST;
        }
        else if (deltaX == -1 && deltaY == 0) {
            direction = Direction.OUEST;
        }
        else if (deltaX == 1 && deltaY == 1) {
            direction = Direction.SUDEST;
        }
        else if (deltaX == -1 && deltaY == 1) {
            direction = Direction.SUDOUEST;
        }
        else if (deltaX == 1 && deltaY == -1) {
            direction = Direction.NORDEST;
        }
        else if (deltaX == -1 && deltaY == -1) {
            direction = Direction.NORDOUEST;
        }
        
        return direction;
    }

    public int getRotationFromDirection(Direction direction)
    {
        return switch (direction)
        {
            case NORD -> 0;
            case SUD -> 180;
            case EST -> 90;
            case OUEST -> -90;
            case NORDEST -> 45;
            case NORDOUEST -> -45;
            case SUDEST -> 135;
            case SUDOUEST -> -135;
        };
    }
}