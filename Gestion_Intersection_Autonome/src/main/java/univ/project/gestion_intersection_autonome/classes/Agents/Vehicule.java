package univ.project.gestion_intersection_autonome.classes.Agents;

import javafx.scene.paint.Color;
import univ.project.gestion_intersection_autonome.classes.Enums.TypeVehicule;
import univ.project.gestion_intersection_autonome.classes.Terrain.Vector2D;

import java.util.*;
import java.io.*;
import java.util.List;

/**
 * Classe représentant un véhicule dans une intersection autonome.
 *
 * Un véhicule est caractérisé par son identifiant unique, son type, ses positions de départ et d'arrivée,
 * sa position actuelle, son itinéraire et sa couleur. La classe gère aussi les conflits entre les itinéraires
 * des véhicules et calcule les temps d'attente dans ces cas.
 */
public class Vehicule {

    /**
     * Identifiant unique du véhicule.
     */
    private final int id;

    /**
     * Type du véhicule (par exemple VOITURE, URGENCE).
     */
    private TypeVehicule type;

    /**
     * Position actuelle du véhicule.
     */
    private Vector2D position;

    /**
     * Position de départ du véhicule.
     */
    private Vector2D positionDepart;

    /**
     * Position d'arrivée du véhicule.
     */
    private Vector2D positionArrivee;

    /**
     * Compteur pour générer les identifiants uniques des véhicules.
     */
    private static int idCompteur = 1;

    /**
     * Indique si le véhicule est actuellement en attente.
     */
    private boolean enAttente;

    /**
     * Itinéraire complet du véhicule.
     */
    private final List<Vector2D> itineraire;

    /**
     * Couleur du véhicule.
     */
    private Color couleur;



    /**
     * Constructeur paramétré.
     *
     * @param type            Type du véhicule.
     * @param positionDepart  Position de départ du véhicule.
     * @param positionArrivee Position d'arrivée du véhicule.
     * @param itineraire      Liste des positions composant l'itinéraire.
     * @param couleur         Couleur du véhicule.
     * @throws IOException Si une erreur d'entrée-sortie se produit.
     */
    public Vehicule(TypeVehicule type, Vector2D positionDepart, Vector2D positionArrivee, List<Vector2D> itineraire, Color couleur) throws IOException {
        this.id = idCompteur++;
        this.type = type;
        this.position = positionDepart.copy();
        this.positionDepart = positionDepart.copy();
        this.positionArrivee = positionArrivee.copy();
        this.enAttente = false;
        this.itineraire = itineraire;
        this.couleur = couleur;
    }

    /**
     * Déplace le véhicule à une nouvelle position.
     *
     * @param pos Nouvelle position du véhicule.
     */
    public void move(Vector2D pos) {
        position.setX(pos.getX());
        position.setY(pos.getY());
    }

    /**
     * Génère une couleur aléatoire pour un véhicule parmi une liste prédéfinie.
     *
     * @return Une couleur aléatoire.
     */
    public static Color genererCouleurAleatoire() {
        List<Color> listeCouleurs = Arrays.asList(
                Color.CRIMSON, Color.DEEPPINK, Color.MEDIUMPURPLE, Color.GOLD,
                Color.ORCHID, Color.MEDIUMSEAGREEN, Color.LIGHTSEAGREEN
        );

        int couleurRandom = new Random().nextInt(listeCouleurs.size());
        return listeCouleurs.get(couleurRandom);
    }

    public int getId() {
        return id;
    }

    public TypeVehicule getType() {
        return type;
    }

    public void setType(TypeVehicule type) {
        this.type = type;
    }

    public Vector2D getPosition() {
        return position;
    }

    public ArrayList<Vector2D> getItineraire() {
        return (ArrayList<Vector2D>) itineraire;
    }

    public Color getCouleur() {
        return couleur;
    }

    public void setCouleur(Color couleur) {
        this.couleur = couleur;
    }

    @Override
    public String toString() {
        return "Vehicule{" +
                "type=" + type +
                ", positionDepart=" + positionDepart +
                ", positionArrivee=" + positionArrivee +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Vehicule vehicule = (Vehicule) obj;
        return id == vehicule.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Modifie l'état d'attente du véhicule.
     *
     * @param enAttente {@code true} si le véhicule est en attente, sinon {@code false}.
     */
    public synchronized void setEnAttente(boolean enAttente) {
        this.enAttente = enAttente;
    }

    public synchronized boolean isEnAttente() {
        return this.enAttente;
    }

    /**
     * Gère les conflits avec les véhicules en attente et calcule le temps d'attente supplémentaire
     * en fonction des itinéraires potentiellement conflictuels.
     *
     * @param vehiculesAttenteEtItineraires Les itinéraires des véhicules en attente.
     * @param tempsAttente            Le temps d'attente déjà calculé.
     * @param monItineraire                    L'itinéraire du véhicule actuel.
     * @return Le temps d'attente supplémentaire à ajouter.
     */
    public int calculTempsAttenteVehiculesAttente(Map<Vehicule, ArrayList<Vector2D>> vehiculesAttenteEtItineraires,
                                                 int tempsAttente, ArrayList<Vector2D> monItineraire) {

        for (Map.Entry<Vehicule, ArrayList<Vector2D>> entry : vehiculesAttenteEtItineraires.entrySet()) {
            ArrayList<Vector2D> itineraireVehiculeAttente = entry.getValue();

            // Calculer l'itinéraire effectif après le temps d'attente actuel
            ArrayList<Vector2D> autreItin = extraireItineraireApresTemps(itineraireVehiculeAttente, tempsAttente);

            int temp = detectionConflitDiagonale(monItineraire,autreItin);

            if (temp == 0){ //diagonale non détectée
                //vérifier les autres cas de collisions
                temp = detectionCollisionSimple(monItineraire,autreItin);
                if (temp == 0){ //collision simple non détectée
                    //vérifier le dernier cas
                    temp = detectionCheminsCroises(monItineraire,autreItin);
                }
            }
            tempsAttente += temp;

        }

        return tempsAttente;
    }

    /**
     * Extrait les positions restantes de l'itinéraire après un certain temps d'attente.
     *
     * @param itineraire   L'itinéraire complet du véhicule.
     * @param tempsAttente Le temps d'attente écoulé.
     * @return Une liste contenant les positions restantes de l'itinéraire.
     */
    public ArrayList<Vector2D> extraireItineraireApresTemps(ArrayList<Vector2D> itineraire, int tempsAttente) {
        if (tempsAttente >= itineraire.size()) {
            return new ArrayList<>(); // L'itinéraire est vide si le temps d'attente dépasse sa taille
        }
        return new ArrayList<>(itineraire.subList(tempsAttente, itineraire.size()));
    }

    /**
     * Crée de nouveaux itinéraires basés sur les positions actuelles des véhicules.
     *
     * @param vehiculesEtItineraires Les itinéraires complets des véhicules.
     * @return Un map associant chaque véhicule à son itinéraire restant.
     */
    private Map<Vehicule, ArrayList<Vector2D>> creerNouveauxItineraires(Map<Vehicule, ArrayList<Vector2D>> vehiculesEtItineraires, boolean engage) {
        Map<Vehicule, ArrayList<Vector2D>> nouveauxItineraires = new HashMap<>();

        for (Vehicule vehicule : vehiculesEtItineraires.keySet()) {
            ArrayList<Vector2D> itineraireComplet = vehiculesEtItineraires.get(vehicule);
            ArrayList<Vector2D> nouvelItineraire = extraireItineraireRestant(vehicule, itineraireComplet,engage);

            if (nouvelItineraire != null){
                nouveauxItineraires.put(vehicule, nouvelItineraire);
            }
        }

        return nouveauxItineraires;
    }

    /**
     * Extrait les positions restantes de l'itinéraire en fonction de la position actuelle du véhicule.
     *
     * @param vehicule          Le véhicule dont l'itinéraire est analysé.
     * @param itineraireComplet L'itinéraire complet du véhicule.
     * @param engage            L'état du véhicule, engagé ou en attente
     * @return Une liste contenant les positions restantes.
     */
    private ArrayList<Vector2D> extraireItineraireRestant(Vehicule vehicule, ArrayList<Vector2D> itineraireComplet, boolean engage) {
        Vector2D positionActuelle = vehicule.getPosition();
        int index = itineraireComplet.indexOf(positionActuelle);

        if (index == -1) {
            if (engage) //il s'est engagé donc si la position n'existe pas, c'est qu'il est sorti de l'intersection
                return null;
            else // en attente, sa posActuelle n'est pas dans l'itinéraire car il ne s'est pas encore engagé
                return itineraireComplet;
        }

        // Extraire le sous-itinéraire restant
        return new ArrayList<>(itineraireComplet.subList(index, itineraireComplet.size()));

    }

    public int calculTempsAttenteVehiculesEngages(Map<Vehicule, ArrayList<Vector2D>> vehiculesEngagesEtItineraires, ArrayList<Vector2D> monItineraire){
        //trunk les itinéraires à partir de la position actuelle de chaque véhicule
        Map<Vehicule, ArrayList<Vector2D>> nouveauxItinerairesEtVehicules = creerNouveauxItineraires(vehiculesEngagesEtItineraires,true);

        int tempsAttente = 0;
        for (Vehicule autreVehicule: nouveauxItinerairesEtVehicules.keySet()) {
            int temp;
            ArrayList<Vector2D> itin = nouveauxItinerairesEtVehicules.get(autreVehicule);
            ArrayList<Vector2D> autreItin = extraireItineraireApresTemps(itin,tempsAttente);
            temp = detectionConflitDiagonale(monItineraire,autreItin);

            if (temp == 0){ //diagonale non détectée
                //vérifier les autres cas de collisions
                temp = detectionCollisionSimple(monItineraire,autreItin);
                if (temp == 0){ //collision simple non détectée
                    //vérifier le dernier cas
                    temp = detectionCheminsCroises(monItineraire,autreItin);
                }
            }
            //pour chaque vehicule je décide si je garde l'ancien temps calculé parce qu'il est supérieur au nouveau temps spécifique
            //au vehicule actuel, donc tempsAttente couvre la collision, sinon je prends le nouveau car supérieur
            tempsAttente += temp;
        }
        
        return tempsAttente;
    }


    /**
     * Ajoute un nombre spécifié de positions null au début d'un itinéraire (liste de Vector2D).
     *
     * @param itineraire   L'itinéraire d'origine sous forme d'une liste de Vector2D.
     * @param tempsAttente Le nombre de positions null à ajouter au début de l'itinéraire.
     * @return Une nouvelle liste contenant les éléments de l'itinéraire d'origine précédés de `tempsAttente` positions null.
     *         Si `tempsAttente` est 0, l'itinéraire d'origine est retourné tel quel.
     */
    public ArrayList<Vector2D> rallongerItineraire(ArrayList<Vector2D> itineraire, int tempsAttente, Vector2D position) {
        // Créer une nouvelle liste pour stocker le résultat
        ArrayList<Vector2D> nouvelItineraire = new ArrayList<>();

        // Ajouter 'tempsAttente' cases null au début
        for (int i = 0; i < tempsAttente; i++) {
            nouvelItineraire.add(position);
        }

        // Ajouter tous les éléments de l'itinéraire d'origine
        nouvelItineraire.addAll(itineraire);

        return nouvelItineraire;
    }

    private int detectionConflitDiagonale(ArrayList<Vector2D> monItineraire, ArrayList<Vector2D> autreItineraire){

        // Vérifie toutes les sous-parties de deux éléments successifs dans monItineraire
        for (int i = 0; i < monItineraire.size() - 1; i++) {
            // Obtenir la sous-partie de deux éléments successifs dans monItineraire
            Vector2D first = monItineraire.get(i);
            Vector2D second = monItineraire.get(i + 1);

            // Inverser la sous-partie pour vérifier dans autreItineraire
            for (int j = 0; j < autreItineraire.size() - 1; j++) {
                if (autreItineraire.get(j).equals(second) && autreItineraire.get(j + 1).equals(first)) {
                    return autreItineraire.size();
                }
            }
        }

        return 0;
    }

    private int detectionCollisionSimple(ArrayList<Vector2D> monItineraire, ArrayList<Vector2D> autreItineraire){
        // Vérifie si deux cases avec le même indice ont le même contenu
        for (int i = 0; i < Math.min(monItineraire.size(), autreItineraire.size()); i++) {
            if (monItineraire.get(i).equals(autreItineraire.get(i))) {
                return 1;
            }
        }

        return 0;
    }

    private int detectionCheminsCroises(ArrayList<Vector2D> monItineraire, ArrayList<Vector2D> autreItineraire) {
        if(!monItineraire.isEmpty() && !autreItineraire.isEmpty()) {
            Vector2D posD1 = monItineraire.get(0);
            Vector2D posA1 = monItineraire.get(monItineraire.size() - 1);

            Vector2D posD2 = autreItineraire.get(0);
            Vector2D posA2 = autreItineraire.get(autreItineraire.size() - 1);

            if (segmentsSeCroisent(posD1, posA1, posD2, posA2)) {
                return 1; // Les segments se croisent
            }
        }
        return 0; // Les segments ne se croisent pas
    }
    private boolean segmentsSeCroisent(Vector2D p1, Vector2D q1, Vector2D p2, Vector2D q2) {
        // Vérifie l'orientation des points
        int o1 = orientation(p1, q1, p2);
        int o2 = orientation(p1, q1, q2);
        int o3 = orientation(p2, q2, p1);
        int o4 = orientation(p2, q2, q1);

        // Cas général : les segments se croisent si les orientations sont différentes
        if (o1 != o2 && o3 != o4) {
            // Vérifier si l'intersection est dans les limites des segments
            Vector2D intersection = calculIntersection(p1, q1, p2, q2);
            return intersection != null && pointDansSegment(p1, q1, intersection) && pointDansSegment(p2, q2, intersection);
        }

        return false;
    }

    private int orientation(Vector2D p, Vector2D q, Vector2D r) {
        double val = (q.getY() - p.getY()) * (r.getX() - q.getX()) - (q.getX() - p.getX()) * (r.getY() - q.getY());
        if (val == 0) return 0; // Colinéaire
        return (val > 0) ? 1 : 2; // Horaire ou antihoraire
    }

    /**
     * Vérifie si deux segments se croisent selon les cas définis dans l'énoncé.
     * @param p1 Le premier point du premier segment
     * @param q1 Le deuxième point du premier segment
     * @param p2 Le premier point du deuxième segment
     * @param q2 Le deuxième point du deuxième segment
     * @return true si les segments se croisent, false sinon
     */
    private Vector2D calculIntersection(Vector2D p1, Vector2D q1, Vector2D p2, Vector2D q2) {
        double a1 = q1.getY() - p1.getY();
        double b1 = p1.getX() - q1.getX();
        double c1 = a1 * p1.getX() + b1 * p1.getY();

        double a2 = q2.getY() - p2.getY();
        double b2 = p2.getX() - q2.getX();
        double c2 = a2 * p2.getX() + b2 * p2.getY();

        double determinant = a1 * b2 - a2 * b1;

        if (determinant == 0) {
            return null; // Les lignes sont parallèles
        }

        double x = (b2 * c1 - b1 * c2) / determinant;
        double y = (a1 * c2 - a2 * c1) / determinant;

        return new Vector2D((int) x, (int) y);
    }

    private boolean pointDansSegment(Vector2D p, Vector2D q, Vector2D r) {
        return Math.min(p.getX(), q.getX()) <= r.getX() && r.getX() <= Math.max(p.getX(), q.getX()) &&
                Math.min(p.getY(), q.getY()) <= r.getY() && r.getY() <= Math.max(p.getY(), q.getY());
    }



}
