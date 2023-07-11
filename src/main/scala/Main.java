import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import scala.collection.JavaConverters;
import scala.collection.Map;
import java.util.Set;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        Method.loadData("BTC");
        Map<String, Method.CurrencyData> currency = Method.getCurrencyRate();

        // Création du tableau
        TableView<ObservableList<String>> tableView = new TableView<>();
        Set<String> keys = JavaConverters.setAsJavaSet(currency.keySet());

        for (String currencyName : keys) {
            TableColumn<ObservableList<String>, String> column = new TableColumn<>(currencyName);
            column.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(0)));
            tableView.getColumns().add(column);
        }


        primaryStage.setTitle("Exchanges Rates Arbitrage");

        VBox vbox = new VBox();
        vbox.setSpacing(10);
        vbox.setPadding(new Insets(20));

        Button refreshButton = new Button("Refresh Data");
        refreshButton.setOnAction(event -> {
            Method.loadData("BTC");
            java.util.Map<String, Object> javaMap = JavaConverters.mapAsJavaMap(Method.getRatesMap());
            System.out.println(javaMap);
        });

        Button arbitrageButton = new Button("Arbitrage");
        arbitrageButton.setOnAction(event -> {
            Method.runArbitrage("BTC", 100);
            Map<String, Method.CurrencyData> scalaMap = Method.getCurrencyRate();
            System.out.println(scalaMap);
        });

        vbox.getChildren().add(refreshButton);
        vbox.getChildren().add(arbitrageButton);
        vbox.getChildren().add(tableView); // Ajout du tableau à la boîte verticale

        Scene scene = new Scene(vbox, 500, 500);
        primaryStage.setScene(scene);
        primaryStage.show();
    }



    public static void main(String[] args) {
        launch(args);
    }
}
