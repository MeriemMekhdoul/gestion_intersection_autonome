module univ.project.gestion_intersection_autonome {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.almasb.fxgl.all;

    opens univ.project.gestion_intersection_autonome to javafx.fxml;
    exports univ.project.gestion_intersection_autonome;

    opens univ.project.gestion_intersection_autonome.controllers to javafx.fxml;
    exports univ.project.gestion_intersection_autonome.controllers;
    exports univ.project.gestion_intersection_autonome.classes;
}