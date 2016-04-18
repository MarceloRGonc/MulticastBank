package Bank;

import Communication.Message;
import Communication.Operation;

import java.io.Serializable;
import java.util.*;

public class BankImpl implements Bank, Serializable{

    /** Accounts counter */
    private static int accountsID;

    // Accounts
    private Map<Integer, Account> accounts;

    // Construtor
    public BankImpl(){
        this.accountsID = 0;
        this.accounts = new HashMap<>();
    }

    // Create Account
    public synchronized int createAccount(String password){
        int accountNumber = accountsID;
        accountsID++;
        Account account = new Account(accountNumber,password,0);
        this.accounts.put(accountNumber,account);
        return accountNumber;
    }

    // Login
    public boolean loginAccount(int accountId,String password){
        return this.accounts.containsKey(accountId) && this.accounts.get(accountId).loginAccount(accountId,password);
    }

    // Movimentos
    public synchronized boolean move(Operation op) {

        Account account = this.accounts.get(op.getOrigin());

        if ((account.getValue() + op.getAmount()) < 0) {
            System.out.println("[BankImpl] ERROR!");
            return false;
        }

        account.addMov(op);
        account.setValue(account.getValue() + op.getAmount());

        return true;
    }

    public boolean move(int value) {
        return false;
    }

    @Override
    public synchronized int getBalance(int accountId) {
        return this.accounts.get(accountId).getValue();
    }

    @Override
    public int getBalance() {
        return 0;
    }

    public boolean transfer(int source, int dest, int amount) { return true;}

    public boolean transfer(Operation op){

        // Verifica se 2 conta existe
        if(!this.accounts.containsKey(op.getDestination())){
            return false;
        }

        Account account1 = this.accounts.get(op.getOrigin());

        // Valor
        int value = account1.getValue() - op.getAmount();

        // Verifica se conta 1 tem saldo suficiente
        if(value < 0){
            return false;
        }

        Account account2 = this.accounts.get(op.getDestination());
        int value2 = account2.getValue() + op.getAmount();

        // update values
        account1.setValue(value);
        account2.setValue(value2);

        // Guardar movimentos
        account1.addMov(op);
        account2.addMov(op);

        return true;
    }

}
