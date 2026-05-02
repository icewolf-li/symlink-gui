module top.nodaoli {
    requires javafx.controls;
    requires javafx.fxml;

    opens top.nodaoli to javafx.fxml;
    exports top.nodaoli;
}
