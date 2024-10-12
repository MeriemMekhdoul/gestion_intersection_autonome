package univ.project.gestion_intersection_autonome.classes;

import java.util.*;

public class Terrain {
    private Cellule[][] grille;
    private final List<Vector2D> sorties;
    private final List<Vector2D> entrees;
    private final List<Intersection> intersections;
    private int largeur;
    private int hauteur;
    private final int NOMBRE_MAX_ROUTES_HORIZONTALES = 1;
    private final int NOMBRE_MAX_ROUTES_VERTICALES = 1;
    private final int ESPACE_MIN = 8;
    private final int ESPACE_MAX = 20;

    // Constructeur par défaut
    public Terrain() {
        entrees = new ArrayList<>();
        sorties = new ArrayList<>();
        intersections = new ArrayList<>();
    }
    public Terrain(int _largeur, int _hauteur){
        this.largeur = _largeur;
        this.hauteur = _hauteur;

        this.grille = new Cellule[largeur][hauteur];

        entrees = new ArrayList<>();
        sorties = new ArrayList<>();
        intersections = new ArrayList<>();

        initialiserGrilleVide();
        genererGrille();

        afficherGrille();
        afficherEntreesEtSorties();
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
        for (int i = 0; i < largeur; i++) {
            for (int j = 0; j < hauteur; j++) {
                grille[i][j] = new Cellule();
            }
        }
    }

    private void genererLigne(int y) {  // Remplir une route horizontale dans la grille
        for (int i = 0; i < largeur; i++) {
            grille[i][y].estValide(true);
            grille[i][y + 1].estValide(true);

            grille[i][y].setTypeZone(TypeZone.ROUTE);
            grille[i][y + 1].setTypeZone(TypeZone.ROUTE);

            grille[i][y].setDirectionsAutorisees(false,false,false,true);
            grille[i][y + 1].setDirectionsAutorisees(false,true,false,false);
        }
        entrees.add(new Vector2D(0,y + 1));
        entrees.add(new Vector2D(largeur - 1, y));
        sorties.add(new Vector2D(0, y));
        sorties.add(new Vector2D(largeur - 1, y + 1));
    }

//peut être améliorée ...
    private void genererColonne(int x) {  // Remplir une route verticale dans la grille
        for (int i = 0; i < hauteur; i++) {
            // Gérer les intersections
            if (grille[x][i].estValide()) { //créer une intersection

                creerIntersection(x,i);

                grille[x][i].setTypeZone(TypeZone.CONFLIT);
                grille[x + 1][i].setTypeZone(TypeZone.CONFLIT);
                grille[x][i + 1].setTypeZone(TypeZone.CONFLIT);
                grille[x + 1][i + 1].setTypeZone(TypeZone.CONFLIT);

                grille[x][i].setDirectionsAutorisees(false,false,true,true);
                grille[x + 1][i].setDirectionsAutorisees(true,false,false,true);
                grille[x][i + 1].setDirectionsAutorisees(false,true,true,false);
                grille[x + 1][i + 1].setDirectionsAutorisees(true,true,false,false);

                grille[x - 1][i].setTypeZone(TypeZone.COMMUNICATION);
                grille[x - 1][i + 1].setTypeZone(TypeZone.COMMUNICATION);
                grille[x][i - 1].setTypeZone(TypeZone.COMMUNICATION);
                grille[x + 1][i - 1].setTypeZone(TypeZone.COMMUNICATION);
                grille[x + 2][i].setTypeZone(TypeZone.COMMUNICATION);
                grille[x + 2][i + 1].setTypeZone(TypeZone.COMMUNICATION);
                grille[x][i + 2].setTypeZone(TypeZone.COMMUNICATION);
                grille[x + 1][i + 2].setTypeZone(TypeZone.COMMUNICATION);

                grille[x - 1][i].setDirectionsAutorisees(false,false,false,true);
                grille[x - 1][i + 1].setDirectionsAutorisees(false,true,false,false);
                grille[x][i - 1].setDirectionsAutorisees(false,false,true,false);
                grille[x + 1][i - 1].setDirectionsAutorisees(true,false,false,false);
                grille[x + 1][i + 2].setDirectionsAutorisees(true,false,false,false);
                grille[x + 2][i].setDirectionsAutorisees(false,false,false,true);
                grille[x + 2][i + 1].setDirectionsAutorisees(false,true,false,false);
                grille[x][i + 2].setDirectionsAutorisees(false,false,true,false);

                i++;  // Sauter la ligne d'après
            } else {
                grille[x][i].estValide(true);
                grille[x + 1][i].estValide(true);

                if(grille[x][i].getTypeZone() == null){
                    grille[x][i].setTypeZone(TypeZone.ROUTE);
                    grille[x + 1][i].setTypeZone(TypeZone.ROUTE);

                    grille[x][i].setDirectionsAutorisees(false,false,true,false);
                    grille[x + 1][i].setDirectionsAutorisees(true,false,false,false);
                }
            }
        }

        entrees.add(new Vector2D(x, 0));
        entrees.add(new Vector2D(x + 1, hauteur - 1));
        sorties.add(new Vector2D(x + 1, 0));
        sorties.add(new Vector2D(x, hauteur - 1));
    }

    // mettre espace_min et espace_max comme constantes en dehors de la methode
    private void genererGrille(){
        Random random = new Random();
        int y_pos = random.nextInt(4,hauteur/4);
        int routeGeneree = 0;
        while((y_pos + 5 < hauteur) && (routeGeneree < NOMBRE_MAX_ROUTES_HORIZONTALES)){
            genererLigne(y_pos);
            routeGeneree++;
            y_pos += random.nextInt(ESPACE_MIN, ESPACE_MAX);
        }

        routeGeneree = 0;
        int x_pos = random.nextInt(4,largeur/4);
        while((x_pos + 5 < largeur) && (routeGeneree < NOMBRE_MAX_ROUTES_VERTICALES)){
            genererColonne(x_pos);
            routeGeneree++;
            x_pos += random.nextInt(ESPACE_MIN,ESPACE_MAX);
        }
    }

    //inutile, seulement pour debug
    public void afficherGrille() {
        for (int i = 0; i < largeur; i++) {
            for (int j = 0; j < hauteur; j++) {
                if (!grille[j][i].estValide()) {
                    System.out.print(". ");  // Afficher un point pour une cellule non valide
                } else {
                    // Afficher selon le type de zone
                    switch (grille[j][i].getTypeZone()) {
                        case COMMUNICATION -> System.out.print("M ");  // Communication
                        case CONFLIT -> System.out.print("C ");  // Conflit
                        case ROUTE -> System.out.print("R ");  // Route
                        default -> System.out.print("? ");  // Pour gérer un cas inconnu
                    }
                }
            }
            System.out.println();  // Passer à la ligne suivante après chaque ligne de la grille
        }
    }

    public void afficherEntreesEtSorties() {
        System.out.println("Entrées:");
        for (Vector2D entree : entrees) {
            System.out.println("Entrée: " + entree);
        }

        System.out.println("Sorties:");
        for (Vector2D sortie : sorties) {
            System.out.println("Sortie: " + sortie);
        }
    }

    // Cette méthode vérifie si une cellule à la position donnée est valide et praticable (est une route)
    public boolean estCelluleValide(Vector2D position) {
        int x = position.getX();
        int y = position.getY();

        // Vérifier que la position est dans les bornes de la grille
        if (x >= 0 && x < largeur && y >= 0 && y < hauteur) {
            Cellule cellule = grille[x][y];  // Obtenir la cellule à cette position

            // Vérifier si la cellule est valide (par exemple, une route ou hors terrain)
            return cellule.estValide();
        }
        return false;  // Si hors limites ou non valide
    }

    public Cellule getCellule(Vector2D position)
    {
        int x = position.getX();
        int y = position.getY();

        if (x >= 0 && x < largeur && y >= 0 && y < hauteur) {
            return grille[position.getX()][position.getY()];
        }
        else {
            throw new IndexOutOfBoundsException("Position hors limite : " + position);
        }
    }

    public Intersection getIntersection(Vector2D position){
        for (Intersection intersection : intersections) {
            if (intersection.contientCellule(position))
                return intersection;
        }
        throw new NoSuchElementException("Aucune intersection ne contient la cellule à la position " + position);
    }

    public void creerIntersection(int x, int i){
        ArrayList<Vector2D> cellulesComm = new ArrayList<>();
        cellulesComm.add(new Vector2D(x-1, i));
        cellulesComm.add(new Vector2D(x-1,i+1));
        cellulesComm.add(new Vector2D(x,i-1));
        cellulesComm.add(new Vector2D(x+1,i-1));
        cellulesComm.add(new Vector2D(x+2, i));
        cellulesComm.add(new Vector2D(x+2,i+1));
        cellulesComm.add(new Vector2D(x,i+2));
        cellulesComm.add(new Vector2D(x+1,i+2));

        Intersection intersection = new Intersection(cellulesComm);
        intersections.add(intersection);
    }
}
