package Communication;

public class Response extends Message {

    /** Message identification */
    private int msgNumber;
    private String vmid;

    private Type op;
    private float amount;
    private boolean response;


    public Response(Type op, boolean m , String vmid, int n) {
        this.vmid = vmid;
        this.msgNumber = n;
        this.op = op;
        this.response = m;
    }

    public Response(Type op, String vmid, int n) {
        this.vmid = vmid;
        this.msgNumber = n;
        this.op = op;
    }

    public Response(Type op, float amount, String vmid, int n) {
        this.op = op;
        this.amount = amount;
        this.vmid = vmid;
        this.msgNumber = n;
    }

    public Type getType() {
        return this.op;
    }

    public float getAmount() { return this.amount; }

    public boolean getResponse() { return this.response; }

    public String getVmid() { return this.vmid; }

    public int getMsgNumber() { return this.msgNumber; }
}
