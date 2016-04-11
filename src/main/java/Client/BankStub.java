package Client;

import Communication.Response;
import net.sf.jgcs.*;
import net.sf.jgcs.jgroups.JGroupsGroup;
import net.sf.jgcs.jgroups.JGroupsProtocolFactory;
import net.sf.jgcs.jgroups.JGroupsService;

import java.io.*;
import java.rmi.dgc.VMID;
import java.util.HashSet;

import static Communication.Message.*;

public class BankStub implements Bank.Bank, MessageListener {

    private ObjectOutputStream output;
    private DataSession dSession = null;
    private Communication.Message response = null;
    private ByteArrayOutputStream bOutput = null;

    private static final String vmid = new VMID().toString();
    private static int count = 0;

    private static HashSet<Integer> wMsg = new HashSet<Integer>();

    public BankStub() {
        try {
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

            Communication.Operation r = new Communication.Operation(Type.BALANCE,vmid, count);

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

            Communication.Operation r = new Communication.Operation(Type.MOVE, amount, vmid, count);

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

    public synchronized void leave() {
        try {
            this.bOutput = new ByteArrayOutputStream();
            this.output = new ObjectOutputStream(this.bOutput);

            Communication.Operation r = new Communication.Operation(Type.LEAVE, vmid, count);

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
            this.response = (Communication.Message) oisHere.readObject();

            if(this.response instanceof Communication.Response
                    && ((Response) this.response).getVmid().equals(vmid)
                    && wMsg.contains(((Response) this.response).getMsgNumber())) {
                notify();
                System.out.println("[" + ((Response) this.response).getMsgNumber()  + "]" + "Receive response!");
                wMsg.remove(((Response) this.response).getMsgNumber());
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
