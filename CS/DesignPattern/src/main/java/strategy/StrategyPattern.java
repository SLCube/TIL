package strategy;

public class StrategyPattern {
    public static void main(String[] args) {
        ShoppingCart shoppingCart = new ShoppingCart();

        Item A = new Item("SLCubeA", 100);
        Item B = new Item("SLCubeB", 300);

        shoppingCart.addItem(A);
        shoppingCart.addItem(B);

        // pay by LunaCard
        shoppingCart.pay(new LUNACardStrategy("SLCube@example.com", "HelloSLCube"));

        // pay by KAKAOCard
        shoppingCart.pay(new KAKAOCardStrategy("SLCube", "1234567", "123", "10/01"));

    }
}