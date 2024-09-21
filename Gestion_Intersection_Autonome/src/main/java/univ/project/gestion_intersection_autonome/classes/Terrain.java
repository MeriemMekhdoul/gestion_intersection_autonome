package univ.project.gestion_intersection_autonome.classes;

import java.util.*;

import static com.almasb.fxgl.core.math.FXGLMath.random;


public class Terrain {

    private Cellule[][] grille;
    private List<Vector2D> sorties;
    private List<Vector2D> entrees;
    private int largeur;
    private int hauteur;

    // Constructeur par défaut
    public Terrain() {
        entrees = new ArrayList<>();
        sorties = new ArrayList<>();
    }
    public Terrain(int _largeur, int _hauteur){
        this.largeur = _largeur;
        this.hauteur = _hauteur;

        this.grille = new Cellule[largeur][hauteur];

        entrees = new ArrayList<>();
        sorties = new ArrayList<>();

        initialiserGrilleVide();
        genererGrille(8 ,20);

        afficherGrille();
    }

    public List<Vector2D> getEntrees() { return entrees;}
    public List<Vector2D> getSorties() { return sorties;}

    public Cellule[][] getGrille() { return grille; }

    public int getLargeur() {
        return largeur;
    }
    public int getHauteur() {
        return hauteur;
    }


    // Méthode pour initialiser la grille avec des espaces vides
    private void initialiserGrilleVide() {
        for (int i = 0; i < hauteur; i++) {
            for (int j = 0; j < largeur; j++) {
                grille[i][j] = new Cellule();
            }
        }
    }

    private void genererLigne(int x) {  // Remplir une route horizontale dans la grille
        for (int i = 0; i < largeur; i++) {
            grille[x][i].estValide(true);
            grille[x + 1][i].estValide(true);
            grille[x][i].setTypeZone(TypeZone.ROUTE);
            grille[x + 1][i].setTypeZone(TypeZone.ROUTE);
        }
        entrees.add(new Vector2D(x + 1,0));
        entrees.add(new Vector2D(x,largeur - 1));
        sorties.add(new Vector2D(x,0));
        sorties.add(new Vector2D(x + 1,largeur - 1));
    }

    private void genererColonne(int y) {  // Remplir une route verticale dans la grille
        for (int i = 0; i < hauteur; i++) {
            // Gérer les intersections
            if (grille[i][y].estValide()) {
                    grille[i][y].setTypeZone(TypeZone.CONFLIT);
                    grille[i][y + 1].setTypeZone(TypeZone.CONFLIT);
                    grille[i + 1][y].setTypeZone(TypeZone.CONFLIT);
                    grille[i + 1][y + 1].setTypeZone(TypeZone.CONFLIT);

                    grille[i][y - 1].setTypeZone(TypeZone.COMMUNICATION);
                    grille[i + 1][y - 1].setTypeZone(TypeZone.COMMUNICATION);
                    grille[i - 1][y].setTypeZone(TypeZone.COMMUNICATION);
                    grille[i - 1][y + 1].setTypeZone(TypeZone.COMMUNICATION);

                    grille[i][y + 2].setTypeZone(TypeZone.COMMUNICATION);
                    grille[i + 1][y + 2].setTypeZone(TypeZone.COMMUNICATION);
                    grille[i + 2][y].setTypeZone(TypeZone.COMMUNICATION);
                    grille[i + 2][y + 1].setTypeZone(TypeZone.COMMUNICATION);

                i++;  // Sauter la ligne d'après
            } else {
                grille[i][y].estValide(true);
                grille[i][y + 1].estValide(true);

                if(grille[i][y].getTypeZone()==null){
                    grille[i][y].setTypeZone(TypeZone.ROUTE);
                    grille[i][y + 1].setTypeZone(TypeZone.ROUTE);
                }
            }
        }

        entrees.add(new Vector2D(0,y));
        entrees.add(new Vector2D(hauteur - 1,y + 1));
        sorties.add(new Vector2D(0,y + 1));
        sorties.add(new Vector2D(hauteur - 1, y));
    }

    private void genererGrille(int espace_min, int espace_max){
        Random random = new Random();
        int x_pos = random.nextInt(4,hauteur/4);
        int routeGeneree = 0;
        while((x_pos + 5 < hauteur) && (routeGeneree<4)){
            genererLigne(x_pos);
            routeGeneree++;
            x_pos += random.nextInt(espace_min,espace_max);
        }

        routeGeneree = 0;
        int y_pos = random.nextInt(4,largeur/4);
        while((y_pos + 5 < largeur) && (routeGeneree<4)){
            genererColonne(y_pos);
            routeGeneree++;
            y_pos += random.nextInt(espace_min,espace_max);
        }
    }

    //inutile mais juste pour debug
    public void afficherGrille() {
        for (int i = 0; i < hauteur; i++) {
            for (int j = 0; j < largeur; j++) {
                if (!grille[i][j].estValide()) {
                    System.out.print(". ");  // Afficher un point pour une cellule non valide
                } else {
                    // Afficher selon le type de zone
                    switch (grille[i][j].getTypeZone()) {
                        case COMMUNICATION:
                            System.out.print("M ");  // Communication
                            break;
                        case CONFLIT:
                            System.out.print("C ");  // Conflit
                            break;
                        case ROUTE:
                            System.out.print("R ");  // Route
                            break;
                        default:
                            System.out.print("? ");  // Pour gérer un cas inconnu
                            break;
                    }
                }
            }
            System.out.println();  // Passer à la ligne suivante après chaque ligne de la grille
        }
    }

}
