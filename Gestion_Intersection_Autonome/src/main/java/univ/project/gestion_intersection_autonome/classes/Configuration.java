package univ.project.gestion_intersection_autonome.classes;


import univ.project.gestion_intersection_autonome.controllers.VehiculeController;

import java.util.*;
import java.util.concurrent.*;

public class Configuration{
    private List<Vehicule> vehicules;
    private ConcurrentHashMap<Vehicule, Message> tempsArrivee;
    private ConcurrentHashMap<Integer, EtatVehicule> etatVehicule;
    private ConcurrentHashMap<Integer, Integer> tempsAttente;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);


    /**
     * Constructeur par défaut de la configuration.
     * Initialise les collections pour les véhicules, les temps d'arrivée et les états des véhicules.
     */
    public Configuration(){
        vehicules = new CopyOnWriteArrayList<>();
        tempsArrivee = new ConcurrentHashMap<>();
        etatVehicule = new ConcurrentHashMap<>();
        tempsAttente = new ConcurrentHashMap<>();
        lancerDecrementationAutomatique(); // Démarrer le compte à rebours automatiquement
    }

    /**
     * Retourne la liste des véhicules présents dans la configuration.
     * @return Une liste des véhicules.
     */
    synchronized public List<Vehicule> getVehicules() {
        return new ArrayList<>(tempsArrivee.keySet());
    }

    public List<Vehicule> getVehicules(Vehicule v) {
        List<Vehicule> vehiculesAvant = new ArrayList<>();

        for (Vehicule vehicule : vehicules) {
            if (vehicule.equals(v)) {
                break; // On arrête la boucle dès qu'on atteint le véhicule `v`
            }
            vehiculesAvant.add(vehicule); // Ajouter les véhicules avant `v`
        }

        return vehiculesAvant;
    }


    /**
     * Ajoute un véhicule et son message associé à la configuration.
     * @param v Le véhicule à ajouter.
     * @param m Le message associé au véhicule.
     */
    synchronized public void nouveauVehicule(Vehicule v, Message m){
        if(!vehicules.contains(v)) {
            vehicules.add(v);
            tempsArrivee.put(v, m);
            etatVehicule.put(v.getId(), EtatVehicule.ATTENTE);
            tempsAttente.put(v.getId(),-1);
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
//            System.out.println("Véhicule avec l'ID " + id + " non trouvé.");
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
        tempsAttente.remove(v.getId());
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
        s.append("id Véhicule | Objet Message | Temps Arrivée | État | tempsAttente\n");
        s.append("----------------------------------------------------\n");

        for (Vehicule v : vehicules) {
            Message m = tempsArrivee.get(v);
            EtatVehicule etat = etatVehicule.get(v.getId());

            String objetMessage = (m != null && m.getObjet() != null) ? m.getObjet().toString() : "Aucun message";
            String tempsArriveeStr = (m != null && m.getT() != null) ? m.getT().toString() : "Inconnu";
            String etatStr = (etat != null) ? etat.toString() : "État inconnu";
            String tempAttente = (tempsAttente.get(v.getId()) != null) ? tempsAttente.get(v.getId()).toString() : "temps null";

            s.append(String.format("id = %d | %s | %s | %s | %s\n", v.getId(), objetMessage, tempsArriveeStr, etatStr, tempAttente));
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

    /**
     * Lancer un compte à rebours qui décrémente les temps d'attente toutes les VAR_CONST millisecondes.
     */
    private void lancerDecrementationAutomatique() {
        scheduler.scheduleAtFixedRate(() -> {
            tempsAttente.forEach((id, temps) -> {
                if (temps > 0) { // Décrémenter uniquement si temps > 0
                    tempsAttente.put(id, temps - 1);
                }
            });
        }, 0, VehiculeController.VITESSE_SIMULATION_MS, TimeUnit.MILLISECONDS);
    }


    /**
     * Ajoute ou met à jour le temps d'attente d'un véhicule.
     * @param id    L'ID du véhicule.
     * @param temps Le temps d'attente à ajouter ou mettre à jour.
     */
    public synchronized void ajouterTempsAttente(int id, int temps) {
        tempsAttente.put(id, temps);
    }

    /**
     * Retourne le temps d'attente d'un véhicule donné.
     * @param id L'ID du véhicule.
     * @return Le temps d'attente actuel du véhicule.
     */
    public int getTempsAttente(int id) {
        return tempsAttente.getOrDefault(id, -1);
    }


    /**
     * Méthode pour arrêter proprement toutes les ressources gérées par la configuration.
     */
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
    }
}
