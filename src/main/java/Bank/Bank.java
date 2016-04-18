package Bank;

public interface Bank {

    float getBalance();

    float getBalance(int accountId);

    boolean move(float value);

    boolean move(int accountId,float value);

    boolean transfer(int source, int dest, float amount);

}