package univ.project.gestion_intersection_autonome.classes;

public class Terrain {
    private char[][] grille;

    //private cellule[][] grille;
    private int largeur;
    private int hauteur;

    // Constructeur par défaut
    public Terrain() {
        this.largeur = 6;  // Largeur de la grille
        this.hauteur = 6;  // Hauteur de la grille
        //essai avec des caractères à la place des cellules
        grille = new char[][] {
                {'.', '.', 'R', 'R', '.', '.'},
                {'.', '.', 'R', 'R', '.', '.'},
                {'R', 'R', 'R', 'R', 'R', 'R'},
                {'R', 'R', 'R', 'R', 'R', 'R'},
                {'.', '.', 'R', 'R', '.', '.'},
                {'.', '.', 'R', 'R', '.', '.'}
        };
    }

    public Terrain(int _largeur, int _hauteur){
        this.largeur = _largeur;
        this.hauteur = _hauteur;
        this.grille = new char[largeur][hauteur];
    }

    public char[][] getGrille() {
        return grille;
    }

    public int getLargeur() {
        return largeur;
    }

    public int getHauteur() {
        return hauteur;
    }


}
