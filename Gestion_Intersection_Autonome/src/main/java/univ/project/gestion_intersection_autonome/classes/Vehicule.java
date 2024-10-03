package univ.project.gestion_intersection_autonome.classes;

import java.util.ArrayList;
import java.util.List;
import java.io.*;
import java.net.*;
public class Vehicule {
    // Données membres
    private  final int id; // sécurise en empêchant toute modification
    private TypeVehicule type;  //final non ?
    private Vector2D position;
    private Vector2D positionDepart;
    private Vector2D positionArrivee;
    private static int idCompteur = 1;// génère un ID pour chaque véhicule
    private boolean enMouvement;

    // Constructeur par défaut
    public Vehicule(TypeVehicule type, Vector2D positionDepart, Vector2D positionArrivee) {
        this.id = idCompteur++; // incrément automatique
        this.type = type; //TypeVehicule.VOITURE;
        this.position = positionDepart;
        this.positionDepart = positionDepart;
        this.positionArrivee = positionArrivee;

    }
    private Socket socket;
    private static PrintWriter out; // Flux de sortie pour envoyer des messages
    private static BufferedReader in;  // Flux d'entrée pour recevoir des messages

    // Constructeur paramétré
    public Vehicule(TypeVehicule type, Vector2D position, Vector2D positionDepart, Vector2D positionArrivee,Socket socket) throws IOException {
        this.id = idCompteur++;
        this.type = type;
        this.position = position;
        this.positionDepart = positionDepart;
        this.positionArrivee = positionArrivee;
        this.enMouvement = true;
        String host = "";
        int port = 0;
        this.socket = new Socket(host, port);

        // Initialize the input and output streams
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
    }


    // Méthodes
    public void move(Vector2D pos) {
        position.setX(pos.getX());
        position.setY(pos.getY());
    }

    // se déplacer automatiquement vers la destination
    //prendre en compte les positions possibles et se déplacer vers la plus optimale selon la destination du véhicule
    public Vector2D seDeplacerVersDestination(List<Vector2D> positionsPossibles) {
        // choisir la meilleure position à prendre pour atteindre la destination
        Vector2D posoptimale = choisirPositionOptimale(positionsPossibles);

        if (posoptimale.getX() < position.getX()) {
            return posoptimale;
        } else if (posoptimale.getX() > position.getX()) {
            return posoptimale;
        } else if (posoptimale.getY() < position.getY()) {
            return posoptimale;
        } else if (posoptimale.getY() > position.getY()) {
            return posoptimale;
        }
        return null; //revoir ici
    }

    //methode qui determine la position par laquelle le véhicule passe selon le chemin le plus court , prend un paramètre un tableau de Vector2D contenant les positions possibles
    public Vector2D choisirPositionOptimale(List<Vector2D> positionsPossibles) {
        //position optimale initialisée par le premier Vector2D du tableau
        // valeur par défaut
        Vector2D posoptimale = positionsPossibles.get(0);
        //calculer distance entre premiere position et la position de destination, celle-ci est stockée dans la variable distanceMin
        double distanceMin = DistanceVersDestination(posoptimale);

        // Parcourir toutes les positions possibles et choisir celle qui est la plus proche a la position de destination (la distance la plus courte)
        for (Vector2D position : positionsPossibles) {
            double distance = DistanceVersDestination(position);
            if (distance < distanceMin) {
                posoptimale = position;
                distanceMin = distance;
            }
        }
        return posoptimale;
    }

    // Algorithme pour calculer la distance vers la destination ; formule de la distance euclidienne.
    private double DistanceVersDestination(Vector2D position) {
        return Math.sqrt(Math.pow(positionArrivee.getX() - position.getX(), 2) +
                Math.pow(positionArrivee.getY() - position.getY(), 2));
    }

    public boolean estArrivee() {
        return position.equals(positionArrivee);
    }

    //utilisation de sockets pour l'envoi de messages d'un vehicule à un ou plusieurs autres vehicules
    //methode pour envoyer un message

    public static void sendMessage(Message message, Vehicule v1,ArrayList<Vehicule>v2) {
        StringBuilder messageStr = new StringBuilder();
        StringBuilder idr = new StringBuilder();
        messageStr.append(message.getT())
                .append(",")
                .append(message.getobjet())
                .append(",")
                .append(message.getItineraire());
            

        // Ajouter les IDs des récepteurs
        for (Vehicule v : message.getv2()) {
            idr.append(v.getId()).append(","); // Ajout de l'ID du récepteur

            
        }
        if (in == null) {
            System.err.println("Error: Input stream is not initialized for receiving messages.");
            return;
        }
        if (out != null) {
            out.println(messageStr); // Envoie le message via le flux de sortie du socket

            out.flush(); // Assure que le message est bien envoyé
        }
        System.out.println("Voiture " + v1.id +" a envoyé le message "+ messageStr + " à "+  idr);
    }
    //methode pour recevoir un message
    public static void receiveMessage(Message message,ArrayList<Vehicule>v2) {
        String receivedMessage;
        StringBuilder idr = new StringBuilder();
        for (Vehicule v : message.getv2()) {
            idr.append(v.getId()).append(","); // Ajout de l'ID du récepteur

        }

        try {
            while ((receivedMessage = in.readLine()) != null) {
                // Traitement du message reçu
                System.out.println("Véhicule " + idr + " a reçu: " + receivedMessage);

                //appeler la méthode qui traite le message
                processMessage(receivedMessage);
            }
        } catch (IOException e) {
            e.printStackTrace(); // si ya erreur , on imprime la trace de la pile d'exécution dans la console
        }
    }

    // Méthode pour traiter le message reçu
    private static void processMessage(String message) {
        // Décomposer le message
        String[] parties = message.split(",");
        //gerer le comportement apres la reception du message
    }
    // Getters et setters
    public int getId() {
        return id;
    }
    public int setId(int id) {
        return id;
    }
    public TypeVehicule getType() {
        return type;
    }

    public void setType(TypeVehicule type) {
        this.type = type;
    }

    public Vector2D getPosition() {
        return position;
    }

    public void setPosition(Vector2D position) {
        this.position = position;
    }

    public Vector2D getPositionDepart() {
        return positionDepart;
    }

    public void setPositionDepart(Vector2D positionDepart) {
        this.positionDepart = positionDepart;
    }

    public Vector2D getPositionArrivee() {
        return positionArrivee;
    }

    public void setPositionArrivee(Vector2D positionArrivee) {
        this.positionArrivee = positionArrivee;
    }

}
