package strategy;

public class LUNACardStrategy implements PaymentStrategy {
    public String emailId;
    public String password;

    public LUNACardStrategy(String emailId, String password) {
        this.emailId = emailId;
        this.password = password;
    }

    @Override
    public void pay(int amount) {
        System.out.println(amount + " paid using LUNACard.");
    }
}
