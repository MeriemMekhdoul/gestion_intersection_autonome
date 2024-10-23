package univ.project.gestion_intersection_autonome.classes;

public enum Objetmessage {
    PASSAGE, //demande de passage
    INFORMATION, //pour informer de l'arrivée du véhicule dans une zone de comm - renommer en ENTREE ?
    TRAFIC, //information sur le trafic dans les autres voies
    CONFIG, //proposition d'une configuration de passage lors d'un conflit
    SORTIE,  //sortie du véhicule de l'intersection
    MARCHE, STOP    //demande d'arrêt de l'intersection à un véhicule
}
