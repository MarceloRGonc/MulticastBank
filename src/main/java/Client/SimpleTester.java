package Client;

import java.util.Random;

/**
 *
 * Tester que cria apenas uma conta
 *
 *
 * */
public class SimpleTester {

        public static void main(String[] args) {
            BankStub bank = new BankStub();
            int n = 500;

            int r = bank.createAccount("1234");
            bank.setAccountIdNumber(r);

            Random rand = new Random();

            float moves = 0;

            long before = System.currentTimeMillis();

            for (int i = 0; i < n; i++) {

                switch(rand.nextInt(2)){

                    //Deposit or withdraw
                    case 0:

                        int value = rand.nextInt(10000)-5000;
                        boolean status = bank.move(value, null);

                        if (status) {
                            moves += value;
                        }

                        break;

                    // Transfer
                    case 1:
                        value = rand.nextInt(100)+1;
                        status = bank.transfer(r,1,value,null);

                        if (status){
                            moves -= value;
                        }
                        break;
                    default:
                        System.out.println("Bad option");
                }
            }

            long after = System.currentTimeMillis();

            long delta = after - before;
            System.out.println("Throughput: "+ (n/((delta)/1000d)));
        }
}
