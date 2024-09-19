package univ.project.gestion_intersection_autonome.classes;

import java.util.*;


public class Terrain {
    //private char[][] grille;

    private List<int[]> entrees;  // Liste pour les entrées (positions des cellules)

    private Cellule[][] grille_c;
    private int largeur;
    private int hauteur;

    // Constructeur par défaut
    public Terrain() {
        this.largeur = 6;  // Largeur de la grille
        this.hauteur = 6;  // Hauteur de la grille
        //essai avec des caractères à la place des cellules
        /*grille = new char[][] {
                {'.', '.', 'R', 'R', '.', '.'},
                {'.', '.', 'R', 'R', '.', '.'},
                {'R', 'R', 'R', 'R', 'R', 'R'},
                {'R', 'R', 'R', 'R', 'R', 'R'},
                {'.', '.', 'R', 'R', '.', '.'},
                {'.', '.', 'R', 'R', '.', '.'}
        };*/
        entrees = new ArrayList<>();
    }

    public Terrain(int _largeur, int _hauteur){
        this.largeur = _largeur;
        this.hauteur = _hauteur;
        //this.grille = new char[largeur][hauteur];

        this.grille_c = new Cellule[largeur][hauteur];

        entrees = new ArrayList<>();

        initialiserGrilleVide();
        genererRoutes();

    }

    /*public char[][] getGrille() {
        return grille;
    }*/

    public Cellule[][] getGrille_c() { return grille_c; }

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
                //grille[i][j] = '.';  // '.' représente un espace vide
                grille_c[i][j] = new Cellule();
            }
        }
    }

    // Méthode pour générer aléatoirement des routes et intersections
    // NOTE : si on décide de garder un seul terrain statique cette méthode n'est plus utile
    private void genererRoutes() {
        Random random = new Random();

        // Générer le nombre de routes horizontales et verticales
        int nbr_rh = random.nextInt(4) + 1;  // Entre 1 et 4 routes horizontales
        int nbr_rv = random.nextInt(4) + 1;  // Entre 1 et 4 routes verticales


        // Générer les positions des routes horizontales
        Set<Integer> positionsHorizontales = new HashSet<>(); // Utiliser un set pour éviter les doublons
        while (positionsHorizontales.size() < nbr_rh) {
            int pos = random.nextInt(4,hauteur - 6);

            /*System.out.println("pos H = ");
            System.out.println(pos);*/

            //  Vérifier que l'écart entre deux routes est d'au moins 4 cases
            boolean estValide = true;
            for (int p : positionsHorizontales) {
                if (Math.abs(p - pos) < 6) {
                    estValide = false;
                    System.out.println("pos rejected");
                    break;
                }
            }

            // Si la position est valide, on l'ajoute
            if (estValide) {
                positionsHorizontales.add(pos);

                // Remplir la liste des entrées
                entrees.add(new int[] {pos+1,0});
                entrees.add(new int[] {pos, largeur-1});
            }
        }

        // Dessiner les routes horizontales
        for (int pos : positionsHorizontales) {
            if (pos + 1 < hauteur) {  // Vérifier que pos + 1 n'est pas hors limites
                for (int i = 0; i < largeur; i++) {
                    /*grille[pos][i] = 'R';
                    grille[pos + 1][i] = 'R';*/

                    grille_c[pos][i].estValide(true);
                    grille_c[pos + 1][i].estValide(true);
                    grille_c[pos][i].setTypeZone(TypeZone.ROUTE);
                    grille_c[pos + 1][i].setTypeZone(TypeZone.ROUTE);
                }
            }
        }

        // Générer les positions des routes verticales - même logique de vérification
        Set<Integer> positionsVerticales = new HashSet<>();
        while (positionsVerticales.size() < nbr_rv) {
            int pos = random.nextInt(4,largeur - 6);  // Position aléatoire dans les limites de la grille
            /*System.out.println("pos V= ");
            System.out.println(pos);*/
            // Vérifier si cette position respecte un espacement d'au moins 4 cases avec les autres routes
            boolean estValide = true;
            for (int p : positionsVerticales) {
                if (Math.abs(p - pos) < 6) {  // Vérifie que l'écart est d'au moins 4 cases
                    estValide = false;
                    System.out.println("pos rejected");
                    break;
                }
            }

            // Si la position est valide, on l'ajoute
            if (estValide) {
                positionsVerticales.add(pos);

                // Remplir la liste des entrées
                entrees.add(new int[] {0,pos});
                entrees.add(new int[] {hauteur-1, pos+1});
            }
        }

        // Dessiner les routes verticales
        for (int pos : positionsVerticales) {
            if (pos + 1 < largeur) {  // Vérifier que pos + 1 n'est pas hors limites-- vérification inutile
                for (int i = 0; i < hauteur; i++) {
                    // Gérer les intersections
                   /* if (grille[i][pos] == 'R') {
                        grille[i][pos] = 'I';  // 'I' pour intersection
                        grille[i][pos + 1] = 'I';
                    } else {
                        grille[i][pos] = 'R';  // Si pas d'intersection, route normale
                        grille[i][pos + 1] = 'R';
                    } */

                    // Gérer les intersections (grille_c)
                    if (grille_c[i][pos].estValide()) {
                        grille_c[i][pos].setTypeZone(TypeZone.CONFLIT);  // la zone de conflit
                        grille_c[i][pos + 1].setTypeZone(TypeZone.CONFLIT);
                        grille_c[i+1][pos].setTypeZone(TypeZone.CONFLIT);
                        grille_c[i+1][pos + 1].setTypeZone(TypeZone.CONFLIT);

                        grille_c[i][pos-1].setTypeZone(TypeZone.COMMUNICATION);
                        grille_c[i+1][pos-1].setTypeZone(TypeZone.COMMUNICATION);
                        grille_c[i-1][pos].setTypeZone(TypeZone.COMMUNICATION);
                        grille_c[i-1][pos+1].setTypeZone(TypeZone.COMMUNICATION);
                        grille_c[i][pos+2].setTypeZone(TypeZone.COMMUNICATION);
                        grille_c[i+1][pos+2].setTypeZone(TypeZone.COMMUNICATION);
                        grille_c[i+2][pos].setTypeZone(TypeZone.COMMUNICATION);
                        grille_c[i+2][pos+1].setTypeZone(TypeZone.COMMUNICATION);

                        i++; // sauter la ligne d'après
                    } else {
                        grille_c[i][pos].estValide(true);  // Si pas d'intersection, route normale
                        grille_c[i][pos + 1].estValide(true);
                        grille_c[i][pos].setTypeZone(TypeZone.ROUTE);
                        grille_c[i][pos + 1].setTypeZone(TypeZone.ROUTE);
                    }
                }
            }
        }
    }


}
