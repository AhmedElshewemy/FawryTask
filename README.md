# FawryTask
FawryTask for interview

A simple Java-based e-commerce system for interview demonstration.

## Features

- Supports perishable, shippable, and digital products
- Handles product expiration and stock management
- Calculates shipping fees for physical products
- Simulates customer checkout with balance validation
- Provides comprehensive test cases

## Requirements

- used Java 17 

## How to Run

1. Compile the code:
   ```sh
   javac ecommerce_system.java
   ```

2. Run the test class:
   ```sh
   java ECommerceSystemTest
   ```

## Example Output

The program will run several test cases, including successful checkouts, insufficient balance, expired products, and more. Example output:

```
=== Test Case 1: Successful Checkout ===
** Shipment notice **
1x Cheese        200g
1x Biscuits        700g
Total package weight 0.9kg
** Checkout receipt **
2x Cheese        200
1x Biscuits        150
1x Mobile Scratch Card        25
- ---------------------
Subtotal         375
Shipping         9
Amount           384
Customer balance after payment: 1616
...
```

## Notes

- All logic is contained in a single file for simplicity.
- See `ecommerce_system.java` for implementation details and test scenarios.
