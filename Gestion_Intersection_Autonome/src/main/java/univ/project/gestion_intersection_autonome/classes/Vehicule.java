package univ.project.gestion_intersection_autonome.classes;

import javafx.scene.paint.Color;
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
    private final Color couleur;

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
        System.out.println("Vehicule : " + id + " état changé et mis à : " + enAttente);
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
            //TODO: c'est le meme code que dans calculTempsAttenteVehiculesengages
            if (temp == 0){ //diagonale non détectée
                //vérifier les autres cas de collisions
                temp = detectionCollisionSimple(monItineraire,autreItin);
                if (temp == 0){ //collision simple non détectée
                    //vérifier le dernier cas
                    temp = detectionCheminsCroises(monItineraire,autreItin);
                }
            }
            tempsAttente = Math.max(tempsAttente,temp);
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
        int index = trouverIndexPosition(itineraireComplet, positionActuelle);

        if (index == -1) {
            if (engage) //il s'est engagé donc si la position n'existe pas, c'est qu'il est sorti de l'intersection
                return null;
            else // en attente, sa posActuelle n'est pas dans l'itinéraire car il ne s'est pas encore engagé
                return itineraireComplet;
        }

        // Extraire le sous-itinéraire restant
        return new ArrayList<>(itineraireComplet.subList(index, itineraireComplet.size()));

    }

    /**
     * Trouve l'indice de la position actuelle dans l'itinéraire complet.
     *
     * @param itineraire       L'itinéraire complet du véhicule.
     * @param positionActuelle La position actuelle du véhicule.
     * @return L'indice de la position actuelle, ou -1 si elle n'est pas trouvée.
     */
    //TODO: remplacer cette methode par getIndexOf()...
    private int trouverIndexPosition(ArrayList<Vector2D> itineraire, Vector2D positionActuelle) {
        for (int i = 0; i < itineraire.size(); i++) {
            //System.out.println(positionActuelle + " // get index : i = " + i + "itin.get(i) = " +itineraire.get(i));
            if (itineraire.get(i).equals(positionActuelle)) {
                return i;
            }
        }
        return -1; // Position non trouvée
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
            //TODO: rajouter un booléen d'abord ??
            if (temp == 0){ //diagonale non détectée
                //vérifier les autres cas de collisions
                temp = detectionCollisionSimple(monItineraire,autreItin);
                if (temp == 0){ //collision simple non détectée
                    //vérifier le dernier cas
                    temp = detectionCheminsCroises(monItineraire,autreItin);
                }
            }// si diagonale détectée ça ne rentre forcément pas dans les autres cas
            //pour chaque vehicule je décide si je garde l'ancien temps calculé parce qu'il est supérieur au nouveau temps spécifique
            //au vehicule actuel, donc tempsAttente couvre la collision, sinon je prends le nouveau car supérieur
            tempsAttente = Math.max(tempsAttente,temp);
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
                    return autreItineraire.size() -1;
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

    //TODO: revoir cette fonction
    private int detectionCheminsCroises(ArrayList<Vector2D> monItineraire, ArrayList<Vector2D> autreItineraire){
        // Parcours de chaque segment de mon itinéraire
        /*for (int i = 0; i < monItineraire.size() - 1; i++) {
            Vector2D startMon = monItineraire.get(i);
            Vector2D endMon = monItineraire.get(i + 1);

            // Parcours de chaque segment de l'autre itinéraire
            for (int j = 0; j < autreItineraire.size() - 1; j++) {
                Vector2D startAutre = autreItineraire.get(j);
                Vector2D endAutre = autreItineraire.get(j + 1);

                // Vérification si les segments se croisent selon les cas que vous avez décrits
                if (intersectionX(startMon, endMon, startAutre, endAutre)) {
                    return 1;
                }
            }
        } */
        return 0;
    }

    /**
     * Vérifie si deux segments se croisent selon les cas définis dans l'énoncé.
     * @param A Le premier point du premier segment
     * @param B Le deuxième point du premier segment
     * @param C Le premier point du deuxième segment
     * @param D Le deuxième point du deuxième segment
     * @return true si les segments se croisent, false sinon
     */
    //TODO: revoir la logique de la fonction, elle n'est pas complète
    private boolean intersectionX(Vector2D A, Vector2D B, Vector2D C, Vector2D D) {

        // Cas 1 : Vérifier si les segments forment un "X" avec i constant et j change
        if (A.getX() == C.getX() && B.getX() == D.getX() && A.getY() != B.getY() && C.getY() != D.getY()) {
            // (i, j) -> (i+1, j+1) et (i+1, j) -> (i, j+1) croisement dans X
            return false;
        }

        // Cas 2 : Vérifier si les segments forment un "X" avec i constant et j change
        // (i, j) -> (i+1, j+1) et (i, j+1) -> (i+1, j) croisement dans X
        //return A.getX() == C.getX() && B.getX() == D.getX() && A.getX() == C.getX()+1 && C.getX() != D.getX();
        return false;
    }
}
