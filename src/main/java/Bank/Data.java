package Bank;

import Communication.Operation;

import java.io.Serializable;
import java.util.Vector;

public class Data implements Serializable{

    /** variables */
    private Vector<String> accounts;
    private Vector<Operation> moviments;

    /** Construtor */
    public Data(){
        this.accounts = new Vector<>();
        this.moviments = new Vector<>();
    }

    /** Insert accounts */
    public void InsertAccounts(int accountid, String password, int balance){
        String str = accountid+":"+password+":"+balance;
        accounts.add(str);
    }

    /** Insert Moviments */
    public void insertMoviments(Operation op){
        this.moviments.add(op);
    }

    /** Obter accounts */
    public Vector<String> getAccounts(){
        Vector<String> r = new Vector<String>();
        for(String str : this.accounts)
            r.add(str);
        return r;
    }

    /** Obter Moviments */
    public Vector<Operation> getOperations(){
        Vector<Operation> r = new Vector<>();
        for(Operation op : this.moviments)
            r.add(op);
        return r;
    }
}
