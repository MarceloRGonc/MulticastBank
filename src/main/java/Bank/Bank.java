package Bank;

public interface Bank {

    float getBalance();

    boolean move(float value);

    boolean transfer(int dest, int amount);

    boolean movements(int n);

}