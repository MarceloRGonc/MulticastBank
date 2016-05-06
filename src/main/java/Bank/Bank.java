package Bank;

import Communication.Operation;

public interface Bank {

    int getBalance(int accountId);

    boolean move(int value, Operation op);

    String moveList(int accountId, int nMovements);

    boolean transfer(int source, int dest, int amount, Operation op);

}