package foodorderapp;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.*;

public class SummaryController {

    @FXML private TableView<OrderSummary> summaryTable;
    @FXML private TableColumn<OrderSummary, String> nameColumn;
    @FXML private TableColumn<OrderSummary, Double> priceColumn;
    @FXML private TableColumn<OrderSummary, String> timeColumn;

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));

        summaryTable.setItems(loadTodaysOrders());

        // ✅ This will apply light lavender background to rows
        summaryTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(OrderSummary item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else {
                    setStyle("-fx-background-color: #E6E6FA;");
                }
            }
        });
    }

    private ObservableList<OrderSummary> loadTodaysOrders() {
        ObservableList<OrderSummary> summaries = FXCollections.observableArrayList();
        String sql = """
            SELECT o.customer_name, SUM(m.price * d.quantity) AS total_price, o.order_time
            FROM orders o
            JOIN order_details d ON o.id = d.order_id
            JOIN menu_items m ON d.menu_item_id = m.id
            WHERE DATE(o.order_time) = CURDATE()
            GROUP BY o.id
        """;

        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                summaries.add(new OrderSummary(
                        rs.getString("customer_name"),
                        rs.getDouble("total_price"),
                        rs.getTimestamp("order_time").toLocalDateTime().toString()
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return summaries;
    }

    @FXML
    private void deleteSelectedOrder() {
        OrderSummary selectedOrder = summaryTable.getSelectionModel().getSelectedItem();

        if (selectedOrder == null) {
            showAlert("No Selection", "Please select an order to delete.");
            return;
        }

        String findOrderIdSql = """
            SELECT o.id FROM orders o
            WHERE o.customer_name = ? AND DATE(o.order_time) = CURDATE()
            ORDER BY o.order_time DESC LIMIT 1
        """;
        String deleteDetailsSql = "DELETE FROM order_details WHERE order_id = ?";
        String deleteOrderSql = "DELETE FROM orders WHERE id = ?";

        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement findStmt = conn.prepareStatement(findOrderIdSql)) {

            findStmt.setString(1, selectedOrder.getName());

            try (ResultSet rs = findStmt.executeQuery()) {
                if (rs.next()) {
                    int orderId = rs.getInt("id");

                    try (PreparedStatement delDetailsStmt = conn.prepareStatement(deleteDetailsSql);
                         PreparedStatement delOrderStmt = conn.prepareStatement(deleteOrderSql)) {

                        delDetailsStmt.setInt(1, orderId);
                        delDetailsStmt.executeUpdate();

                        delOrderStmt.setInt(1, orderId);
                        delOrderStmt.executeUpdate();

                        summaryTable.getItems().remove(selectedOrder);
                        showAlert("Order Deleted", "The selected order has been successfully deleted.");
                    }
                } else {
                    showAlert("Not Found", "Could not find order in database.");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Database error: " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

