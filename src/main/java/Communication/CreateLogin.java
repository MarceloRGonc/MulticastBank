package Communication;

/**
 * Created by brunorebelo on 17/04/16.
 */

public class CreateLogin extends Message {

    /** Message identification */
    private String vmid;
    private int msgNumber;

    /** Message type */
    private Type type;

    /** Account and Password */
    private int account;
    private String password;

    /** balance */
    private int balance;

    /** login sucess */
    private boolean sucess;

    /** bool control */
    private boolean control;

    public CreateLogin(Type op, String vmid, int msgNumber, int account, String password, boolean control) {
        this.type = op;
        this.vmid = vmid;
        this.msgNumber = msgNumber;
        this.account = account;
        this.password = password;
        this.control = control;
    }

    public CreateLogin(Type op, String vmid, int msgNumber, int account, String password, boolean sucess,boolean control) {
        this.type = op;
        this.vmid = vmid;
        this.msgNumber = msgNumber;
        this.account = account;
        this.password = password;
        this.sucess = sucess;
        this.control = control;
    }

    public CreateLogin(int account, String password, int balance) {
        this.account = account;
        this.password = password;
        this.balance = balance;
    }

    public Type getType() { return this.type; }

    public String getVMID() { return this.vmid; }

    public int getMsgNumber() { return this.msgNumber; }

    public int getAccount() { return this.account; }

    public String getPassword() { return this.password; }

    public boolean getSucess(){ return this.sucess; }

    public boolean getControl() { return this.control; }

    public int getBalance(){ return this.balance; }
}
