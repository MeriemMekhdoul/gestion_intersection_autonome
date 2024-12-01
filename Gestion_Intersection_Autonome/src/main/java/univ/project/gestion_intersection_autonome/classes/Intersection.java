package univ.project.gestion_intersection_autonome.classes;

import javafx.scene.layout.VBox;
import univ.project.gestion_intersection_autonome.controllers.VehiculeController;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Intersection implements IntersectionListener {
    private final List<Vector2D> cellulesCommunication; // Liste des cellules qui appartiennent à l'intersection (à revoir)
    private final ArrayList<Vector2D> pointsEntree;
    private ConcurrentHashMap<Direction, Integer> etatTrafic;  //ajouter une énum état trafic ?
    private /*final*/ Configuration configuration;
    private final Terrain terrain;
    private List<IntersectionListener> listeners;
    private List<VehiculeControllerListener> vehiculeControllers;
    private ArrayList<Vehicule> vehiculesBloqués;
    public static final int NB_VEHICULES_MAX = 2; // on considère qu'un embouteillage se forme si 2 cellules consécutives sont occupées
    private boolean intersectionBloquee;
    private int nbVehiculesUrgence;
    private Map<VehiculeUrgence, Vector2D> vehiculesUrgencePresents;

    public Intersection(List<Vector2D> cellulesInfluence, ArrayList<Vector2D> _pointsEntree, Terrain terrain) {
        this.cellulesCommunication = cellulesInfluence;
        this.etatTrafic = new ConcurrentHashMap<>();
        this.configuration = new Configuration();
        this.terrain = terrain;
        pointsEntree = _pointsEntree;
        listeners = new ArrayList<>();
        vehiculeControllers = new ArrayList<>();
        vehiculesBloqués = new ArrayList<>();
        intersectionBloquee = false;
        nbVehiculesUrgence = 0;
        vehiculesUrgencePresents = new HashMap<>();
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

    synchronized public void supprimerVehicule(Vehicule v){
        configuration.supprimerVehicule(v);
    }

    synchronized public void editConfig(Vehicule v, EtatVehicule etat) {
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

    synchronized public List<Vehicule> getVehicules(){
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
            if (!listener.equals(message.getv1())) { // TODO: vehicle is not an intersection
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

    //send message to all listeners
    public void sendMessageToVehiculeControllers(Message message) {
        for (VehiculeControllerListener listener : vehiculeControllers) {
            listener.onMessageReceivedFromIntersection(message);
        }
    }

    /**
     * Envoie un message à tous les contrôleurs de véhicules spécifiés.
     *
     * @param message     Le message à envoyer.
     * @param controllers La liste des contrôleurs de véhicules à qui le message sera envoyé.
     */
    public void sendMessageToVehiculeControllers(Message message, ArrayList<VehiculeControllerListener> controllers) {
        for (VehiculeControllerListener listener : controllers) {
            sendMessageToVehiculeController(message,listener);
        }
    }

    /**
     * Envoie un message à un contrôleur de véhicule spécifique.
     * Si le message indique "MARCHE", le contrôleur notifie le véhicule de reprendre son exécution.
     *
     * @param message    Le message à envoyer.
     * @param controller Le contrôleur de véhicule qui doit recevoir le message.
     */
    public void sendMessageToVehiculeController(Message message, VehiculeControllerListener controller) {
        // Si le message est 'MARCHE', le véhicule doit reprendre ou continuer son exécution
        /*if (Objects.requireNonNull(message.getObjet()) == Objetmessage.MARCHE) {
            controller.notify();
        }*/
        if(controller == null)
            System.err.println("contrôleur non trouvé.... ");
        else
            controller.onMessageReceivedFromIntersection(message);
    }

    /**
     * Gère un message reçu d'un contrôleur de véhicule.
     * Effectue les actions nécessaires selon le type de message, dans le cas d'une demande de PASSAGE
     * ou de l'information de SORTIE du véhicule d'urgence (donc bloquer/débloquer les voies)
     *
     * @param message Le message reçu du contrôleur de véhicule.
     */
    @Override
    // Traitement du message reçu de la part du véhicule controller
    public void onMessageReceivedFromVehiculeController(Message message) {

        // Obtenir la voie par laquelle le véhicule arrive (lecture, pas besoin de protection)
        Vector2D voieEntree = getVoieEntree(message.getv1().getPosition().copy());
        /*System.out.println("VEHICULE ID = " + message.getv1().getId() +
                " position actuelle du véhicule : " + message.getv1().getPosition() +
                " voie d'entrée : " + voieEntree);*/

        // Synchroniser uniquement la partie critique
        synchronized (this) {
            switch (message.getObjet()) {
                case PASSAGE -> {
                    nbVehiculesUrgence++;
                    //TODO: vérifier le cast ou généraliser le type de la map
                    vehiculesUrgencePresents.put((VehiculeUrgence) message.getv1(),voieEntree);
                    if (intersectionBloquee) {
                        //System.out.println("[véhicule : " + message.getv1().getId() + " ] doit débloquer sa voie : " + voieEntree);
                        Vehicule vehiculeAnotifier = debloquerVoie(voieEntree);
                        if (vehiculeAnotifier != null) { // is null dans le cas ou la case de la voir d'entrée est vide, donc ne contient aucun véhicule
                            notifyController(vehiculeAnotifier, Objetmessage.MARCHE);
                        }
                    } else {
                        //System.out.println("[véhicule : " + message.getv1().getId() + " ] entre en " + voieEntree + " mais ne débloque rien");
                        intersectionBloquee = true;
                        execEntreeVehiculeUrgence(message, voieEntree);
                    }
                }
                case SORTIE -> {
                    if (nbVehiculesUrgence > 1) {
                        /*System.out.println("[véhicule : " + message.getv1().getId() + " ] s'est engagé dans l'intersection par : " + voieEntree +
                                "MAIS il y a d'autres véhicules dans l'I.");*/

                        nbVehiculesUrgence--;
                        vehiculesUrgencePresents.remove((VehiculeUrgence) message.getv1());

                        boolean bloquer = true;
                        for (VehiculeUrgence autreVehiculeUrgence: vehiculesUrgencePresents.keySet()) {
                            Vector2D voieEntreeAutreVehicule = vehiculesUrgencePresents.get(autreVehiculeUrgence);
                            if(voieEntreeAutreVehicule == voieEntree) {
                                //System.out.println("[véhicule : " + message.getv1().getId() + " ] a trouvé un autre VU dans sa voie (donc ne doit pas la bloquer)");
                                bloquer = false;
                                break;
                            }
                        }
                        if (bloquer) {
                            //System.out.println("[véhicule : " + message.getv1().getId() + " ] doit bloquer sa voie derriere lui car personne d'autre derriere : " + voieEntree);

                            Vehicule vehiculeBloque = bloquerVoie(voieEntree);
                            if (vehiculeBloque != null) {
                                vehiculesBloqués.add(vehiculeBloque);
                                notifyController(vehiculeBloque, Objetmessage.STOP);
                            }
                        } //il reste une voiture derriere moi sur la même voie je ne dois pas bloquer la voie!

                    } else { //nbVehiculesUrgence = 1 (le véhicule actuel)

                        intersectionBloquee = false;
                        nbVehiculesUrgence--;
                        vehiculesUrgencePresents.remove((VehiculeUrgence) message.getv1());

                        //System.out.println("[véhicule : " + message.getv1().getId() + " ] s'est engagé et aucun VU dans l'I.");

                        execSortieVehiculeUrgence(message.getEntreeUrgence());
                    }
                }
            }
        }
    }

    /**
     * Bloque une voie d'entrée spécifique en empêchant les véhicules d'y avancer.
     * Si un véhicule est présent sur la voie, il est mis en attente.
     *
     * @param voieEntree La position de la voie d'entrée à bloquer.
     * @return Le véhicule bloqué, ou `null` s'il n'y avait aucun véhicule à bloquer.
     */
    private Vehicule bloquerVoie(Vector2D voieEntree) {
        //System.out.println("Blocage : position " + voieEntree);

        if (terrain.getCellule(voieEntree).contientVehicule()) {
            Vehicule vehicule = terrain.getCellule(voieEntree).getVehicule();

            if (vehicule != null) {
                vehicule.setEnAttente(true);
                //System.out.println("Véhicule bloqué : " + vehicule.getId());
                return vehicule;
            }
        }

        terrain.getCellule(voieEntree).setOccupee(true);

        return null; // Aucun véhicule à bloquer
    }

    /**
     * Débloque une voie d'entrée spécifique et libère le véhicule en attente dans cette voie.
     *
     * @param voieEntree La position de la voie d'entrée à débloquer.
     * @return Le véhicule débloqué, ou `null` s'il n'y avait aucun véhicule à débloquer.
     */
    private Vehicule debloquerVoie(Vector2D voieEntree) {
        //System.out.println("Déblocage : position " + voieEntree);

        for (Vehicule vehicule : vehiculesBloqués) {
            if (terrain.getCellule(voieEntree).getIdVoiture() == vehicule.getId()) {
                vehicule.setEnAttente(false);
                //System.out.println("Véhicule débloqué : " + vehicule.getId());
                return vehicule;
            }
        }
        terrain.getCellule(voieEntree).setOccupee(false);

        return null; // Aucun véhicule débloqué
    }

    /**
     * Obtient la voie d'entrée correspondante à une position donnée.
     *
     * @param position La position actuelle à partir de laquelle chercher la voie d'entrée.
     * @return La position de la voie d'entrée, ou `null` si aucune correspondance n'est trouvée.
     */
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

    /**
     * Gère la sortie d'un véhicule d'urgence à partir d'une voie d'entrée.
     * Libère les cases et les véhicules en attente sur d'autres voies.
     *
     * @param voieEntree La position de la voie d'entrée utilisée par le véhicule d'urgence.
     */
    public void execSortieVehiculeUrgence(Vector2D voieEntree){
        //System.out.println("EXEC SORTIE VEHICULE URGENCE /// voie entree = " + voieEntree);
        //libérer les cases ET les véhicules
        //get les vehicules qui sont en attente pour leur envoyer un signal et les remettre en marche
        ArrayList<Vehicule> vehiculesEnAttente = setEtatCellulesEtVehicules(false, voieEntree);

        // Préparer le message pour indiquer que les véhicules peuvent redémarrer
        notifyControllers(vehiculesEnAttente, Objetmessage.MARCHE);
    }

    /**
     * Gère l'entrée d'un véhicule d'urgence dans l'intersection.
     * Bloque les voies d'entrée et envoie un signal d'arrêt aux véhicules en attente.
     *
     * @param message    Le message indiquant l'entrée du véhicule d'urgence.
     * @param voieEntree La position de la voie d'entrée utilisée par le véhicule d'urgence.
     */
    public void execEntreeVehiculeUrgence(Message message, Vector2D voieEntree) {
        //System.out.println("EXEC ENTREE VEHICULE URGENCE /// voie entree = " + voieEntree);

        // Bloquer les entrées et envoyer un signal d'arrêt aux véhicules en attente
        ArrayList<Vehicule> vehiculesEnAttente = setEtatCellulesEtVehicules(true, voieEntree);
        vehiculesEnAttente.remove(message.getv1()); //TODO: this should not be necessary..

        notifyControllers(vehiculesEnAttente, Objetmessage.STOP);
    }

    /**
     * Met à jour l'état des cellules et des véhicules (bloqués ou débloqués).
     *
     * @param etat      `true` pour bloquer, `false` pour débloquer.
     * @param voieEntree La voie d'entrée à exclure de la mise à jour.
     * @return Une liste des véhicules qui étaient en attente, si applicable.
     */
    public ArrayList<Vehicule> setEtatCellulesEtVehicules(boolean etat, Vector2D voieEntree) {
        ArrayList<Vehicule> vehiculesEnAttente = new ArrayList<>();

        for (Vector2D position : pointsEntree) {
            if (!voieEntree.equals(position)) {
                Vehicule vehicule;
                if (etat) {
                    vehicule = bloquerVoie(position);
                    if (vehicule != null) {
                        vehiculesBloqués.add(vehicule);
                    }
                } else {
                    vehicule = debloquerVoie(position);
                    if (vehicule != null) {
                        vehiculesEnAttente.add(vehicule);
                    }
                }
            }
        }

        if (!etat) {
            vehiculesBloqués.clear();
        }

        return vehiculesEnAttente;
    }

    /**
     * Récupère les contrôleurs des véhicules en attente.
     *
     * @param vehiculesEnAttente La liste des véhicules en attente.
     * @return Une liste des contrôleurs associés à ces véhicules.
     */
    public ArrayList<VehiculeControllerListener> getControllers(ArrayList<Vehicule> vehiculesEnAttente) {
        ArrayList<VehiculeControllerListener> controllersEnAttente = new ArrayList<>();

        for (Vehicule vehicule: vehiculesEnAttente) {
            VehiculeControllerListener vc = getController(vehicule);
            if(vc != null){
                controllersEnAttente.add(vc);
            }
        }

        /* Récupérer les contrôleurs associés aux véhicules en attente
        for (VehiculeControllerListener controllerListener : vehiculeControllers) {
            VehiculeController controller = (VehiculeController) controllerListener;
            if (vehiculesEnAttente.contains(controller.getVehicule())) {
                controllersEnAttente.add(controllerListener);
            }
        }*/
        return controllersEnAttente;
    }

    /**
     * Récupère le contrôleur associé à un véhicule donné.
     *
     * @param vehiculeEnAttente Le véhicule pour lequel on cherche le contrôleur.
     * @return Le contrôleur associé, ou `null` si aucun n'est trouvé.
     */
    public VehiculeControllerListener getController(Vehicule vehiculeEnAttente) {

        // Récupérer le contrôleur associé au véhicule en attente
        for (VehiculeControllerListener controllerListener : vehiculeControllers) {
            VehiculeController controller = (VehiculeController) controllerListener;
            if (vehiculeEnAttente == controller.getVehicule()) {
                return controllerListener;
            }
        }
        return null;
    }

    /**
     * Notifie une liste de véhicules avec un message spécifique.
     *
     * @param vehiculesEnAttente La liste des véhicules à notifier.
     * @param objet              Le type de message à envoyer.
     */
    public void notifyControllers(ArrayList<Vehicule> vehiculesEnAttente, Objetmessage objet) {
        if (!vehiculesEnAttente.isEmpty()){// Créer et envoyer le message aux contrôleurs associés
            Message message = new Message();
            message.setObjet(objet);
            message.setv2(vehiculesEnAttente);

            ArrayList<VehiculeControllerListener> controllersEnAttente = getControllers(vehiculesEnAttente);
            sendMessageToVehiculeControllers(message, controllersEnAttente);
        }
    }

    /**
     * Notifie un véhicule spécifique avec un message.
     *
     * @param vehiculeEnAttente Le véhicule à notifier.
     * @param objet             Le type de message à envoyer.
     */
    public void notifyController(Vehicule vehiculeEnAttente, Objetmessage objet) {
            Message message = new Message();
            message.setObjet(objet);
            message.setv2(vehiculeEnAttente);

            VehiculeControllerListener controllerEnAttente = getController(vehiculeEnAttente);
            sendMessageToVehiculeController(message, controllerEnAttente);
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

                // Vérification si la cellule est occupée
                if (cellule.estOccupee()) {
                    cellulesOccupees++;
                    if (cellulesOccupees >= NB_VEHICULES_MAX) {
                        return true; // Embouteillage détecté
                    }
                }

                // Vérifier la direction autorisée et avancer dans la voie
                if (cellule.isDirectionAutorisee(Direction.NORD) && posVoiture.getY()!= 0) {
                    posVoiture.setY(posVoiture.getY() - 1); // Avance vers le nord
                } else if (cellule.isDirectionAutorisee(Direction.SUD) && posVoiture.getY() < terrain.getHauteur()) {
                    posVoiture.setY(posVoiture.getY() + 1); // Avance vers le sud
                } else if (cellule.isDirectionAutorisee(Direction.EST) && posVoiture.getX() < terrain.getLargeur()) {
                    posVoiture.setX(posVoiture.getX() + 1); // Avance vers l'est
                } else if (cellule.isDirectionAutorisee(Direction.OUEST) && posVoiture.getX()!= 0) {
                    posVoiture.setX(posVoiture.getX() - 1); // Avance vers l'ouest
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
                    autresVehicules.add(cellule.getVehicule());
                }
            }
        }

        return autresVehicules;
    }

    public void afficherConfiguration(){
        System.out.println(configuration);
    }


}








