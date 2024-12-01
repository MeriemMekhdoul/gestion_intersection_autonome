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
     * Calcule le temps d'attente pour le véhicule actuel en fonction des itinéraires
     * des autres véhicules engagés et en attente.
     *
     * @param vehiculesEngagesEtItineraires Les itinéraires des véhicules déjà engagés.
     * @param vehiculesAttenteEtItineraires Les itinéraires des véhicules en attente.
     * @param itineraire                    L'itinéraire du véhicule actuel.
     * @return Le temps d'attente total (en secondes).
     */
    public int calculTempsAttente(
            Map<Vehicule, ArrayList<Vector2D>> vehiculesEngagesEtItineraires,
            Map<Vehicule, ArrayList<Vector2D>> vehiculesAttenteEtItineraires,
            ArrayList<Vector2D> itineraire) {
        Map<Vehicule, ArrayList<Vector2D>> nouveauxItineraires = creerNouveauxItineraires(vehiculesEngagesEtItineraires);

        ArrayList<Vehicule> vehiculesEnConflit = new ArrayList<>();
        Map<Vehicule,ArrayList<Vector2D>> vehiculesDiagonalesEtItineraire = new HashMap<>();

        int tempsAttente = 0;

        //vehicules engagés uniquement
        if (conflit(nouveauxItineraires, itineraire, vehiculesEnConflit,vehiculesDiagonalesEtItineraire)) {
            tempsAttente = calculerTempsAttentePourConflit(vehiculesEnConflit, vehiculesDiagonalesEtItineraire, itineraire);
        }

        //véhicules en attente
        if (tempsAttente > 0) {
            tempsAttente += gererConflitsAvecVehiculesAttente(vehiculesAttenteEtItineraires, tempsAttente, itineraire);
        }

        return tempsAttente;
    }

    /**
     * Gère les conflits avec les véhicules en attente et calcule le temps d'attente supplémentaire
     * en fonction des itinéraires potentiellement conflictuels.
     *
     * @param vehiculesAttenteEtItineraires Les itinéraires des véhicules en attente.
     * @param tempsAttenteActuel            Le temps d'attente déjà calculé.
     * @param itineraire                    L'itinéraire du véhicule actuel.
     * @return Le temps d'attente supplémentaire à ajouter.
     */
    private int gererConflitsAvecVehiculesAttente(Map<Vehicule, ArrayList<Vector2D>> vehiculesAttenteEtItineraires, int tempsAttenteActuel, ArrayList<Vector2D> itineraire) {
        boolean[] diagonale = new boolean[1];

        for (Map.Entry<Vehicule, ArrayList<Vector2D>> entry : vehiculesAttenteEtItineraires.entrySet()) {
            Vehicule vehiculeEnAttente = entry.getKey();
            ArrayList<Vector2D> itineraireVehiculeAttente = entry.getValue();

            // Calculer l'itinéraire effectif après le temps d'attente actuel
            ArrayList<Vector2D> itineraireModifie = extraireItineraireApresTemps(itineraireVehiculeAttente, tempsAttenteActuel);
            int tempsAttenteSupplementaire = 0;

            // Vérifier les conflits avec l'itinéraire actuel
            if (compareItineraire(itineraire,itineraireModifie,diagonale)) {
                if(!diagonale[0]) {
                    System.out.println("V:" + this.getId() + " traite : ID = " + vehiculeEnAttente.getId() + " pos: " + vehiculeEnAttente.getPosition() +
                            "itin = " + itineraireModifie + "n'est pas dans la diagonale mais cause un conflit");
                    tempsAttenteSupplementaire++;  // Ajouter 1 seconde d'attente
                }
                else {
                    tempsAttenteSupplementaire += (itineraireModifie.size() -1);
                    System.out.println("V:" + this.getId() + " MON ITINERAIRE : " + itineraire + "IDautrevoiture = " + vehiculeEnAttente.getId() + " pos: " + vehiculeEnAttente.getPosition() +
                            "itin = " + itineraireModifie + "est dans la diagonale \n temps attente actuel : " +
                            tempsAttenteActuel + "nouveau temps = " + tempsAttenteSupplementaire);
                }
            }
            tempsAttenteActuel += tempsAttenteSupplementaire;
        }

        return tempsAttenteActuel;
    }

    /**
     * Extrait les positions restantes de l'itinéraire après un certain temps d'attente.
     *
     * @param itineraire   L'itinéraire complet du véhicule.
     * @param tempsAttente Le temps d'attente écoulé.
     * @return Une liste contenant les positions restantes de l'itinéraire.
     */
    private ArrayList<Vector2D> extraireItineraireApresTemps(ArrayList<Vector2D> itineraire, int tempsAttente) {
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
    private Map<Vehicule, ArrayList<Vector2D>> creerNouveauxItineraires(Map<Vehicule, ArrayList<Vector2D>> vehiculesEtItineraires) {
        Map<Vehicule, ArrayList<Vector2D>> nouveauxItineraires = new HashMap<>();

        for (Vehicule vehicule : vehiculesEtItineraires.keySet()) {
            ArrayList<Vector2D> itineraireComplet = vehiculesEtItineraires.get(vehicule);
            ArrayList<Vector2D> nouvelItineraire = extraireItineraireRestant(vehicule, itineraireComplet);

            System.out.println("V:" + this.getId() + " traite : ID=" + vehicule.getId() + "pos actuelle = " + vehicule.getPosition()
                    + "itin complet = " + itineraireComplet + "itintronqué = " + nouvelItineraire);

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
     * @return Une liste contenant les positions restantes.
     * @throws IndexOutOfBoundsException Si la position actuelle n'est pas trouvée dans l'itinéraire.
     */
    private ArrayList<Vector2D> extraireItineraireRestant(Vehicule vehicule, ArrayList<Vector2D> itineraireComplet) {
        Vector2D positionActuelle = vehicule.getPosition();
        int index = trouverIndexPosition(itineraireComplet, positionActuelle);

        if (index == -1) {
  /*          System.err.println("index = -1, ID = " + vehicule.getId() + " soit elle est sortie, soit erreur");
            System.err.println("posActuelle = " + vehicule.getPosition() + " itineraire complet = " + itineraireComplet);
*/
            return null; //TODO: null veut peut etre dire que le vehicule ne s'est pas encore engagé, donc i = 0 ..
            //throw new IndexOutOfBoundsException("Position actuelle non trouvée dans l'itinéraire !");
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
    private int trouverIndexPosition(ArrayList<Vector2D> itineraire, Vector2D positionActuelle) {
        for (int i = 0; i < itineraire.size(); i++) {
            //System.out.println(positionActuelle + " // get index : i = " + i + "itin.get(i) = " +itineraire.get(i));
            if (itineraire.get(i).equals(positionActuelle)) {
                return i;
            }
        }
        return -1; // Position non trouvée
    }


    /**
     * Calcule le temps d'attente supplémentaire causé par les conflits avec les autres véhicules.
     *
     * @param vehiculesEnConflit La liste des véhicules en conflit.
     * @return Le temps d'attente total en secondes.
     */
    private int calculerTempsAttentePourConflit(ArrayList<Vehicule> vehiculesEnConflit, Map<Vehicule,ArrayList<Vector2D>> vehiculesDiagonalesEtItineraire, ArrayList<Vector2D> itineraire) {
        int tempsAttente = 0;
        for (Vehicule vehicule : vehiculesEnConflit) {
            if (vehicule.getId() != this.id) {//TODO: pourquoi ca c'est nécessaire ??
                tempsAttente++;
            }
        }
        if (!vehiculesDiagonalesEtItineraire.isEmpty()){
            for (Vehicule vehicule : vehiculesDiagonalesEtItineraire.keySet()) {
                if (vehicule.getId() != this.id) {//TODO: pourquoi ca c'est nécessaire ??
                    tempsAttente += gererConflitsAvecVehiculesAttente(vehiculesDiagonalesEtItineraire,tempsAttente,itineraire);
                    //tempsAttente += vehiculesDiagonalesEtItineraire.get(vehicule).size();

                }
            }
        }
        return tempsAttente;
    }

    /**
     * Vérifie s'il existe un conflit entre l'itinéraire actuel et ceux des autres véhicules.
     *
     * @param vehiculesEtItineraires Un map contenant les itinéraires des autres véhicules.
     * @param itineraire             L'itinéraire du véhicule actuel.
     * @param vehiculesEnConflit     La liste des véhicules causant un conflit (mise à jour).
     * @return {@code true} si un conflit est détecté, sinon {@code false}.
     */
    public boolean conflit(Map<Vehicule,ArrayList<Vector2D>> vehiculesEtItineraires,
                           ArrayList<Vector2D> itineraire,
                           ArrayList<Vehicule> vehiculesEnConflit,
                           Map<Vehicule,ArrayList<Vector2D>> vehiculesDiagonalesEtItineraire) {
        // Vider la liste des véhicules en conflit pour un nouveau calcul
        vehiculesEnConflit.clear();
        boolean[] diagonale = new boolean[1];

        for (Vehicule v : vehiculesEtItineraires.keySet()) {
            ArrayList<Vector2D> itineraireAutreVehicule = vehiculesEtItineraires.get(v);

            // Si une collision est détectée entre les itinéraires
            if (compareItineraire(itineraire, itineraireAutreVehicule,diagonale)) {
                if(!diagonale[0])
                    // Ajouter le véhicule en conflit à la liste
                    vehiculesEnConflit.add(v);
                else
                    vehiculesDiagonalesEtItineraire.put(v,itineraireAutreVehicule);
            }
        }

        // Si des véhicules en conflit sont détectés, retourner vrai
        return !(vehiculesEnConflit.isEmpty() && vehiculesDiagonalesEtItineraire.isEmpty());
    }

    /**
     * Compare deux itinéraires pour détecter une collision potentielle.
     *
     * @param itin1 Premier itinéraire.
     * @param itin2 Deuxième itinéraire.
     * @return {@code true} s'il y a une collision, sinon {@code false}.
     */
    public boolean compareItineraire(ArrayList<Vector2D> itin1, ArrayList<Vector2D> itin2, boolean[] diagonale) {

        // Vérifie toutes les sous-parties de deux éléments successifs dans itin1
        for (int i = 0; i < itin1.size() - 1; i++) {
            // Obtenir la sous-partie de deux éléments successifs dans itin1
            Vector2D first = itin1.get(i);
            Vector2D second = itin1.get(i + 1);

            // Inverser la sous-partie pour vérifier dans itin2
            for (int j = 0; j < itin2.size() - 1; j++) {
                System.out.println("VID= " + this.getId() + " fst = " + first + "snd =  " + second + "itin2["+j+"] = "+itin2.get(j) +"itin2["+j+1+"] = "+itin2.get(j+1));
                if (itin2.get(j).equals(second) && itin2.get(j + 1).equals(first)) {
                    System.out.println("VID= " + this.getId() + " a détecté une diagonale");
                    diagonale[0] = true; // Collision diagonale détectée
                    return true;
                }
            }
        }

        // Vérifie si deux cases avec le même indice ont le même contenu
        for (int i = 0; i < Math.min(itin1.size(), itin2.size()); i++) {
            if (itin1.get(i).equals(itin2.get(i))) {
                diagonale[0] = false; // Collision simple détectée
                return true;
            }
        }

        diagonale[0] = false; // Pas de collision
        return false;
    }

    }
