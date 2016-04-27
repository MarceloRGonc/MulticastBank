package Client;

import java.util.Random;

public class Tester {

    public static void main(String[] args) {
        BankStub bank = new BankStub();

        int r = bank.createAccount("1234");
        bank.setAccountIdNumber(r);

        float moves = 0;
        for (int i = 0; i < 500; i++) {
            Random rand = new Random();

            int value = rand.nextInt(10000)-5000;

            boolean status = bank.move(value, null);

            if (status) {
                moves += value;
            }
        }
        System.out.println("Bank balance: " + bank.getBalance(r));
        System.out.println("Expected balance:: " + moves);
        bank.leave(r);
        return ;
    }
}
