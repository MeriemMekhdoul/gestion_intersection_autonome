package univ.project.gestion_intersection_autonome.classes;

import java.util.*;

public class AStar
{
    private Terrain terrain;

    public AStar(Terrain terrain) {
        this.terrain = terrain;
    }

    public List<Vector2D> trouverChemin(Vector2D positionDepart, Vector2D positionArrivee)
    {
        System.out.println("Recherche du chemin de " + positionDepart + " à " + positionArrivee);

        PriorityQueue<Noeud> openList = new PriorityQueue<>(); // noeuds à explorer
        Map<Vector2D, Noeud> allNodes = new HashMap<>(); // tous les noeuds visités ou en attente

        Noeud noeudDepart = new Noeud(positionDepart, null, 0, heuristique(positionDepart, positionArrivee));

        openList.add(noeudDepart);
        allNodes.put(positionDepart, noeudDepart);

        while (!openList.isEmpty()) // tant qu'il reste des noeuds à explorer
        {
            Noeud noeudActuel = openList.poll(); // retourne et supprime l'élément à la fin de la queue

            if (noeudActuel.position.equals(positionArrivee)) {
                return recontruireChemin(noeudActuel);
            }

            for (Vector2D posititionVoisin : trouverVoisins(noeudActuel.position))
            {
                // si la cellule est dans le terrain
                if (!terrain.estCelluleValide(posititionVoisin)) continue;

                Cellule neighborCell = terrain.getCellule(posititionVoisin);

                // et si la cellule est bien une route
                if (!neighborCell.estValide()) continue;

                double tentativeGScore = noeudActuel.gScore + 1; // incrémentation du coût

                // ajoute le noeud s'il n'existe pas
                Noeud noeudVoisin = allNodes.getOrDefault(posititionVoisin, new Noeud(posititionVoisin));
                allNodes.put(posititionVoisin, noeudVoisin);

                // on met à jour les voisins en fonction de leurs coûts
                if (tentativeGScore < noeudVoisin.gScore)
                {
                    noeudVoisin.precedent = noeudActuel;
                    noeudVoisin.gScore = tentativeGScore;
                    noeudVoisin.fScore = tentativeGScore + heuristique(posititionVoisin, positionArrivee);

                    if (!openList.contains(noeudVoisin)) {
                        openList.add(noeudVoisin);
                    }
                }
            }
        }

        System.err.println("Aucun chemin trouvé de " + positionDepart + " à " + positionArrivee);
        return Collections.emptyList();
    }

    // effectue le chemin en arrière en récupérant les noeuds précédents depuis l'arrivée
    private List<Vector2D> recontruireChemin(Noeud noeud)
    {
        List<Vector2D> chemin = new ArrayList<>();

        while (noeud != null) {
            chemin.add(noeud.position);
            noeud = noeud.precedent;
        }

        Collections.reverse(chemin); // on remonte à l'envers puis on retourne la liste

        return chemin;
    }

    // calcul du coût d'un noeud jusqu'à l'arrivée
    private double heuristique(Vector2D a, Vector2D b) {
        return Math.max(Math.abs(b.getX() - a.getX()), Math.abs(b.getY() - a.getY())); // heuristique de manhattan (taxi cab)
    }

    private List<Vector2D> trouverVoisins(Vector2D position)
    {
        List<Vector2D> voisins = new ArrayList<>();
        int x = position.getX();
        int y = position.getY();

        Cellule currentCell = terrain.getCellule(position);
        boolean[] directions = currentCell.getDirectionsAutorisees(); // NORD 0, EST 1, SUD 2, OUEST 3

        // peut être amélioré, voir plus tard

        // NORD
        if (directions[0] && y > 0)
        {
            Vector2D nord = new Vector2D(x, y - 1);

            if (terrain.estCelluleValide(nord)) {
                voisins.add(nord);
            }
        }

        // EST
        if (directions[1] && x < terrain.getLargeur() - 1)
        {
            Vector2D est = new Vector2D(x + 1, y);

            if (terrain.estCelluleValide(est)) {
                voisins.add(est);
            }
        }

        // SUD
        if (directions[2] && y < terrain.getHauteur() - 1)
        {
            Vector2D sud = new Vector2D(x, y + 1);

            if (terrain.estCelluleValide(sud)) {
                voisins.add(sud);
            }
        }

        // OUEST
        if (directions[3] && x > 0)
        {
            Vector2D ouest = new Vector2D(x - 1, y);

            if (terrain.estCelluleValide(ouest)) {
                voisins.add(ouest);
            }
        }

        return voisins;
    }


    private static class Noeud implements Comparable<Noeud>
    {
        Vector2D position;
        Noeud precedent;
        double gScore; // coût total depuis un noeud
        double fScore; // coût total estimé (gScore + heuristique)

        // en définissant le noeud de départ avec des scores infinis on s'assure que tout noeud trouvé ensuite aura un coût inférieur
        Noeud(Vector2D position) {
            this.position = position;
            this.gScore = Double.POSITIVE_INFINITY;
            this.fScore = Double.POSITIVE_INFINITY;
        }

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
