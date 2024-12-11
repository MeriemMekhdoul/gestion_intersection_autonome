module univ.project.gestion_intersection_autonome {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.almasb.fxgl.all;
    requires java.desktop;

    opens univ.project.gestion_intersection_autonome to javafx.fxml;
    exports univ.project.gestion_intersection_autonome;

    opens univ.project.gestion_intersection_autonome.controleurs to javafx.fxml;
    exports univ.project.gestion_intersection_autonome.controleurs;
    exports univ.project.gestion_intersection_autonome.classes;
    exports univ.project.gestion_intersection_autonome.classes.Agents;
    exports univ.project.gestion_intersection_autonome.classes.Enums;
    exports univ.project.gestion_intersection_autonome.classes.Terrain;
}