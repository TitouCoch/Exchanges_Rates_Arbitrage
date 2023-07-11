
import javafx.application.Application;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import scala.collection.JavaConverters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Main extends Application {

    private GridPane gridPane;
    private Map<String, Method.CurrencyData> currency;
    private java.util.Map<String, Object> rates;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Method.loadData("BTC");
        currency = JavaConverters.mapAsJavaMap(Method.getCurrencyRate());
        rates = JavaConverters.mapAsJavaMap(Method.getRatesMap());

        List<String> currencyList = new ArrayList<>(currency.keySet());

        gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        Text currencyText = new Text("Currency");
        currencyText.setTextAlignment(TextAlignment.CENTER);
        gridPane.add(currencyText, 0, 0);

        int column = 1;
        int row = 1;
        for (String currencyName : currency.keySet()) {
            Text textColumn = new Text(currencyName);
            GridPane.setHalignment(textColumn, HPos.CENTER);
            gridPane.add(textColumn, column, 0);

            Text textRow = new Text(currencyName);
            GridPane.setHalignment(textRow, HPos.CENTER);
            gridPane.add(textRow, 0, row);

            column++;
            row++;
        }

        displayData();

        primaryStage.setTitle("Exchanges Rates Arbitrage");

        Button refreshButton = new Button("Refresh Data");
        refreshButton.setOnAction(event -> {
            Method.loadData("BTC");
            rates = JavaConverters.mapAsJavaMap(Method.getRatesMap());
            displayData();
        });

        Button arbitrageButton = new Button("Arbitrage");
        arbitrageButton.setOnAction(event -> {
        });

        Button evolutionButton = new Button("Evolution");
        evolutionButton.setOnAction(event -> {
            System.out.println("Evolution");
        });

        int walletAmount = 100;
        Text walletText = new Text("Wallet: " + walletAmount);
        walletText.setFill(Color.BLACK);

        HBox hbox = new HBox(refreshButton, arbitrageButton, evolutionButton, walletText);
        hbox.setAlignment(Pos.CENTER);
        hbox.setSpacing(10);
        hbox.setPadding(new Insets(20));
        VBox vbox = new VBox(gridPane, hbox);
        vbox.setSpacing(10);
        vbox.setPadding(new Insets(20));

        Scene scene = new Scene(vbox, 700, 600);
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void displayData() {
        int numCurrencies = currency.size();
        for (int i = 0; i < numCurrencies; i++) {
            for (int j = 0; j < numCurrencies; j++) {
                Rectangle rectangle = new Rectangle(100, 100);
                rectangle.setFill(Color.LIGHTGRAY);

                String currencyPair = currency.keySet().toArray()[i] + "-" + currency.keySet().toArray()[j];
                Text numberText = new Text(String.valueOf(rates.get(currencyPair)));
                numberText.setFill(Color.BLACK);

                StackPane stackPane = new StackPane(rectangle, numberText);
                gridPane.add(stackPane, j + 1, i + 1);
            }
        }
    }
}
