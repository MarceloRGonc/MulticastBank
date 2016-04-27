package Server;

import Bank.BankImpl;
import Bank.Data;
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

    /** DataBase */
    private String nameDatabase;

    /** Store messages that arrived after the status request */
    private int keepCounter = 0;
    private boolean keep = false;
    private ArrayList<Communication.Message> keepMessages = new ArrayList<>();

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

        if (!flag) {
            //System.out.println("Movements: " + bank.getCount());
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
                keepMessages.add(keepCounter,receive);
                keepCounter++;
            } else if(state != 1 && receive instanceof Operation) {

                Operation op = (Operation) receive;
                doOperations(op);

            }
            else if(state != 1 && receive instanceof CreateLogin) {
                CreateLogin cl = (CreateLogin) receive;

                switch (cl.getType()) {
                    case REGISTER:
                        if(state != 1 && cl.getControl()) {
                            clres = new CreateLogin(Type.REGISTER, cl.getVMID(), cl.getMsgNumber(), bank.createAccount(cl.getPassword(),0), cl.getPassword(), false);
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
                            /** Traz dados e devolve ao utilizador para este atualizar a sua BD */
                            Data data = bank.exportData(st.getCreateLogin(),st.getOperation());

                            /** insert data */
                            StateTransfer opt = new StateTransfer(Type.SENDSTATE, vmid, data);
                            sendResponse(opt, false);
                        } else if(state == 1){
                            System.out.println("Keeping messages");
                            keep = true;
                        }
                        break;

                    case SENDSTATE:
                        if(state == 1) {
                            System.out.println("Receive State! ");

                            Data data = st.getBank();

                            /** atualizar dados */
                            bank.updateData(data);

                            /** Array sorted in arrival order */
                            for(Communication.Message m : keepMessages){
                                Operation op = (Operation) m;

                                /** Verificar se mensagem já foi realizada */
                                boolean bool = bank.operationRealized(op.getOrigin(),op.getMsgNumber());

                                if( bool ){
                                    System.out.println("\nRepeated message!\n");
                                    System.out.println("[" + op.getVMID().hashCode() + " - " + op.getMsgNumber() + "] ");
                                } else{
                                    doOperations(op);
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
        this.bank.shutdown();
        System.exit(0);
    }

    private synchronized void doOperations(Operation op) throws IOException{
        Response res;
        boolean result;
        switch (op.getType()) {
            case MOVE:
                result = bank.move(op.getAmount(), op);
                    res = new Response(Type.MOVE, result, op.getVMID(),
                            op.getMsgNumber(),op.getOrigin());
                    sendResponse(res, true);
                break;

            case TRANSFER:
                     result = bank.transfer(op.getOrigin(), op.getDestination(), op.getAmount(), op);
                    res = new Response(Type.TRANSFER, result, op.getVMID(),
                            op.getMsgNumber(),op.getOrigin());
                    sendResponse(res, true);

                break;

            case BALANCE:
                    res = new Response(Type.BALANCE, bank.getBalance(op.getOrigin()),
                            op.getVMID(), op.getMsgNumber(),op.getOrigin());
                    sendResponse(res, true);

                break;

            case MOVEMENTS:
                    // op.getAmount()refere-se ao número de movimentos
                    String str = bank.moveList(op.getOrigin(),op.getAmount());
                    res = new Response(Type.MOVEMENTS,op.getVMID(), op.getMsgNumber(),op.getOrigin(),str);
                    sendResponse(res, true);

                break;

            case LEAVE:
                    res = new Response(Type.LEAVE, op.getVMID(),
                            op.getMsgNumber(),op.getDestination());
                    sendResponse(res, false);

                break;
        }
    }
}
