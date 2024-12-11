package univ.project.gestion_intersection_autonome.classes.Terrain;

import java.util.Objects;

/**
 * Classe représentant un vecteur en deux dimensions (2D).
 *
 * Cette classe est utilisée pour représenter les positions et effectuer
 * des calculs tels que la distance entre deux points.
 */
public class Vector2D {

    /** Coordonnée X du vecteur. */
    private int x;

    /** Coordonnée Y du vecteur. */
    private int y;

    /**
     * Constructeur par défaut qui initialise le vecteur à (1, 1).
     */
    public Vector2D() {
        this.x = 1;
        this.y = 1;
    }

    /**
     * Constructeur paramétré pour initialiser les coordonnées du vecteur.
     *
     * @param x la coordonnée X (doit être positive).
     * @param y la coordonnée Y (doit être positive).
     * @throws IllegalArgumentException si les valeurs de X ou Y sont négatives.
     */
    public Vector2D(int x, int y) {
        if (x < 0 || y < 0) {
            throw new IllegalArgumentException("Les valeurs de X et Y ne peuvent pas être négatives !");
        }
        this.x = x;
        this.y = y;
    }

    /**
     * Retourne la coordonnée X.
     *
     * @return la coordonnée X.
     */
    public int getX() {
        return x;
    }

    /**
     * Définit la coordonnée X.
     *
     * @param x la nouvelle valeur pour X (doit être positive).
     * @throws IllegalArgumentException si la valeur de X est négative.
     */
    public void setX(int x) {
        if (x < 0) {
            throw new IllegalArgumentException("La valeur de X ne peut pas être négative !");
        }
        this.x = x;
    }

    /**
     * Retourne la coordonnée Y.
     *
     * @return la coordonnée Y.
     */
    public int getY() {
        return y;
    }

    /**
     * Définit la coordonnée Y.
     *
     * @param y la nouvelle valeur pour Y (doit être positive).
     * @throws IllegalArgumentException si la valeur de Y est négative.
     */
    public void setY(int y) {
        if (y < 0) {
            throw new IllegalArgumentException("La valeur de Y ne peut pas être négative !");
        }
        this.y = y;
    }

    /**
     * Crée une copie de l'objet Vector2D.
     *
     * @return une nouvelle instance de Vector2D avec les mêmes coordonnées.
     */
    public Vector2D copy() {
        return new Vector2D(this.x, this.y);
    }

    /**
     * Retourne une représentation textuelle du vecteur.
     *
     * @return une chaîne de caractères au format "(x, y)".
     */
    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    /**
     * Vérifie si un autre objet est égal à ce vecteur.
     *
     * @param object l'objet à comparer.
     * @return {@code true} si l'objet est un Vector2D avec les mêmes coordonnées, sinon {@code false}.
     */
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Vector2D other)) return false;
        return this.x == other.x && this.y == other.y;
    }

    /**
     * Retourne le code de hachage du vecteur, basé sur ses coordonnées.
     *
     * @return le code de hachage.
     */
    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    /**
     * Calcule la distance de Manhattan entre ce vecteur et un autre.
     *
     * @param autrePosition le vecteur cible.
     * @return la distance de Manhattan entre les deux vecteurs.
     */
    public double distance(Vector2D autrePosition) {
        return Math.abs(this.x - autrePosition.getX()) + Math.abs(this.y - autrePosition.getY());
    }
}
