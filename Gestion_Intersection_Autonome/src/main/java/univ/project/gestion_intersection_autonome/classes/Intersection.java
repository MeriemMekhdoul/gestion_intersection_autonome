package univ.project.gestion_intersection_autonome.classes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Intersection implements IntersectionListener {
    private final List<Vector2D> cellulesCommunication; // Liste des cellules qui appartiennent à l'intersection (à revoir)
    private final ArrayList<Vector2D> pointsEntree;
    private ConcurrentHashMap<Direction, Integer> etatTrafic;  //ajouter une énum état trafic ?
    private Configuration configuration;

    public Intersection(List<Vector2D> cellulesInfluence) {
        this.cellulesCommunication = cellulesInfluence;
        this.etatTrafic = new ConcurrentHashMap<>();
        this.configuration = new Configuration();
        this.terrain = terrain;
        pointsEntree = _pointsEntree;
    }



    public ArrayList<Vector2D> getPointsEntree() {
        return pointsEntree;
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


    //listener intersection de intersection

    private List<IntersectionListener> listeners = new ArrayList<>();

    public void addListener(IntersectionListener listener) {

        listeners.add(listener);
    }
    public void removeListener (IntersectionListener listener){
        listeners.remove(listener);
    }

    private void notifyListeners(Message message) {
        for (IntersectionListener listener : listeners) {
            if (!listener.equals(message.getv1())) {
                listener.messageIntersection(message);
            }
        }
    }
    public void sendMessageintersection (Message message) {
        notifyListeners(message); // Notifie tous les observateurs
    }



    @Override
    public void messageIntersection(Message message) {
        System.out.println("Le véhicule de type \"" + message.getv1().getType() + "\" et id \"" + message.getv1().getId() +
                "\" envoie ce message : " + message.getT() + ", objet : " + message.getObjet() +
                ", itinéraire : " + message.getItineraire());

        System.out.println("recu");

    }
    //listener vc de intersection

    private List<VehiculeControllerListener> vehiculeControllers = new ArrayList<>();

    public void addVehiculeControllerListener(VehiculeControllerListener listener) {
        vehiculeControllers.add(listener);
    }

    public void removeVehiculeControllerListener(VehiculeControllerListener listener) {
        vehiculeControllers.remove(listener);
    }

    public void sendMessageToVehiculeControllers(Message message) {
        for (VehiculeControllerListener listener : vehiculeControllers) {
            listener.onMessageReceivedFromIntersection(message);
        }
    }

    @Override
    public void onMessageReceivedFromVehiculeController(Message message) {
        //changer
        System.out.println("Le véhicule de type \"" + message.getv1().getType() + "\" et id \"" + message.getv1().getId() +
                "\" envoie ce message : " + message.getT() + ", objet : " + message.getObjet() +
                ", itinéraire : " + message.getItineraire());

        System.out.println("recu");
    }

}







