package univ.project.gestion_intersection_autonome.classes;

import javafx.scene.paint.Color;
import java.util.*;
import java.io.*;
import java.util.List;

public class Vehicule implements VehiculeListener {
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
    private List<VehiculeListener> listeners = new ArrayList<>();

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

    //methode qui determine la position par laquelle le véhicule passe selon le chemin le plus court , prend un paramètre un tableau de Vector2D contenant les positions possibles
    public Vector2D choisirPositionOptimale(List<Vector2D> positionsPossibles) {
        // dans le cas où le véhicule est en bordure
        if (positionsPossibles == null || positionsPossibles.isEmpty()) {
            System.out.println("Aucune position valide pour le véhicule.");
            return position; // renvoie la position actuelle si aucune position trouvée
        }

        //position optimale initialisée par le premier Vector2D du tableau
        // valeur par défaut
        Vector2D posoptimale = positionsPossibles.get(0);
        //calculer distance entre premiere position et la position de destination, celle-ci est stockée dans la variable distanceMin
        double distanceMin = DistanceVersDestination(posoptimale);

        // Parcourir toutes les positions possibles et choisir celle qui est la plus proche a la position de destination (la distance la plus courte)
        for (Vector2D position : positionsPossibles) {
            double distance = DistanceVersDestination(position);
            if (distance < distanceMin) {
                posoptimale = position;
                distanceMin = distance;
            }
        }
        return posoptimale;
    }

    // Algorithme pour calculer la distance vers la destination ; formule de la distance euclidienne.
    private double DistanceVersDestination(Vector2D position) {
        return Math.sqrt(Math.pow(positionArrivee.getX() - position.getX(), 2) +
                Math.pow(positionArrivee.getY() - position.getY(), 2));
    }

    public boolean estArrivee() {
        return position.equals(positionArrivee);
    }


    // generer une couleur aleatoire
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

    public int setId(int id) {
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

    // Méthode pour s'inscrire à des notifications
    public void addListener(VehiculeListener listener) {
        listeners.add(listener);
    }

    // Méthode pour se désinscrire
    public void removeListener(VehiculeListener listener) {
        listeners.remove(listener);
    }

    // Méthode pour notifier tous les écouteurs
    private void notifyListeners(Message message) {
        for (VehiculeListener listener : listeners) {
            if (!listener.equals(message.getv1())) { // Ne pas notifier l'expéditeur
                listener.onMessageReceived(message);
            }
        }
    }

    @Override
    public String toString() {
        return "Vehicule{" +
                "type=" + type +
                ", positionDepart=" + positionDepart +
                ", positionArrivee=" + positionArrivee +
                '}';
    }

    public void sendMessage(Message message) {
        notifyListeners(message); // Notifie tous les observateurs
    }


    @Override
    public void onMessageReceived(Message message) {
        // Traitement du message reçu
        System.out.println("Le véhicule de type \"" + message.getv1().getType() + "\" et id \"" + message.getv1().getId() +
                "\" envoie ce message : " + message.getT() + ", objet : " + message.getObjet() +
                ", itinéraire : " + message.getItineraire());

        System.out.println("Le véhicule de type \"" + this.getType() + "\" et id \"" + this.getId() + "\" a reçu ce message.");

        // Ajouter des actions spécifiques en fonction du type d'objet ou du contenu du message
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

    public void setEnAttente(boolean enAttente) {
        this.enAttente = enAttente;
    }
}