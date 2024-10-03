package univ.project.gestion_intersection_autonome.classes;

public enum Objetmessage {
    PASSAGE, //demande de passage
    INFORMATION, //pour informer de l'arrivée du véhicule dans une zone de comm - renommer en ENTREE ?
    TRAFIC, //information sur le traffic dans les autres voies
    CONFLIT, //proposition d'une configuration de passage lors d'un conflit - renommer en CONFIG ?
    SORTIE  //sortie du véhicule de l'intersection
}
