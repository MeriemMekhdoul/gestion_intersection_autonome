package univ.project.gestion_intersection_autonome.classes;

import java.util.*;

public class Cellule {

    public enum TypeZone {
        CONFLIT,
        COMMUNICATION
    }

    private int idVoiture;

    private TypeZone typeZone ;

    private boolean valide ;

    private boolean occupee ;

    //constructeur
    public Cellule {

        this.idVoiture= idVoiture;

        typeZone=null;

        valide=false;

        occupee=false;

    }


    //set idVoiture
    public void getIdVoiture (){};
    //get idVoiture
    public int getIdVoiture (){return idVoiture};





}
