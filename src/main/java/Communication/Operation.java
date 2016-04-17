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
    private float amount;

    public Operation(Type op, String vmid, int msgNumber, int orig) {
        this.vmid = vmid;
        this.msgNumber = msgNumber;
        this.orig = orig;
        this.dest = 0;
        this.type = op;
        this.amount = 0;
    }

    public Operation(Type op, String vmid, int msgNumber, int orig, float amount) {
        this.vmid = vmid;
        this.msgNumber = msgNumber;
        this.orig = orig;
        this.dest = 0;
        this.type = op;
        this.amount = amount;
    }

    public Operation(Type op, String vmid, int msgNumber, int orig, int dest, float amount) {
        this.vmid = vmid;
        this.msgNumber = msgNumber;
        this.type = op;
        this.orig = orig;
        this.dest = dest;
        this.amount = amount;
    }

    public Type getType() { return this.type; }

    public String getVMID() { return this.vmid; }

    public int getMsgNumber() { return this.msgNumber; }

    public float getAmount() { return this.amount; }

    public int getOrigin() { return this.dest; }

    public int getDestination() { return this.dest; }

}
