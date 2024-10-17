package univ.project.gestion_intersection_autonome.classes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Intersection {
    private final List<Vector2D> cellulesCommunication; // Liste des cellules qui appartiennent à l'intersection (à revoir)
    private ConcurrentHashMap<Direction, Integer> etatTrafic;  //ajouter une énum état trafic ?
    private Configuration configuration;

    public Intersection(List<Vector2D> cellulesInfluence) {
        this.cellulesCommunication = cellulesInfluence;
        this.etatTrafic = new ConcurrentHashMap<>();
        this.configuration = new Configuration();
    }

    public boolean contientCellule(Vector2D position) {
        return cellulesCommunication.contains(position);
    }

    public void ajouterVehicule(Vehicule v, Message m){
        configuration.nouveauVehicule(v,m);
    }

    public void supprimerVehicule(Vehicule v){
        configuration.supprimerVehicule(v);
    }

    public void editConfig(Vehicule v, EtatVehicule etat) {
        configuration.editEtat(v.getId(),etat);
    }

    public ArrayList<Vehicule> getVehiculesEnAttente() {
        ArrayList<Vehicule> vehiculeEnAttente = new ArrayList<>();
        for (Vehicule v : configuration.getVehicules()) {
            if (configuration.getEtat(v.getId()) == EtatVehicule.ATTENTE)
                vehiculeEnAttente.add(v);
        }
        return vehiculeEnAttente;
    }

    public ArrayList<Vehicule> getVehiculesEngages() {
        ArrayList<Vehicule> vehiculeEngages = new ArrayList<>();
        for (Vehicule v : configuration.getVehicules()) {
            if (configuration.getEtat(v.getId()) == EtatVehicule.ENGAGE)
                vehiculeEngages.add(v);
        }
        return vehiculeEngages;
    }
    public Message getMessage(Vehicule v){
        return configuration.getMessage(v);
    }

    public boolean aucunVehicule() {
        return configuration.getVehicules().isEmpty();
    }
    public ArrayList<Vehicule> getVehicules(){
        return configuration.getVehicules();
    }

    public void afficherConfig() {
        System.out.println(configuration);
    }

}
