package foodorderapp;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.SelectionMode;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javafx.scene.control.TextInputDialog;
import java.util.Optional;

public class FoodMenuController {

    @FXML
    private ComboBox<String> foodComboBox;
    @FXML
    private Spinner<Integer> quantitySpinner;
    @FXML
    private TableView<MenuItem> orderTable;
    @FXML
    private TableColumn<MenuItem, String> itemNameColumn;
    @FXML
    private TableColumn<MenuItem, Integer> quantityColumn;
    @FXML
    private TableColumn<MenuItem, Double> totalColumn;
    @FXML
    private Text totalText;

    public void initialize() {
        // Fill ComboBox with food items
        foodComboBox.getItems().addAll(
            "Pizza", "Burger", "Pasta", "Spaghetti", "Fries", "Sandwich","Salad", "Nuggets",
            "Coke", "Juice", "Water", "Ice Cream", "Cake","Coffee"
        );

        // Setup TableColumn cell value factories
        itemNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));

        // Initialize Spinner with integer values from 1 to 100, default 1
        quantitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1));

        // Enable single selection mode for the table
        orderTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        // Initialize total text
        totalText.setText("Total: PKR 0.00");
    }

    @FXML
    private void addItem(ActionEvent event) {
        String food = foodComboBox.getValue();
        Integer quantityValue = quantitySpinner.getValue();

        if (food == null || quantityValue == null) {
            System.out.println("Please select a food item and quantity.");
            return;
        }

        double price = getPrice(food);
        MenuItem item = new MenuItem(food, quantityValue, price);

        orderTable.getItems().add(item);

        // Auto select the newly added item
        orderTable.getSelectionModel().select(item);

        updateTotal();
    }

    @FXML
    private void removeItem(ActionEvent event) {
        MenuItem selectedItem = orderTable.getSelectionModel().getSelectedItem();

        if (selectedItem != null) {
            orderTable.getItems().remove(selectedItem);
            updateTotal();
        } else {
            System.out.println("No item selected to remove");
        }
    }

    private void updateTotal() {
        double total = orderTable.getItems().stream().mapToDouble(MenuItem::getTotal).sum();
        totalText.setText(String.format("Total: PKR %.2f", total));
    }

    private double getPrice(String food) {
        switch (food) {
            case "Pizza": return 2500.0;
            case "Burger": return 850.0;
            case "Pasta": return 1400.0;
            case "Spaghetti": return 1300.0;
            case "Fries": return 450.0;
            case "Sandwich": return 700.0;
            case "Salad":return 600.0;
            case "Nuggets": return 750.0;
            case "Coke": return 180.0;
            case "Juice": return 250.0;
            case "Water": return 100.0;
            case "Ice Cream": return 300.0;
            case "Cake": return 500.0;
            case "Coffee": return 300.0; 
            default: return 0;
        }
    }

    @FXML
    private void goToMainMenu(ActionEvent event) {
        try {
            Parent mainRoot = FXMLLoader.load(getClass().getResource("/main.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(mainRoot));
            stage.setTitle("Main Menu");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void saveOrderToDatabase() {
        // This method is now disabled since 'Place Order' is handled from the main menu.
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Order Placement Disabled");
        alert.setHeaderText(null);
        alert.setContentText("Order placement is now available only from the main menu. Please use the main menu to place your order.");
        alert.showAndWait();
    }

    @FXML
    private void placeOrder(ActionEvent event) {
        if (orderTable.getItems().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Items");
            alert.setHeaderText(null);
            alert.setContentText("Please add at least one item to your order before placing it.");
            alert.showAndWait();
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/OrderMenu.fxml"));
            Parent orderMenuRoot = loader.load();
            // Pass order items to OrderMenuController
            OrderMenuController orderMenuController = loader.getController();
            orderMenuController.setOrderItems(orderTable.getItems());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(orderMenuRoot));
            stage.setTitle("Place Order");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Could not open the order form.");
            alert.showAndWait();
        }
    }
}