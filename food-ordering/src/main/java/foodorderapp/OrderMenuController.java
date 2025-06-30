package foodorderapp;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class OrderMenuController {
    @FXML
    private TextField nameField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField addressField;
    @FXML
    private Label orderSummaryLabel;
    @FXML
    private javafx.scene.control.Button placeOrderButton;

    private List<MenuItem> orderItems;

    public void setOrderItems(List<MenuItem> orderItems) {
        this.orderItems = orderItems;
    }

    @FXML
    private void placeOrder() {
        String name = nameField.getText();
        String phone = phoneField.getText();
        String address = addressField.getText();
        // Save order to MySQL
        try (Connection conn = MySQLConnection.getConnection()) {
            String sql = "INSERT INTO orders (customer_name, phone, address) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, name);
                stmt.setString(2, phone);
                stmt.setString(3, address);
                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    // Get generated order ID
                    int orderId = -1;
                    try (java.sql.ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            orderId = generatedKeys.getInt(1);
                        }
                    }
                    // Insert order items and menu items if not exist
                    if (orderItems != null && orderId != -1) {
                        for (MenuItem item : orderItems) {
                            // Insert menu item if it doesn't exist
                            String menuCheckSql = "SELECT id FROM menu_items WHERE name = ? LIMIT 1";
                            int menuItemId = -1;
                            try (PreparedStatement checkStmt = conn.prepareStatement(menuCheckSql)) {
                                checkStmt.setString(1, item.getName());
                                try (java.sql.ResultSet rs = checkStmt.executeQuery()) {
                                    if (rs.next()) {
                                        menuItemId = rs.getInt("id");
                                    } else {
                                        // Insert new menu item
                                        String insertMenuSql = "INSERT INTO menu_items (name, price) VALUES (?, ?)";
                                        try (PreparedStatement insertMenuStmt = conn.prepareStatement(insertMenuSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                                            insertMenuStmt.setString(1, item.getName());
                                            insertMenuStmt.setDouble(2, item.getPrice());
                                            insertMenuStmt.executeUpdate();
                                            try (java.sql.ResultSet genMenuKeys = insertMenuStmt.getGeneratedKeys()) {
                                                if (genMenuKeys.next()) {
                                                    menuItemId = genMenuKeys.getInt(1);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            // Insert into order_details
                            if (menuItemId != -1) {
                                String itemSql = "INSERT INTO order_details (order_id, menu_item_id, quantity) VALUES (?, ?, ?)";
                                try (PreparedStatement itemStmt = conn.prepareStatement(itemSql)) {
                                    itemStmt.setInt(1, orderId);
                                    itemStmt.setInt(2, menuItemId);
                                    itemStmt.setInt(3, item.getQuantity());
                                    itemStmt.executeUpdate();
                                }
                            }
                        }
                    }
                    // Show attractive pop-up message with icon and styled text
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("🎉 Order Success!");
                    alert.setHeaderText("Order Placed Successfully!");
                    alert.setContentText("\uD83C\uDF89 Thank you, " + name + "! Your order has been placed.\n\uD83D\uDC4D We appreciate your business!\nEnjoy your meal! 🍽️");
                    Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
                    alertStage.getScene().getRoot().setStyle("-fx-font-size: 16px; -fx-font-family: 'Segoe UI', Arial, sans-serif;");
                    alert.showAndWait();
                    orderSummaryLabel.setText("\uD83C\uDF89 Order placed successfully for " + name + "!\nThank you for your order! 🍽️");
                    orderSummaryLabel.setStyle("-fx-text-fill: #219150; -fx-font-weight: bold; -fx-font-size: 18px; -fx-background-color: #eaffea; -fx-padding: 10px; -fx-border-radius: 8px; -fx-background-radius: 8px;");
                } else {
                    orderSummaryLabel.setText("Failed to place order for " + name);
                }
            }
        } catch (SQLException e) {
            orderSummaryLabel.setText("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void filterItems(KeyEvent event) {
        // Implement filtering logic here
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
    public void initialize() {
        placeOrderButton.setDisable(true);
        nameField.textProperty().addListener((obs, oldVal, newVal) -> validateFields());
        phoneField.textProperty().addListener((obs, oldVal, newVal) -> validateFields());
        addressField.textProperty().addListener((obs, oldVal, newVal) -> validateFields());
    }

    private void validateFields() {
        boolean disable = nameField.getText().trim().isEmpty() ||
                          phoneField.getText().trim().isEmpty() ||
                          addressField.getText().trim().isEmpty();
        placeOrderButton.setDisable(disable);
    }
    @FXML
    private void showTodaysSummary(ActionEvent event) {
        try {
            Parent summaryRoot = FXMLLoader.load(getClass().getResource("/todays_summary.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Today's Order Summary");
            stage.setScene(new Scene(summaryRoot));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}