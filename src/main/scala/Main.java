import javafx.application.Application;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import scala.Option;
import scala.Tuple2;
import scala.collection.JavaConverters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class Main extends Application {
    private Map<String, Method.CurrencyData> currency;
    private Map<String, Object> rates;
    private GridPane gridPane;
    private Float walletAmount;
    AtomicReference<Text> walletText = new AtomicReference<>();
    private List<String> lastTransactionList = new ArrayList<>();
    public static void main(String[] args) {
        launch(args);
    }
    private XYChart.Series<Number, Number> profitSeries;
    private LineChart<Number, Number> lineChart;


    @Override
    public void start(Stage primaryStage) {
        Method.loadData("BTC");
        currency = JavaConverters.mapAsJavaMap(Method.getCurrencyRate());
        rates = JavaConverters.mapAsJavaMap(Method.getRatesMap());
        loadGrid();
        lineChart = createWalletEvolutionChart();
        displayData();


        primaryStage.setTitle("Exchange Rates Arbitrage");

        Button refreshButton = createStyledButton("Refresh Data");
        refreshButton.setOnAction(event -> displayData());

        Button arbitrageButton = createStyledButton("Arbitrage");
        arbitrageButton.setOnAction(event -> {
            Option<Tuple2<Object, scala.collection.immutable.List<String>>> arbitrageResult = Method.runArbitrage("BTC", walletAmount);
            lastTransactionList = JavaConverters.asJava(arbitrageResult.get()._2);
            float profit = (Float) arbitrageResult.get()._1;

            walletAmount += profit;
            walletText.get().setText("Profit : " + profit);
            addToProfitSeries(walletAmount);
        });

        Button showLastTransaction = createStyledButton("Last Transaction");
        showLastTransaction.setOnAction(event -> displayLastTransactionList());

        Button openChartButton = createStyledButton("Wallet Evolution");
        openChartButton.setOnAction(event -> openChartWindow(profitSeries));

        walletText = new AtomicReference<>(new Text("Profit : " + 0.0f));
        walletText.get().setFont(Font.font("Arial", FontWeight.BOLD, 22));
        walletText.get().setFill(Color.GREEN);
        walletText.get().setTranslateX(25);

        HBox hbox = new HBox(10, refreshButton, arbitrageButton, showLastTransaction, openChartButton, walletText.get());
        hbox.setAlignment(Pos.CENTER);
        hbox.setPadding(new Insets(20));

        VBox vbox = new VBox(10, gridPane, hbox);
        vbox.setPadding(new Insets(20));

        Scene scene = new Scene(vbox, 800, 650);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public void displayData() {
        Method.loadData("BTC");
        rates = JavaConverters.mapAsJavaMap(Method.getRatesMap());
        int numCurrencies = currency.size();
        for (int i = 0; i < numCurrencies; i++) {
            for (int j = 0; j < numCurrencies; j++) {
                Rectangle rectangle = new Rectangle(100, 100);
                rectangle.setFill(Color.LIGHTGRAY);

                String currencyPair = currency.keySet().toArray()[i] + "-" + currency.keySet().toArray()[j];
                double price = Double.parseDouble(rates.get(currencyPair).toString());

                Label label = new Label(String.valueOf(price));
                label.setFont(Font.font("Arial"));
                label.setTextFill(Color.BLACK);

                GridPane.setHalignment(label, HPos.CENTER);
                GridPane.setMargin(label, new Insets(5));

                gridPane.add(rectangle, j + 1, i + 1);
                gridPane.add(label, j + 1, i + 1);
            }
        }
    }

    private void loadGrid() {
        gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(30, 0, 0, 0));


        Text currencyText = new Text("Currency");
        currencyText.setTextAlignment(TextAlignment.CENTER);
        currencyText.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        GridPane.setHalignment(currencyText, HPos.CENTER);
        GridPane.setMargin(currencyText, new Insets(5));
        gridPane.add(currencyText, 0, 0);

        walletAmount = 100.0f;
        int column = 1;
        int row = 1;
        for (String currencyName : currency.keySet()) {
            Text textColumn = new Text(currencyName);
            textColumn.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            GridPane.setHalignment(textColumn, HPos.CENTER);
            GridPane.setMargin(textColumn, new Insets(5));
            gridPane.add(textColumn, column, 0);

            Text textRow = new Text(currencyName);
            textRow.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            GridPane.setHalignment(textRow, HPos.CENTER);
            GridPane.setMargin(textRow, new Insets(5));
            gridPane.add(textRow, 0, row);

            column++;
            row++;
        }
    }

    private void displayLastTransactionList() {
        Stage stage = new Stage();
        stage.setTitle("Last Transaction List");

        GridPane transactionGridPane = new GridPane();
        transactionGridPane.setAlignment(Pos.CENTER);
        transactionGridPane.setHgap(10);
        transactionGridPane.setVgap(10);
        transactionGridPane.setStyle("-fx-background-color: #F0F0F0; -fx-padding: 20px;");

        Label titleLabel = new Label("Last Transaction List");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        GridPane.setHalignment(titleLabel, HPos.CENTER);
        GridPane.setMargin(titleLabel, new Insets(10));
        transactionGridPane.add(titleLabel, 0, 0, 2, 1);

        Label indexLabel = new Label("Index");
        indexLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        GridPane.setHalignment(indexLabel, HPos.CENTER);
        GridPane.setMargin(indexLabel, new Insets(5));
        transactionGridPane.add(indexLabel, 0, 1);

        Label transactionLabel = new Label("Transaction");
        transactionLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        GridPane.setHalignment(transactionLabel, HPos.CENTER);
        GridPane.setMargin(transactionLabel, new Insets(5));
        transactionGridPane.add(transactionLabel, 1, 1);

        for (int i = 0; i < lastTransactionList.size(); i++) {
            Label indexValueLabel = new Label(String.valueOf(i + 1));
            indexValueLabel.setFont(Font.font("Arial"));
            GridPane.setHalignment(indexValueLabel, HPos.CENTER);
            GridPane.setMargin(indexValueLabel, new Insets(5));
            transactionGridPane.add(indexValueLabel, 0, i + 2);

            Label transactionValueLabel = new Label(lastTransactionList.get(i));
            transactionValueLabel.setFont(Font.font("Arial"));
            GridPane.setHalignment(transactionValueLabel, HPos.CENTER);
            GridPane.setMargin(transactionValueLabel, new Insets(5));
            transactionGridPane.add(transactionValueLabel, 1, i + 2);
        }

        Scene scene = new Scene(transactionGridPane, 300, 250);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    private LineChart<Number, Number> createWalletEvolutionChart() {
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Transactions");
        yAxis.setLabel("Profit");

        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Wallet Evolution");

        lineChart.setLegendVisible(false);
        lineChart.setAnimated(false);
        lineChart.setStyle("-fx-background-color: #F0F0F0;");

        profitSeries = new XYChart.Series<>();
        profitSeries.setName("Profit");
        lineChart.getData().add(profitSeries);

        return lineChart;
    }

    private void addToProfitSeries(float profit) {
        int transactionCount = profitSeries.getData().size() + 1;
        profitSeries.getData().add(new XYChart.Data<>(transactionCount, walletAmount + profit));
    }

    private Button createStyledButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: Black; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8px 16px;");
        return button;
    }

    private void openChartWindow(XYChart.Series<Number, Number> series) {
        Stage chartStage = new Stage();
        chartStage.setTitle("Wallet Evolution Chart");

        LineChart<Number, Number> chartWindowLineChart = lineChart;
        chartWindowLineChart.setLegendVisible(false);

        XYChart.Series<Number, Number> chartWindowSeries = new XYChart.Series<>();
        chartWindowSeries.setName("Profit");

        for (XYChart.Data<Number, Number> data : series.getData()) {
            chartWindowSeries.getData().add(new XYChart.Data<>(data.getXValue(), data.getYValue()));
        }
        chartWindowLineChart.getData().add(chartWindowSeries);

        VBox chartWindowLayout = new VBox(10, chartWindowLineChart);
        chartWindowLayout.setPadding(new Insets(20));

        Scene chartScene = new Scene(chartWindowLayout, 500, 400);
        chartStage.setScene(chartScene);
        chartStage.setResizable(false);
        chartStage.show();
    }
}
