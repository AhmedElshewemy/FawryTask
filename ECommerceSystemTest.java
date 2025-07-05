import java.time.LocalDate;
import java.util.*;

// Base Product class
abstract class Product {
    private String name;
    private double price;
    private int quantity;
    
    public Product(String name, double price, int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }
    
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    
    public void reduceQuantity(int amount) {
        if (amount > quantity) {
            throw new IllegalArgumentException("Cannot reduce quantity by more than available stock");
        }
        this.quantity -= amount;
    }
    
    public abstract boolean isExpired();
    public abstract boolean requiresShipping();
    public abstract double getWeight(); // Returns 0 for non-shippable items
}

// Perishable products that can expire
class PerishableProduct extends Product {
    private LocalDate expirationDate;
    private double weight;
    
    public PerishableProduct(String name, double price, int quantity, LocalDate expirationDate, double weight) {
        super(name, price, quantity);
        this.expirationDate = expirationDate;
        this.weight = weight;
    }
    
    @Override
    public boolean isExpired() {
        return LocalDate.now().isAfter(expirationDate);
    }
    
    @Override
    public boolean requiresShipping() {
        return true; // Perishable items typically require shipping
    }
    
    @Override
    public double getWeight() {
        return weight;
    }
}

// Non-perishable products that require shipping
class ShippableProduct extends Product {
    private double weight;
    
    public ShippableProduct(String name, double price, int quantity, double weight) {
        super(name, price, quantity);
        this.weight = weight;
    }
    
    @Override
    public boolean isExpired() {
        return false; // Non-perishable items don't expire
    }
    
    @Override
    public boolean requiresShipping() {
        return true;
    }
    
    @Override
    public double getWeight() {
        return weight;
    }
}

// Digital products that don't require shipping
class DigitalProduct extends Product {
    public DigitalProduct(String name, double price, int quantity) {
        super(name, price, quantity);
    }
    
    @Override
    public boolean isExpired() {
        return false; // Digital products don't expire
    }
    
    @Override
    public boolean requiresShipping() {
        return false;
    }
    
    @Override
    public double getWeight() {
        return 0; // Digital products have no weight
    }
}

// Interface for shipping service
interface Shippable {
    String getName();
    double getWeight();
}

// Cart item to track product and quantity
class CartItem {
    private Product product;
    private int quantity;
    
    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }
    
    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }
    public double getTotalPrice() { return product.getPrice() * quantity; }
}

// Customer class
class Customer {
    private String name;
    private double balance;
    
    public Customer(String name, double balance) {
        this.name = name;
        this.balance = balance;
    }
    
    public String getName() { return name; }
    public double getBalance() { return balance; }
    
    public void deductBalance(double amount) {
        if (amount > balance) {
            throw new IllegalArgumentException("Insufficient balance");
        }
        this.balance -= amount;
    }
}

// Shopping cart
class Cart {
    private List<CartItem> items = new ArrayList<>();
    
    public void add(Product product, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (quantity > product.getQuantity()) {
            throw new IllegalArgumentException("Requested quantity exceeds available stock");
        }
        
        // Check if product already exists in cart
        for (CartItem item : items) {
            if (item.getProduct().equals(product)) {
                int newQuantity = item.getQuantity() + quantity;
                if (newQuantity > product.getQuantity()) {
                    throw new IllegalArgumentException("Total quantity in cart exceeds available stock");
                }
                items.remove(item);
                items.add(new CartItem(product, newQuantity));
                return;
            }
        }
        
        items.add(new CartItem(product, quantity));
    }
    
    public List<CartItem> getItems() { return new ArrayList<>(items); }
    public boolean isEmpty() { return items.isEmpty(); }
    public void clear() { items.clear(); }
}

// Shipping service adapter
class ShippingItem implements Shippable {
    private String name;
    private double weight;
    
    public ShippingItem(String name, double weight) {
        this.name = name;
        this.weight = weight;
    }
    
    @Override
    public String getName() { return name; }
    
    @Override
    public double getWeight() { return weight; }
}

// Shipping service
class ShippingService {
    private static final double SHIPPING_RATE_PER_KG = 10.0; // $10 per kg
    
    public static double calculateShippingFee(List<Shippable> items) {
        if (items.isEmpty()) return 0;
        
        double totalWeight = items.stream()
            .mapToDouble(Shippable::getWeight)
            .sum();
        
        System.out.println("** Shipment notice **");
        for (Shippable item : items) {
            System.out.printf("1x %s        %.0fg%n", item.getName(), item.getWeight() * 1000);
        }
        System.out.printf("Total package weight %.1fkg%n", totalWeight);
        
        return totalWeight * SHIPPING_RATE_PER_KG;
    }
}

// E-commerce system
class ECommerceSystem {
    public static void checkout(Customer customer, Cart cart) {
        // Validate cart is not empty
        if (cart.isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }
        
        // Validate stock and expiration
        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            if (item.getQuantity() > product.getQuantity()) {
                throw new IllegalArgumentException("Product " + product.getName() + " is out of stock");
            }
            if (product.isExpired()) {
                throw new IllegalArgumentException("Product " + product.getName() + " has expired");
            }
        }
        
        // Calculate subtotal
        double subtotal = cart.getItems().stream()
            .mapToDouble(CartItem::getTotalPrice)
            .sum();
        
        // Collect shippable items
        List<Shippable> shippableItems = new ArrayList<>();
        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            if (product.requiresShipping()) {
                for (int i = 0; i < item.getQuantity(); i++) {
                    shippableItems.add(new ShippingItem(product.getName(), product.getWeight()));
                }
            }
        }
        
        // Calculate shipping fee
        double shippingFee = ShippingService.calculateShippingFee(shippableItems);
        double totalAmount = subtotal + shippingFee;
        
        // Validate customer balance
        if (customer.getBalance() < totalAmount) {
            throw new IllegalArgumentException("Customer's balance is insufficient");
        }
        
        // Process payment and update stock
        customer.deductBalance(totalAmount);
        for (CartItem item : cart.getItems()) {
            item.getProduct().reduceQuantity(item.getQuantity());
        }
        
        // Print checkout receipt
        System.out.println("** Checkout receipt **");
        for (CartItem item : cart.getItems()) {
            System.out.printf("%dx %s        %.0f%n", 
                item.getQuantity(), 
                item.getProduct().getName(), 
                item.getTotalPrice());
        }
        System.out.println("- ---------------------");
        System.out.printf("Subtotal         %.0f%n", subtotal);
        System.out.printf("Shipping         %.0f%n", shippingFee);
        System.out.printf("Amount           %.0f%n", totalAmount);
        System.out.printf("Customer balance after payment: %.0f%n", customer.getBalance());
        
        // Clear cart after successful checkout
        cart.clear();
    }
}

// Test class with comprehensive examples
public class ECommerceSystemTest {
    public static void main(String[] args) {
        // Create products
        Product cheese = new PerishableProduct("Cheese", 100, 10, LocalDate.now().plusDays(7), 0.2);
        Product biscuits = new PerishableProduct("Biscuits", 150, 5, LocalDate.now().plusDays(30), 0.7);
        Product tv = new ShippableProduct("TV", 500, 3, 15.0);
        Product mobile = new ShippableProduct("Mobile", 800, 5, 0.3);
        Product scratchCard = new DigitalProduct("Mobile Scratch Card", 25, 100);
        Product expiredMilk = new PerishableProduct("Expired Milk", 50, 2, LocalDate.now().minusDays(1), 1.0);
        
        // Create customers
        Customer customer1 = new Customer("John Doe", 2000);
        Customer customer2 = new Customer("Jane Smith", 100);
        
        System.out.println("=== Test Case 1: Successful Checkout ===");
        try {
            Cart cart1 = new Cart();
            cart1.add(cheese, 2);
            cart1.add(biscuits, 1);
            cart1.add(scratchCard, 1);
            ECommerceSystem.checkout(customer1, cart1);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        
        System.out.println("\n=== Test Case 2: Mixed Products with Shipping ===");
        try {
            Cart cart2 = new Cart();
            cart2.add(tv, 1);
            cart2.add(mobile, 2);
            cart2.add(scratchCard, 3);
            ECommerceSystem.checkout(customer1, cart2);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        
        System.out.println("\n=== Test Case 3: Empty Cart ===");
        try {
            Cart emptyCart = new Cart();
            ECommerceSystem.checkout(customer1, emptyCart);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        
        System.out.println("\n=== Test Case 4: Insufficient Balance ===");
        try {
            Cart cart3 = new Cart();
            cart3.add(tv, 2);
            ECommerceSystem.checkout(customer2, cart3);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        
        System.out.println("\n=== Test Case 5: Out of Stock ===");
        try {
            Cart cart4 = new Cart();
            cart4.add(cheese, 20); // Only 8 left after first purchase
            ECommerceSystem.checkout(customer1, cart4);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        
        System.out.println("\n=== Test Case 6: Expired Product ===");
        try {
            Cart cart5 = new Cart();
            cart5.add(expiredMilk, 1);
            ECommerceSystem.checkout(customer1, cart5);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        
        System.out.println("\n=== Test Case 7: Digital Products Only (No Shipping) ===");
        try {
            Cart cart6 = new Cart();
            cart6.add(scratchCard, 5);
            ECommerceSystem.checkout(customer1, cart6);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        
        System.out.println("\n=== Final Stock Check ===");
        System.out.println("Cheese remaining: " + cheese.getQuantity());
        System.out.println("Biscuits remaining: " + biscuits.getQuantity());
        System.out.println("TV remaining: " + tv.getQuantity());
        System.out.println("Mobile remaining: " + mobile.getQuantity());
        System.out.println("Scratch Card remaining: " + scratchCard.getQuantity());
    }
}