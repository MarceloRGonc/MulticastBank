package Client;

import java.util.Random;

/**
 * Tester that creates an account and performs transfer, deposit and withdrawal operations
 * At the end presents the expected and the real balance
 * */
public class SimpleTester {

    public static void main(String[] args) {
        /** Number of operations to perform */
        int n = 500;

        BankStub bank = new BankStub();
        int source = bank.createAccount("1234");
        int dest = bank.createAccount("1234");
        bank.setAccountIdNumber(source);

        /** Balance counter */
        float moves = 0;
        Random rand = new Random();

        for (int i = 0; i < n; i++) {
            switch(rand.nextInt(2)){

                /** Deposit or withdraw */
                case 0:
                    int value = rand.nextInt(10000)-5000;
                    boolean status = bank.move(value, null);
                    if (status) { moves += value; }
                    break;

                /** Transfer */
                case 1:
                    value = rand.nextInt(100)+1;
                    status = bank.transfer(source, dest, value, null);
                    if (status){ moves -= value; }
                    break;

                default:
                    System.out.println("Bad option");
            }
        }
        System.out.println("Expected balance: " + moves);
        System.out.println("Real balance: " + bank.getBalance(source));

    }
}
