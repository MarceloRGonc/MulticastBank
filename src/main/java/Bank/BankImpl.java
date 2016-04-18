package Bank;

import java.io.Serializable;
import java.util.*;

public class BankImpl implements Bank, Serializable{

    /** Accounts counter */
    private int accountsID;

    // Accounts
    private Map<Integer, Account> accounts;

    // Construtor
    public BankImpl(){
        this.accountsID = 0;
        this.accounts = new HashMap<>();
    }

    // Create Account
    public synchronized int createAccount(String password){
        Account account = new Account(accountsID,password,0);
        accounts.put(accountsID,account);
        return accountsID++;
    }

    // Login
    public boolean loginAccount(int accountId,String password){
        return this.accounts.containsKey(accountId) && this.accounts.get(accountId).loginAccount(accountId,password);
    }

    // Movimentos
    public synchronized boolean move(int accountId, float value) {

        /**Account account = this.accounts.get(accountId);

        if ((account.getValue() + value) < 0) {
            System.out.println("[BankImpl] ERROR!");
            return false;
        }

        Mov.Type type;

        if(value < 0){
            type = Mov.Type.WITHDRAW;
        }
        else{
            type = Mov.Type.DEPOSIT;
        }

        Mov mov = new  Mov(type,value);

        account.addMov(mov);
        account.setValue(account.getValue() + value);
    */
        return true;
    }

    public boolean move(float value) {
        return false;
    }

    @Override
    public synchronized float getBalance(int accountId) {
        return this.accounts.get(accountId).getValue();
    }

    @Override
    public float getBalance() {
        return 0;
    }

    public boolean transfer(int source, int dest, float amount) { return true;}

}
