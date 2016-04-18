package Bank;

import java.util.*;
import Communication.Operation;

/**
 * Created by brunorebelo on 17/04/16.
 */

public class Account {

    private int accountId;
    private String accountPassword;
    private Map<String,Operation> operations;
    private float value;

    public Account (int accountId, String accountPassword, float value){
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
            this.operations.put(op.getVMID(),op);
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public void addMov(Operation o){
        this.operations.put(o.getVMID(),o);
    }

    public boolean loginAccount(int accountId,String accountPassword){
        return accountId == this.accountId && accountPassword.equals(this.accountPassword);
    }
}
