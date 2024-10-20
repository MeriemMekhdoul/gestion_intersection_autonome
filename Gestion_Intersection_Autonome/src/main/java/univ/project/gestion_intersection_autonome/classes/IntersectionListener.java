package univ.project.gestion_intersection_autonome.classes;

public interface IntersectionListener {
    void messageIntersection(Message message);

    void onMessageReceivedFromVehiculeController(Message message);

}
