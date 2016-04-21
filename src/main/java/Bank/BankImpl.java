package Bank;

import Communication.Operation;
import java.io.Serializable;
import java.util.*;

public class BankImpl implements Bank, Serializable{

    private int count = 0;
    public void add() { count++; }
    public int getCount() { return count; }

    /** Accounts counter */
    private static int accountsID;

    /** Accounts */
    private Map<Integer, Account> accounts;

    /** Construtor */
    public BankImpl(){
        this.accountsID = 0;
        this.accounts = new HashMap<>();
    }

    /** Create Account */
    public int createAccount(String password){
        int accountNumber = accountsID;
        accountsID++;
        Account account = new Account(accountNumber,password,0);
        this.accounts.put(accountNumber,account);
        return accountNumber;
    }

    /** Login */
    public boolean loginAccount(int accountId,String password){
        boolean exists = this.accounts.containsKey(accountId);
        boolean success = false;
        if(exists) {
            success = this.accounts.get(accountId).loginAccount(accountId, password);
        }
        return  exists && success;
    }

    /** Movimentos */
    public boolean move(int value, Operation op) {

        Account account = this.accounts.get(op.getOrigin());

        if ((account.getValue() + op.getAmount()) < 0) {
            System.out.println("[BankImpl] ERROR!");
            return false;
        }

        account.addMov(op);
        account.setValue(account.getValue() + op.getAmount());

        return true;
    }

    public int getBalance(int accountId) {
        return this.accounts.get(accountId).getValue();
    }

    public boolean transfer(int source, int dest, int amount, Operation op){

        /** Verifica se 2 conta existe */
        if(!this.accounts.containsKey(dest)){
            return false;
        }

        Account account1 = this.accounts.get(source);

        /** Valor */
        int value = account1.getValue() - amount;

        /** Verifica se conta 1 tem saldo suficiente */
        if(value < 0){
            return false;
        }

        Account account2 = this.accounts.get(dest);
        int value2 = account2.getValue() + amount;

        /** update values */
        account1.setValue(value);
        account2.setValue(value2);

        /** Guardar movimentos */
        account1.addMov(op);
        account2.addMov(op);

        return true;
    }

    public synchronized String moveList(int accountId, int nMoviments){
        return this.accounts.get(accountId).getMoves(nMoviments);
    }

}
