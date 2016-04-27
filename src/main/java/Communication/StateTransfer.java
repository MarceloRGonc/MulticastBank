package Communication;

import Bank.BankImpl;
import Bank.Data;

public class StateTransfer extends Message {

    /** Message identification */
    private String vmid;
    private Type op;
    private Data bank;

    /** Operation and CreateLogin */
    private int operation;
    private int createLogin;

    public StateTransfer(Type op, String n, Data bank) {
        this.vmid = n;
        this.op = op;
        this.bank = bank;
    }

    public StateTransfer(Type op, String vmid, int createLogin, int operation) {
        this.op = op;
        this.vmid = vmid;
        this.createLogin = createLogin;
        this.operation = operation;
    }

    public Type getType() { return this.op; }

    public String getVMID() { return String.valueOf(this.vmid); }

    public Data getBank(){ return this.bank;}

    public int getOperation() { return  this.operation; }

    public int getCreateLogin(){ return this.createLogin; }
}
