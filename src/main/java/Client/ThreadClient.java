package Client;

import java.util.Random;

public class ThreadClient implements Runnable {
    private final int accountNumber;

    private static Random r = new Random();

    public ThreadClient(int accountNumber) {
        this.accountNumber = accountNumber;
    }

    @Override
    public void run() {
        try {
            BankStub bank = new BankStub();

            bank.setAccountIdNumber(accountNumber);

            Thread.sleep(100);

            while(true){

                long before = System.currentTimeMillis();

                switch(r.nextInt(3 - 1 ) + 1) {
                    /** Movement */
                    case 1:
                        int value = r.nextInt(10000)-5000;
                        boolean status = bank.move(value, null);

                        if (status) {
                            int sourceValue = MultipleTester.accounts.get(accountNumber) + value;
                            MultipleTester.accounts.put(accountNumber, sourceValue);
                        }
                        break;

                    /** Transfer */
                    case 2:
                        value = r.nextInt(500);
                        int destination = r.nextInt(MultipleTester.getLastNumber() - MultipleTester.getFirstNumber()) + MultipleTester.getFirstNumber();
                        status = bank.transfer(accountNumber, destination, value, null);

                        if (status){
                            int destValue = MultipleTester.accounts.get(destination) + value;
                            int sourceValue = MultipleTester.accounts.get(accountNumber) - value;
                            MultipleTester.accounts.put(destination, destValue);
                            MultipleTester.accounts.put(accountNumber, sourceValue);
                        }
                        break;

                    /** Balance */
                    case 3:
                        bank.getBalance(accountNumber);
                        break;

                    /** Moviments */
                    case 4:
                        int nMoviments = r.nextInt(10 - 1) + 1;
                        bank.moveList(accountNumber, nMoviments);
                        break;
                }
                long after = System.currentTimeMillis();
                MultipleTester.inc( after - before );
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
