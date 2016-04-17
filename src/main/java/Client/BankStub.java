package Client;

import Communication.*;
import net.sf.jgcs.*;
import net.sf.jgcs.Message;
import net.sf.jgcs.jgroups.*;

import java.io.*;
import java.rmi.dgc.VMID;
import java.util.HashSet;

import static Communication.Message.*;

public class BankStub implements Bank.Bank, MessageListener {

    private ObjectOutputStream output;
    private DataSession dSession = null;
    private ByteArrayOutputStream bOutput = null;

    /** Account id */
    private static int accountId;

    /** Identification */
    private static final String vmid = new VMID().toString();

    /** Message counter */
    private static int count = 0;

    private Response response = null;
    private static HashSet<Integer> wMsg = new HashSet<>();

    public BankStub(int accountId) {
        try {

            this.accountId = accountId;

            JGroupsProtocolFactory pf = new JGroupsProtocolFactory();
            JGroupsGroup group = new JGroupsGroup("Bank");
            Protocol p = pf.createProtocol();
            this.dSession = p.openDataSession(group);
            this.dSession.setMessageListener(this);

            ControlSession cs = p.openControlSession(group);
            cs.join();
        } catch (IOException ex) {
            System.out.println("Something went wrong!");
        }
    }

    public synchronized float getBalance() {
        float res = -1;
        try {
            this.bOutput = new ByteArrayOutputStream();
            this.output = new ObjectOutputStream(this.bOutput);

            Communication.Operation r = new Communication.Operation(Type.BALANCE, vmid, count, this.accountId);

            wMsg.add(count++);

            this.output.writeObject(r);

            byte[] data = bOutput.toByteArray();

            Message msg = dSession.createMessage();
            msg.setPayload(data);
            this.response = null;
            dSession.multicast(msg, new JGroupsService(), null);

            while (this.response == null) {
                wait();
            }

            res = this.response.getAmount();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return res;
    }

    public synchronized boolean move(float amount) {
        boolean res = false;

        try {
            this.bOutput = new ByteArrayOutputStream();
            this.output = new ObjectOutputStream(this.bOutput);

            Communication.Operation r = new Communication.Operation(Type.MOVE, vmid, count, this.accountId, amount);

            wMsg.add(count++);

            this.output.writeObject(r);
            byte[] data = bOutput.toByteArray();

            Message msg = dSession.createMessage();
            msg.setPayload(data);
            this.response = null;
            dSession.multicast(msg, new JGroupsService(), null);

            while (this.response == null) {
                wait();
            }

            res = this.response.getResponse();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return res;
    }

    @Override
    public boolean transfer(int dest, int amount) {
        return false;
    }

    @Override
    public boolean movements(int n) {
        return false;
    }

    public synchronized void leave() {
        try {
            this.bOutput = new ByteArrayOutputStream();
            this.output = new ObjectOutputStream(this.bOutput);

            Communication.Operation r = new Communication.Operation(Type.LEAVE, vmid, count, this.accountId);

            output.writeObject(r);

            wMsg.add(count++);

            byte[] data = bOutput.toByteArray();

            Message msg = dSession.createMessage();
            msg.setPayload(data);
            this.response = null;
            dSession.multicast(msg, new JGroupsService(), null);

            while (this.response == null) {
                wait();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public synchronized Object onMessage(Message msg) {
        ObjectInputStream oisHere = null;
        try {

            ByteArrayInputStream bInput = new ByteArrayInputStream(msg.getPayload());
            oisHere = new ObjectInputStream(bInput);
            Communication.Message res = (Communication.Message) oisHere.readObject();

            if(res instanceof Communication.Response) {

                this.response = (Response) res;

                if( this.response.getVMID().equals(vmid)
                        && wMsg.contains(this.response.getMsgNumber())) {

                    notify();
                    System.out.println("[" + this.response.getMsgNumber()  + "]" + "Receive response!");
                    wMsg.remove(this.response.getMsgNumber());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (oisHere != null) { oisHere.close(); }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }
}
