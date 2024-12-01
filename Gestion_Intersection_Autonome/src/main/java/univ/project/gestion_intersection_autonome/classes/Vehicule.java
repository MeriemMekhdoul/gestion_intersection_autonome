package univ.project.gestion_intersection_autonome.classes;

import javafx.scene.paint.Color;
import java.util.*;
import java.io.*;
import java.util.List;

public class Vehicule /*implements VehiculeListener*/ {
    // Données membres
    private final int id; // sécurise en empêchant toute modification
    private TypeVehicule type;  //final non ?
    private Vector2D position;
    private Vector2D positionDepart;
    private Vector2D positionArrivee;
    private static int idCompteur = 1;// génère un ID pour chaque véhicule
    private boolean enAttente;
    private final List<Vector2D> itineraire;
    private final Color couleur;

    // Constructeur paramétré
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

    public void move(Vector2D pos) {
        position.setX(pos.getX());
        position.setY(pos.getY());
    }


    public boolean estArrivee() {
        return position.equals(positionArrivee);
    }


    // generer une couleur aléatoire
    public static Color genererCouleurAleatoire()
    {
        List<Color> listeCouleurs = Arrays.asList(
                Color.CRIMSON, Color.DEEPPINK, Color.MEDIUMPURPLE, Color.GOLD,
                Color.ORCHID, Color.MEDIUMSEAGREEN, Color.LIGHTSEAGREEN
        );

        int couleurRandom = new Random().nextInt(listeCouleurs.size());

        return listeCouleurs.get(couleurRandom);
    }

    // Getters et setters
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

    public void setPosition(Vector2D position) {
        this.position = position;
    }

    public Vector2D getPositionDepart() {
        return positionDepart;
    }

    public void setPositionDepart(Vector2D positionDepart) {
        this.positionDepart = positionDepart;
    }

    public Vector2D getPositionArrivee() {
        return positionArrivee;
    }

    public void setPositionArrivee(Vector2D positionArrivee) {
        this.positionArrivee = positionArrivee;
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

    public boolean isEnAttente() {
        return enAttente;
    }

    synchronized public void setEnAttente(boolean enAttente) {
        this.enAttente = enAttente;
        System.out.println("vehicule : "+ id + " etat changé et mis à : " + enAttente);
    }

    /**
     * Calcule le temps d'attente du véhicule actuel en fonction des itinéraires
     * des autres véhicules potentiellement en conflit.
     *
     * @param vehiculesEngagesEtItineraires Un map contenant chaque véhicule et son itinéraire respectif.
     * @param itineraire             L'itinéraire du véhicule actuel.
     * @return Le temps d'attente en secondes causé par les conflits potentiels avec d'autres véhicules.
     */

    public int calculTempsAttente(
            Map<Vehicule, ArrayList<Vector2D>> vehiculesEngagesEtItineraires,
            Map<Vehicule, ArrayList<Vector2D>> vehiculesAttenteEtItineraires,
            ArrayList<Vector2D> itineraire) {

        //Créer nouveaux itin à partir de la position actuelle du vehicule
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

    // Méthode pour gérer les conflits avec les véhicules en attente
    private int gererConflitsAvecVehiculesAttente(Map<Vehicule, ArrayList<Vector2D>> vehiculesAttenteEtItineraires, int tempsAttenteActuel, ArrayList<Vector2D> itineraire) {
        boolean[] diagonale = new boolean[1];

        for (Map.Entry<Vehicule, ArrayList<Vector2D>> entry : vehiculesAttenteEtItineraires.entrySet()) {
            Vehicule vehiculeEnAttente = entry.getKey();
            ArrayList<Vector2D> itineraireVehiculeAttente = entry.getValue();

            // Calculer l'itinéraire effectif du véhicule en attente après son temps d'attente actuel
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

    // Méthode pour extraire l'itinéraire après un certain temps d'attente
    private ArrayList<Vector2D> extraireItineraireApresTemps(ArrayList<Vector2D> itineraire, int tempsAttente) {
        if (tempsAttente >= itineraire.size()) {
            return new ArrayList<>(); // Si le temps d'attente dépasse ou égale la taille de l'itinéraire, il est vide
        }
        return new ArrayList<>(itineraire.subList(tempsAttente, itineraire.size()));
    }

    // Méthode pour créer de nouveaux itinéraires à partir des positions actuelles
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

    // Méthode pour extraire l'itinéraire restant à partir de la position actuelle du véhicule
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

    // Méthode pour trouver l'indice de la position actuelle dans l'itinéraire
    private int trouverIndexPosition(ArrayList<Vector2D> itineraire, Vector2D positionActuelle) {
        for (int i = 0; i < itineraire.size(); i++) {
            //System.out.println(positionActuelle + " // get index : i = " + i + "itin.get(i) = " +itineraire.get(i));
            if (itineraire.get(i).equals(positionActuelle)) {
                return i;
            }
        }
        return -1; // Non trouvé
    }


    // Méthode pour calculer le temps d'attente en fonction des véhicules en conflit
    private int calculerTempsAttentePourConflit(ArrayList<Vehicule> vehiculesEnConflit,
                                                Map<Vehicule,ArrayList<Vector2D>> vehiculesDiagonalesEtItineraire, ArrayList<Vector2D> itineraire)
    {
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
     * Vérifie s'il existe un conflit potentiel entre l'itinéraire du véhicule actuel et ceux des autres véhicules.
     * Si un conflit est détecté, met à jour la liste des véhicules en conflit.
     *
     * @param vehiculesEtItineraires Un map contenant chaque véhicule et son itinéraire respectif.
     * @param itineraire             L'itinéraire du véhicule actuel dans l'intersection.
     * @param vehiculesEnConflit      La liste des véhicules qui causent un conflit (mise à jour si un conflit est détecté).
     * @return `true` s'il existe un conflit avec un ou plusieurs véhicules, sinon `false`.
     */
    public boolean conflit(Map<Vehicule,ArrayList<Vector2D>> vehiculesEtItineraires,
                           ArrayList<Vector2D> itineraire,
                           ArrayList<Vehicule> vehiculesEnConflit,
                           Map<Vehicule,ArrayList<Vector2D>> vehiculesDiagonalesEtItineraire) {
        // Vider la liste des véhicules en conflit pour un nouveau calcul
        vehiculesEnConflit.clear();
        boolean[] diagonale = new boolean[1];

        // Récupérer un tableau des itinéraires depuis les messages
        for (Vehicule v : vehiculesEtItineraires.keySet()) {
            ArrayList<Vector2D> itineraireAutreVehicule = vehiculesEtItineraires.get(v);//message.getItineraire();

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
     * Compare deux itinéraires pour détecter une éventuelle collision. (à renommer en détécterCollision())
     *
     * @param itin1 Le premier itinéraire.
     * @param itin2 Le second itinéraire.
     * @return `true` s'il y a une collision, sinon `false`.
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
