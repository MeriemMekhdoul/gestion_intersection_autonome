package univ.project.gestion_intersection_autonome.classes;

import univ.project.gestion_intersection_autonome.controllers.VehiculeController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Intersection implements IntersectionListener {
    private final List<Vector2D> cellulesCommunication; // Liste des cellules qui appartiennent à l'intersection (à revoir)
    private final ArrayList<Vector2D> pointsEntree;
    private ConcurrentHashMap<Direction, Integer> etatTrafic;  //ajouter une énum état trafic ?
    private Configuration configuration;
    private final Terrain terrain;
    private List<IntersectionListener> listeners = new ArrayList<>();
    private List<VehiculeControllerListener> vehiculeControllers = new ArrayList<>();
    public final int DISTANCE = 2; // on considère qu'un embouteillage se forme si 2 cellules consécutives sont occupées



    public Intersection(List<Vector2D> cellulesInfluence, ArrayList<Vector2D> _pointsEntree, Terrain terrain) {
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


    //listener intersection de l'intersection
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
    public void sendMessageIntersection (Message message) {
        notifyListeners(message); // Notifie tous les observateurs
    }

    @Override //traitement message reçu d'une intersection
    public void messageIntersection(Message message) {
        System.out.println("Le véhicule de type \"" + message.getv1().getType() + "\" et id \"" + message.getv1().getId() +
                "\" envoie ce message : " + message.getT() + ", objet : " + message.getObjet() +
                ", itinéraire : " + message.getItineraire());

        System.out.println("recu");
    }

    //listener vc de intersection
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

    public void sendMessageToVehiculeControllers(Message message, ArrayList<VehiculeControllerListener> controllers) {
        for (VehiculeControllerListener listener : controllers) {
            listener.onMessageReceivedFromIntersection(message);
        }
    }
    public void sendMessageToVehiculeControllers(Message message, VehiculeControllerListener controller) {
        controller.onMessageReceivedFromIntersection(message);
    }

    @Override //traitement du message reçu de la part du véhicule controller
    public void onMessageReceivedFromVehiculeController(Message message) {

        //get la voie par laquelle le vehicule arrive pour pouvoir bloquer les autres
        Vector2D voieEntree = getVoieEntree(message.getv1().getPosition());
        System.out.println("position actuelle du véhicule : " + message.getv1().getPosition() + " voie d'entrée : " + voieEntree);

        switch (message.getObjet()) {
            case PASSAGE -> {
                execEntreeVehiculeUrgence(message, voieEntree);
            }
            case SORTIE -> {
                execSortieVehiculeUrgence(voieEntree);
            }
        }
    }

    public Vector2D getVoieEntree(Vector2D position) {
        ArrayList<Vector2D> positionsPossibles = new ArrayList<>();
        boolean sameX = true;

        for (Vector2D pos : cellulesCommunication) {
            if (pos.getX() == position.getX()){
                positionsPossibles.add(pos.copy());
            } else if (pos.getY() == pos.getY()) {
                positionsPossibles.add(pos.copy());
                sameX = false;
            }
            //else on passe au suivant
        }
        Vector2D pos1 = positionsPossibles.get(0);
        Vector2D pos2 = positionsPossibles.get(1);
        if (sameX) {
            return terrain.getCellule(position).isDirectionAutorisee(Direction.NORD)
                    ? (pos1.getY() > pos2.getY() ? pos1 : pos2)
                    : (pos1.getY() < pos2.getY() ? pos1 : pos2);
        } else {
            return terrain.getCellule(position).isDirectionAutorisee(Direction.EST)
                    ? (pos1.getX() < pos2.getX() ? pos1 : pos2)
                    : (pos1.getX() > pos2.getX() ? pos1 : pos2);
        }
    }

    public void execSortieVehiculeUrgence(Vector2D voieEntree){
        System.out.println("un VéhiculeController a envoyé un message de SORTIE à l'intersection");
        //libérer les cases ET les véhicules
        //get les vehicules qui sont en attente pour leur envoyer un signal et les remettre en marche
        ArrayList<Vehicule> vehiculesEnAttente = setEtatCellulesEtVehicules(true, voieEntree);

        // Préparer le message pour indiquer que les véhicules peuvent redémarrer
        sendControllerMessage(vehiculesEnAttente, Objetmessage.MARCHE);
    }

    public void execEntreeVehiculeUrgence(Message message, Vector2D voieEntree) {
        System.out.println("un VéhiculeController a envoyé un message d'ENTRÉE à l'intersection");
        System.out.println("sender id : " + message.getv1().getId() + " , type = " + message.getv1().getType());

        // Bloquer les entrées et envoyer un signal d'arrêt aux véhicules en attente
        ArrayList<Vehicule> vehiculesEnAttente = setEtatCellulesEtVehicules(true, voieEntree);
        sendControllerMessage(vehiculesEnAttente, Objetmessage.STOP);
    }

    public ArrayList<Vehicule> setEtatCellulesEtVehicules(boolean etat, Vector2D voieEntree) {
        ArrayList<Vehicule> vehiculesEnAttentes = new ArrayList<>();

        for (Vector2D position : cellulesCommunication) {
            if(!voieEntree.equals(position) && isEntreeIntersection(position)) {
                if (terrain.getCellule(position).estOccupee() && (terrain.getCellule(position).getIdVoiture() != 0)) {
                    int idVoiture = terrain.getCellule(position).getIdVoiture();
                    Vehicule vehicule = configuration.getVehicule(idVoiture);
                    vehicule.setEnAttente(etat);
                    vehiculesEnAttentes.add(vehicule);
                } else {
                    terrain.getCellule(position).setOccupee(etat);
                }
            }
        }

        return vehiculesEnAttentes;
    }

    private boolean isEntreeIntersection(Vector2D position) {
        return pointsEntree.contains(position);
    }

    public ArrayList<VehiculeControllerListener> getControllers(ArrayList<Vehicule> vehiculesEnAttente) {
        ArrayList<VehiculeControllerListener> controllersEnAttente = new ArrayList<>();

        // Récupérer les contrôleurs associés aux véhicules en attente
        for (VehiculeControllerListener controllerListener : vehiculeControllers) {
            VehiculeController controller = (VehiculeController) controllerListener;
            if (vehiculesEnAttente.contains(controller.getVehicule())) {
                controllersEnAttente.add(controllerListener);
            }
        }

        return controllersEnAttente;
    }

    public void sendControllerMessage(ArrayList<Vehicule> vehiculesEnAttente, Objetmessage objet) {
        // Créer et envoyer le message aux contrôleurs associés
        Message message = new Message();
        message.setObjet(objet);
        message.setv2(vehiculesEnAttente);

        ArrayList<VehiculeControllerListener> controllersEnAttente = getControllers(vehiculesEnAttente);
        sendMessageToVehiculeControllers(message, controllersEnAttente);
    }

    public boolean verifierEmbouteillage(Vector2D position) {
        Vector2D voieEntree = getVoieEntree(position);
        int cellulesOccupees = 0;
        Vector2D cellulePosition = new Vector2D();

        if (voieEntree.getX() == position.getX()) { // Cas d'une voie verticale
            int startY = Math.min(voieEntree.getY(), position.getY());
            int endY = Math.max(voieEntree.getY(), position.getY());

            // Parcourir les cellules entre les deux points (le long de l'axe Y)

            for (int y = startY; y <= endY; y++) {
                cellulePosition.setX(position.getX());
                cellulePosition.setY(y);
                Cellule cellule = terrain.getCellule(cellulePosition.copy());

                if (cellule.estOccupee()) {
                    cellulesOccupees++;
                    if (cellulesOccupees >= DISTANCE) {
                        return true; // Embouteillage détecté
                    }
                }
            }
        } else { // Cas d'une voie horizontale
            // Si les coordonnées Y sont égales, on a une voie horizontale.
            int startX = Math.min(voieEntree.getX(), position.getX());
            int endX = Math.max(voieEntree.getX(), position.getX());

            // Parcourir les cellules entre les deux points (le long de l'axe X)
            for (int x = startX; x <= endX; x++) {
                cellulePosition.setX(x);
                cellulePosition.setY(position.getY());
                Cellule cellule = terrain.getCellule(cellulePosition);

                if (cellule.estOccupee()) {
                    cellulesOccupees++;
                    if (cellulesOccupees >= DISTANCE) {
                        return true; // Embouteillage détecté
                    }
                }
            }
        }

        return false; // Pas d'embouteillage détecté
    }

}








