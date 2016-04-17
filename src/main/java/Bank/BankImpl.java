package Bank;

import java.io.Serializable;

public class BankImpl implements Bank, Serializable{

    /** Accounts counter */
    private int accountsID;

    /**
      * Accounts
      * Map<Id, Accounts>
     */

    /** Operation counter */
    private int operationID;

    /**
      * Moves
      * Map<Id, Operation>
     */

    float balance = 0;

    public synchronized boolean move(float value) {
        if (balance < 0) {
            System.out.println("[BankImpl] ERROR!");
            return false;



        }

        balance += value;
        if (balance < 0) {
            balance -= value;
            System.out.println("[BankImpl] ERROR!");
            return false;
        }
        return true;
    }

    @Override
    public boolean transfer(int dest, int amount) {
        return false;
    }

    @Override
    public boolean movements(int n) {
        return false;
    }

    public synchronized float getBalance() {
        float res = balance;
        return res;
    }

    public void setBalance(float balance) {
        this.balance = balance;
    }
}
