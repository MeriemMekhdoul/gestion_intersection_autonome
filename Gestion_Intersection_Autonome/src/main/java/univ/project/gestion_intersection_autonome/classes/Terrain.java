package univ.project.gestion_intersection_autonome.classes;

import java.util.*;

import static com.almasb.fxgl.core.math.FXGLMath.random;


public class Terrain {

    private List<int[]> entrees;  // Liste pour les entrées (positions des cellules)
    private List<Vector2D> sorties;
    private List<Vector2D> _entrees;

    private Cellule[][] grille;
    private int largeur;
    private int hauteur;

    // Constructeur par défaut
    public Terrain() {
        _entrees = new ArrayList<>();
        sorties = new ArrayList<>();
    }

    public Terrain(int _largeur, int _hauteur){
        this.largeur = _largeur;
        this.hauteur = _hauteur;

        this.grille = new Cellule[largeur][hauteur];

        entrees = new ArrayList<>();
        _entrees = new ArrayList<>();
        sorties = new ArrayList<>();

        initialiserGrilleVide();
        genererGrille(8 ,20);

        afficherGrille();
    }

    public List<int[]> getEntrees() { return entrees;}
    public List<Vector2D> get_entrees() { return _entrees;}
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

        // Vérifier si les positions sont valides avant d'ajouter les entrées
        //if (x + 1 < hauteur) {
            entrees.add(new int[]{x + 1, 0});
            _entrees.add(new Vector2D(x + 1,0));
        entrees.add(new int[]{x, largeur - 1});
        _entrees.add(new Vector2D(x,largeur - 1));
        sorties.add(new Vector2D(x,0));
        sorties.add(new Vector2D(x + 1,largeur - 1));
        //}
    }

    private void genererColonne(int y) {  // Remplir une route verticale dans la grille
        for (int i = 0; i < hauteur; i++) {
            // Gérer les intersections
            if (grille[i][y].estValide()) {
                //if (i + 1 < hauteur && y + 1 < largeur) {
                    grille[i][y].setTypeZone(TypeZone.CONFLIT);
                    grille[i][y + 1].setTypeZone(TypeZone.CONFLIT);
                    grille[i + 1][y].setTypeZone(TypeZone.CONFLIT);
                    grille[i + 1][y + 1].setTypeZone(TypeZone.CONFLIT);

                    // Vérifier les indices avant de définir les zones de communication
                    //if (y - 1 >= 0 && i - 1 >= 0) {
                        grille[i][y - 1].setTypeZone(TypeZone.COMMUNICATION);
                        grille[i + 1][y - 1].setTypeZone(TypeZone.COMMUNICATION);
                        grille[i - 1][y].setTypeZone(TypeZone.COMMUNICATION);
                        grille[i - 1][y + 1].setTypeZone(TypeZone.COMMUNICATION);
                    //}
                    //if (i + 2 < hauteur && y + 2 < largeur) {
                        grille[i][y + 2].setTypeZone(TypeZone.COMMUNICATION);
                        grille[i + 1][y + 2].setTypeZone(TypeZone.COMMUNICATION);
                        grille[i + 2][y].setTypeZone(TypeZone.COMMUNICATION);
                        grille[i + 2][y + 1].setTypeZone(TypeZone.COMMUNICATION);
                    //}
                //}
                i++;  // Sauter la ligne d'après
            } else {
                grille[i][y].estValide(true);
                if (y + 1 < largeur) {
                    grille[i][y + 1].estValide(true);
                }
                if(grille[i][y].getTypeZone()==null){
                    grille[i][y].setTypeZone(TypeZone.ROUTE);
                    grille[i][y + 1].setTypeZone(TypeZone.ROUTE);
                }
            }
        }

        // Ajouter des entrées si les indices sont valides
        if (y + 1 < largeur) {
            entrees.add(new int[]{0, y});
            entrees.add(new int[]{hauteur - 1, y + 1});
        }
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
