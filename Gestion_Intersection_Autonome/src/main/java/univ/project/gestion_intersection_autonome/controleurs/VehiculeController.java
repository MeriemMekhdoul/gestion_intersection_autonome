package univ.project.gestion_intersection_autonome.controleurs;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import univ.project.gestion_intersection_autonome.classes.*;
import univ.project.gestion_intersection_autonome.classes.Agents.Intersection;
import univ.project.gestion_intersection_autonome.classes.Agents.Vehicule;
import univ.project.gestion_intersection_autonome.classes.Enums.EtatVehicule;
import univ.project.gestion_intersection_autonome.classes.Enums.Objetmessage;
import univ.project.gestion_intersection_autonome.classes.Enums.TypeVehicule;
import univ.project.gestion_intersection_autonome.classes.Enums.TypeZone;
import univ.project.gestion_intersection_autonome.classes.Terrain.Cellule;
import univ.project.gestion_intersection_autonome.classes.Terrain.Terrain;
import univ.project.gestion_intersection_autonome.classes.Terrain.Vector2D;

import java.util.*;

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
    public static final int VITESSE_SIMULATION_MS = 300;
    protected boolean togglePoliceImage = false;

    public static final Map<Image, Color> imageToColorMap = new HashMap<>();

    static {
        try {
            imageToColorMap.put(new Image(VehiculeController.class.getResource("/images/cars/voiture_rouge.png").toExternalForm()), Color.CRIMSON);
            imageToColorMap.put(new Image(VehiculeController.class.getResource("/images/cars/voiture_rose.png").toExternalForm()), Color.DEEPPINK);
            imageToColorMap.put(new Image(VehiculeController.class.getResource("/images/cars/voiture_violette.png").toExternalForm()), Color.MEDIUMPURPLE);
            imageToColorMap.put(new Image(VehiculeController.class.getResource("/images/cars/voiture_jaune.png").toExternalForm()), Color.GOLD);
            imageToColorMap.put(new Image(VehiculeController.class.getResource("/images/cars/voiture_verte.png").toExternalForm()), Color.MEDIUMSEAGREEN);
            imageToColorMap.put(new Image(VehiculeController.class.getResource("/images/cars/voiture_bleue.png").toExternalForm()), Color.LIGHTSEAGREEN);
            imageToColorMap.put(new Image(VehiculeController.class.getResource("/images/cars/voiture_orange.png").toExternalForm()), Color.ORANGE);
            imageToColorMap.put(new Image(VehiculeController.class.getResource("/images/cars/police1.png").toExternalForm()), Color.WHITE);
            imageToColorMap.put(new Image(VehiculeController.class.getResource("/images/cars/police2.png").toExternalForm()), Color.WHITE);
        }
        catch (Exception e) {
            System.err.println("Erreur lors du chargement des images : " + e.getMessage());
        }
    }

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

        ArrayList<Vector2D> deplacements = getItinIntersection();

        Message message = new Message();
        message.setObjet(Objetmessage.INFORMATION);
        message.setv1(vehicule);
        message.setItineraire(deplacements);

        intersection.ajouterVehicule(vehicule, message); // L'ajouter à la configuration (état = ATTENTE)

        ArrayList<Vehicule> vehiculesEngages = intersection.getVehiculesEngages(); // Les véhicules engagés

        //get tous les vehicules avant moi
        List<Vehicule> vehiculesDansIntersection = intersection.getVehicules(vehicule);

        if (vehiculesDansIntersection.size() == 1) { // le véhicule actuel est présent uniquement
            intersection.editConfig(vehicule, EtatVehicule.ENGAGE);
            intersection.ajouterTempsAttente(vehicule.getId(),0);
            // Dessiner l'itinéraire sur la grille
            Platform.runLater(() -> terrainController.dessinerItineraire(deplacements, vehicule));
            avancerIntersection(deplacements);
        }
        else {
            // Récupérer les infos (itinéraires) des autres véhicules
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
                } else {
                    if(m.getv1().getId() != vehicule.getId())
                        vehiculesAttenteEtItineraires.put(m.getv1(), m.getItineraire());
                }
            }

            int tempsAttente = vehicule.calculTempsAttenteVehiculesEngages(vehiculesEngagesEtItineraires,deplacements);

            //s'il reste des véhicules en attente à prendre en compte
            if (!vehiculesAttenteEtItineraires.isEmpty()){
                //get TA des véhicules en attente
                for (Vehicule v: vehiculesAttenteEtItineraires.keySet()) {
                    //get temps attente
                    int ta = intersection.getTempsAttente(v.getId());
                    while(ta == -1){ //attendre que le véhiciule en attente devant nous finisse son calcul
                        pauseEntreMouvements(VITESSE_SIMULATION_MS);
                        ta = intersection.getTempsAttente(v.getId());
                    }

                    if(ta>0){
                        //rallonger l'ancien itinéraire du véhicule en attente après son temps d'attente
                        ArrayList<Vector2D> nouvelItin = v.rallongerItineraire(vehiculesAttenteEtItineraires.get(v),ta,vehicule.getPosition());

                        //modifier l'itinéraire dans la map vehiculesEtItineraires
                        vehiculesAttenteEtItineraires.put(v,nouvelItin);
                    } //sinon laisser tel quel
                }
                //check conflit avec ces vehicules et update temps d'attente
                tempsAttente += vehicule.calculTempsAttenteVehiculesAttente(vehiculesAttenteEtItineraires,tempsAttente,deplacements);
            }

            //s'il n'y en a pas ou après avoir fait les calculs
            intersection.ajouterTempsAttente(vehicule.getId(),tempsAttente);
            pauseEntreMouvements(tempsAttente * VITESSE_SIMULATION_MS);

            intersection.editConfig(vehicule, EtatVehicule.ENGAGE);
            // Dessiner l'itinéraire sur la grille
            Platform.runLater(() -> terrainController.dessinerItineraire(deplacements, vehicule));
            avancerIntersection(deplacements);

            Platform.runLater(() -> terrainController.effacerItineraire(vehicule, anciennePosition));

        }

        intersection.supprimerVehicule(vehicule);
        intersection.removeVehiculeControllerListener(this);
    }

    protected boolean estDansCommunication(Vector2D position) {
        return terrain.getCellule(position).getTypeZone() == TypeZone.COMMUNICATION;
    }

    /**
     * Génère l'itinéraire du véhicule au sein de l'intersection à partir de sa position actuelle.
     *
     * @return Une liste de positions représentant l'itinéraire dans l'intersection.
     */
    public ArrayList<Vector2D> getItinIntersection() {
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
                //while (terrain.getCellule(pos).estOccupee()) {
                    pauseEntreMouvements(VITESSE_SIMULATION_MS);
                //}
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
    protected void mettreAJourGraphique()
    {
        Platform.runLater(() -> {
            if (vehicule.getType() == TypeVehicule.URGENCE) {
                // Alterner les images pour l'animation du gyrophare
                Image nextImage = togglePoliceImage ? getImageByPath("/images/cars/police1.png") : getImageByPath("/images/cars/police2.png");
                vehiculeShape.setFill(new ImagePattern(nextImage));
                togglePoliceImage = !togglePoliceImage;
            }

            // Animer le déplacement
            terrainController.animerDeplacementVehicule(vehiculeShape, anciennePosition, nouvellePosition, VITESSE_SIMULATION_MS);
        });
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
        Shape shape = new Rectangle(terrainController.TAILLE_CELLULE, terrainController.TAILLE_CELLULE);
        Image assignedImage = null;

        if (typeVehicule == TypeVehicule.URGENCE) {
            assignedImage = togglePoliceImage
                    ? getImageByPath("/images/cars/police1.png")
                    : getImageByPath("/images/cars/police2.png");
            togglePoliceImage = !togglePoliceImage;
        } else {
            List<Image> regularImages = new ArrayList<>(imageToColorMap.keySet());
            regularImages.removeIf(img -> imageToColorMap.get(img).equals(Color.WHITE)); // Exclure les images de police

            Random random = new Random();
            assignedImage = regularImages.get(random.nextInt(regularImages.size()));
        }

        if (assignedImage == null) {
            System.err.println("Aucune image assignée au véhicule de type : " + typeVehicule);
            return shape; // Retourne une forme par défaut ou gérez comme vous le souhaitez
        }

        shape.setFill(new ImagePattern(assignedImage));
        vehicule.setCouleur(imageToColorMap.get(assignedImage));

        // Positionnement initial du véhicule
        double initialX = vehicule.getPosition().getX() * terrainController.TAILLE_CELLULE;
        double initialY = vehicule.getPosition().getY() * terrainController.TAILLE_CELLULE;
        shape.setTranslateX(initialX);
        shape.setTranslateY(initialY);

        Platform.runLater(() -> terrainController.vehiclePane.getChildren().add(shape));

        return shape;
    }


    /**
     * Méthode utilitaire pour récupérer une image par chemin.
     *
     * @param path Chemin relatif de l'image.
     * @return L'image correspondante ou null si non trouvée.
     */
    private Image getImageByPath(String path) {
        return imageToColorMap.keySet().stream().filter(img -> img.getUrl().contains(path)).findFirst().orElse(null);
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
            vehicule.setEnAttente(false);
            notify(); // Réveille un thread en attente
        }
    }

    /**
     * Met le véhicule en pause, interrompant son déplacement.
     */
    public void mettreEnPause() {
        synchronized (this) { // Synchronisation sur l'objet courant
            enPause = true;
            vehicule.setEnAttente(true);
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
