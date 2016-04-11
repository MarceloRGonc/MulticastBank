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

    public Object onMessage(Message msg) {
        try {
            ObjectInputStream oInput;
            ByteArrayInputStream bInput = new ByteArrayInputStream(msg.getPayload());
            oInput = new ObjectInputStream(bInput);
            Communication.Message op = (Communication.Message) oInput.readObject();

            Response res = null;
            boolean flag = true;
            if(op instanceof Operation || op instanceof StateTransfer) {
                switch (op.getType()) {
                    case MOVE:
                        if(state != 1) {
                            float value = op.getAmount();
                            boolean result = bank.move(value);
                            res = new Response(Type.MOVE, result, ((Operation) op).getVmid(),
                                    ((Operation) op).getMsgNumber());
                        }
                        break;
                    case BALANCE:
                        if(state != 1) {
                            res = new Response(Type.BALANCE, bank.getBalance(),
                                    ((Operation) op).getVmid(), ((Operation) op).getMsgNumber());
                        }
                        break;
                    case LEAVE:
                        if(state != 1) {
                            res = new Response(Type.LEAVE, ((Operation) op).getVmid(),
                                    ((Operation) op).getMsgNumber());
                            flag = false;
                        }
                        break;
                    case STATE:
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
                        break;
                }
                if(op instanceof Operation) {
                    ByteArrayOutputStream bOuput = new ByteArrayOutputStream();
                    ObjectOutputStream oosHere = new ObjectOutputStream(bOuput);

                    oosHere.writeObject(res);
                    byte[] data = bOuput.toByteArray();
                    Message toSend = dSession.createMessage();
                    toSend.setPayload(data);
                    System.out.println("[" + res.getVmid().hashCode() + " - " + res.getMsgNumber() + "] "
                            + "Sent response! Balance: " + bank.getBalance());
                    dSession.multicast(toSend, new JGroupsService(), null);
                    add();

                    if (!flag) {
                        System.out.println("Movements: " + getCount());
                    }
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

            if( list.size() != 0 && state != 1){

                StateTransfer res = new StateTransfer(Type.STATE, 0, bank);

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
            } else {
                //state = 2;
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
