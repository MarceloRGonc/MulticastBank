package Server;

import Bank.BankImpl;
import Bank.Data;
import Communication.*;
import net.sf.jgcs.*;
import net.sf.jgcs.Message;
import net.sf.jgcs.annotation.PointToPoint;
import net.sf.jgcs.jgroups.JGroupsGroup;
import net.sf.jgcs.jgroups.JGroupsProtocolFactory;
import net.sf.jgcs.jgroups.JGroupsService;

import java.io.*;
import java.net.SocketAddress;
import java.rmi.dgc.VMID;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import static Communication.Message.Type;


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

    /** DataBase */
    private String nameDatabase;

    /** Store messages that arrived after the status request */
    private boolean keep = false;
    private ConcurrentLinkedQueue<Communication.Message> keepMessages = new ConcurrentLinkedQueue<>();

    public Server(JGroupsGroup group, Protocol p, String nameDatabase) {
        try {

            this.nameDatabase = nameDatabase;

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
        if(args.length == 1) {
            try {
                /** Args[0] name of the server */
                bank = new BankImpl(args[0]);

                System.out.println("[Server] Server started");

                JGroupsProtocolFactory pf = new JGroupsProtocolFactory();
                group = new JGroupsGroup("Bank");
                p = pf.createProtocol();
                new Server(group, p, args[0]);
                while (true) {
                    Thread.sleep(10000);
                }
            } catch (GroupException ex) {
                ex.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized void sendResponse(Object obj, SocketAddress dest) throws IOException {
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
           /** System.out.println("[" + vmid.hashCode() + " - " + msgNumber + "] "
                    + "Sent response! Account: " + ((Response) obj).getAccountId() +
                    " Balance: " + bank.getBalance(((Response) obj).getAccountId()));*/
        } else if(obj instanceof Communication.CreateLogin) {
            vmid = ((CreateLogin) obj).getVMID();
            msgNumber = ((CreateLogin) obj).getMsgNumber();
            /**System.out.println("[" + vmid.hashCode() + " - " + msgNumber + "] "
                    + "Sent response! Account: " + ((CreateLogin) obj).getAccount() );*/
        } else if(obj instanceof Communication.StateTransfer) {
            System.out.println("[StateTransfer] Sent current state!");
        }

        dSession.multicast(toSend, new JGroupsService(), null, new PointToPoint(dest));
    }

    public synchronized Object onMessage(Message msg) {
        try {
            ObjectInputStream oInput;
            ByteArrayInputStream bInput = new ByteArrayInputStream(msg.getPayload());
            oInput = new ObjectInputStream(bInput);
            Communication.Message receive = (Communication.Message) oInput.readObject();

            CreateLogin clres;
            if (keep && receive instanceof Operation){
                keepMessages.add(receive);
            } else if(state != 1 && receive instanceof Operation) {

                Operation op = (Operation) receive;
                doOperations(op, msg.getSenderAddress());

            }
            else if(state != 1 && receive instanceof CreateLogin) {
                CreateLogin cl = (CreateLogin) receive;

                switch (cl.getType()) {
                    case REGISTER:
                        if(state != 1 && cl.getControl()) {
                            clres = new CreateLogin(Type.REGISTER, cl.getVMID(), cl.getMsgNumber(), bank.createAccount(cl.getPassword(),0), cl.getPassword(), false);
                            sendResponse(clres, msg.getSenderAddress());
                        }
                        break;

                    case LOGIN:
                        if(state != 1 && cl.getControl()) {

                            clres = new CreateLogin(Type.REGISTER, cl.getVMID(), cl.getMsgNumber(), cl.getAccount(), cl.getPassword(), bank.loginAccount(cl.getAccount(), cl.getPassword()), false);

                            sendResponse(clres, msg.getSenderAddress());
                        }
                        break;
                }
            } else if(receive instanceof StateTransfer) {
                StateTransfer st = (StateTransfer) receive;

                switch (st.getType()) {
                    case ASKSTATE:
                        if(state != 1) {
                            Data data = bank.exportData(st.getCreateLogin(),st.getOperation());

                            /** Insert data */
                            StateTransfer opt = new StateTransfer(Type.RECEIVESTATE, vmid, data);
                            sendResponse(opt, msg.getSenderAddress());
                        } else if(state == 1){
                            System.out.println("Keeping messages");
                            keep = true;
                        }
                        break;

                    case RECEIVESTATE:
                        if(state == 1) {
                            System.out.println("Receive State! ");

                            Data data = st.getBank();

                            /** Update data */
                            bank.updateData(data);

                            /** Array sorted in arrival order */
                            boolean flag = true;
                            while(flag){

                                Operation op = (Operation) keepMessages.poll();
                                if(op == null){
                                    flag = false;
                                } else {
                                    /** Verifies if message was already realized */
                                    boolean bool = bank.operationRealized(op.getOrigin(), op.getMsgNumber());

                                    if (bool) {
                                        System.out.println("Repeated message!");
                                    } else {
                                        doOperations(op, msg.getSenderAddress());
                                    }
                                }
                            }
                            keep = false;
                            this.state = 2;
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

            /** Request state */
            if( state == 1 && (this.mSession.getMembership().getMembershipList().size() != 1)){

                int cl = bank.lastAccountInserted();
                int op = bank.lastMovimentInserted();

                /* Envia para o grupo para informar que os ultimos dados que tem são estes */
                StateTransfer res = new StateTransfer(Type.ASKSTATE, vmid,cl,op);
                System.out.print("Last Account: " + cl);
                System.out.print("\nLast Moviment: " + op + "\n");

                ByteArrayOutputStream bOuput = new ByteArrayOutputStream();
                ObjectOutputStream oosHere = new ObjectOutputStream(bOuput);

                oosHere.writeObject(res);
                byte[] data = bOuput.toByteArray();
                Message toSend = dSession.createMessage();
                toSend.setPayload(data);
                System.out.println("[ - " + res.getVMID()  + "] "
                        + "Ask State!");
                dSession.multicast(toSend, new JGroupsService(), null);

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
        this.bank.shutdown();
        System.exit(0);
    }

    private synchronized void doOperations(Operation op, SocketAddress dest) throws IOException{
        Response res;
        boolean result;
        switch (op.getType()) {
            case MOVE:
                result = bank.move(op.getAmount(), op);
                    res = new Response(Type.MOVE, result, op.getVMID(),
                            op.getMsgNumber(), op.getOrigin());
                    sendResponse(res, dest);
                break;

            case TRANSFER:
                     result = bank.transfer(op.getOrigin(), op.getDestination(), op.getAmount(), op);
                    res = new Response(Type.TRANSFER, result, op.getVMID(),
                            op.getMsgNumber(),op.getOrigin());
                    sendResponse(res, dest);
                break;

            case BALANCE:
                    res = new Response(Type.BALANCE, bank.getBalance(op.getOrigin()),
                            op.getVMID(), op.getMsgNumber(),op.getOrigin());
                    sendResponse(res, dest);
                    System.out.println("[" + vmid.hashCode() + " - " + res.getMsgNumber() + "] "
                        + "Sent response! Account Balance: " + bank.getBalance(res.getAccountId()));
                break;

            case MOVEMENTS:
                    // op.getAmount()refere-se ao número de movimentos
                    String str = bank.moveList(op.getOrigin(),op.getAmount());
                    res = new Response(Type.MOVEMENTS,op.getVMID(), op.getMsgNumber(),op.getOrigin(),str);
                    sendResponse(res, dest);

                break;

            case LEAVE:
                    res = new Response(Type.LEAVE, op.getVMID(),
                            op.getMsgNumber(),op.getDestination());
                    sendResponse(res, dest);

                break;
        }
    }
}
