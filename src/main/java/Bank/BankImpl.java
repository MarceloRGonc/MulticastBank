package Bank;

import java.io.Serializable;

public class BankImpl implements Bank, Serializable{

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

    public synchronized float getBalance() {
        float res = balance;
        return res;
    }

    public void setBalance(float balance) {
        this.balance = balance;
    }
}
