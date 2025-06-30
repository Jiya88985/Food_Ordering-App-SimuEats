package foodorderapp;

public class Order {
    private String customerName;
    private String customerPhone;
    private double totalAmount;

    public Order(String customerName, String customerPhone, double totalAmount) {
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.totalAmount = totalAmount;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public double getTotalAmount() {
        return totalAmount;
    }
}