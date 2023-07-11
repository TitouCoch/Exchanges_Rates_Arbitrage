import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.Map;

public class Main extends Application {
    private Map<String, Method.CurrencyData> currencyRate;

    public void setCurrencyRate(Map<String, Method.CurrencyData> currencyRate) {
        this.currencyRate = currencyRate;
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Exchanges Rates Arbitrage");

        GridPane gridPane = createGridPane();
        HBox buttonsBox = createButtonsBox();

        VBox vbox = new VBox(gridPane, buttonsBox);
        vbox.setSpacing(10);
        vbox.setPadding(new Insets(20));

        Scene scene = new Scene(vbox, 500, 500);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private GridPane createGridPane() {
        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        if (currencyRate != null) {
            // Ajoutez les devises à la première ligne et la première colonne
            int row = 1;
            int col = 1;
            for (String currency : currencyRate.keySet()) {
                Label currencyLabel = new Label(currency);
                gridPane.add(currencyLabel, col, 0); // Première ligne
                gridPane.add(currencyLabel, 0, row); // Première colonne
                col++;
                row++;
            }

            // Ajoutez les taux de change dans le reste du tableau
            row = 1;
            for (String currencyRow : currencyRate.keySet()) {
                col = 1;
                for (String currencyCol : currencyRate.keySet()) {
                    float exchangeRate = getExchangeRate(currencyRow, currencyCol); // Obtenez le taux de change entre les deux devises
                    Label rateLabel = new Label(String.valueOf(exchangeRate));
                    gridPane.add(rateLabel, col, row);
                    col++;
                }
                row++;
            }
        }

        return gridPane;
    }

    private float getExchangeRate(String currencyRow, String currencyCol) {
        // Récupérez le taux de change de la map currencyRate en utilisant les devises currencyRow et currencyCol
        if (currencyRow.equals(currencyCol)) {
            return 1.0f;
        } else {
            // Logique pour obtenir le taux de change entre les deux devises
            // Remplacez cette partie avec votre logique spécifique
            return 0.0f;
        }
    }

    private HBox createButtonsBox() {
        Button refreshButton = new Button("Refresh Data");
        Button arbitrageButton = new Button("Arbitrage");

        HBox hbox = new HBox(refreshButton, arbitrageButton);
        hbox.setSpacing(10);
        hbox.setAlignment(Pos.CENTER);

        return hbox;
    }

    public static void main(String[] args) {
        Method method = new Method();
        method.loadData("BTC");
        launch(args);
    }
}
