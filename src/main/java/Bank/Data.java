package Bank;

import Communication.Operation;
import java.io.Serializable;
import java.util.Vector;

/**
 * Missing data from another server
 */
public class Data implements Serializable{

    private Vector<String> accounts;
    private Vector<Operation> movements;

    /** Construtor */
    public Data(){
        this.accounts = new Vector<>();
        this.movements = new Vector<>();
    }

    /** Insert accounts
     *
     * @param accountid - account id
     * @param password - user password
     * @param balance - account balance
     */
    public void InsertAccounts(int accountid, String password, int balance){
        String str = accountid+":"+password+":"+balance;
        accounts.add(str);
    }

    /** Insert Movements
     * @param op - operation realized
     */
    public void insertMovements(Operation op){
        this.movements.add(op);
    }

    /** Get accounts
     * @return all accounts
     */
    public Vector<String> getAccounts(){
        Vector<String> r = new Vector<String>();
        for(String str : this.accounts)
            r.add(str);
        return r;
    }

    /** Get Movements
     * @return all movements
     */
    public Vector<Operation> getOperations(){
        Vector<Operation> r = new Vector<>();
        for(Operation op : this.movements)
            r.add(op);
        return r;
    }
}
