module exrc.miniprojet {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.j;

    opens exrc_miniprojet.controller to javafx.fxml;
    exports exrc_miniprojet;
}
