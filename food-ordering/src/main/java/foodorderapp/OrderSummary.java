package foodorderapp;

public class OrderSummary {
    private String name;
    private double price;
    private String time;

    public OrderSummary(String name, double price, String time) {
        this.name = name;
        this.price = price;
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public String getTime() {
        return time;
    }
}

