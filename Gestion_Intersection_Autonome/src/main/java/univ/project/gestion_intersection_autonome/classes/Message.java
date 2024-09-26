package univ.project.gestion_intersection_autonome.classes;

import java.util.ArrayList;


public class Message {

    private int t ;

    private Vehicule v1 ;

    private ArrayList<Vehicule>v2 ;

    private Objetmessage objet ;

    private ArrayList<Vector2D> itineraire ;

    //constructeur par défaut

    public Message(){

        this.t = 0 ;

        this.v1 = null ;

        this.v2 = new ArrayList<>() ;

        this.objet = null;

        this.itineraire = new ArrayList<>() ; }
    //constructeur parametre
    public Message(int t, Vehicule v1, ArrayList<Vehicule>v2, Objetmessage objet, ArrayList<Vector2D> itineraire ){

        this.t=t;

        this.v1=v1;

        this.v2=v2;

        this.objet=objet;

        this.itineraire=itineraire;

    }
    //setters et getters
    public void setT(int t){this.t=t;}
    public int getT(){return t;}

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









}

