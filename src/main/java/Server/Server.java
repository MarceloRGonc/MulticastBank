package Server;

import Bank.BankImpl;
import Communication.*;
import net.sf.jgcs.*;
import net.sf.jgcs.Message;
import net.sf.jgcs.jgroups.JGroupsGroup;
import net.sf.jgcs.jgroups.JGroupsProtocolFactory;
import net.sf.jgcs.jgroups.JGroupsService;
import java.io.*;
import java.net.SocketAddress;
import java.rmi.dgc.VMID;
import java.util.ArrayList;
import java.util.List;
import static Communication.Message.Type;

/**
 * Created by MGonc on 15/02/16.
 */
public class Server implements MessageListener, MembershipListener {

    /** Server identification */
    private static final String vmid = new VMID().toString();
    private static BankImpl bank = null;
    private static DataSession dSession = null;
    private static MembershipSession mSession = null;
    private static JGroupsGroup group = null;
    private static Protocol p = null;

    /** Current state 1 - started now 2 - Updated */
    private int state;

    /** Store messages that arrived after the status request */
    private int keepCounter = 0;
    private boolean keep = false;
    private ArrayList<Communication.Message> keepMessages = new ArrayList<>();


    public Server(JGroupsGroup group, Protocol p) {
        try {
            this.state = 1;
            this.group = group;
            this.p = p;

            this.dSession = p.openDataSession(group);
            this.dSession.setMessageListener(this);

            this.mSession = (MembershipSession) p.openControlSession(group);
            this.mSession.setMembershipListener(this);
            ControlSession cs = this.p.openControlSession(group);
            cs.join();
        } catch (GroupException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {

            bank = new BankImpl();

            System.out.println("[Server] Server started");

            JGroupsProtocolFactory pf = new JGroupsProtocolFactory();
            group = new JGroupsGroup("Bank");
            p = pf.createProtocol();
            new Server(group, p);
            while (true) {
                Thread.sleep(10000);
            }
        } catch (GroupException ex){
            ex.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private synchronized void sendResponse(Object obj, boolean flag) throws IOException {
        ByteArrayOutputStream bOuput = new ByteArrayOutputStream();
        ObjectOutputStream oosHere = new ObjectOutputStream(bOuput);

        oosHere.writeObject(obj);
        byte[] data = bOuput.toByteArray();
        Message toSend = dSession.createMessage();
        toSend.setPayload(data);

        String vmid;
        int msgNumber;

        if(obj instanceof Communication.Response) {
            vmid = ((Response) obj).getVMID();
            msgNumber = ((Response) obj).getMsgNumber();
            System.out.println("[" + vmid.hashCode() + " - " + msgNumber + "] "
                    + "Sent response! Account: " + ((Response) obj).getAccountId() +
                    " Balance: " + bank.getBalance(((Response) obj).getAccountId()));
        } else if(obj instanceof Communication.CreateLogin) {
            vmid = ((CreateLogin) obj).getVMID();
            msgNumber = ((CreateLogin) obj).getMsgNumber();
            System.out.println("[" + vmid.hashCode() + " - " + msgNumber + "] "
                    + "Sent response! Account: " + ((CreateLogin) obj).getAccount() );
        } else if(obj instanceof Communication.StateTransfer) {
            System.out.println("[StateTransfer] Sent current state!");
        }

        dSession.multicast(toSend, new JGroupsService(), null);
        bank.add();

        if (!flag) {
            System.out.println("Movements: " + bank.getCount());
        }
    }

    public synchronized Object onMessage(Message msg) {
        try {
            ObjectInputStream oInput;
            ByteArrayInputStream bInput = new ByteArrayInputStream(msg.getPayload());
            oInput = new ObjectInputStream(bInput);
            Communication.Message receive = (Communication.Message) oInput.readObject();

            CreateLogin clres;
            if (keep && receive instanceof Operation){
                keepMessages.add(keepCounter++,receive);
            } else if(receive instanceof Operation) {

                Operation op = (Operation) receive;
                doOperations(op);

            }
            else if(receive instanceof CreateLogin) {
                CreateLogin cl = (CreateLogin) receive;

                switch (cl.getType()) {
                    case REGISTER:
                        if(state != 1 && cl.getControl()) {
                            clres = new CreateLogin(Type.REGISTER, cl.getVMID(), cl.getMsgNumber(), bank.createAccount(cl.getPassword()), cl.getPassword(), false);
                            sendResponse(clres, true);
                        }
                        break;

                    case LOGIN:
                        if(state != 1 && cl.getControl()) {

                            clres = new CreateLogin(Type.REGISTER, cl.getVMID(), cl.getMsgNumber(), cl.getAccount(), cl.getPassword(), bank.loginAccount(cl.getAccount(), cl.getPassword()), false);

                            sendResponse(clres, true);
                        }
                        break;
                }
            } else if(receive instanceof StateTransfer) {
                StateTransfer st = (StateTransfer) receive;

                switch (st.getType()) {
                    case ASKSTATE:
                        if(state != 1) {
                            StateTransfer opt = new StateTransfer(Type.SENDSTATE, vmid, bank);
                            sendResponse(opt, false);
                            return null;
                        } else if(state == 1){
                            System.out.println("Keeping messages");
                            keep = true;
                        }
                        break;

                    case SENDSTATE:
                        if(state == 1) {
                            System.out.println("Receive State! ");
                            this.bank = (BankImpl) st.getBank();
                            /** Make old operations */
                            doOldOperations();
                            keep = false;
                            this.state = 2;
                            return null;
                        }
                        break;
                }

            }
            } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    public synchronized void onMembershipChange() {
        try {
            List<SocketAddress> list = this.mSession.getMembership().getJoinedMembers();

            if( state == 1 && (this.mSession.getMembership().getMembershipList().size() != 1)){

                StateTransfer res = new StateTransfer(Type.ASKSTATE, vmid);

                for(SocketAddress s : list){
                    ByteArrayOutputStream bOuput = new ByteArrayOutputStream();
                    ObjectOutputStream oosHere = new ObjectOutputStream(bOuput);

                    oosHere.writeObject(res);
                    byte[] data = bOuput.toByteArray();
                    Message toSend = dSession.createMessage();
                    toSend.setPayload(data);
                    System.out.println("[ - " + res.getVMID()  + "] "
                            + "Ask State!");
                    dSession.multicast(toSend, new JGroupsService(), null);
                }
            } else if(this.mSession.getMembership().getMembershipList().size() == 1){
                state = 2;
                keep = false;
            }
        } catch (InvalidStateException e) {
            e.printStackTrace();
        } catch (GroupException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void onExcluded() {
        System.exit(0);
    }

    private void doOldOperations() throws IOException {
        for(Communication.Message receive : keepMessages){
            Operation op = (Operation) receive;
            doOperations(op);
        }

    }

    private void doOperations(Operation op) throws IOException{
        Response res;
        switch (op.getType()) {
            case MOVE:
                if(state != 1) {
                    boolean result = bank.move(op.getAmount(), op);
                    res = new Response(Type.MOVE, result, op.getVMID(),
                            op.getMsgNumber(),op.getOrigin());
                    sendResponse(res, true);
                }
                break;

            case TRANSFER:
                if(state != 1) {
                    boolean result = bank.transfer(op.getOrigin(), op.getDestination(), op.getAmount(), op);
                    res = new Response(Type.TRANSFER, result, op.getVMID(),
                            op.getMsgNumber(),op.getOrigin());
                    sendResponse(res, true);
                }
                break;

            case BALANCE:
                if(state != 1) {
                    res = new Response(Type.BALANCE, bank.getBalance(op.getOrigin()),
                            op.getVMID(), op.getMsgNumber(),op.getOrigin());
                    sendResponse(res, true);
                }
                break;

            case MOVEMENTS:
                if(state != 1) {
                    // op.getDestination() refere-se ao número de movimentos
                    String str = bank.moveList(op.getOrigin(),op.getDestination());
                    res = new Response(Type.MOVEMENTS,op.getVMID(), op.getMsgNumber(),op.getOrigin(),str);
                    sendResponse(res, true);
                }
                break;

            case LEAVE:
                if(state != 1) {
                    res = new Response(Type.LEAVE, op.getVMID(),
                            op.getMsgNumber(),op.getDestination());
                    sendResponse(res, false);
                }
                break;
        }
    }
}
