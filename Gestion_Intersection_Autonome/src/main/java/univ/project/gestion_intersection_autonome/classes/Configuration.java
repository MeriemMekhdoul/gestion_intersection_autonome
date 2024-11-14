package univ.project.gestion_intersection_autonome.classes;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Configuration {
    private List<Vehicule> vehicules;  // Peut être remplacé par CopyOnWriteArrayList si nécessaire
    private ConcurrentHashMap<Vehicule, Message> tempsArrivee;
    private ConcurrentHashMap<Integer, EtatVehicule> etatVehicule;

    public Configuration(){
        vehicules = new CopyOnWriteArrayList<>();
        tempsArrivee = new ConcurrentHashMap<>();
        etatVehicule = new ConcurrentHashMap<>();
    }

    synchronized public List<Vehicule> getVehiculesTemp() {
        return vehicules;
    }
    synchronized public List<Vehicule> getVehicules() {
        return new ArrayList<>(tempsArrivee.keySet());
    }

    synchronized public void nouveauVehicule(Vehicule v, Message m){
        if(!vehicules.contains(v))
            vehicules.add(v);
        tempsArrivee.put(v,m);
        etatVehicule.put(v.getId(),EtatVehicule.ATTENTE);
    }

    synchronized public void nouveauVehiculeTemp(Vehicule v){
        if(!vehicules.contains(v))
            vehicules.add(v);
    }

    public ConcurrentHashMap<Integer, EtatVehicule> getEtatVehicule() {
        return etatVehicule;
    }

    synchronized public void editEtat(Integer id, EtatVehicule etat) {
        // Si le véhicule existe dans la carte, on met à jour son état
        if (etatVehicule.containsKey(id)) {
            etatVehicule.put(id, etat); // Mise à jour de l'état du véhicule
        } else {
            // Si l'ID du véhicule n'existe pas encore, on l'ajoute
            etatVehicule.put(id, etat);
        }
    }

    synchronized public EtatVehicule getEtat(Integer id) throws NoSuchElementException {
        if (!etatVehicule.containsKey(id)) {
            throw new NoSuchElementException("L'identifiant du véhicule " + id + " est introuvable dans la liste des états.");
        }
        return etatVehicule.get(id);
    }

    synchronized public void supprimerVehicule(Vehicule v){
        vehicules.remove(v);
        tempsArrivee.remove(v);
        etatVehicule.remove(v.getId());
    }

    public Message getMessage(Vehicule v){
        return tempsArrivee.get(v);
    }

    /*@Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Configuration autre = (Configuration) obj;
        return  Objects.equals(vehicules, autre.vehicules) &&
                Objects.equals(tempsArrivee, autre.tempsArrivee) &&
                Objects.equals(etatVehicule, autre.etatVehicule);
    }*/

    /**
     * Comparer uniquement l'ordre des véhicules dans la configuration
     * @param config : la configuration à comparer avec
     * @return vrai si les deux configs ont les mêmes véhicules (dans l'ordre), faux sinon
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

        // Titre du tableau
        s.append("id Véhicule | Objet Message | Temps Arrivée | État\n");
        s.append("----------------------------------------------------\n");

        // Parcours des véhicules
        for (Vehicule v : vehicules) {
            Message m = tempsArrivee.get(v); // Récupération du message lié au véhicule
            EtatVehicule etat = etatVehicule.get(v.getId()); // Récupération de l'état du véhicule

            // Gestion des valeurs nulles pour éviter les erreurs
            String objetMessage = (m != null && m.getObjet() != null) ? m.getObjet().toString() : "Aucun message";
            String tempsArriveeStr = (m != null && m.getT() != null) ? m.getT().toString() : "Inconnu";
            String etatStr = (etat != null) ? etat.toString() : "État inconnu";

            // Utilisation de String.format pour un formatage plus propre
            s.append(String.format("id = %d | %s | %s | %s\n", v.getId(), objetMessage, tempsArriveeStr, etatStr));
        }

        return s.toString();
    }

    public Vehicule getVehicule(int idVoiture) {
        for (Vehicule v : vehicules) {
            if (v.getId() == idVoiture)
                return v;
        }
        return null;
    }
}
