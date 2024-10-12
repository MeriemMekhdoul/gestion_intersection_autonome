package univ.project.gestion_intersection_autonome.classes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Objects;


public class Message {
    private Instant t ;
    private Vehicule v1 ;
    private ArrayList<Vehicule>v2 ;
    private Objetmessage objet ;
    private ArrayList<Vector2D> itineraire ;
    private Configuration configuration;

    //constructeur par défaut
    public Message(){
        this.t = Instant.now();
        this.v1 = null ;
        this.v2 = new ArrayList<>() ;
        this.objet = null;
        this.itineraire = new ArrayList<>() ;
    }

    //constructeur parametre
    public Message(Instant t, Vehicule v1, ArrayList<Vehicule>v2, Objetmessage objet, ArrayList<Vector2D> itineraire){
        this.t=t;
        this.v1=v1;
        this.v2=v2;
        this.objet=objet;
        this.itineraire=itineraire;
    }

    //setters et getters
    public void setT(Instant t){this.t=t;}
    public Instant getT(){return t;}

    public void setv1(Vehicule v1){this.v1=v1;}
    public Vehicule getv1() {
        return v1;
    }


    public void setv2(ArrayList<Vehicule>v2){this.v2=v2;}
    public ArrayList<Vehicule> getv2() {return v2;}

    public void setItineraire(ArrayList<Vector2D>itineraire){this.itineraire=itineraire;}
    public ArrayList<Vector2D> getItineraire() {
        return itineraire;
    }

    public void setObjet(Objetmessage objet){this.objet=objet;}
    public Objetmessage getobjet() {return objet;}

    public Configuration getConfiguration() {
        return configuration;
    }
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Le véhicule de type \"").append(v1.getType()).append("\" et id \"").append(v1.getId()).append("\" ");
        sb.append("envoie ce message : ").append(t).append(", objet : ").append(objet).append(", itinéraire : ").append(itineraire);
        return sb.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(t, v1, v2, objet, itineraire);
    }
}

