package Communication;

import Bank.Bank;

public class StateTransfer extends Message {

    /** Message identification */
    private int msgNumber;
    private Type op;
    private Bank bank;
    private boolean response;

    public StateTransfer(Type op, int n, Bank b) {
        this.msgNumber = n;
        this.op = op;
        this.bank = b;
    }

    public Type getType() { return this.op; }

    public float getAmount() { return 0; }

    public boolean getResponse() { return this.response; }

    public int getMsgNumber() { return this.msgNumber; }

    public Bank getBank(){
        return bank;
    }
}
