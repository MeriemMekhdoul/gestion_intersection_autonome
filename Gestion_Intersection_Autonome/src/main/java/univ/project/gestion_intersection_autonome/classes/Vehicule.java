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
    /*public int calculTempsAttente(Map<Vehicule,ArrayList<Vector2D>> vehiculesEngagesEtItineraires, Map<Vehicule,ArrayList<Vector2D>> vehiculesAttenteEtItineraires, ArrayList<Vector2D> itineraire) {
        ArrayList<Vehicule> vehiculesenconflit = new ArrayList<>();
        Map<Vehicule,ArrayList<Vector2D>> newMap = new HashMap<>();
        int tempsAttente = 0;

        for (Vehicule v : vehiculesEngagesEtItineraires.keySet()) {
            ArrayList<Vector2D> itineraireAmodifierList = vehiculesEngagesEtItineraires.get(v);
            Vector2D[] itineraireAmodifier = itineraireAmodifierList.toArray(new Vector2D[0]); // Conversion en tableau
            System.out.println("je suis dans calculTempsAttente : ID = " + v.getId() + " pos actuelle : " + v.getPosition() + " itineraire restant : " + Arrays.toString(itineraireAmodifier));

            Vector2D posActuV = v.getPosition();
            int index = -1;

            // Trouver l'indice de la position actuelle dans le tableau
            for (int i = 0; i < itineraireAmodifier.length; i++) {
                if (itineraireAmodifier[i].equals(posActuV)) {
                    index = i;
                    break;
                }
            }

            if (index == -1) {
                throw new IndexOutOfBoundsException("Position actuelle non trouvée dans l'itinéraire !");
            }

            // Copier le reste de l'itinéraire à partir de l'indice trouvé
            int newLength = itineraireAmodifier.length - index;
            Vector2D[] newItineraireArray = new Vector2D[newLength];
            System.arraycopy(itineraireAmodifier, index, newItineraireArray, 0, newLength);

            // Convertir le tableau en ArrayList
            ArrayList<Vector2D> newItineraire = new ArrayList<>(Arrays.asList(newItineraireArray));

            // Ajouter à newMap
            newMap.put(v, newItineraire);
        }

        if(conflit(newMap,itineraire,vehiculesenconflit)) {
            for (Vehicule v : vehiculesenconflit) {
                if (v.getId() != this.id) {
                    tempsAttente++;
                }
            }
        }
        return tempsAttente; // Retourner le temps d'attente
    }*/

    public int calculTempsAttente(
            Map<Vehicule, ArrayList<Vector2D>> vehiculesEngagesEtItineraires,
            Map<Vehicule, ArrayList<Vector2D>> vehiculesAttenteEtItineraires,
            ArrayList<Vector2D> itineraire) {

        Map<Vehicule, ArrayList<Vector2D>> nouveauxItineraires = creerNouveauxItineraires(vehiculesEngagesEtItineraires);
        ArrayList<Vehicule> vehiculesEnConflit = new ArrayList<>();
        int tempsAttente = 0;

        if (verifierConflit(nouveauxItineraires, itineraire, vehiculesEnConflit)) {
            tempsAttente = calculerTempsAttentePourConflit(vehiculesEnConflit);
            System.out.println("temps d'attente calculé avec vehicules engagés seulement : "  +tempsAttente);
        }

        if (tempsAttente > 0) { //TODO: vérifier si ce n'est pas pour tout
            tempsAttente += gererConflitsAvecVehiculesAttente(vehiculesAttenteEtItineraires, tempsAttente, itineraire);
            System.out.println("temps d'attente calculé avec vehicules en attente : "  +tempsAttente);
        }

        return tempsAttente;
    }

    // Méthode pour gérer les conflits avec les véhicules en attente
    private int gererConflitsAvecVehiculesAttente(Map<Vehicule, ArrayList<Vector2D>> vehiculesAttenteEtItineraires, int tempsAttenteActuel, ArrayList<Vector2D> itineraire) {
        int tempsAttenteSupplementaire = 0;

        for (Map.Entry<Vehicule, ArrayList<Vector2D>> entry : vehiculesAttenteEtItineraires.entrySet()) {
            //Vehicule vehiculeEnAttente = entry.getKey();
            ArrayList<Vector2D> itineraireVehiculeAttente = entry.getValue();

            // Calculer l'itinéraire effectif du véhicule en attente après son temps d'attente actuel
            ArrayList<Vector2D> itineraireModifie = extraireItineraireApresTemps(itineraireVehiculeAttente, tempsAttenteActuel);

            // Vérifier les conflits avec l'itinéraire actuel
            if (compareItineraire(itineraireModifie, itineraire)) {
                tempsAttenteSupplementaire++;  // Ajouter 1 seconde d'attente
            }
        }

        return tempsAttenteSupplementaire;
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
            nouveauxItineraires.put(vehicule, nouvelItineraire);
        }

        return nouveauxItineraires;
    }

    // Méthode pour extraire l'itinéraire restant à partir de la position actuelle du véhicule
    private ArrayList<Vector2D> extraireItineraireRestant(Vehicule vehicule, ArrayList<Vector2D> itineraireComplet) {
        Vector2D positionActuelle = vehicule.getPosition();
        int index = trouverIndexPosition(itineraireComplet, positionActuelle);

        if (index == -1) {
            throw new IndexOutOfBoundsException("Position actuelle non trouvée dans l'itinéraire !");
        }

        // Extraire le sous-itinéraire restant
        return new ArrayList<>(itineraireComplet.subList(index, itineraireComplet.size()));
    }

    // Méthode pour trouver l'indice de la position actuelle dans l'itinéraire
    private int trouverIndexPosition(ArrayList<Vector2D> itineraire, Vector2D positionActuelle) {
        for (int i = 0; i < itineraire.size(); i++) {
            if (itineraire.get(i).equals(positionActuelle)) {
                return i;
            }
        }
        return -1; // Non trouvé
    }

    // Méthode pour vérifier s'il y a des conflits entre les itinéraires
    private boolean verifierConflit(Map<Vehicule, ArrayList<Vector2D>> nouveauxItineraires, ArrayList<Vector2D> itineraire, ArrayList<Vehicule> vehiculesEnConflit) {
        return conflit(nouveauxItineraires, itineraire, vehiculesEnConflit);
    }

    // Méthode pour calculer le temps d'attente en fonction des véhicules en conflit
    private int calculerTempsAttentePourConflit(ArrayList<Vehicule> vehiculesEnConflit) {
        int tempsAttente = 0;
        for (Vehicule vehicule : vehiculesEnConflit) {
            if (vehicule.getId() != this.id) {
                tempsAttente++;
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
    public boolean conflit(Map<Vehicule,ArrayList<Vector2D>> vehiculesEtItineraires, ArrayList<Vector2D> itineraire,ArrayList<Vehicule> vehiculesEnConflit) {
        // Vider la liste des véhicules en conflit pour un nouveau calcul
        vehiculesEnConflit.clear();

        // Récupérer un tableau des itinéraires depuis les messages
        for (Vehicule v : vehiculesEtItineraires.keySet()) {
            ArrayList<Vector2D> itineraireAutreVehicule = vehiculesEtItineraires.get(v);//message.getItineraire();

            // Si une collision est détectée entre les itinéraires
            if (compareItineraire(itineraire, itineraireAutreVehicule)) {
                // Ajouter le véhicule en conflit à la liste
                vehiculesEnConflit.add(v);
            }
        }
        // Si des véhicules en conflit sont détectés, retourner vrai
        return !vehiculesEnConflit.isEmpty();
    }

    /**
     * Compare deux itinéraires pour détecter une éventuelle collision. (à renommer en détécterCollision())
     *
     * @param itin1 Le premier itinéraire.
     * @param itin2 Le second itinéraire.
     * @return `true` s'il y a une collision, sinon `false`.
     */
    public boolean compareItineraire(ArrayList<Vector2D> itin1, ArrayList<Vector2D> itin2) {
        for (int i = 0; i < itin1.size(); i++) {
            if (i < itin2.size()) {
                if (itin1.get(i).equals(itin2.get(i)))
                    return true;
            } else return false;
        }
        return false; //pas de collision
    }


}