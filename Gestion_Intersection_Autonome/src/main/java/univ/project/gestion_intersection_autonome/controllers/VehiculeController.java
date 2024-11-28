package univ.project.gestion_intersection_autonome.controllers;

import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import univ.project.gestion_intersection_autonome.classes.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contrôleur de véhicule qui gère le déplacement du véhicule sur le terrain,
 * les interactions avec les intersections et la mise à jour graphique.
 */
public class VehiculeController implements Runnable, VehiculeControllerListener {
    protected final Vehicule vehicule;
    protected final Terrain terrain;
    protected final TerrainController terrainController;
    protected Vector2D anciennePosition;
    protected Vector2D nouvellePosition;
    protected final Shape vehiculeShape; // Référence de la forme du véhicule
    protected boolean entreeIntersection = true; // Pour savoir si on entre ou on sort d'une intersection
    protected List<VehiculeControllerListener> listeners = new ArrayList<>();
    protected IntersectionListener intersectionListener; // Écouteur pour l'intersection
    protected boolean enPause = false;
    public static final int VITESSE_SIMULATION_MS = 100;

    /**
     * Constructeur du contrôleur de véhicule.
     *
     * @param vehicule          Le véhicule à contrôler.
     * @param terrain           Le terrain sur lequel le véhicule se déplace.
     * @param terrainController Le contrôleur du terrain pour les mises à jour graphiques.
     */
    public VehiculeController(Vehicule vehicule, Terrain terrain, TerrainController terrainController) {
        this.vehicule = vehicule;
        this.terrain = terrain;
        this.terrainController = terrainController;
        this.vehiculeShape = creerVehiculeShape(vehicule.getType());
    }

    /**
     * Méthode exécutée par le thread, gère le déplacement du véhicule sur son itinéraire.
     */
    @Override
    public void run() {
        List<Vector2D> itineraire = vehicule.getItineraire();
        anciennePosition = vehicule.getPosition().copy();
        nouvellePosition = vehicule.getPosition().copy();
        mettreAJourGraphique();

        // On démarre à 1, car 0 est la position de départ (actuelle)
        for (int i = 0; i < itineraire.size(); i++) {
            // Vérifier si le véhicule est en pause
            synchronized (this) {
                while (enPause) {
                    try {
                        wait(); // Attend que 'enPause' soit false
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        System.out.println("Le thread du véhicule a été interrompu.");
                    }
                }
            }

            // Déplacer le véhicule uniquement s'il n'est pas en pause
            anciennePosition = vehicule.getPosition().copy();
            nouvellePosition = itineraire.get(i);

            deplacement();
            // Mettre l'index à jour dans le cas du déplacement dans l'intersection
            i = itineraire.indexOf(nouvellePosition);
        }
        finDeplacement();
    }

    /**
     * Méthode appelée lorsque le véhicule a terminé son déplacement.
     * Libère la dernière cellule occupée et met à jour l'interface graphique.
     */
    public void finDeplacement() {
        // Libérer la dernière cellule occupée
        Cellule cell = terrain.getCellule(nouvellePosition);
        cell.setOccupee(false);
        cell.setIdVoiture(0);
        cell.setVehicule(null);

        Platform.runLater(() -> {
            terrainController.vehiclePane.getChildren().remove(vehiculeShape);
            terrainController.getSimulation().supprimerVehicule(vehicule, this);
        });
    }

    /**
     * Gère le déplacement du véhicule, en distinguant les zones d'intersection et les zones normales.
     */
    public void deplacement() {
        if (estDansCommunication(anciennePosition) && entreeIntersection) {
            entrerIntersection();
            entreeIntersection = false; // Le véhicule est sorti de l'intersection
        } else {
            deplacerHorsIntersection();
            // Mettre à jour l'attribut "entreeIntersection" pour savoir si on arrive de nouveau dans une intersection ou pas
            if (estDansCommunication(nouvellePosition)) {
                entreeIntersection = true;
                // Ajouter le véhicule temporairement à la configuration de l'intersection
                Intersection i = terrain.getIntersection(nouvellePosition);
                i.ajouterVehiculeTemp(vehicule);
            }
        }
    }

    /**
     * Déplace le véhicule hors des intersections, en gérant les éventuelles collisions.
     */
    protected void deplacerHorsIntersection() {
        // Vérification de l'occupation de la cellule
        if (terrain.getCellule(nouvellePosition).estOccupee()) {
            // Attente de la libération de la cellule
            while (terrain.getCellule(nouvellePosition).estOccupee()) {
                pauseEntreMouvements(VITESSE_SIMULATION_MS);
            }
        }

        vehicule.move(nouvellePosition);

        mettreAJourCellules();
        mettreAJourGraphique();
        pauseEntreMouvements(VITESSE_SIMULATION_MS);
    }

    /**
     * Gère l'entrée du véhicule dans une intersection, en réalisant les négociations nécessaires avec les autres véhicules.
     */
    protected void entrerIntersection() {
        Intersection intersection = terrain.getIntersection(anciennePosition);
        intersection.addVehiculeControllerListener(this);

        ArrayList<Vector2D> deplacements = gestionIntersection();

        // Dessiner l'itinéraire sur la grille
        Platform.runLater(() -> terrainController.dessinerItineraire(deplacements, vehicule));

        Message message = new Message();
        message.setObjet(Objetmessage.INFORMATION);
        message.setv1(vehicule);
        message.setItineraire(deplacements);

        intersection.ajouterVehicule(vehicule, message); // L'ajouter à la configuration
        ArrayList<Vehicule> vehiculesEngages = intersection.getVehiculesEngages(); // Les véhicules engagés

        List<Vehicule> vehiculesDansIntersection = intersection.getVehicules();

        if (vehiculesDansIntersection.size() == 1) {
            intersection.editConfig(vehicule, EtatVehicule.ENGAGE);
            avancerIntersection(deplacements);
        } else {
            // Entrer dans le mode négociation, calculs et gestion des priorités
            // Récupérer les infos (itinéraires) des autres
            ArrayList<Message> messagesVoitures = new ArrayList<>();
            for (Vehicule v : vehiculesDansIntersection) {
                messagesVoitures.add(intersection.getMessage(v));
            }

            // Construire une map de Vehicule + itinéraire
            Map<Vehicule, ArrayList<Vector2D>> vehiculesEngagesEtItineraires = new HashMap<>();
            Map<Vehicule, ArrayList<Vector2D>> vehiculesAttenteEtItineraires = new HashMap<>();

            for (Message m : messagesVoitures) {
                if (vehiculesEngages.contains(m.getv1())) {
                    vehiculesEngagesEtItineraires.put(m.getv1(), m.getItineraire());
                } else
                    vehiculesAttenteEtItineraires.put(m.getv1(), m.getItineraire());
            }

            int tempsAttente = vehicule.calculTempsAttente(vehiculesEngagesEtItineraires, vehiculesAttenteEtItineraires, deplacements);
            pauseEntreMouvements(tempsAttente * VITESSE_SIMULATION_MS);

            intersection.editConfig(vehicule, EtatVehicule.ENGAGE);

            avancerIntersection(deplacements);

            Platform.runLater(() -> terrainController.effacerItineraire(vehicule, anciennePosition));
            // À la sortie, envoyer un message de SORTIE (à qui ??) => intersection ou véhicules destinataires ?
        }

        intersection.supprimerVehicule(vehicule);
        intersection.removeVehiculeControllerListener(this);
    }

    /**
     * Vérifie s'il y a un potentiel conflit dans l'intersection avec les autres véhicules.
     * Si un conflit est détecté, met à jour la liste des véhicules en conflit.
     *
     * @param messagesRecus       Liste des messages contenant les informations sur les véhicules et leurs itinéraires.
     * @param itineraire          L'itinéraire du véhicule actuel.
     * @param vehiculesEnConflit  Liste des véhicules qui causent un conflit (mise à jour si conflit détecté).
     * @param itinerairesVoitures Liste des itinéraires des autres véhicules.
     * @return `true` s'il y a un conflit, sinon `false`.
     */
    public boolean conflit(ArrayList<Message> messagesRecus, ArrayList<Vector2D> itineraire, ArrayList<Vehicule> vehiculesEnConflit,
                           ArrayList<ArrayList<Vector2D>> itinerairesVoitures) {
        // Vider la liste des véhicules en conflit pour un nouveau calcul
        vehiculesEnConflit.clear();

        int i = 0;

        // Récupérer un tableau des itinéraires depuis les messages
        for (Message message : messagesRecus) {
            ArrayList<Vector2D> itineraireAutreVehicule = itinerairesVoitures.get(i);
            i++;

            // Si une collision est détectée entre les itinéraires
            if (compareItineraire(itineraire, itineraireAutreVehicule)) {
                // Ajouter le véhicule en conflit à la liste
                vehiculesEnConflit.add(message.getv1());
            }
        }
        // Si des véhicules en conflit sont détectés, retourner vrai
        return !vehiculesEnConflit.isEmpty();
    }

    /**
     * Compare deux itinéraires pour détecter une éventuelle collision.
     *
     * @param itin1 Le premier itinéraire.
     * @param itin2 Le second itinéraire.
     * @return `true` s'il y a une collision, sinon `false`.
     */
    public boolean compareItineraire(ArrayList<Vector2D> itin1, ArrayList<Vector2D> itin2) {
        for (int i = 0; i < itin1.size(); i++) {
            if (i < itin2.size()) {
                if (itin1.get(i).equals(itin2.get(i)))
                    return true;
            } else return false;
        }
        return false; // Pas de collision
    }

    /**
     * Vérifie si la position donnée se trouve dans une zone de communication (intersection).
     *
     * @param position La position à vérifier.
     * @return `true` si la position est dans une zone de communication, sinon `false`.
     */
    protected boolean estDansCommunication(Vector2D position) {
        return terrain.getCellule(position).getTypeZone() == TypeZone.COMMUNICATION;
    }

    /**
     * Génère l'itinéraire du véhicule au sein de l'intersection à partir de sa position actuelle.
     *
     * @return Une liste de positions représentant l'itinéraire dans l'intersection.
     */
    public ArrayList<Vector2D> gestionIntersection() {
        List<Vector2D> itineraire = vehicule.getItineraire();
        ArrayList<Vector2D> deplacements = new ArrayList<>();

        int index = itineraire.indexOf(anciennePosition) + 1;

        // Tant que je ne suis pas dans une cellule de communication, je continue sinon j'arrête et je renvoie mon tableau
        Vector2D posSuivante = itineraire.get(index);
        deplacements.add(posSuivante);

        while (!estDansCommunication(posSuivante)) {
            index++;
            posSuivante = itineraire.get(index);
            deplacements.add(posSuivante);
        }
        return deplacements;
    }

    /**
     * Fait avancer le véhicule à travers l'intersection en suivant les déplacements spécifiés.
     *
     * @param deplacements Liste des positions à parcourir dans l'intersection.
     */
    protected void avancerIntersection(List<Vector2D> deplacements) {
        for (Vector2D pos : deplacements) {
            anciennePosition = vehicule.getPosition().copy();

            // Vérification de l'occupation de la cellule
            if (terrain.getCellule(pos).estOccupee()) {
                // Attente de la libération de la cellule
                while (terrain.getCellule(pos).estOccupee()) {
                    pauseEntreMouvements(VITESSE_SIMULATION_MS / 3);
                }
            }

            vehicule.move(pos);
            mettreAJourCellules();
            mettreAJourGraphique();
            pauseEntreMouvements(VITESSE_SIMULATION_MS);
        }
    }

    /**
     * Met à jour l'état des cellules du terrain en fonction de la nouvelle position du véhicule.
     */
    public synchronized void mettreAJourCellules() {
        nouvellePosition = vehicule.getPosition().copy();
        Cellule cell2 = terrain.getCellule(nouvellePosition);

        if (!anciennePosition.equals(nouvellePosition)) {
            Cellule cell1 = terrain.getCellule(anciennePosition);
            cell1.setOccupee(false);
            cell1.setIdVoiture(0);
            cell1.setVehicule(null);

            Platform.runLater(() -> terrainController.effacerItineraire(vehicule, anciennePosition));
        }

        cell2.setOccupee(true);
        cell2.setIdVoiture(vehicule.getId());
        cell2.setVehicule(vehicule);
    }

    /**
     * Met à jour l'interface graphique pour refléter la nouvelle position du véhicule.
     */
    protected void mettreAJourGraphique() {
        Platform.runLater(() -> terrainController.animerDeplacementVehicule(vehiculeShape, anciennePosition, nouvellePosition, VITESSE_SIMULATION_MS));
    }

    /**
     * Met en pause le thread du véhicule pour un certain temps.
     *
     * @param millisecondes Durée de la pause en millisecondes.
     */
    protected void pauseEntreMouvements(int millisecondes) {
        try {
            Thread.sleep(millisecondes);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Crée la représentation graphique du véhicule en fonction de son type.
     *
     * @param typeVehicule Le type du véhicule.
     * @return La forme graphique représentant le véhicule.
     */
    protected Shape creerVehiculeShape(TypeVehicule typeVehicule) {
        Color couleurVehicule = vehicule.getCouleur();
        Shape shape;

        double radius = (double) terrainController.TAILLE_CELLULE / 2; // Ajuster si nécessaire

        switch (typeVehicule) {
            case URGENCE -> shape = new Circle(radius, Color.BLUE);
            default -> shape = new Circle(radius, couleurVehicule);
        }

        double initialX = vehicule.getPosition().getX() * terrainController.TAILLE_CELLULE + radius;
        double initialY = vehicule.getPosition().getY() * terrainController.TAILLE_CELLULE + radius;

        shape.setTranslateX(initialX);
        shape.setTranslateY(initialY);

        Platform.runLater(() -> terrainController.vehiclePane.getChildren().add(shape));

        return shape;
    }

    /**
     * Ajoute un écouteur pour les messages du véhicule.
     *
     * @param listener L'écouteur à ajouter.
     */
    public void addListener(VehiculeControllerListener listener) {
        listeners.add(listener);
    }

    /**
     * Retire un écouteur des messages du véhicule.
     *
     * @param listener L'écouteur à retirer.
     */
    public void removeListener(VehiculeControllerListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notifie tous les écouteurs d'un nouveau message.
     *
     * @param message Le message à envoyer aux écouteurs.
     */
    protected void notifyListeners(Message message) {
        for (VehiculeControllerListener listener : listeners) {
            if (!listener.equals(message.getv1())) {
                listener.messageVc(message);
            }
        }
    }

    /**
     * Envoie un message à tous les écouteurs du véhicule.
     *
     * @param message Le message à envoyer.
     */
    public void sendMessageVc(Message message) {
        notifyListeners(message); // Notifie tous les observateurs
    }

    /**
     * Gère la réception d'un message d'un autre véhicule.
     *
     * @param message Le message reçu.
     */
    @Override
    public void messageVc(Message message) {
        // Traitement du message reçu
    }

    /**
     * Ajoute un écouteur pour les messages des intersections.
     *
     * @param listener L'écouteur à ajouter.
     */
    public void addIntersectionListener(IntersectionListener listener) {
        intersectionListener = listener;
    }

    /**
     * Envoie un message à l'intersection associée.
     *
     * @param message Le message à envoyer.
     */
    public void sendMessageToIntersections(Message message) {
        intersectionListener.onMessageReceivedFromVehiculeController(message);
    }

    /**
     * Gère la réception d'un message de l'intersection.
     *
     * @param message Le message reçu de l'intersection.
     */
    @Override
    public void onMessageReceivedFromIntersection(Message message) {
        System.out.println("Le véhicule de type \"" + vehicule.getType() + "\" avec l'id \"" + vehicule.getId() + "\" a reçu ce message de " + message.getObjet());

        // Traitement du message en fonction de l'objet
        switch (message.getObjet()) {
            case MARCHE ->
                // Si le message est MARCHE, le véhicule doit reprendre ou continuer son exécution
                    reprendreExecution();
            case STOP ->
                // Si le message est STOP, le véhicule doit se mettre en pause
                    mettreEnPause();
            default -> System.out.println("Objet de message non reconnu : " + message.getObjet());
        }
    }

    /**
     * Reprend l'exécution du véhicule si celui-ci était en pause.
     */
    public void reprendreExecution() {
        synchronized (this) { // Synchronisation sur l'objet courant
            enPause = false;
            System.out.println("Véhicule reprend son déplacement");
            notify(); // Réveille un thread en attente
        }
    }

    /**
     * Met le véhicule en pause, interrompant son déplacement.
     */
    public void mettreEnPause() {
        synchronized (this) { // Synchronisation sur l'objet courant
            enPause = true;
            System.out.println("Véhicule mis en pause");
            while (enPause) { // Boucle pour rester en attente tant que le véhicule est en pause
                try {
                    wait(); // Le thread se met en attente
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Bonne pratique : restaurer l'état d'interruption
                }
            }
        }
    }

    /**
     * Retourne le véhicule contrôlé par ce contrôleur.
     *
     * @return Le véhicule associé.
     */
    public Vehicule getVehicule() {
        return vehicule;
    }
}
