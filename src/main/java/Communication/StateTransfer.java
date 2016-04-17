package Communication;

import Bank.Bank;

public class StateTransfer extends Message {

    /** Message identification */
    private int msgNumber;
    private Type op;
    private Bank bank;

    public StateTransfer(Type op, int n, Bank b) {
        this.msgNumber = n;
        this.op = op;
        this.bank = b;
    }

    public Type getType() { return this.op; }

    public String getVMID() { return String.valueOf(this.msgNumber); }

    public int getMsgNumber() { return this.msgNumber; }

    public Bank getBank(){
        return bank;
    }
}
