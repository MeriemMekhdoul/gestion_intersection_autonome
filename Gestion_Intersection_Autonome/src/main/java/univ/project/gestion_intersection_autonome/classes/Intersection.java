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
    private ArrayList<Vehicule> vehiculesBloqués;
    public final int NB_VEHICULES_MAX = 2; // on considère qu'un embouteillage se forme si 2 cellules consécutives sont occupées

    public Intersection(List<Vector2D> cellulesInfluence, ArrayList<Vector2D> _pointsEntree, Terrain terrain) {
        this.cellulesCommunication = cellulesInfluence;
        this.etatTrafic = new ConcurrentHashMap<>();
        this.configuration = new Configuration();
        this.terrain = terrain;
        pointsEntree = _pointsEntree;
        vehiculesBloqués = new ArrayList<>();
    }



    public ArrayList<Vector2D> getPointsEntree() {
        return pointsEntree;
    }
    public boolean contientCellule(Vector2D position) {
        return cellulesCommunication.contains(position);
    }

    synchronized public void ajouterVehicule(Vehicule v, Message m){
        configuration.nouveauVehicule(v,m);
    }
    synchronized public void ajouterVehiculeTemp(Vehicule v){
        configuration.nouveauVehiculeTemp(v);
    }

    public void supprimerVehicule(Vehicule v){
        configuration.supprimerVehicule(v);
    }

    public void editConfig(Vehicule v, EtatVehicule etat) {
        configuration.editEtat(v.getId(),etat);
    }

    public ArrayList<Vehicule> getVehiculesEnAttente() {
        ArrayList<Vehicule> vehiculeEnAttente = new ArrayList<>();
        synchronized(configuration){
            for (int id : configuration.getEtatVehicule().keySet()) {
                Vehicule v = configuration.getVehicule(id);
                if (configuration.getEtat(id) == EtatVehicule.ATTENTE) {
                    vehiculeEnAttente.add(v);
                }
            }
        }
        return vehiculeEnAttente;
    }

    public ArrayList<Vehicule> getVehiculesEngages() {
        ArrayList<Vehicule> vehiculesEngages = new ArrayList<>();
        synchronized(configuration){
            for (int id : configuration.getEtatVehicule().keySet()) {
                Vehicule v = configuration.getVehicule(id);
                if (configuration.getEtat(id) == EtatVehicule.ENGAGE) {
                    vehiculesEngages.add(v);
                }
            }
        }
        return vehiculesEngages;
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
    public void sendMessageToVehiculeController(Message message, VehiculeControllerListener controller) {
        // Si le message est 'MARCHE', le véhicule doit reprendre ou continuer son exécution
        if (Objects.requireNonNull(message.getObjet()) == Objetmessage.MARCHE) {
            controller.notify();
        }
        controller.onMessageReceivedFromIntersection(message);
    }

    @Override //traitement du message reçu de la part du véhicule controller
    public void onMessageReceivedFromVehiculeController(Message message) {

        //get la voie par laquelle le vehicule arrive pour pouvoir bloquer les autres
        Vector2D voieEntree = getVoieEntree(message.getv1().getPosition().copy());
        System.out.println("VEHICULE ID = " + message.getv1().getId() + " position actuelle du véhicule : " + message.getv1().getPosition() + " voie d'entrée : " + voieEntree);

        switch (message.getObjet()) {
            case PASSAGE -> {
                execEntreeVehiculeUrgence(message, voieEntree);
            }
            case ENTREE -> {
                System.out.println("pos actuelle a passer a exec sortie : " + message.getEntreeUrgence());
                execSortieVehiculeUrgence(message.getEntreeUrgence());
            }
        }
    }

    public Vector2D getVoieEntree(Vector2D position) {
        for (Vector2D pos : pointsEntree) {
            if (pos.getX() == position.getX()) {
                return pos;
            } else if (pos.getY() == position.getY()) {
                return pos;
            }
        }
        return null;
    }

    public void execSortieVehiculeUrgence(Vector2D voieEntree){
        System.out.println("EXEC SORTIE VEHICULE URGENCE /// voie entree = " + voieEntree);
        System.out.println("un VéhiculeController a envoyé un message de SORTIE à l'intersection");
        //libérer les cases ET les véhicules
        //get les vehicules qui sont en attente pour leur envoyer un signal et les remettre en marche
        ArrayList<Vehicule> vehiculesEnAttente = setEtatCellulesEtVehicules(false, voieEntree);

        // Préparer le message pour indiquer que les véhicules peuvent redémarrer
        sendControllerMessage(vehiculesEnAttente, Objetmessage.MARCHE);
    }

    public void execEntreeVehiculeUrgence(Message message, Vector2D voieEntree) {
        System.out.println("un VéhiculeController a envoyé un message d'ENTRÉE à l'intersection");
        System.out.println("sender id : " + message.getv1().getId() + " , type = " + message.getv1().getType());

        System.out.println("EXEC ENTREE VEHICULE URGENCE /// voie entree = " + voieEntree);

        // Bloquer les entrées et envoyer un signal d'arrêt aux véhicules en attente
        ArrayList<Vehicule> vehiculesEnAttente = setEtatCellulesEtVehicules(true, voieEntree);
        sendControllerMessage(vehiculesEnAttente, Objetmessage.STOP);
    }

    public ArrayList<Vehicule> setEtatCellulesEtVehicules(boolean etat, Vector2D voieEntree) {
        ArrayList<Vehicule> vehiculesEnAttentes = new ArrayList<>();

        for (Vector2D position : pointsEntree) {
            if(!voieEntree.equals(position)) {
                System.out.println("i'm in setEtatCellulesEtVehicules : print configuration avant modifs :\n" + configuration);
                System.out.println("pos a bloquer : " + position);

                if(etat){ //à l'entrée
                    terrain.getCellule(position).setOccupee(true);
                    if (terrain.getCellule(position).contientVehicule()) {
                        int idVoiture = terrain.getCellule(position).getIdVoiture();
                        Vehicule vehicule = terrain.getCellule(position).getVehicule();

                        System.out.println("ETAT=TRUE l'id de la voiture a modif "+ idVoiture + " \nprint config :\n" + configuration);

                        if (vehicule != null) {
                            vehicule.setEnAttente(true);
                            //TODO:Send msg here
                            vehiculesEnAttentes.add(vehicule);
                            vehiculesBloqués.add(vehicule);
                        } else System.out.println("le véhicule dans la cellule est NULL weird parceque son id = " + idVoiture);

                    } else System.out.println("cellule ne contient pas de véhicule à priori");
                } else {
                    for (Vehicule v: vehiculesBloqués) {
                        System.out.println("ETAT=FALSE l'id de la voiture a modif "+ v.getId() + " \nprint config :\n" + configuration);
                        if (terrain.getCellule(position).getIdVoiture() == v.getId()){
                            v.setEnAttente(false);
                            //send msg here & supp de cette liste
                            vehiculesEnAttentes.add(v);  //les véhicules à notifier
                        } else System.out.println("la cellule "+ terrain.getCellule(position) + " contient id = " + terrain.getCellule(position).getIdVoiture()
                        + " mais la voiture bloquée est = " + v.getId());
                    }
                    terrain.getCellule(position).setOccupee(false);
                }
            } //je bloque pas
        }
        if (!etat)
            vehiculesBloqués.clear();
        else
            System.out.println("les vehicules bloqués (état = true) : " + vehiculesBloqués);

        return vehiculesEnAttentes;
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
        if (vehiculesEnAttente.isEmpty()){// Créer et envoyer le message aux contrôleurs associés
            Message message = new Message();
            message.setObjet(objet);
            message.setv2(vehiculesEnAttente);

            ArrayList<VehiculeControllerListener> controllersEnAttente = getControllers(vehiculesEnAttente);
            sendMessageToVehiculeControllers(message, controllersEnAttente);
        }
    }

    /**
     * Vérifie si un embouteillage se produit dans l'intersection en fonction de la position actuelle du véhicule.
     * Un embouteillage est détecté si plus de `NB_VEHICULES_MAX` véhicules sont présents dans n'importe quelle voie de l'intersection.
     *
     * @param position La position actuelle du véhicule (représentée par un objet `Vector2D`).
     * @return `true` si un embouteillage est détecté, sinon `false`.
     */
    public boolean verifierEmbouteillage(Vector2D position) {
        int cellulesOccupees = 0;

        Vector2D voieEntree = getVoieEntree(position);
        // Calcul de la distance entre la position actuelle et l'entrée de l'intersection
        int distance = getDistance(position, voieEntree);

        // Parcourir chaque point d'entrée (chaque voie différente)
        for (Vector2D pos : pointsEntree) {
            // Vérification des cellules dans la voie d'entrée en fonction de la direction
            Vector2D posVoiture = pos.copy();

            for (int d = 0; d <= distance; d++) {
                // Vérifier dans quelle direction se déplacer (en fonction de la voie)
                Cellule cellule = terrain.getCellule(posVoiture);

                // Vérifier la direction autorisée et avancer dans la voie
                if (cellule.isDirectionAutorisee(Direction.NORD)) {
                    posVoiture.setY(posVoiture.getY() - 1); // Avance vers le nord
                } else if (cellule.isDirectionAutorisee(Direction.SUD)) {
                    posVoiture.setY(posVoiture.getY() + 1); // Avance vers le sud
                } else if (cellule.isDirectionAutorisee(Direction.EST)) {
                    posVoiture.setX(posVoiture.getX() + 1); // Avance vers l'est
                } else if (cellule.isDirectionAutorisee(Direction.OUEST)) {
                    posVoiture.setX(posVoiture.getX() - 1); // Avance vers l'ouest
                }

                // Vérification si la cellule est occupée
                if (cellule.estOccupee()) {
                    cellulesOccupees++;
                    if (cellulesOccupees >= NB_VEHICULES_MAX) {
                        return true; // Embouteillage détecté
                    }
                }
            }
        }

        return false; // Pas d'embouteillage détecté
    }

    /**
     * Calcule et retourne le nombre de cases entre deux positions (distance de Manhattan)
     * sur une même ligne ou colonne dans une grille.
     *
     * @param position   La position de départ.
     * @param voieEntree La position d'arrivée, généralement l'entrée de l'intersection.
     * @return Le nombre de cases entre la position et l'entrée de l'intersection.
     */
    private int getDistance(Vector2D position, Vector2D voieEntree){
        int start;
        int end;

        if (voieEntree.getX() == position.getX()) { // Cas d'une voie verticale
            start = Math.min(voieEntree.getY(), position.getY());
            end = Math.max(voieEntree.getY(), position.getY());
        } else { // Cas d'une voie horizontale
            start = Math.min(voieEntree.getX(), position.getX());
            end = Math.max(voieEntree.getX(), position.getX());
        }
        return end - start;
    }

    /**
     * Récupère tous les véhicules se trouvant dans les voies à partir des points d'entrée,
     * dans un rayon donné (distance) depuis chaque point d'entrée.
     *
     * @param distance La distance maximale (en cases) depuis chaque point d'entrée pour rechercher les véhicules.
     * @return Une liste des véhicules présents dans les autres voies, dans la limite de la distance spécifiée.
     */
    private List<Vehicule> obtenirTousLesVehiculesDansAutresVoies(int distance) {
        List<Vehicule> autresVehicules = new ArrayList<>();

        // Parcourir chaque point d'entrée (chaque voie différente)
        for (Vector2D position : pointsEntree) {
            Vector2D posVoiture = position.copy();
            // Parcourir les cellules dans un rayon autour du point d'entrée
            for (int d = 0; d <= distance; d++) {
            // Obtenir la cellule à la distance d le long de la voie

                // Vérifier si la cellule contient un véhicule
                Cellule cellule = terrain.getCellule(posVoiture);
                if (cellule.isDirectionAutorisee(Direction.NORD)) {
                    // Si la direction NORD est autorisée, on diminue Y pour avancer vers le haut
                    posVoiture.setY(posVoiture.getY() - 1);
                } else if (cellule.isDirectionAutorisee(Direction.SUD)) {
                    // Si la direction SUD est autorisée, on augmente Y pour avancer vers le bas
                    posVoiture.setY(posVoiture.getY() + 1);
                } else if (cellule.isDirectionAutorisee(Direction.EST)) {
                    // Si la direction 'EST' est autorisée, on augmente X pour avancer vers la droite
                    posVoiture.setX(posVoiture.getX() + 1);
                } else if (cellule.isDirectionAutorisee(Direction.OUEST)) {
                    // Si la direction OUEST est autorisée, on diminue X pour avancer vers la gauche
                    posVoiture.setX(posVoiture.getX() - 1);
                }

                if (cellule.contientVehicule()) {
                    // Ajouter le véhicule dans la liste des autres véhicules
                    //autresVehicules.add(cellule.getVehicule());
                }
            }
        }

        return autresVehicules;
    }

    public void afficherConfiguration(){
        System.out.println(configuration);
    }


}








