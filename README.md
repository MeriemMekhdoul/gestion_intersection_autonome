# Projet Véhicule Autonome : Gestion automatique de la traversée d’une intersection intelligente

## Description

Le projet **Véhicule Autonome : Gestion automatique de la traversée d’une intersection intelligente** vise à simuler et gérer des véhicules autonomes dans un environnement urbain. Chaque véhicule interagit avec d'autres véhicules et les intersections via un système de communication en temps réel basé sur des messages. Le système permet aux véhicules de traverser des intersections de manière autonome en évitant les conflits et en respectant les priorités de passage.

Le projet implémente un système de gestion d'intersections intelligentes où les véhicules peuvent communiquer entre eux pour éviter les conflits et optimiser leur traversée des intersections.

## Fonctionnalités principales

- **Communication entre véhicules** : Les véhicules peuvent envoyer et recevoir des messages pour échanger des informations sur leur itinéraire.
- **Gestion des intersections** : Les intersections détectent les véhicules et gèrent leur passage en fonction de la situation.
- **Gestion des conflits** : Le système détecte et évite les conflits entre véhicules se dirigeant vers une intersection en même temps.
- **Simulation de la circulation** : Les véhicules se déplacent sur un terrain simulé, avec la possibilité d'interagir avec d'autres véhicules et d'attendre ou passer selon un temps d'attente calculé.
- **Gestion des priorités de passage** : Les véhicules prioritaires, comme les véhicules de police, peuvent traverser l'intersection en priorité si nécessaire.

## Architecture du projet

### Classes principales

- **`Vehicule`** : Représente un véhicule autonome. Chaque véhicule possède une position, une direction et un comportement de communication.
- **`VehiculeController`** : Contrôle le comportement des véhicules, y compris la gestion de leur mouvement et de la communication.
- **`Intersection`** : Représente une intersection où les véhicules se croisent. Elle gère la détection des conflits et la communication avec les véhicules.
- **`Message`** : Représente un message envoyé entre les véhicules ou entre une intersection et un véhicule. Ce message contient des informations sur la position, l'état, et l'itinéraire d'un véhicule.
- **`Simulation`** : Coordonne l'ensemble de la simulation, initialisant les véhicules et les intersections, et gérant leur comportement dans le temps.

### Concepts clés

1. **Listeners** : Les véhicules et les intersections utilisent un mécanisme de listeners pour recevoir des notifications sur les événements de l'autre.
2. **Algorithme d’évitement de conflit** : Les véhicules peuvent détecter les conflits dans les intersections et attendre pour éviter une collision.
3. **Priorité au véhicule de police** : Si un véhicule de type police est détecté, il peut passer en priorité en cas de congestion.

## Fonctionnement du projet

1. **Initialisation** : La simulation est initialisée avec un certain nombre de véhicules et d’intersections.
2. **Déplacement des véhicules** : Les véhicules se déplacent selon un algorithme qui calcule la meilleure trajectoire vers leur destination.
3. **Communication** : Les véhicules envoient des messages aux intersections pour annoncer leur arrivée et vérifier la disponibilité de l'intersection.
4. **Gestion des conflits** : Lorsque plusieurs véhicules arrivent en même temps dans une intersection, le système vérifie les conflits et attribue une priorité en fonction des règles de circulation.
5. **Priorité au véhicule de police** : Si un véhicule de type police est détecté, il peut passer en priorité en cas de congestion.

## Installation et Configuration

### Prérequis

- **Java 17+** : Le projet est développé en Java. Assurez-vous d’avoir une version compatible de Java installée.
- **IDE recommandé** : Utilisez un IDE comme IntelliJ IDEA ou Eclipse pour faciliter le développement et l'exécution du projet.

### Installation

1. Clonez le repository du projet :

   ```bash
   git clone https://forge.univ-lyon1.fr/p2310195/l3_projet_sa5.git
