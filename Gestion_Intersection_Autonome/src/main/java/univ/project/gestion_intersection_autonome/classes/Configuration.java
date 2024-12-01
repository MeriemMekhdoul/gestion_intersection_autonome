package univ.project.gestion_intersection_autonome.classes;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Configuration {
    private List<Vehicule> vehicules;
    private ConcurrentHashMap<Vehicule, Message> tempsArrivee;
    private ConcurrentHashMap<Integer, EtatVehicule> etatVehicule;

    /**
     * Constructeur par défaut de la configuration.
     * Initialise les collections pour les véhicules, les temps d'arrivée et les états des véhicules.
     */
    public Configuration(){
        vehicules = new CopyOnWriteArrayList<>();
        tempsArrivee = new ConcurrentHashMap<>();
        etatVehicule = new ConcurrentHashMap<>();
    }

    /**
     * Ajoute un véhicule à la configuration et crée un label pour l'afficher dans la VBox.
     * @param vehicule Le véhicule à ajouter.
     */
    public void ajouterVehiculeAvecLabel(Vehicule vehicule) {
        vehicules.add(vehicule);

        Label label = new Label(vehicule.getType().toString() + " " + vehicule.getId() + " | " + getNomCouleur(vehicule.getCouleur()) + " | " + etatVehicule.get(vehicule.getId()).toString());
        label.setId("vehicule-" + vehicule.getId());
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

    }


    /**
     * Obtient le nom d'une couleur à partir de l'objet Color.
     * @param couleur L'objet Color à convertir en nom.
     * @return Le nom de la couleur en tant que chaîne de caractères.
     */
    private String getNomCouleur(Color couleur) {
        for (Field field : Color.class.getFields()) {
            try {
                if (field.getType().equals(Color.class) && field.get(null).equals(couleur)) {
                    return field.getName();
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return "Couleur personnalisée";
    }


    /**
     * Retourne la liste des véhicules dans la configuration (temporaire).
     * @return Une liste des véhicules présents dans la configuration.
     */

    synchronized public List<Vehicule> getVehiculesTemp() {
        return vehicules;
    }

    /**
     * Retourne la liste des véhicules présents dans la configuration.
     * @return Une liste des véhicules.
     */
    synchronized public List<Vehicule> getVehicules() {
        return new ArrayList<>(tempsArrivee.keySet());
    }

    /**
     * Ajoute un véhicule et son message associé à la configuration.
     * @param v Le véhicule à ajouter.
     * @param m Le message associé au véhicule.
     */
    synchronized public void nouveauVehicule(Vehicule v, Message m){
        if(!vehicules.contains(v)) {
            vehicules.add(v);
        }
        tempsArrivee.put(v, m);
        etatVehicule.put(v.getId(), EtatVehicule.ATTENTE);
    }

    /**
     * Ajoute temporairement un véhicule sans message associé.
     * @param v Le véhicule à ajouter.
     */
    synchronized public void nouveauVehiculeTemp(Vehicule v){
        if(!vehicules.contains(v)) {
            vehicules.add(v);
            etatVehicule.put(v.getId(), EtatVehicule.ATTENTE);
        }
    }

    /**
     * Retourne la map des états des véhicules.
     * @return La map des états des véhicules.
     */
    public ConcurrentHashMap<Integer, EtatVehicule> getEtatVehicule() {
        return etatVehicule;
    }

    /**
     * Modifie l'état d'un véhicule et met à jour son label.
     * @param id L'ID du véhicule à mettre à jour.
     * @param etat Le nouvel état du véhicule.
     */
    synchronized public void editEtat(Integer id, EtatVehicule etat) {
        Vehicule vehicule = vehicules.stream()
                .filter(v -> v.getId() == id)
                .findFirst()
                .orElse(null);

        if (vehicule != null) {
            etatVehicule.put(id, etat);
        } else {
            System.out.println("Véhicule avec l'ID " + id + " non trouvé.");
        }
    }

    /**
     * Retourne l'état d'un véhicule à partir de son ID.
     * @param id L'ID du véhicule dont on souhaite obtenir l'état.
     * @return L'état du véhicule.
     * @throws NoSuchElementException Si l'ID du véhicule est introuvable.
     */
    synchronized public EtatVehicule getEtat(Integer id) throws NoSuchElementException {
        if (!etatVehicule.containsKey(id)) {
            throw new NoSuchElementException("L'identifiant du véhicule " + id + " est introuvable dans la liste des états.");
        }
        return etatVehicule.get(id);
    }

    /**
     * Supprime un véhicule et ses informations associées.
     * @param v Le véhicule à supprimer.
     */
    synchronized public void supprimerVehicule(Vehicule v){
        vehicules.remove(v);
        tempsArrivee.remove(v);
        etatVehicule.remove(v.getId());
    }

    /**
     * Retourne le message associé à un véhicule.
     * @param v Le véhicule dont on veut obtenir le message.
     * @return Le message associé au véhicule.
     */
    public Message getMessage(Vehicule v){
        return tempsArrivee.get(v);
    }

    /**
     * Compare cette configuration avec une autre.
     * @param config La configuration à comparer.
     * @return true si les configurations sont identiques (même ordre de véhicules), false sinon.
     */
    public boolean compare(Configuration config){
        List<Vehicule> _vehicules = config.getVehicules();
        if (_vehicules.size() != vehicules.size())
            return false;
        else {
            for (int i = 0; i < vehicules.size(); i++) {
                if (vehicules.get(i).getId() != _vehicules.get(i).getId())
                    return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(vehicules, tempsArrivee, etatVehicule);
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("id Véhicule | Objet Message | Temps Arrivée | État\n");
        s.append("----------------------------------------------------\n");

        for (Vehicule v : vehicules) {
            Message m = tempsArrivee.get(v);
            EtatVehicule etat = etatVehicule.get(v.getId());

            String objetMessage = (m != null && m.getObjet() != null) ? m.getObjet().toString() : "Aucun message";
            String tempsArriveeStr = (m != null && m.getT() != null) ? m.getT().toString() : "Inconnu";
            String etatStr = (etat != null) ? etat.toString() : "État inconnu";

            s.append(String.format("id = %d | %s | %s | %s\n", v.getId(), objetMessage, tempsArriveeStr, etatStr));
        }
        return s.toString();
    }

    /**
     * Retourne le véhicule correspondant à un ID spécifique.
     * @param idVoiture L'ID du véhicule à rechercher.
     * @return Le véhicule correspondant, ou null si aucun véhicule n'a cet ID.
     */
    public Vehicule getVehicule(int idVoiture) {
        for (Vehicule v : vehicules) {
            if (v.getId() == idVoiture)
                return v;
        }
        return null;
    }
}
