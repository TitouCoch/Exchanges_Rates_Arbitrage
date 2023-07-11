import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Exchanges Rates Arbitrage");

        VBox vbox = new VBox();
        vbox.setSpacing(10);
        vbox.setPadding(new Insets(20));

        Button refreshButton = new Button("Refresh Data");
        refreshButton.setOnAction(event -> {
            System.out.println("Refresh data");
        });

        Button arbitrageButton = new Button("Arbitrage");
        arbitrageButton.setOnAction(event -> {
            System.out.println("Arbitrage");
        });

        vbox.getChildren().add(refreshButton);
        vbox.getChildren().add(arbitrageButton);

        Scene scene = new Scene(vbox, 500, 500);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
