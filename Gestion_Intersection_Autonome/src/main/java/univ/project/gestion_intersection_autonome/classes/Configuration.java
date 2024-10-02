package univ.project.gestion_intersection_autonome.classes;

import java.util.*;

public class Configuration {
    private List<Vehicule> vehicules;
    private Map<Vehicule, Message> tempsArrivee;
    private Map<Integer,Character> etatVehicule; //on stocke l'id du véhicule et "A" pour "attente", "E" pour "engagé" - ajouter une énum ?

    public Configuration(){
        vehicules = new ArrayList<>();
        tempsArrivee = new HashMap<>();
        etatVehicule = new HashMap<>();
    }

    public List<Vehicule> getVehicules() {
        return vehicules;
    }

    public void nouveauVehicule(Vehicule v, Message m){
        tempsArrivee.put(v,m);
        etatVehicule.put(v.getId(),'A');
    }

    public void editEtat(Integer id, Character etat) {
        // Si le véhicule existe dans la carte, on met à jour son état
        if (etatVehicule.containsKey(id)) {
            etatVehicule.put(id, etat); // Mise à jour de l'état du véhicule
        } else {
            // Si l'ID du véhicule n'existe pas encore, on l'ajoute
            etatVehicule.put(id, etat);
        }
    }

    public Character getEtat(Integer id) throws NoSuchElementException {
        if (!etatVehicule.containsKey(id)) {
            throw new NoSuchElementException("L'identifiant du véhicule " + id + " est introuvable dans la liste des états.");
        }
        return etatVehicule.get(id);
    }

    public void supprimerVehicule(Vehicule v){
        vehicules.remove(v);
        tempsArrivee.remove(v);
        etatVehicule.remove(v.getId());
    }

}
