package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.sql.SQLException;

public class Main extends Application
{
    @Override
    public void start(Stage primaryStage) throws Exception
    {
        try
        {
            Controller.initTable();

            Parent root = FXMLLoader.load(getClass().getResource("mainwindow.fxml"));
            primaryStage.setTitle("Hello Products");
            primaryStage.setScene(new Scene(root, 1000, 700));
            primaryStage.show();
        }
        catch (SQLException e)
        {
            String warning = "Проблемы с подключением к базе!";
            Controller.pushWarning(warning);
        }
    }

    public static void main(String[] args)
    {
        launch(args);
    }
}
