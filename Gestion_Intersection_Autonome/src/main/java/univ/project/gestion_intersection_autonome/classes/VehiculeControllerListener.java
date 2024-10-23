package univ.project.gestion_intersection_autonome.classes;

public interface VehiculeControllerListener {

    void messageVc (Message message) ;

    void onMessageReceivedFromIntersection(Message message);
}
