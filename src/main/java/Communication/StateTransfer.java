package Communication;

import Bank.BankImpl;

public class StateTransfer extends Message {

    /** Message identification */
    private String vmid;
    private Type op;
    private BankImpl bank;

    public StateTransfer(Type op, String n, BankImpl b) {
        this.vmid = n;
        this.op = op;
        this.bank = b;
    }

    public StateTransfer(Type op, String vmid) {
        this.vmid = vmid;
        this.op = op;
        this.bank = null;
    }

    public Type getType() { return this.op; }

    public String getVMID() { return String.valueOf(this.vmid); }

    public BankImpl getBank(){
        return bank;
    }
}
