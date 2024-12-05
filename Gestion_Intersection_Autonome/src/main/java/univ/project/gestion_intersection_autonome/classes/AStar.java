package univ.project.gestion_intersection_autonome.classes;

import java.util.*;

public class AStar {

    // Terrain sur lequel se déroule l'algorithme
    private Terrain terrain;

    /**
     * Constructeur de la classe AStar.
     *
     * @param terrain Le terrain utilisé pour le calcul des chemins.
     */
    public AStar(Terrain terrain) {
        this.terrain = terrain;
    }

    /**
     * Trouve le chemin le plus court entre deux positions sur le terrain.
     * Utilise l'algorithme A* pour rechercher un chemin optimal.
     *
     * @param positionDepart  La position de départ.
     * @param positionArrivee La position d'arrivée.
     * @return Une liste de positions (`Vector2D`) représentant le chemin trouvé,
     *         ou une liste vide si aucun chemin n'est trouvé.
     */
    public List<Vector2D> trouverChemin(Vector2D positionDepart, Vector2D positionArrivee) {
//        System.out.println("Recherche du chemin de " + positionDepart + " à " + positionArrivee);

        // Liste de priorité des noeuds à explorer
        PriorityQueue<Noeud> openList = new PriorityQueue<>();

        // Map contenant tous les noeuds explorés ou en attente
        Map<Vector2D, Noeud> allNodes = new HashMap<>();

        // Initialisation du noeud de départ
        Noeud noeudDepart = new Noeud(positionDepart, null, 0, heuristique(positionDepart, positionArrivee));
        openList.add(noeudDepart);
        allNodes.put(positionDepart, noeudDepart);

        // Boucle principale de recherche
        while (!openList.isEmpty()) {
            // Récupère et supprime le noeud ayant le plus faible coût total (fScore)
            Noeud noeudActuel = openList.poll();

            // Si la position actuelle est la destination, on reconstruit le chemin
            if (noeudActuel.position.equals(positionArrivee)) {
                return recontruireChemin(noeudActuel);
            }

            // Exploration des voisins de la position actuelle
            for (Vector2D positionVoisin : trouverVoisins(noeudActuel.position)) {
                // Vérifie si la cellule voisine est valide sur le terrain
                if (!terrain.estCelluleValide(positionVoisin)) continue;

                Cellule neighborCell = terrain.getCellule(positionVoisin);

                // Vérifie si la cellule est une route accessible
                if (!neighborCell.estValide()) continue;

                // Détermine si le mouvement est diagonal
                boolean isDiagonalMove = (positionVoisin.getX() != noeudActuel.position.getX())
                        && (positionVoisin.getY() != noeudActuel.position.getY());

                // Calcul du coût pour se déplacer vers cette cellule
                double cost = isDiagonalMove ? Math.sqrt(2) : 1.0;
                double tentativeGScore = noeudActuel.gScore + cost;

                // Récupère ou crée le noeud pour la position voisine
                Noeud noeudVoisin = allNodes.getOrDefault(positionVoisin, new Noeud(positionVoisin));
                allNodes.put(positionVoisin, noeudVoisin);

                // Met à jour le noeud voisin si un meilleur chemin est trouvé
                if (tentativeGScore < noeudVoisin.gScore) {
                    noeudVoisin.precedent = noeudActuel;
                    noeudVoisin.gScore = tentativeGScore;
                    noeudVoisin.fScore = tentativeGScore + heuristique(positionVoisin, positionArrivee);

                    // Ajoute le noeud voisin à la liste ouverte s'il n'y est pas déjà
                    if (!openList.contains(noeudVoisin)) {
                        openList.add(noeudVoisin);
                    }
                }
            }
        }

        System.err.println("Aucun chemin trouvé de " + positionDepart + " à " + positionArrivee);
        return Collections.emptyList();
    }

    /**
     * Reconstruit le chemin en remontant les noeuds depuis la destination.
     *
     * @param noeud Le dernier noeud (destination).
     * @return Une liste de positions (`Vector2D`) représentant le chemin reconstruit.
     */
    private List<Vector2D> recontruireChemin(Noeud noeud) {
        List<Vector2D> chemin = new ArrayList<>();

        // Remonte les noeuds en suivant les références au noeud précédent
        while (noeud != null) {
            chemin.add(noeud.position);
            noeud = noeud.precedent;
        }

        // Inverse la liste pour obtenir le chemin dans le bon ordre
        Collections.reverse(chemin);
        return chemin;
    }

    /**
     * Calcule une heuristique basée sur la distance euclidienne entre deux points.
     *
     * @param a La position de départ.
     * @param b La position d'arrivée.
     * @return La distance euclidienne entre les deux points.
     */
    private double heuristique(Vector2D a, Vector2D b) {
        return Math.sqrt(Math.pow((b.getX() - a.getX()), 2) + Math.pow((b.getY() - a.getY()), 2));
    }

    /**
     * Trouve les positions voisines accessibles depuis une position donnée.
     *
     * @param position La position actuelle.
     * @return Une liste de positions (`Vector2D`) correspondant aux voisins accessibles.
     */
    private List<Vector2D> trouverVoisins(Vector2D position) {
        List<Vector2D> voisins = new ArrayList<>();
        int x = position.getX();
        int y = position.getY();

        Cellule currentCell = terrain.getCellule(position);
        boolean[] directions = currentCell.getDirectionsAutorisees(); // Directions autorisées (N, E, S, O, etc.)

        // Vérifie et ajoute chaque direction valide
        if (directions[0] && y > 0) voisins.add(new Vector2D(x, y - 1)); // NORD
        if (directions[1] && x < terrain.getLargeur() - 1) voisins.add(new Vector2D(x + 1, y)); // EST
        if (directions[2] && y < terrain.getHauteur() - 1) voisins.add(new Vector2D(x, y + 1)); // SUD
        if (directions[3] && x > 0) voisins.add(new Vector2D(x - 1, y)); // OUEST

        // Diagonales
        if (directions[4] && x > 0 && y > 0) voisins.add(new Vector2D(x - 1, y - 1)); // NORD-OUEST
        if (directions[5] && x < terrain.getLargeur() - 1 && y > 0) voisins.add(new Vector2D(x + 1, y - 1)); // NORD-EST
        if (directions[6] && x > 0 && y < terrain.getHauteur() - 1) voisins.add(new Vector2D(x - 1, y + 1)); // SUD-OUEST
        if (directions[7] && x < terrain.getLargeur() - 1 && y < terrain.getHauteur() - 1) voisins.add(new Vector2D(x + 1, y + 1)); // SUD-EST

        return voisins;
    }

    /**
     * Classe interne représentant un noeud dans le graphe du terrain.
     */
    private static class Noeud implements Comparable<Noeud> {
        Vector2D position; // Position actuelle du noeud
        Noeud precedent;   // Noeud précédent dans le chemin
        double gScore;     // Coût du chemin depuis le noeud de départ
        double fScore;     // Coût total estimé (gScore + heuristique)

        /**
         * Constructeur d'un noeud avec des coûts infinis (par défaut).
         *
         * @param position La position du noeud.
         */
        Noeud(Vector2D position) {
            this.position = position;
            this.gScore = Double.POSITIVE_INFINITY;
            this.fScore = Double.POSITIVE_INFINITY;
        }

        /**
         * Constructeur d'un noeud avec des coûts définis.
         *
         * @param position La position du noeud.
         * @param precedent Le noeud précédent dans le chemin.
         * @param gScore Le coût depuis le noeud de départ.
         * @param fScore Le coût total estimé (gScore + heuristique).
         */
        Noeud(Vector2D position, Noeud precedent, double gScore, double fScore) {
            this.position = position;
            this.precedent = precedent;
            this.gScore = gScore;
            this.fScore = fScore;
        }

        @Override
        public int compareTo(Noeud other) {
            return Double.compare(this.fScore, other.fScore);
        }
    }
}
