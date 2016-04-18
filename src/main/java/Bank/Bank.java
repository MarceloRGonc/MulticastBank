package Bank;

import Communication.Operation;

public interface Bank {

    int getBalance();

    int getBalance(int accountId);

    boolean move(int value);

    boolean move(Operation op);

    boolean transfer(int source, int dest, int amount);

    boolean transfer(Operation op);

}