package univ.project.gestion_intersection_autonome.classes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Intersection {
    private final List<Vector2D> cellulesCommunication; // Liste des cellules qui appartiennent à l'intersection (à revoir)
    private Map<Direction, Integer> etatTrafic;  //ajouter une énum état trafic ?
    private Configuration configuration;

    public Intersection(List<Vector2D> cellulesInfluence) {
        this.cellulesCommunication = cellulesInfluence;
        this.etatTrafic = new HashMap<>();
        this.configuration = new Configuration();
    }

    public boolean contientCellule(Vector2D position) {
        return cellulesCommunication.contains(position);
    }

    public void addV(Vehicule v, Message m){
        configuration.nouveauVehicule(v,m);
    }

    public void suppV(Vehicule v){
        configuration.supprimerVehicule(v);
    }

    public void editConfig(Vehicule v, Character etat) {
        configuration.editEtat(v.getId(),etat);
    }

    public ArrayList<Vehicule> getVehiculesEnAttente() {
        ArrayList<Vehicule> vehiculeEnAttente = new ArrayList<>();
        for (Vehicule v : configuration.getVehicules()) {
            if (configuration.getEtat(v.getId()) == 'A')
                vehiculeEnAttente.add(v);
        }
        return vehiculeEnAttente;
    }


}
