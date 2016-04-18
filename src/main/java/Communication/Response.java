package Communication;

public class Response extends Message {

    /** Message identification */
    private int msgNumber;
    private String vmid;

    /** Message type */
    private Type op;
    private int amount;
    private boolean response;
    private int accountId;


    public Response(Type op, boolean m , String vmid, int n,int accountId) {
        this.vmid = vmid;
        this.msgNumber = n;
        this.op = op;
        this.response = m;
        this.accountId = accountId;
    }

    public Response(Type op, String vmid, int n,int accountId) {
        this.vmid = vmid;
        this.msgNumber = n;
        this.op = op;
        this.accountId = accountId;
    }

    public Response(Type op, int amount, String vmid, int n,int accountId) {
        this.op = op;
        this.amount = amount;
        this.vmid = vmid;
        this.msgNumber = n;
        this.accountId = accountId;
    }

    public Type getType() {
        return this.op;
    }

    public String getVMID() { return this.vmid; }

    public int getAmount() { return this.amount; }

    public boolean getResponse() { return this.response; }

    public int getMsgNumber() { return this.msgNumber; }

    public int getAccountId(){ return  this.accountId; }
}
