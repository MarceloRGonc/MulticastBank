package Client;

import Communication.*;
import net.sf.jgcs.*;
import net.sf.jgcs.Message;
import net.sf.jgcs.jgroups.*;
import java.io.*;
import java.rmi.dgc.VMID;
import java.util.HashSet;
import Bank.Bank;

import static Communication.Message.*;

public class BankStub implements Bank, MessageListener {

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
    private CreateLogin createLoginResponse = null;
    private static HashSet<Integer> wMsg = new HashSet<>();

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

    /** Update account number */
    public void setAccountId(int accountId){
        this.accountId = accountId;
    }

    public synchronized int getBalance(int accountId) {
        int res = -1;
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

    public synchronized boolean move(int amount, Operation op) {
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

    // Create Account
    // -1 erro
    public synchronized int createAccount(String password) {
        int res = -1;
        try {
            this.bOutput = new ByteArrayOutputStream();
            this.output = new ObjectOutputStream(this.bOutput);

            // Valor 0 é para ser ignorado, apenas colocado para não criar mais um tipo de mensagem
            Communication.CreateLogin r = new Communication.CreateLogin(Type.REGISTER, vmid, count, 0, password,true);

            wMsg.add(count++);

            this.output.writeObject(r);

            byte[] data = bOutput.toByteArray();

            Message msg = dSession.createMessage();
            msg.setPayload(data);

            this.createLoginResponse = null;
            dSession.multicast(msg, new JGroupsService(), null);

            // Espera resposta
            while (this.createLoginResponse == null) {
                wait();
            }

            res = this.createLoginResponse.getAccount();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return res;
    }

    // Login
    public synchronized boolean login(int accountId, String password) {
        boolean bool = false;
        try {
            this.bOutput = new ByteArrayOutputStream();
            this.output = new ObjectOutputStream(this.bOutput);

            // Valor 0 é para ser ignorado, apenas colocado para não criar mais um tipo de mensagem
            Communication.CreateLogin r = new Communication.CreateLogin(Type.LOGIN, vmid, count, accountId, password,true);

            output.writeObject(r);

            wMsg.add(count++);

            byte[] data = bOutput.toByteArray();

            Message msg = dSession.createMessage();
            msg.setPayload(data);
            this.createLoginResponse = null;
            dSession.multicast(msg, new JGroupsService(), null);

            while (this.createLoginResponse == null) {
                wait();
            }

            bool = this.createLoginResponse.getSucess();

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
            this.bOutput = new ByteArrayOutputStream();
            this.output = new ObjectOutputStream(this.bOutput);

            Communication.Operation r = new Communication.Operation(Type.TRANSFER, vmid, count, source, dest, amount);

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

    public synchronized String moveList(int accountId, int nMoviments){
        String res = "";

        try {
            this.bOutput = new ByteArrayOutputStream();
            this.output = new ObjectOutputStream(this.bOutput);

            Communication.Operation r = new Communication.Operation(Type.MOVEMENTS, vmid, count, this.accountId, nMoviments);

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

            res = this.response.getResult();

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

                this.response = (Response) res;

                if( this.response.getVMID().equals(vmid)
                        && wMsg.contains(this.response.getMsgNumber())) {

                    notify();
                    System.out.println("[" + this.response.getMsgNumber()  + "]" + "Receive response!");
                    wMsg.remove(this.response.getMsgNumber());
                }
            }
            else if(res instanceof CreateLogin) {

                this.createLoginResponse = (CreateLogin) res;

                if( this.createLoginResponse.getVMID().equals(vmid)
                        && wMsg.contains(this.createLoginResponse.getMsgNumber())
                        && !this.createLoginResponse.getControl()) {

                    notify();
                    System.out.println("[" + this.createLoginResponse.getMsgNumber()  + "]" + "Receive response!");
                    wMsg.remove(this.createLoginResponse.getMsgNumber());
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
