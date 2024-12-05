package univ.project.gestion_intersection_autonome.classes;

import java.util.*;

public class Terrain {
    private Cellule[][] grille;
    private final List<Vector2D> entreesVoieGauche;
    private final List<Vector2D> entreesVoieDroite;
    private final List<Vector2D> sortiesVoieGauche;
    private final List<Vector2D> sortiesVoieDroite;
    private final List<Intersection> intersections;
    private int largeur;
    private int hauteur;
    private final int NOMBRE_MAX_ROUTES_HORIZONTALES = 1;
    private final int NOMBRE_MAX_ROUTES_VERTICALES = 1;
    private final int ESPACE_MIN = 8;
    private final int ESPACE_MAX = 16;

    // Constructeur par défaut
    public Terrain() {
        entreesVoieGauche = new ArrayList<>();
        entreesVoieDroite = new ArrayList<>();
        sortiesVoieGauche = new ArrayList<>();
        sortiesVoieDroite = new ArrayList<>();
        intersections = new ArrayList<>();
    }

    public Terrain(int _largeur, int _hauteur){
        this.largeur = _largeur;
        this.hauteur = _hauteur;

        this.grille = new Cellule[largeur][hauteur];

        entreesVoieGauche = new ArrayList<>();
        entreesVoieDroite = new ArrayList<>();
        sortiesVoieGauche = new ArrayList<>();
        sortiesVoieDroite = new ArrayList<>();

        intersections = new ArrayList<>();

        initialiserGrilleVide();
        genererGrille();

        afficherGrille();
        afficherEntreesEtSorties();
    }

    public List<Vector2D> getEntreesVoieGauche() {
        return entreesVoieGauche;
    }

    public List<Vector2D> getEntreesVoieDroite() {
        return entreesVoieDroite;
    }

    public List<Vector2D> getSortiesVoieGauche() {
        return sortiesVoieGauche;
    }

    public List<Vector2D> getSortiesVoieDroite() {
        return sortiesVoieDroite;
    }

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
            grille[i][y + 2].estValide(true);
            grille[i][y + 3].estValide(true);

            grille[i][y].setTypeZone(TypeZone.ROUTE);
            grille[i][y + 1].setTypeZone(TypeZone.ROUTE);
            grille[i][y + 2].setTypeZone(TypeZone.ROUTE);
            grille[i][y + 3].setTypeZone(TypeZone.ROUTE);

            grille[i][y].setDirectionsAutorisees(false,false,false,true,false,false,false,false);
            grille[i][y + 1].setDirectionsAutorisees(false,false,false,true,false,false,false,false);
            grille[i][y + 2].setDirectionsAutorisees(false,true,false,false,false,false,false,false);
            grille[i][y + 3].setDirectionsAutorisees(false,true,false,false,false,false,false,false);
        }

        // Côté Ouest (x = 0)
        sortiesVoieDroite.add(new Vector2D(0, y));        // SD
        sortiesVoieGauche.add(new Vector2D(0, y + 1));    // SG
        entreesVoieGauche.add(new Vector2D(0, y + 2));    // EG
        entreesVoieDroite.add(new Vector2D(0, y + 3));    // ED

        // Côté Est (x = largeur - 1)
        entreesVoieDroite.add(new Vector2D(largeur - 1, y));     // ED
        entreesVoieGauche.add(new Vector2D(largeur - 1, y + 1)); // EG
        sortiesVoieGauche.add(new Vector2D(largeur - 1, y + 2)); // SG
        sortiesVoieDroite.add(new Vector2D(largeur - 1, y + 3)); // SD
    }

    //peut être améliorée ...
    private void genererColonne(int x) {  // Remplir une route verticale dans la grille
        for (int i = 0; i < hauteur; i++) {
            // Gérer les intersections
            if (grille[x][i].estValide()) { //créer une intersection

                creerIntersection(x,i);

                grille[x][i].setTypeZone(TypeZone.CONFLIT);
                grille[x][i+1].setTypeZone(TypeZone.CONFLIT);
                grille[x][i+2].setTypeZone(TypeZone.CONFLIT);
                grille[x][i+3].setTypeZone(TypeZone.CONFLIT);

                grille[x + 1][i].setTypeZone(TypeZone.CONFLIT);
                grille[x + 1][i+1].setTypeZone(TypeZone.CONFLIT);
                grille[x + 1][i+2].setTypeZone(TypeZone.CONFLIT);
                grille[x + 1][i+3].setTypeZone(TypeZone.CONFLIT);

                grille[x + 2][i].setTypeZone(TypeZone.CONFLIT);
                grille[x + 2][ i+1 ].setTypeZone(TypeZone.CONFLIT);
                grille[x + 2][ i+2 ].setTypeZone(TypeZone.CONFLIT);
                grille[x + 2][ i+3 ].setTypeZone(TypeZone.CONFLIT);

                grille[x + 3][i].setTypeZone(TypeZone.CONFLIT);
                grille[x + 3][i + 1].setTypeZone(TypeZone.CONFLIT);
                grille[x + 3][i + 2].setTypeZone(TypeZone.CONFLIT);
                grille[x + 3][i + 3].setTypeZone(TypeZone.CONFLIT);


                grille[x][i].setDirectionsAutorisees(false,false,true,true,false,false,false,true);
                grille[x][i+1].setDirectionsAutorisees(false,false,true,true,false,false,false,false);
                grille[x][i+2].setDirectionsAutorisees(false,true,true,false,false,true,false,true);
                grille[x][i+3].setDirectionsAutorisees(false,true,true,false,false,true,false,false);

                grille[x + 1][i].setDirectionsAutorisees(false,false,true,true,false,false,true,true);
                grille[x + 1][i+1].setDirectionsAutorisees(false,false,true,true,true,true,false,true);
                grille[x + 1][i+2].setDirectionsAutorisees(false,true,true,false,true,true,true,false);
                grille[x + 1][i+3].setDirectionsAutorisees(false,true,true,false,false,false,false,false);

                grille[x + 2][i].setDirectionsAutorisees(true,false,false,true,false,false,false,false);
                grille[x + 2][ i+1 ].setDirectionsAutorisees(true,false,false,true,false,true,true,true);
                grille[x + 2][ i+2 ].setDirectionsAutorisees(true,true,false,false,true,false,true,true);
                grille[x + 2][ i+3 ].setDirectionsAutorisees(true,true,false,false,true,true,false,false);

                grille[x + 3][i].setDirectionsAutorisees(true,false,false,true,false,false,true,false);
                grille[x + 3][i + 1].setDirectionsAutorisees(true,false,false,true,true,false,true,false);
                grille[x + 3][i + 2].setDirectionsAutorisees(true,true,false,false,false,false,false,false);
                grille[x + 3][i + 3].setDirectionsAutorisees(true,true,false,false,true,false,false,false);




                grille[x - 1][i].setTypeZone(TypeZone.COMMUNICATION);
                grille[x - 1][i + 1].setTypeZone(TypeZone.COMMUNICATION);
                grille[x - 1][i + 2].setTypeZone(TypeZone.COMMUNICATION);
                grille[x - 1][i + 3].setTypeZone(TypeZone.COMMUNICATION);


                grille[x][i - 1].setTypeZone(TypeZone.COMMUNICATION);
                grille[x + 1][i - 1].setTypeZone(TypeZone.COMMUNICATION);
                grille[x + 2][i - 1].setTypeZone(TypeZone.COMMUNICATION);
                grille[x + 3][i - 1].setTypeZone(TypeZone.COMMUNICATION);


                grille[x + 4][i].setTypeZone(TypeZone.COMMUNICATION);
                grille[x + 4][i + 1].setTypeZone(TypeZone.COMMUNICATION);
                grille[x + 4][i + 2].setTypeZone(TypeZone.COMMUNICATION);
                grille[x + 4][i + 3].setTypeZone(TypeZone.COMMUNICATION);



                grille[x][i+4].setTypeZone(TypeZone.COMMUNICATION);
                grille[x + 1][i+4].setTypeZone(TypeZone.COMMUNICATION);
                grille[x + 2][i+4].setTypeZone(TypeZone.COMMUNICATION);
                grille[x + 3][i+4].setTypeZone(TypeZone.COMMUNICATION);




                grille[x - 1][i].setDirectionsAutorisees(false,false,false,true,false,false,false,false);
                grille[x - 1][i + 1].setDirectionsAutorisees(false,false,false,true,false,false,false,false);
                grille[x - 1][i + 2].setDirectionsAutorisees(false,true,false,false,false,false,false,false);
                grille[x - 1][i + 3].setDirectionsAutorisees(false,true,false,false,false,false,false,false);



                grille[x][i - 1].setDirectionsAutorisees(false,false,true,false,false,false,false,false);
                grille[x + 1][i - 1].setDirectionsAutorisees(false,false,true,false,false,false,false,false);
                grille[x + 2][i - 1].setDirectionsAutorisees(true,false,false,false,false,false,false,false);
                grille[x + 3][i - 1].setDirectionsAutorisees(true,false,false,false,false,false,false,false);



                grille[x +4][i].setDirectionsAutorisees(false,false,false,true,false,false,false,false);
                grille[x + 4][i + 1].setDirectionsAutorisees(false,false,false,true,false,false,false,false);
                grille[x + 4][i + 2].setDirectionsAutorisees(false,true,false,false,false,false,false,false);
                grille[x + 4][i + 3].setDirectionsAutorisees(false,true,false,false,false,false,false,false);


                grille[x][i + 4].setDirectionsAutorisees(false,false,true,false,false,false,false,false);
                grille[x + 1][i + 4].setDirectionsAutorisees(false,false,true,false,false,false,false,false);
                grille[x + 2][i + 4].setDirectionsAutorisees(true,false,false,false,false,false,false,false);
                grille[x + 3][i + 4].setDirectionsAutorisees(true,false,false,false,false,false,false,false);

                i+=3;  // Sauter la ligne d'après
            } else {
                grille[x][i].estValide(true);
                grille[x + 1][i].estValide(true);
                grille[x + 2][i].estValide(true);
                grille[x + 3][i].estValide(true);


                if(grille[x][i].getTypeZone() == null){
                    grille[x][i].setTypeZone(TypeZone.ROUTE);
                    grille[x + 1][i].setTypeZone(TypeZone.ROUTE);
                    grille[x + 2][i].setTypeZone(TypeZone.ROUTE);
                    grille[x + 3][i].setTypeZone(TypeZone.ROUTE);


                    grille[x][i].setDirectionsAutorisees(false,false,true,false,false,false,false,false);
                    grille[x + 1][i].setDirectionsAutorisees(false,false,true,false,false,false,false,false);
                    grille[x + 2][i].setDirectionsAutorisees(true,false,false,false,false,false,false,false);
                    grille[x + 3][i].setDirectionsAutorisees(true,false,false,false,false,false,false,false);
                }
            }
        }

        // Côté Nord (y = 0)
        entreesVoieDroite.add(new Vector2D(x, 0));       // ED
        entreesVoieGauche.add(new Vector2D(x + 1, 0));   // EG
        sortiesVoieGauche.add(new Vector2D(x + 2, 0));   // SG
        sortiesVoieDroite.add(new Vector2D(x + 3, 0));   // SD

        // Côté Sud (y = hauteur - 1)
        sortiesVoieDroite.add(new Vector2D(x, hauteur - 1));     // SD
        sortiesVoieGauche.add(new Vector2D(x + 1, hauteur - 1)); // SG
        entreesVoieGauche.add(new Vector2D(x + 2, hauteur - 1)); // EG
        entreesVoieDroite.add(new Vector2D(x + 3, hauteur - 1)); // ED
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

    public void afficherEntreesEtSorties()
    {
        System.out.println("Entrées voie Gauche :");
        for (Vector2D entree : entreesVoieGauche) {
            System.out.println("Entrée: " + entree);
        }
        System.out.println("Entrées voie Droite :");
        for (Vector2D entree : entreesVoieDroite) {
            System.out.println("Entrée: " + entree);
        }

        System.out.println("Sorties voie Gauche :");
        for (Vector2D sortie : sortiesVoieGauche) {
            System.out.println("Sortie: " + sortie);
        }
        System.out.println("Sorties voie Droite :");
        for (Vector2D sortie : sortiesVoieDroite) {
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

    public Intersection getIntersectionPlusProche(Vector2D position){
        // Parcourir toutes les intersections dans le terrain
        for (Intersection intersection : intersections) {
            // Vérifier chaque point d'entrée de l'intersection
            for (Vector2D pointEntree : intersection.getPointsEntree()) {
                // Calculer la distance entre la position et le point d'entrée
                double distance = position.distance(pointEntree);

                // Si la distance est inférieure ou égale à 5, alors on est proche de l'intersection
                if (distance <= 5) {
                    return intersection;
                }
            }
        }

        return null;
    }

    public void creerIntersection(int x, int i){
        ArrayList<Vector2D> cellulesComm = new ArrayList<>();
        ArrayList<Vector2D> pointsEntree = new ArrayList<>();

        // OUEST
        cellulesComm.add(new Vector2D(x-1, i));
        cellulesComm.add(new Vector2D(x-1,i+1));
        cellulesComm.add(new Vector2D(x-1,i+2));
        cellulesComm.add(new Vector2D(x-1,i+3));

        pointsEntree.add(cellulesComm.get(cellulesComm.size() - 1));
        pointsEntree.add(cellulesComm.get(cellulesComm.size() - 2));

        // NORD
        cellulesComm.add(new Vector2D(x + 2,i-1));
        cellulesComm.add(new Vector2D(x + 3,i-1));
        cellulesComm.add(new Vector2D(x,i-1));
        cellulesComm.add(new Vector2D(x + 1,i-1));

        pointsEntree.add(cellulesComm.get(cellulesComm.size() - 1));
        pointsEntree.add(cellulesComm.get(cellulesComm.size() - 2));


        // EST
        cellulesComm.add(new Vector2D(x + 4,i + 2));
        cellulesComm.add(new Vector2D(x + 4,i + 3));
        cellulesComm.add(new Vector2D(x + 4, i));
        cellulesComm.add(new Vector2D(x + 4,i + 1));

        pointsEntree.add(cellulesComm.get(cellulesComm.size() - 1));
        pointsEntree.add(cellulesComm.get(cellulesComm.size() - 2));

        // SUD
        cellulesComm.add(new Vector2D(x, i + 4));
        cellulesComm.add(new Vector2D(x + 1,i + 4));
        cellulesComm.add(new Vector2D(x + 2,i + 4));
        cellulesComm.add(new Vector2D(x + 3,i + 4));

        pointsEntree.add(cellulesComm.get(cellulesComm.size() - 1));
        pointsEntree.add(cellulesComm.get(cellulesComm.size() - 2));


        Intersection intersection = new Intersection(cellulesComm, pointsEntree, this);
        intersections.add(intersection);
    }

    // Méthode pour vérifier si une position est proche d'une intersection (moins de 5 cases)
    public boolean estProcheIntersection(Vector2D position) {
        // Parcourir toutes les intersections dans le terrain
        for (Intersection intersection : intersections) {
            // Vérifier chaque point d'entrée de l'intersection
            for (Vector2D pointEntree : intersection.getPointsEntree()) {
                // Calculer la distance entre la position et le point d'entrée
                double distance = position.distance(pointEntree);

                // Si la distance est inférieure ou égale à 5, alors on est proche de l'intersection
                if (distance <= 5) {
                    return true;
                }
            }
        }
        // Si aucune intersection n'est proche, retourner false
        return false;
    }
}
