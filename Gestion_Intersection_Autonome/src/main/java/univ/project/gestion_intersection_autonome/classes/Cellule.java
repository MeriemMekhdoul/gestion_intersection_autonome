package univ.project.gestion_intersection_autonome.classes;

import java.util.*;

public class Cellule {
    private int idVoiture;

    private TypeZone typeZone ;

    private boolean valide ;

    private boolean occupee ;

    // Constructeur par défaut
    public Cellule() {
        this.idVoiture = 0 ;
        this.typeZone = null;
        this.valide = false;
        this.occupee = false;
    }

    // Constructeur avec paramètres
    public Cellule(int idVoiture, TypeZone typeZone, boolean valide, boolean occupee) {
        this.idVoiture = idVoiture;
        this.typeZone = typeZone;
        this.valide = valide;
        this.occupee = occupee;
    }

    // Getters et Setters

    public int getIdVoiture() {
        return idVoiture;
    }

    public void setIdVoiture(int idVoiture) {
        this.idVoiture = idVoiture;
    }

    public TypeZone getTypeZone() {
        return typeZone;
    }

    public void setTypeZone(TypeZone typeZone) {
        this.typeZone = typeZone;
    }

    public boolean estValide() {
        return valide;
    }

    public void estValide(boolean valide) {
        this.valide = valide;
    }

    public boolean estOccupee() {
        return occupee;
    }

    public void setOccupee(boolean occupee) {
        this.occupee = occupee;
    }
}






