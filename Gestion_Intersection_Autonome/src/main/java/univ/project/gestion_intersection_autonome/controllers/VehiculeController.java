package univ.project.gestion_intersection_autonome.controllers;

import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import univ.project.gestion_intersection_autonome.classes.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class VehiculeController implements Runnable {
    private final Vehicule vehicule;
    private final Terrain terrain;
    private final TerrainController terrainController;
    private Vector2D anciennePosition;
    private Vector2D nouvellePosition;
    private final Shape vehiculeShape; // référence de la forme du véhicule
    private boolean entreeIntersection = true; // pour savoir si on rentre ou on sort d'une intersection
    private final int VITESSE_SIMULATION_MS = 1000;

    public VehiculeController(Vehicule vehicule, Terrain terrain, TerrainController terrainController) {
        this.vehicule = vehicule;
        this.terrain = terrain;
        this.terrainController = terrainController;
        this.vehiculeShape = creerVehiculeShape(vehicule.getType());
    }

    @Override
    public void run() {
        List<Vector2D> itineraire = vehicule.getItineraire();
        //mettreAJourGraphique();
        // on démarre à 1, car 0 est la position de départ (actuelle)
        for (int i = 1; i < itineraire.size(); i++) {
            anciennePosition = vehicule.getPosition().copy();
            nouvellePosition = itineraire.get(i);
/*
            // vérification de l'occupation de la cellule
            if (terrain.getCellule(nouvellePosition).estOccupee()) {
                System.out.println("Cellule occupée");

                // attente libération
                while (terrain.getCellule(nouvellePosition).estOccupee()) {
                    System.out.println("Attente libération cellule");
                    pauseEntreMouvements(VITESSE_SIMULATION_MS);
                }
            }
*/
            //pauseEntreMouvements(VITESSE_SIMULATION_MS);
            deplacement();
            //mettre l'index à jour dans le cas du déplacement dans l'intersection
            i = itineraire.indexOf(nouvellePosition);
        }

        //libérer la dernière cellule occupée
        Cellule cell = terrain.getCellule(nouvellePosition);
        cell.setOccupee(false);
        cell.setIdVoiture(0);

        System.out.println("Le véhicule " + vehicule.getId() + " est arrivé à destination !");

        Platform.runLater(() -> {
            terrainController.effacerVehicule(nouvellePosition, vehiculeShape);
            System.out.println("Véhicule " + vehicule.getId() + " effacé");
            terrainController.getSimulation().supprimerVehicule(vehicule, this);
            System.out.println("Véhicule " + vehicule.getId() + " supprimé");
        });
    }


    public void deplacement() {
        if (estDansCommunication(anciennePosition) && entreeIntersection) {
            System.out.println("je suis dans une zone de communication");
            entrerIntersection();
            entreeIntersection = false; //je suis sortie de l'intersection
        } else {
            System.out.println("je suis en zone normale");
            deplacerHorsIntersection();
            //mettre à jour l'attribut "entree" pour savoir si on arrive de nouveau dans une intersection ou pas
            if (estDansCommunication(nouvellePosition)) {
                System.out.println("je m'apprête à entrer dans une nouvelle intersection");
                entreeIntersection = true;
            }
        }
    }

    private void deplacerHorsIntersection() {
        System.out.println("Déplacement hors intersection");

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

        System.out.println("Le véhicule " + vehicule.getId() + " se déplace vers : " + vehicule.getPosition());
        mettreAJourCellules();
        mettreAJourGraphique();
        pauseEntreMouvements(VITESSE_SIMULATION_MS);
    }

    private void entrerIntersection() {
        Intersection intersection = terrain.getIntersection(anciennePosition);

        System.out.println("Entrée dans une intersection");
        ArrayList<Vector2D> deplacements = gestionIntersection();
        System.out.println("Itinéraire dans l'intersection : " + deplacements);

        Message message = new Message();
        //Rajouter un nv constructeur
        message.setObjet(Objetmessage.INFORMATION);
        message.setv1(vehicule);
        message.setItineraire(deplacements);

        intersection.ajouterVehicule(vehicule, message); //l'ajouter a la config
        /*System.out.println("[ID VOITURE ACTUELLE : " + vehicule.getId() + "] j'affiche la config de l'intersection :");
        intersection.afficherConfig();
*/
        ArrayList<Vehicule> vehiculesEngages = intersection.getVehiculesEngages(); //les véhicules qui ne sont pas engagés
        System.out.println("vehiculesEngages  = " + vehiculesEngages);

        ArrayList<Vehicule> vehiculesDansIntersection = intersection.getVehicules();
        System.out.println("vehiculesDansIntersection  = " + vehiculesDansIntersection);


        if (vehiculesDansIntersection.size() == 1) {
            //send message "Engagée" ????
            intersection.editConfig(vehicule, EtatVehicule.ENGAGE);
            System.out.println("aucun vehicule dans l'intersection donc j'avance'");
            avancerIntersection(deplacements);
            //quand on arrive a la fin (la sortie de la zone) on envoie un message de sortie et on supprime l'objet vehicule de la config de l'intersection
            //si on envoie un msg de sortie à l'intersection, ça sera un signal pour supprimer la voiture de sa config et ne pas faire l'action içi
            intersection.supprimerVehicule(vehicule);
            System.out.println("je suis sorti de l'intersection et j'ai supp le vehicule de la config");
        } else //entrer dans le mode négociation, calculs et gestion des priorités
        {
            System.out.println("des véhicule sont dans l'intersection");
            //récupérer les infos (itinéraires) des autres
            ArrayList<Message> messagesVoitures = new ArrayList<>();
            for (Vehicule v : vehiculesDansIntersection) {
                messagesVoitures.add(intersection.getMessage(v));
            }

            //set up les listeners
            //liaisonListeners(vehiculesEnConflit);

            System.out.println("je calcule mon temps d'attente");
            int tempsAttente = calculs(messagesVoitures, deplacements, vehiculesEngages);
/*
                //Créer un nouveau message et l'envoyer aux véhicules en conflit
                Message messageConfig = new Message();
                messageConfig.setv1(vehicule);
                messageConfig.setv2(vehiculesEnConflit);
                messageConfig.setObjet(Objetmessage.CONFIG);
                messageConfig.setConfiguration(configProposee);

                vehicule.sendMessage(messageConfig);
*/
            System.out.println("temps d'attente calculé et estimé à : " + tempsAttente + " secondes");
            pauseEntreMouvements(tempsAttente * VITESSE_SIMULATION_MS);
            System.out.println("attente effectuée, conflit évité, update l'état à ENGAGE puis j'avance");

            intersection.editConfig(vehicule, EtatVehicule.ENGAGE);
            //Envoyer un message avant de s'engager ??

            avancerIntersection(deplacements);
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
    public static boolean conflit(ArrayList<Message> messagesReçus, ArrayList<Vector2D> itineraire, ArrayList<Vehicule> vehiculesEnConflit) {
        // Vider la liste des véhicules en conflit pour un nouveau calcul
        vehiculesEnConflit.clear();

        // Récupérer un tableau des itinéraires depuis les messages
        for (Message message : messagesReçus) {
            ArrayList<Vector2D> itineraireAutreVehicule = message.getItineraire();

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

    public int calculs(ArrayList<Message> messagesReçus, ArrayList<Vector2D> itineraire, ArrayList<Vehicule> vehiculesEngages)
    {
        ArrayList<Vehicule> vehiculesenconflit  = new ArrayList<>() ;
        int tempsAttente = 0;
        if (conflit(messagesReçus, itineraire, vehiculesenconflit)) {
            for (Vehicule v : vehiculesenconflit) {
                if (vehiculesEngages.contains(v) && (v != vehicule)) {
                    tempsAttente++;
                }
            }
        }
        return tempsAttente; // Retourner le temps d'attente
    }


    public void liaisonListeners(ArrayList<Vehicule> destinataires) {
        for (Vehicule v : destinataires) {
            vehicule.addListener(v);
        }
    }

    private boolean estDansCommunication(Vector2D position) {
        return terrain.getCellule(position).getTypeZone() == TypeZone.COMMUNICATION;
    }

    /**
     * Retourne une liste des positions des cellules voisines accessibles à partir d'une position donnée,
     * en fonction des directions autorisées et en évitant le retour en arrière.
     *
     * @param positionActuelle La position actuelle du véhicule représentée par un objet `Vector2D`.
     * @return Une liste de positions (objets `Vector2D`) des cellules accessibles à partir de la position actuelle.
     */
    public List<Vector2D> getCellulesAutour(Vector2D positionActuelle) {
        List<Vector2D> cellulesPotentielles = new ArrayList<>();

        Cellule celluleActuelle = terrain.getGrille()[positionActuelle.getX()][positionActuelle.getY()];
        boolean[] directionsAutorisees = celluleActuelle.getDirectionsAutorisees();

        // Remplir les cellules potentielles en fonction des directions autorisées
        if (directionsAutorisees[0] && positionActuelle.getY() - 1 >= 0) {
            cellulesPotentielles.add(new Vector2D(positionActuelle.getX(), positionActuelle.getY() - 1));
        }
        if (directionsAutorisees[1] && positionActuelle.getX() + 1 < terrain.getLargeur()) {
            cellulesPotentielles.add(new Vector2D(positionActuelle.getX() + 1, positionActuelle.getY()));
        }
        if (directionsAutorisees[2] && positionActuelle.getY() + 1 < terrain.getHauteur()) {
            cellulesPotentielles.add(new Vector2D(positionActuelle.getX(), positionActuelle.getY() + 1));
        }
        if (directionsAutorisees[3] && positionActuelle.getX() - 1 >= 0) {
            cellulesPotentielles.add(new Vector2D(positionActuelle.getX() - 1, positionActuelle.getY()));
        }

        return cellulesPotentielles;
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

    private void avancerIntersection(List<Vector2D> deplacements) {
        for (Vector2D pos : deplacements) {
            anciennePosition = vehicule.getPosition().copy();
            vehicule.move(pos);
            // Afficher les informations de déplacement
            System.out.println("Le véhicule " + vehicule.getId() + " se déplace vers : " + vehicule.getPosition());
            mettreAJourCellules();
            mettreAJourGraphique();
            pauseEntreMouvements(VITESSE_SIMULATION_MS);
        }
    }

    public synchronized void mettreAJourCellules() {
        nouvellePosition = vehicule.getPosition().copy();
        Cellule cell2 = terrain.getCellule(nouvellePosition);
        Cellule cell1 = terrain.getCellule(anciennePosition);

        cell1.setOccupee(false);
        cell1.setIdVoiture(0);
        cell2.setOccupee(true);
        cell2.setIdVoiture(vehicule.getId());
    }

    private void mettreAJourGraphique() {
        Platform.runLater(() -> terrainController.updateCellule(anciennePosition, nouvellePosition, vehiculeShape));
    }

    private void pauseEntreMouvements(int millisecondes) {
        try {
            Thread.sleep(millisecondes);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Shape creerVehiculeShape(TypeVehicule typeVehicule) {
        List<Color> listeCouleurs = Arrays.asList(Color.HOTPINK, Color.DEEPPINK, Color.ORANGE, Color.LIME, Color.MAGENTA, Color.CYAN, Color.PURPLE, Color.GOLD);
        int couleurRandom = new Random().nextInt(listeCouleurs.size());

        switch (typeVehicule) {
            case VOITURE -> {
                return new Circle(5, listeCouleurs.get(couleurRandom));
            }
/*            case URGENCE -> {
                return new Rectangle(10, 10, Color.BLUE); // voir plus tard pour alterner rouge / bleu
            }
            case BUS -> {
                return new Rectangle(10, 10, Color.BLUE); // voir plus tard
            }*/
            default -> {
                return new Circle(5, listeCouleurs.get(couleurRandom));
            }
        }
    }

}