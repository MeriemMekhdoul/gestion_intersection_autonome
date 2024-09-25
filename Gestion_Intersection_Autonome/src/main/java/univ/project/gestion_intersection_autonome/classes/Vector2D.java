package univ.project.gestion_intersection_autonome.classes;

import java.util.Objects;

public class Vector2D
{
    private int x;
    private int y;

    // Constructeur par défaut
    public Vector2D() {
        this.x = 1;
        this.y = 1;
    }

    // Constructeur paramétré
    public Vector2D(int x, int y)
    {
        if (x < 0 || y < 0) {
            throw new IllegalArgumentException("Les valeurs de X et Y ne peuvent pas être négatives !");
        }
        this.x = x;
        this.y = y;
    }

    // Getters et setters
    public int getX() {
        return x;
    }

    public void setX(int x)
    {
        if (x < 0) {
            throw new IllegalArgumentException("La valeur de X ne peut pas être négative !");
        }
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y)
    {
        if (y < 0) {
            throw new IllegalArgumentException("La valeur de Y ne peut pas être négative !");
        }
        this.y = y;
    }

    // permet d'obtenir une copie de l'objet sachant que les objets en java sont passés par référence
    public Vector2D copy() {
        return new Vector2D(this.x, this.y);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    @Override
    public boolean equals (Object object) {
        if (!(object instanceof Vector2D other)) return false; // vérifie si on compare bien deux vectors
        return this.x == other.x && this.y == other.y;
    }

    // permet de vérifier le hash de l'objet, sinon erreur lors de la vérification des positions dans la map de vectors2D
    @Override
    public int hashCode () {
        return Objects.hash(x, y);
    }

}
