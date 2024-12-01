package univ.project.gestion_intersection_autonome.controllers;

import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.shape.Circle;
import univ.project.gestion_intersection_autonome.classes.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public static final int VITESSE_SIMULATION_MS = 300;

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
        cell.setVehicule(null);

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
                //ajouter le véhicule temporairement a la config de l'intersection
                Intersection i = terrain.getIntersection(nouvellePosition);
                i.ajouterVehiculeTemp(vehicule);
                //i.afficherConfiguration();

            }
        }
    }

    protected void deplacerHorsIntersection() {
        //System.out.println("Déplacement hors intersection");

        // vérification de l'occupation de la cellule
        if (terrain.getCellule(nouvellePosition).estOccupee()) {
            //System.out.println("Cellule occupée");

            // attente libération
            while (terrain.getCellule(nouvellePosition).estOccupee()) {
                //System.out.println("Attente libération cellule");
                pauseEntreMouvements(VITESSE_SIMULATION_MS);
            }
        }

        vehicule.move(nouvellePosition);

        mettreAJourCellules();
        mettreAJourGraphique();
        //System.out.println("Vehicule ("+ vehicule.getType() + ") id = "+ vehicule.getId() + " s'est déplacé en : " + nouvellePosition);
        pauseEntreMouvements(VITESSE_SIMULATION_MS);
    }

    protected void entrerIntersection() {
        Intersection intersection = terrain.getIntersection(anciennePosition);
        intersection.addVehiculeControllerListener(this);

        ArrayList<Vector2D> deplacements = gestionIntersection();

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

        List<Vehicule> vehiculesDansIntersection = intersection.getVehicules();


        if (vehiculesDansIntersection.size() == 1) {
            intersection.editConfig(vehicule, EtatVehicule.ENGAGE);
            avancerIntersection(deplacements);
        } else //entrer dans le mode négociation, calculs et gestion des priorités
        {
            //récupérer les infos (itinéraires) des autres
            ArrayList<Message> messagesVoitures = new ArrayList<>();
            for (Vehicule v : vehiculesDansIntersection) {
                messagesVoitures.add(intersection.getMessage(v));
            }

            //construire map de Vehicule + itineraire
            Map<Vehicule,ArrayList<Vector2D>> vehiculesEngagesEtItineraires = new HashMap<>();
            Map<Vehicule,ArrayList<Vector2D>> vehiculesAttenteEtItineraires = new HashMap<>();

            for (Message m : messagesVoitures) {
                if (vehiculesEngages.contains(m.getv1())) {
                    vehiculesEngagesEtItineraires.put(m.getv1(),m.getItineraire());
                } else
                    vehiculesAttenteEtItineraires.put(m.getv1(),m.getItineraire());
            }

            int tempsAttente = vehicule.calculTempsAttente(vehiculesEngagesEtItineraires,vehiculesAttenteEtItineraires,deplacements);
            System.out.println("VEHICLE ACTUEL id = " + vehicule.getId() + "pos = " + vehicule.getPosition() + "temps attente estimé = " + tempsAttente + " mon itin: " + deplacements);
            pauseEntreMouvements(tempsAttente * VITESSE_SIMULATION_MS);

            intersection.editConfig(vehicule, EtatVehicule.ENGAGE);

            avancerIntersection(deplacements);

            Platform.runLater(() -> terrainController.effacerItineraire(vehicule, anciennePosition));

        }

        intersection.supprimerVehicule(vehicule);
        intersection.removeVehiculeControllerListener(this);
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

            // vérification de l'occupation de la cellule
            if (terrain.getCellule(pos).estOccupee()) {
                //System.out.println("Cellule occupée");

                // attente libération
                //while (terrain.getCellule(pos).estOccupee()) {
                    //System.out.println("Attente libération cellule");
                    pauseEntreMouvements(VITESSE_SIMULATION_MS);
                //}
            }

            vehicule.move(pos);
            // Afficher les informations de déplacement
            //System.out.println("Le véhicule " + vehicule.getId() + " se déplace vers : " + vehicule.getPosition());
            mettreAJourCellules();
            mettreAJourGraphique();
            //System.out.println("Vehicule ("+ vehicule.getType() + ") id = "+ vehicule.getId() + " s'est déplacé en : " + nouvellePosition);
            pauseEntreMouvements(VITESSE_SIMULATION_MS);
        }
    }

    public synchronized void mettreAJourCellules()
    {
        nouvellePosition = vehicule.getPosition().copy();
        Cellule cell2 = terrain.getCellule(nouvellePosition);

        if (!anciennePosition.equals(nouvellePosition)) {
            Cellule cell1 = terrain.getCellule(anciennePosition);
            cell1.setOccupee(false);
            cell1.setIdVoiture(0);
            cell1.setVehicule(null);

            Platform.runLater(() -> {
                terrainController.effacerItineraire(vehicule, anciennePosition);
            });
        }

        cell2.setOccupee(true);
        cell2.setIdVoiture(vehicule.getId());
        cell2.setVehicule(vehicule);
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
        System.out.println("Le véhicule de type \"" + vehicule.getType() + "\" avec l'id \"" + vehicule.getId() + "\" a reçu ce message de "+ message.getObjet());

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

    public void reprendreExecution() {
        synchronized (this) { // Synchronisation sur l'objet courant
            enPause = false;
            System.out.println("Véhicule reprend son déplacement");
            notify(); // Réveille un thread en attente
        }
    }

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


    public Vehicule getVehicule() {
        return vehicule;
    }

}



