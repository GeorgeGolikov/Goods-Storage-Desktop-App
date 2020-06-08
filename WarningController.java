package sample;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class WarningController
{
    @FXML
    private Label warningLabel_;

    public void setLabelText(String text)
    {
        warningLabel_.setText(text);
    }

    @FXML
    private Button btnOk_;

    @FXML
    private void buttonOkClicked()
    {
        Stage stage = (Stage) btnOk_.getScene().getWindow();
        stage.close();
    }
}
