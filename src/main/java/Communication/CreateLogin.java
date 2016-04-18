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

    /** login sucess */
    private boolean sucess;

    public CreateLogin(Type op, String vmid, int msgNumber, int account, String password) {
        this.type = op;
        this.vmid = vmid;
        this.msgNumber = msgNumber;
        this.account = account;
        this.password = password;
    }

    public CreateLogin(Type op, String vmid, int msgNumber, int account, String password, boolean sucess) {
        this.type = op;
        this.vmid = vmid;
        this.msgNumber = msgNumber;
        this.account = account;
        this.password = password;
        this.sucess = sucess;
    }

    public Type getType() { return this.type; }

    public String getVMID() { return this.vmid; }

    public int getMsgNumber() { return this.msgNumber; }

    public int getAccount() { return this.account; }

    public String getPassword() { return this.password; }

    public boolean getSucess(){ return this.sucess; }
}
