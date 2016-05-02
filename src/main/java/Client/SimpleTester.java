package Client;

import java.util.Random;

/**
 *
 *
 *
 *
 * */
public class SimpleTester {
        public static void main(String[] args) {
            BankStub bank = new BankStub();

            int r = bank.createAccount("1234");
            bank.setAccountIdNumber(r);

            Random rand = new Random();
            int opcaoEscolhida;

            float moves = 0;

            for (int i = 0; i < 500; i++) {

                opcaoEscolhida = rand.nextInt(2);

                switch(opcaoEscolhida){

                    //Deposit or withdraw
                    case 1:

                        int value = rand.nextInt(10000)-5000;
                        boolean status = bank.move(value, null);

                        if (status) {
                            moves += value;
                        }

                        break;

                    // Transfer
                    case 2:
                        value = rand.nextInt(500);
                        status = bank.transfer(r,r-1,value,null);

                        if (status){
                            moves += value;
                        }
                        break;
                }
            }
            System.out.println("Bank balance: " + bank.getBalance(r));
            System.out.println("Expected balance:: " + moves);
            bank.leave(r);
            return ;
        }
}
