package Bank;

import java.util.*;
import Communication.Operation;

/**
 * Created by brunorebelo on 17/04/16.
 */

public class Account {

    private int accountId;
    private String accountPassword;
    private Map<Integer,Operation> operations;
    private int value;

    public Account (int accountId, String accountPassword, int value){
        this.accountId = accountId;
        this.accountPassword = accountPassword;
        this.operations = new HashMap<>();
        this.value = value;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public String getAccountPassword() {
        return accountPassword;
    }

    public void setAccountPassword(String accountPassword) {
        this.accountPassword = accountPassword;
    }

    public HashMap<String,Operation> getOperation() {
        HashMap<String,Operation> r = new HashMap<>();
        for(Operation op : this.operations.values())
            r.put(op.getVMID(),op);
        return r;
    }

    public void setOperations(HashMap<String,Operation> operations) {
        this.operations = new HashMap<>();
        for(Operation op : operations.values())
            this.operations.put(op.getMsgNumber(),op);
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public void addMov(Operation o){
        this.operations.put(o.getMsgNumber(),o);
    }

    public boolean loginAccount(int accountId,String accountPassword){
        return accountId == this.accountId && accountPassword.equals(this.accountPassword);
    }

    public String getMoves(int nMoviments){

        int total = this.operations.size();
        StringBuilder s = new StringBuilder();
        s.append("Account: " + this.accountId + "\n");

        while (nMoviments > 0 && total > 0){
            s.append(operations.get(total-1).toString());
            total--;
            nMoviments--;
        }
        return s.toString();
    }
}
