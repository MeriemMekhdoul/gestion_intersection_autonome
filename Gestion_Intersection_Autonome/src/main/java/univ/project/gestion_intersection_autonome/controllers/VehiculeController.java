package univ.project.gestion_intersection_autonome.controllers;

import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import javafx.scene.shape.Circle;
import univ.project.gestion_intersection_autonome.classes.*;

import java.util.ArrayList;
import java.util.List;

public class VehiculeController implements Runnable,VehiculeControllerListener {
    protected final Vehicule vehicule;
    protected final Terrain terrain;
    protected final TerrainController terrainController;
    protected Vector2D anciennePosition;
    protected Vector2D nouvellePosition;
    protected final Shape vehiculeShape; // référence de la forme du véhicule
    protected boolean entreeIntersection = true; // pour savoir si on rentre ou on sort d'une intersection
    protected List<VehiculeControllerListener> listeners = new ArrayList<>();
    protected IntersectionListener intersectionListener; //on n'a peut-être pas besoin d'une liste ?? une seule intersection suffit
    protected boolean enPause = false;
    protected final int VITESSE_SIMULATION_MS = 100;

    public VehiculeController(Vehicule vehicule, Terrain terrain, TerrainController terrainController) {
        this.vehicule = vehicule;
        this.terrain = terrain;
        this.terrainController = terrainController;
        this.vehiculeShape = creerVehiculeShape(vehicule.getType());
    }

    @Override
    public void run() {
        List<Vector2D> itineraire = vehicule.getItineraire();
        anciennePosition = vehicule.getPosition().copy();
        nouvellePosition = vehicule.getPosition().copy();
        mettreAJourGraphique();

        // on démarre à 1, car 0 est la position de départ (actuelle)
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
            //System.out.println("J'entre dans le run avant de me déplacer");
            //System.out.println("J'affiche avant de me déplacer");

            deplacement();
            //mettre l'index à jour dans le cas du déplacement dans l'intersection
            i = itineraire.indexOf(nouvellePosition);
        }
        finDeplacement();
    }
    public void finDeplacement(){
        // Libérer la dernière cellule occupée
        Cellule cell = terrain.getCellule(nouvellePosition);
        cell.setOccupee(false);
        cell.setIdVoiture(0);

        Platform.runLater(() -> {
            terrainController.vehiclePane.getChildren().remove(vehiculeShape);
            terrainController.getSimulation().supprimerVehicule(vehicule, this);
        });
    }

    public void deplacement() {
        //System.out.println("Le véhicule se déplace");
        if (estDansCommunication(anciennePosition) && entreeIntersection) {
            //System.out.println("je suis dans une zone de communication");
            entrerIntersection();
            entreeIntersection = false; //je suis sortie de l'intersection
        } else {
            //System.out.println("je suis en zone normale");
            deplacerHorsIntersection();
            //mettre à jour l'attribut "entree" pour savoir si on arrive de nouveau dans une intersection ou pas
            if (estDansCommunication(nouvellePosition)) {
                //System.out.println("je m'apprête à entrer dans une nouvelle intersection");
                entreeIntersection = true;
            }
        }
    }

    protected void deplacerHorsIntersection() {
        //System.out.println("Déplacement hors intersection");

        // vérification de l'occupation de la cellule
        if (terrain.getCellule(nouvellePosition).estOccupee()) {
            System.out.println("Cellule occupée");

            // attente libération
            while (terrain.getCellule(nouvellePosition).estOccupee()) {
                System.out.println("Attente libération cellule");
                pauseEntreMouvements(VITESSE_SIMULATION_MS);
            }
        }

        vehicule.move(nouvellePosition);

        //System.out.println("Le véhicule " + vehicule.getId() + " se déplace vers : " + vehicule.getPosition());
        mettreAJourCellules();
        mettreAJourGraphique();
        pauseEntreMouvements(VITESSE_SIMULATION_MS);
    }

    protected void entrerIntersection() {
        Intersection intersection = terrain.getIntersection(anciennePosition);
        intersection.addVehiculeControllerListener(this);

        //System.out.println("Entrée dans une intersection");
        ArrayList<Vector2D> deplacements = gestionIntersection();
        //System.out.println("Itinéraire dans l'intersection : " + deplacements);

        // dessiner l'itinéraire sur la grille
        Platform.runLater(() -> {
            terrainController.dessinerItineraire(deplacements, vehicule);
        });

        Message message = new Message();
        //Rajouter un nv constructeur
        message.setObjet(Objetmessage.INFORMATION);
        message.setv1(vehicule);
        message.setItineraire(deplacements);

        intersection.ajouterVehicule(vehicule, message); //l'ajouter a la config
        ArrayList<Vehicule> vehiculesEngages = intersection.getVehiculesEngages(); //les véhicules qui ne sont pas engagés
        //System.out.println("vehiculesEngages  = " + vehiculesEngages);

        ArrayList<Vehicule> vehiculesDansIntersection = intersection.getVehicules();
        //System.out.println("vehiculesDansIntersection  = " + vehiculesDansIntersection);


        if (vehiculesDansIntersection.size() == 1) {
            //send message "Engagée" ????
            intersection.editConfig(vehicule, EtatVehicule.ENGAGE);
            //System.out.println("aucun vehicule dans l'intersection donc j'avance'");
            avancerIntersection(deplacements);
            intersection.supprimerVehicule(vehicule);
            //System.out.println("je suis sorti de l'intersection et j'ai supp le vehicule de la config");
        } else //entrer dans le mode négociation, calculs et gestion des priorités
        {
            //System.out.println("des véhicule sont dans l'intersection");
            //récupérer les infos (itinéraires) des autres
            ArrayList<Message> messagesVoitures = new ArrayList<>();
            for (Vehicule v : vehiculesDansIntersection) {
                messagesVoitures.add(intersection.getMessage(v));
            }


            //System.out.println("je calcule mon temps d'attente");
            int tempsAttente = calculs(messagesVoitures, deplacements, vehiculesEngages);

            //System.out.println("temps d'attente calculé et estimé à : " + tempsAttente + " secondes");
            pauseEntreMouvements(tempsAttente * VITESSE_SIMULATION_MS);
            //System.out.println("attente effectuée, conflit évité, update l'état à ENGAGE puis j'avance");

            intersection.editConfig(vehicule, EtatVehicule.ENGAGE);
            //Envoyer un message avant de s'engager ??

            avancerIntersection(deplacements);

            Platform.runLater(() -> {
                terrainController.effacerItineraire(vehicule, anciennePosition);
            });

            intersection.supprimerVehicule(vehicule);


            //à la sortie envoyer un msg de SORTIE (à qui ??) => intersection ou véhiculesDestinataires ?
            //}
        }
    }

    /**
     * La fonction vérifie s'il y aura un potentiel conflit dans l'intersection.
     * Si un conflit est détecté, retourne `true` et met à jour la liste des véhicules impliqués dans le conflit.
     * Sinon, retourne `false` et la liste reste vide.
     *
     * @param messagesReçus      Liste des messages contenant les informations sur les véhicules et leurs itinéraires.
     * @param itineraire         L'itinéraire du véhicule actuel.
     * @param vehiculesEnConflit Liste des véhicules qui causent un conflit (mise à jour si conflit détecté).
     * @return `true` s'il y a un conflit, sinon `false`.
     */
    public static boolean conflit(ArrayList<Message> messagesReçus, ArrayList<Vector2D> itineraire, ArrayList<Vehicule> vehiculesEnConflit
            , ArrayList<ArrayList<Vector2D>> itinerairesVoitures) {
        // Vider la liste des véhicules en conflit pour un nouveau calcul
        vehiculesEnConflit.clear();

        int i = 0;

        // Récupérer un tableau des itinéraires depuis les messages
        for (Message message : messagesReçus) {
            ArrayList<Vector2D> itineraireAutreVehicule = itinerairesVoitures.get(i);//message.getItineraire();
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
     * Compare deux itinéraires pour détecter une éventuelle collision. (à renommer en détécterCollision())
     *
     * @param itin1 Le premier itinéraire.
     * @param itin2 Le second itinéraire.
     * @return `true` s'il y a une collision, sinon `false`.
     */
    public static boolean compareItineraire(ArrayList<Vector2D> itin1, ArrayList<Vector2D> itin2) {
        for (int i = 0; i < itin1.size(); i++) {
            if (i < itin2.size()) {
                if (itin1.get(i).equals(itin2.get(i)))
                    return true;
            } else return false;
        }
        return false; //pas de collision
    }

    public int calculs(ArrayList<Message> messagesReçus, ArrayList<Vector2D> itineraire, ArrayList<Vehicule> vehiculesEngages) {
        ArrayList<Vehicule> vehiculesenconflit = new ArrayList<>();
        int tempsAttente = 0;

        ArrayList<ArrayList<Vector2D>> nouveauxItineraires = new ArrayList<>();

        for (Message message : messagesReçus) {
            ArrayList<Vector2D> itineraireAmodifier = message.getItineraire();

            if (vehiculesEngages.contains(message.getv1())) {
                Vector2D posActuV = message.getv1().getPosition().copy();
                int index = itineraireAmodifier.indexOf(posActuV);
                //truck tableau a partir de l'index
                ArrayList<Vector2D> newItineraire = new ArrayList<>();
                for (int i = index; i < itineraireAmodifier.size(); i++) {
                    newItineraire.add(itineraireAmodifier.get(i));
                }
                nouveauxItineraires.add(newItineraire);
            }
            nouveauxItineraires.add(itineraireAmodifier);
        }

        if (conflit(messagesReçus, itineraire, vehiculesenconflit, nouveauxItineraires)) {
            for (Vehicule v : vehiculesenconflit) {
                if (vehiculesEngages.contains(v) && (v != vehicule)) {
                    tempsAttente++;
                }
            }
        }
        return tempsAttente; // Retourner le temps d'attente
    }


    protected boolean estDansCommunication(Vector2D position) {
        return terrain.getCellule(position).getTypeZone() == TypeZone.COMMUNICATION;
    }

    public ArrayList<Vector2D> gestionIntersection() {
        List<Vector2D> itineraire = vehicule.getItineraire();
        ArrayList<Vector2D> deplacements = new ArrayList<>();

        int index = itineraire.indexOf(anciennePosition) + 1;

        //tant que je ne suis pas dans une cellule de communication je continue sinon j'arrête et je renvoie mon tableau
        Vector2D posSuivante = itineraire.get(index);
        deplacements.add(posSuivante);

        while (!estDansCommunication(posSuivante)) {
            index++;
            posSuivante = itineraire.get(index);
            deplacements.add(posSuivante);
        }
        return deplacements;
    }

    protected void avancerIntersection(List<Vector2D> deplacements) {
        for (Vector2D pos : deplacements) {
            anciennePosition = vehicule.getPosition().copy();
            vehicule.move(pos);
            // Afficher les informations de déplacement
            //System.out.println("Le véhicule " + vehicule.getId() + " se déplace vers : " + vehicule.getPosition());
            mettreAJourCellules();
            mettreAJourGraphique();
            pauseEntreMouvements(VITESSE_SIMULATION_MS);
        }
    }

    public synchronized void mettreAJourCellules()
    {
        //System.out.println("mettreAJourCellules called with anciennePosition: " + anciennePosition + ", nouvellePosition: " + nouvellePosition);
        nouvellePosition = vehicule.getPosition().copy();
        Cellule cell2 = terrain.getCellule(nouvellePosition);

        if (!anciennePosition.equals(nouvellePosition)) {
            Cellule cell1 = terrain.getCellule(anciennePosition);
            cell1.setOccupee(false);
            cell1.setIdVoiture(0);

            Platform.runLater(() -> {
                terrainController.effacerItineraire(vehicule, anciennePosition);
            });
        }

        cell2.setOccupee(true);
        cell2.setIdVoiture(vehicule.getId());
    }

    protected void mettreAJourGraphique() {
        Platform.runLater(() -> {
            terrainController.animerDeplacementVehicule(vehiculeShape, anciennePosition, nouvellePosition, VITESSE_SIMULATION_MS);
        });
    }


    protected void pauseEntreMouvements(int millisecondes) {
        try {
            Thread.sleep(millisecondes);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected Shape creerVehiculeShape(TypeVehicule typeVehicule) {
        Color couleurVehicule = vehicule.getCouleur();
        Shape shape;

        double radius = terrainController.TAILLE_CELLULE / 2; // Ajuster si nécessaire

        switch (typeVehicule) {
            case VOITURE -> {
                shape = new Circle(radius, couleurVehicule);
            }
            case URGENCE -> {
                shape = new Circle(radius, Color.BLUE);
            }
            default -> {
                shape = new Circle(radius, couleurVehicule);
            }
        }

        double initialX = vehicule.getPosition().getX() * terrainController.TAILLE_CELLULE + radius;
        double initialY = vehicule.getPosition().getY() * terrainController.TAILLE_CELLULE + radius;

        shape.setTranslateX(initialX);
        shape.setTranslateY(initialY);

        Platform.runLater(() -> {
            terrainController.vehiclePane.getChildren().add(shape);
        });

        return shape;
    }





    //vehiculeController(s) en écoute de ce vehiculeController
    public void addListener(VehiculeControllerListener listener) {
        listeners.add(listener);
    }
    public void removeListener (VehiculeControllerListener listener){
            listeners.remove(listener);
    }
    protected void notifyListeners(Message message) {
        for (VehiculeControllerListener listener : listeners) {
            if (!listener.equals(message.getv1())) {
                listener.messageVc(message);
            }
        }
    }
    public void sendMessageVc (Message message) {
        notifyListeners(message); // Notifie tous les observateurs
    }
    @Override // Traitement du message reçu
    public void messageVc(Message message) {
        //System.out.println("Le véhicule de type \"" + message.getv1().getType() + "\" et id \"" + message.getv1().getId() +
        //        "\" envoie ce message : " + message.getT() + ", objet : " + message.getObjet() +
        //        ", itinéraire : " + message.getItineraire());

        //System.out.println("Le véhicule de type \"" + vehicule.getType() + "\" et id \"" + vehicule.getId() + "\" a reçu ce message.");
    }


    //intersections en écoute du vc
    public void addIntersectionListener(IntersectionListener listener) {
        intersectionListener = listener;
    }

    /*public void removeIntersectionListener(IntersectionListener listener) {
        intersections.remove(listener);
    }*/

    public void sendMessageToIntersections(Message message) {
        intersectionListener.onMessageReceivedFromVehiculeController(message);
    }
    @Override //traitement du message reçu de l'intersection
    public void onMessageReceivedFromIntersection(Message message) {
        System.out.println("Le véhicule de type \"" + vehicule.getType() + "\" avec l'id \"" + vehicule.getId() + "\" a reçu ce message.");

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

    public synchronized void mettreEnPause() {
        enPause = true;
        System.out.println("Véhicule mis en pause");
    }

    public synchronized void reprendreExecution() {
        enPause = false;
        notify(); // Relance le thread si le véhicule est en pause
        System.out.println("Véhicule reprend son déplacement");
    }

    public Vehicule getVehicule() {
        return vehicule;
    }

}



