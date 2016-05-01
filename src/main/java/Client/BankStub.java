package Client;

import Bank.Bank;
import Communication.CreateLogin;
import Communication.Operation;
import Communication.Response;
import net.sf.jgcs.*;
import net.sf.jgcs.jgroups.JGroupsGroup;
import net.sf.jgcs.jgroups.JGroupsProtocolFactory;
import net.sf.jgcs.jgroups.JGroupsService;

import java.io.*;
import java.rmi.dgc.VMID;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import static Communication.Message.Type;

public class BankStub implements Bank, MessageListener {

    /** Identification */
    private static final String vmid = new VMID().toString();

    private DataSession dSession = null;

    /** Message counter */
    private static int count = 0;

    private ConcurrentHashMap<Integer,Response> responses;
    private ConcurrentHashMap<Integer,CreateLogin> clResponses;
    private HashSet<Integer> wMsg = new HashSet<>();

    /** Number accountId */
    private int accountIdNumber;

    /** Set AccountId */
    public void setAccountIdNumber(int accountId){
        this.accountIdNumber = accountId;
    }

    public BankStub() {
        try {
            JGroupsProtocolFactory pf = new JGroupsProtocolFactory();
            JGroupsGroup group = new JGroupsGroup("Bank");
            Protocol p = pf.createProtocol();
            this.dSession = p.openDataSession(group);
            this.dSession.setMessageListener(this);
            this.responses = new ConcurrentHashMap<>();
            this.clResponses = new ConcurrentHashMap<>();

            ControlSession cs = p.openControlSession(group);
            cs.join();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public synchronized int getBalance(int accountId) {
        int res = -1;
        try {
            ByteArrayOutputStream bOutput = new ByteArrayOutputStream();
            ObjectOutputStream output = new ObjectOutputStream(bOutput);

            Communication.Operation r = new Communication.Operation(Type.BALANCE, vmid, count, accountId);

            int i = count++;
            addWMsg(i);

            output.writeObject(r);

            byte[] data = bOutput.toByteArray();

            Message msg = dSession.createMessage();
            msg.setPayload(data);

            dSession.multicast(msg, new JGroupsService(), null);

            while (!responses.containsKey(i)) {
                wait();
            }

            res = responses.get(i).getAmount();
            responses.remove(i);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return res;
    }

    public synchronized boolean move(int amount, Operation op) {
        boolean res = false;

        try {
            ByteArrayOutputStream bOutput = new ByteArrayOutputStream();
            ObjectOutputStream output = new ObjectOutputStream(bOutput);

            Communication.Operation r = new Communication.Operation(Type.MOVE, vmid, count, this.accountIdNumber, amount);

            int i = count++;
            addWMsg(i);

            output.writeObject(r);
            byte[] data = bOutput.toByteArray();

            Message msg = dSession.createMessage();
            msg.setPayload(data);

            dSession.multicast(msg, new JGroupsService(), null);

            while (!responses.containsKey(i)) {
                wait();
            }

            res = responses.get(i).getResponse();
            responses.remove(i);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return res;
    }

    public synchronized int createAccount(String password) {
        int res = -1;
        try {
            ByteArrayOutputStream bOutput = new ByteArrayOutputStream();
            ObjectOutputStream output = new ObjectOutputStream(bOutput);

            Communication.CreateLogin r = new Communication.CreateLogin(Type.REGISTER, vmid, count, 0, password,true);

            int i = count++;
            addWMsg(i);

            output.writeObject(r);

            byte[] data = bOutput.toByteArray();

            Message msg = dSession.createMessage();
            msg.setPayload(data);

            dSession.multicast(msg, new JGroupsService(), null);

            while (!clResponses.containsKey(i)) {
                wait();
            }

            res = clResponses.get(i).getAccount();
            clResponses.remove(i);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return res;
    }

    public synchronized boolean login(int accountId, String password) {
        boolean bool = false;
        try {
            ByteArrayOutputStream bOutput = new ByteArrayOutputStream();
            ObjectOutputStream output = new ObjectOutputStream(bOutput);

            Communication.CreateLogin r = new Communication.CreateLogin(Type.LOGIN, vmid, count, accountId, password,true);

            output.writeObject(r);

            int i = count++;
            addWMsg(i);

            byte[] data = bOutput.toByteArray();

            Message msg = dSession.createMessage();
            msg.setPayload(data);

            dSession.multicast(msg, new JGroupsService(), null);

            while (!clResponses.containsKey(i)) {
                wait();
            }

            bool = clResponses.get(i).getSucess();
            clResponses.remove(i);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return bool;
    }

    @Override
    public synchronized boolean transfer(int source,int dest, int amount, Operation o) {
        boolean res = false;

        try {
            ByteArrayOutputStream bOutput = new ByteArrayOutputStream();
            ObjectOutputStream output = new ObjectOutputStream(bOutput);

            Communication.Operation r = new Communication.Operation(Type.TRANSFER, vmid, count, source, dest, amount);

            int i = count++;
            addWMsg(i);

            output.writeObject(r);
            byte[] data = bOutput.toByteArray();

            Message msg = dSession.createMessage();
            msg.setPayload(data);

            dSession.multicast(msg, new JGroupsService(), null);

            while (!responses.containsKey(i)) {
                wait();
            }

            res = responses.get(i).getResponse();
            responses.remove(i);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return res;
    }

    public synchronized void leave(int accountId) {
        try {
            ByteArrayOutputStream bOutput = new ByteArrayOutputStream();
            ObjectOutputStream output = new ObjectOutputStream(bOutput);

            Communication.Operation r = new Communication.Operation(Type.LEAVE, vmid, count, accountId);

            output.writeObject(r);

            int i = count++;
            addWMsg(i);

            byte[] data = bOutput.toByteArray();

            Message msg = dSession.createMessage();
            msg.setPayload(data);

            dSession.multicast(msg, new JGroupsService(), null);

            while (!responses.containsKey(i)) {
                wait();
            }

            responses.remove(i);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public synchronized String moveList(int accountId, int nMoviments){
        String res = "";

        try {
            ByteArrayOutputStream bOutput = new ByteArrayOutputStream();
            ObjectOutputStream output = new ObjectOutputStream(bOutput);

            Communication.Operation r = new Communication.Operation(Type.MOVEMENTS, vmid, count, accountId, nMoviments);
            int i = count++;
            addWMsg(i);

            output.writeObject(r);
            byte[] data = bOutput.toByteArray();

            Message msg = dSession.createMessage();
            msg.setPayload(data);

            dSession.multicast(msg, new JGroupsService(), null);

            while (!responses.containsKey(i)) {
                wait();
            }

            res = responses.get(i).getResult();
            responses.remove(i);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return res;
    }

    public synchronized Object onMessage(Message msg) {
        ObjectInputStream oisHere = null;
        try {
            ByteArrayInputStream bInput = new ByteArrayInputStream(msg.getPayload());
            oisHere = new ObjectInputStream(bInput);
            Communication.Message res = (Communication.Message) oisHere.readObject();

            if(res instanceof Communication.Response) {

                Response r = (Response) res;

                if( r.getVMID().equals(vmid) && wMsg.contains(r.getMsgNumber())) {

                    responses.put(r.getMsgNumber(), r);
                    notify();
                    removeWMsg(r.getMsgNumber());
                }
            }
            else if(res instanceof CreateLogin) {

                CreateLogin clR = (CreateLogin) res;

                if( clR.getVMID().equals(vmid) && wMsg.contains(clR.getMsgNumber()) && !clR.getControl()) {
                    clResponses.put(clR.getMsgNumber(), clR);
                    notify();
                    removeWMsg(clR.getMsgNumber());
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

    private synchronized void addWMsg(Integer msg){
        wMsg.add(msg);
    }

    private synchronized void removeWMsg(Integer msg){
        wMsg.remove(msg);
    }
}
