package Communication;

public class Operation extends Message {
    /** Message identification */
    private String vmid;
    private int msgNumber;

    /** Message orgin account */
    private int orig;

    /** Account movement destination */
    private int dest;

    /** Message type */
    private Type type;

    /** Amount involved in the operation */
    private int amount;

    public Operation(Type op, String vmid, int msgNumber, int orig) {
        this.vmid = vmid;
        this.msgNumber = msgNumber;
        this.orig = orig;
        this.dest = 0;
        this.type = op;
        this.amount = 0;
    }

    public Operation(Type op, String vmid, int msgNumber, int orig, int amount) {
        this.vmid = vmid;
        this.msgNumber = msgNumber;
        this.orig = orig;
        this.dest = 0;
        this.type = op;
        this.amount = amount;
    }

    public Operation(Type op, String vmid, int msgNumber, int orig, int dest, int amount) {
        this.vmid = vmid;
        this.msgNumber = msgNumber;
        this.type = op;
        this.orig = orig;
        this.dest = dest;
        this.amount = amount;
    }

    public Operation(Operation op) {
        this.vmid = op.getVMID();
        this.msgNumber = op.getMsgNumber();
        this.type = op.getType();
        this.orig = op.getOrigin();
        this.dest = op.getDestination();
        this.amount = op.getAmount();
    }

    public Type getType() { return this.type; }

    public String getVMID() { return this.vmid; }

    public int getMsgNumber() { return this.msgNumber; }

    public int getAmount() { return this.amount; }

    public int getOrigin() { return this.orig; }

    public int getDestination() { return this.dest; }

    public String toString(){
        String res = "";
        res += "Number Msg: " + this.msgNumber;
        res += "\nAmount: " + this.amount;

        if(this.type == Type.TRANSFER)
            res += "\nType: Transfer to account: " + this.dest;
        else {
            if(this.amount > 0){
                res += "\nType: Deposit";
            }
            else{
                res += "\nType: Withdraw";
            }
        }
        System.out.println(
        "\n" + res + "\n");
        return res;
    }

    public Operation clone() {
        return new Operation(this);
    }
}
