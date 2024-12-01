package univ.project.gestion_intersection_autonome.classes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Objects;

/**
 * La classe Message représente un message envoyé dans le cadre de la gestion du trafic entre véhicules et intersections.
 * Ce message peut contenir des informations sur un véhicule, un itinéraire, un objet associé, ainsi qu'une urgence ou une demande spécifique.
 */
public class Message {

    private Instant t;  // Le temps d'envoi du message
    private Vehicule v1;  // Le véhicule qui envoie le message
    private ArrayList<Vehicule> v2;  // Liste des véhicules destinataires du message
    private Objetmessage objet;  // Objet associé au message (ex: demande de passage, état du véhicule, etc.)
    private ArrayList<Vector2D> itineraire;  // Itinéraire associé au message
    private Configuration configuration;  // Configuration de l'environnement liée au message
    private Vector2D entreeUrgence;  // Position de l'entrée d'urgence (si applicable)

    // Constructeur par défaut
    public Message() {
        this.t = Instant.now();  // Le message est créé avec l'heure actuelle
        this.v1 = null;  // Aucun véhicule spécifié par défaut
        this.v2 = new ArrayList<>();  // Liste vide pour les véhicules destinataires
        this.objet = null;  // Aucun objet spécifié par défaut
        this.itineraire = new ArrayList<>();  // Itinéraire vide par défaut
    }

    // Constructeur paramétré
    public Message(Instant t, Vehicule v1, ArrayList<Vehicule> v2, Objetmessage objet, ArrayList<Vector2D> itineraire) {
        this.t = t;
        this.v1 = v1;
        this.v2 = v2;
        this.objet = objet;
        this.itineraire = itineraire;
    }

    // Constructeur spécifique pour les véhicules d'urgence
    public Message(VehiculeUrgence vehiculeUrgence, String m) {
        // Construction non implémentée ici (peut être ajoutée selon le contexte d'urgence)
    }

    // Setters et getters
    public void setT(Instant t) { this.t = t; }
    public Instant getT() { return t; }

    public void setv1(Vehicule v1) { this.v1 = v1; }
    public Vehicule getv1() { return v1; }

    public void setv2(ArrayList<Vehicule> v2) { this.v2 = v2; }
    public void setv2(Vehicule v) { this.v2.add(v); }  // Ajoute un véhicule à la liste des destinataires
    public ArrayList<Vehicule> getv2() { return v2; }

    public void setItineraire(ArrayList<Vector2D> itineraire) { this.itineraire = itineraire; }
    public ArrayList<Vector2D> getItineraire() { return itineraire; }

    public void setObjet(Objetmessage objet) { this.objet = objet; }
    public Objetmessage getObjet() { return objet; }

    public Configuration getConfiguration() { return configuration; }
    public void setConfiguration(Configuration configuration) { this.configuration = configuration; }

    /**
     * Méthode de représentation sous forme de chaîne de caractères du message.
     *
     * @return La chaîne de caractères représentant le message.
     */
    public String toString() {
        return "Le véhicule de type \"" + v1.getType() + "\" et id \"" + v1.getId() + "\" " +
                "envoie ce message : " + t + ", objet : " + objet + ", itinéraire : " + itineraire;
    }

    /**
     * Redéfinition de la méthode hashCode pour générer un code de hachage basé sur les attributs du message.
     *
     * @return Le code de hachage du message.
     */
    @Override
    public int hashCode() {
        return Objects.hash(t, v1, v2, objet, itineraire);
    }

    public Vector2D getEntreeUrgence() { return entreeUrgence; }
    public void setEntreeUrgence(Vector2D position) { entreeUrgence = position; }
}
