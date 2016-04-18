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
import java.util.List;
import static Communication.Message.Type;

/**
 * Created by MGonc on 15/02/16.
 */
public class Server implements MessageListener, MembershipListener {

    private static BankImpl bank = null;
    private static DataSession dSession = null;
    private static MembershipSession mSession = null;
    private static JGroupsGroup group = null;
    private static Protocol p = null;

    private int state;

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

    private int count = 0;
    private void add() { count++; }
    private int getCount() { return count; }

    private void sendResponse(Object obj, boolean flag) throws IOException {
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
        }
        else {
            vmid = ((CreateLogin) obj).getVMID();
            msgNumber = ((CreateLogin) obj).getMsgNumber();
            System.out.println("[" + vmid.hashCode() + " - " + msgNumber + "] "
                    + "Sent response! Account: " + ((CreateLogin) obj).getAccount() );
        }

        dSession.multicast(toSend, new JGroupsService(), null);
        add();

        if (!flag) {
            System.out.println("Movements: " + getCount());
        }
    }

    public Object onMessage(Message msg) {
        try {
            ObjectInputStream oInput;
            ByteArrayInputStream bInput = new ByteArrayInputStream(msg.getPayload());
            oInput = new ObjectInputStream(bInput);
            Communication.Message receive = (Communication.Message) oInput.readObject();

            Response res;
            CreateLogin clres;

            if(receive instanceof Operation) {

                Operation op = (Operation) receive;
                switch (op.getType()) {
                    case MOVE:
                        if(state != 1) {
                            boolean result = bank.move(op);
                            res = new Response(Type.MOVE, result, op.getVMID(),
                                    op.getMsgNumber(),op.getOrigin());
                            sendResponse(res, true);
                        }
                        break;

                    case TRANSFER:
                        if(state != 1) {
                            boolean result = bank.transfer(op);
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

                    case LEAVE:
                        if(state != 1) {
                            res = new Response(Type.LEAVE, op.getVMID(),
                                    op.getMsgNumber(),op.getDestination());
                            sendResponse(res, false);
                        }
                        break;

                    /*case STATE:
                        if(state == 1) {
                            System.out.println("Recebi Estado! ");
                            if(op instanceof StateTransfer){
                                StateTransfer opt = (StateTransfer) op;
                                this.bank.setBalance(opt.getBank().getBalance());
                                System.out.println("Estado: " + bank.getBalance());
                                this.state = 2;
                            }
                            return null;
                        }
                        break;*/
                }
            }
            else if(receive instanceof CreateLogin) {
                CreateLogin cl = (CreateLogin) receive;

                switch (cl.getType()) {
                    case REGISTER:
                        if(state != 1 && cl.getControl()) {
                            clres = new CreateLogin(Type.REGISTER,cl.getVMID(),cl.getMsgNumber(),bank.createAccount(cl.getPassword()),cl.getPassword(),false);
                            sendResponse(clres, true);
                        }
                        break;

                    case LOGIN:
                        if(state != 1 && cl.getControl()) {
                            clres = new CreateLogin(Type.REGISTER,cl.getVMID(),cl.getMsgNumber(),cl.getAccount(),cl.getPassword(),bank.loginAccount(cl.getAccount(),cl.getPassword()),false);
                            sendResponse(clres, true);
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

    public void onMembershipChange() {
        try {
            List<SocketAddress> list = this.mSession.getMembership().getJoinedMembers();

            if( state != 1){

                StateTransfer res = new StateTransfer(Type.ASKSTATE, 0, bank);

                for(SocketAddress s : list){
                    ByteArrayOutputStream bOuput = new ByteArrayOutputStream();
                    ObjectOutputStream oosHere = new ObjectOutputStream(bOuput);

                    oosHere.writeObject(res);
                    byte[] data = bOuput.toByteArray();
                    Message toSend = dSession.createMessage();
                    toSend.setPayload(data);
                    System.out.println("[ - " + res.getMsgNumber()  + "] "
                            + "Sent State!");
                    dSession.multicast(toSend, new JGroupsService(), null);
                }
            } else if( list.size() == 1) {
                state = 2;
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
}
