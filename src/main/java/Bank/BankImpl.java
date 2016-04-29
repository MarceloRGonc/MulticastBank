package Bank;

import Communication.Operation;
import org.apache.derby.jdbc.EmbeddedXADataSource;
import javax.sql.XAConnection;
import java.io.Serializable;
import java.sql.*;
import java.util.Vector;

public class BankImpl implements Bank, Serializable{

    /** Database */
    private String nameDatabase;

    /** Connection BD */
    private static XAConnection xaconn;
    private static Connection conn;

    /** Counters */
    private int countAccount;
    private synchronized void addAccount(){ this.countAccount++; }
    private synchronized int getCountAccount() { return this.countAccount; }
    private synchronized void setCountAccount(int c){ this.countAccount = c; }

    private int countMoviment;
    private synchronized void addMoviment(){ this.countMoviment++; }
    private synchronized int getCountMoviment() { return this.countMoviment; }
    private synchronized void setCountMoviment(int c){ this.countMoviment = c; }

    /** Construtor */
    public BankImpl(String nameDatabase){
        this.nameDatabase = nameDatabase;
        initBD();
    }

    /** Create Connection, FALSE = ERROR */
    private boolean createConnection(){
        try {
            EmbeddedXADataSource ds = new EmbeddedXADataSource();
            ds.setDatabaseName(this.nameDatabase);
            ds.setCreateDatabase("create");
            xaconn = ds.getXAConnection();
            conn = xaconn.getConnection();
            return true;
        }
        catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /** Create schema DataBase, If Exists = FALSE */
    private boolean createSchema(){
        try {
            Statement stmt = conn.createStatement();
            stmt.execute("CREATE SCHEMA BANK");
            stmt.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** Set Schema DataBase If Exists = TRUE */
    private boolean setSchema(){
        try {
            Statement stmt = conn.createStatement();
            stmt.execute("SET SCHEMA BANK");
            stmt.close();
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Create Table Account If Exists = FALSE */
    private boolean createTableAccount() {
        try {
            Statement stmt = conn.createStatement();
            stmt.execute("CREATE TABLE ACCOUNTS (accountid INTEGER not NULL," +
                    "password VARCHAR(30) not NULL, balance INTEGER not NULL, PRIMARY KEY ( accountid ))");
            stmt.close();
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Create Table Moviments If Exists = FALSE*/
    private boolean createTableMoviments() {
        try {
            Statement stmt = conn.createStatement();
            stmt.execute("CREATE TABLE MOVIMENTS (id INTEGER not NULL, " +
                    "accountid INTEGER not NULL, msg INTEGER not NULL, operation VARCHAR(40) not NULL, " +
                    "balance INTEGER not NULL, PRIMARY KEY ( id ),FOREIGN KEY (accountid) References ACCOUNTS (accountid))");
            stmt.close();
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Shutdown connection */
    public void shutdown(){
        try {
            if (conn != null) {
                conn.close();
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** Init BD */
    private void initBD(){
        if(createConnection() == true){
            boolean flag = false;
            if(!createSchema()){
                flag = true;
            }
            setSchema();
            if(!flag){
                createTableAccount();
                System.out.println("Created Table Account;");
                createTableMoviments();
                System.out.println("Created Account Moviments;");
            }
            getCounters();
            System.out.println("Counter Account: " + getCountAccount());
            System.out.println("Counter Moviment: " + getCountMoviment());
        }
    }

    private void getCounters() {
        try {
            Statement stmt = conn.createStatement();

            /** Get last account inserted */
            ResultSet results = stmt.executeQuery("select * from ACCOUNTS Order by accountid DESC");

            if(results.next()){
                setCountAccount(results.getInt(1));
                addAccount();
            }
            else{
                setCountAccount(1);
            }

            results.close();
            stmt.close();
        }
        catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }

        try {
            Statement stmt = conn.createStatement();

            /** Get last moviment inserted */
            ResultSet results = stmt.executeQuery("select * from MOVIMENTS Order by id DESC");

            if(results.next()){
                setCountMoviment(results.getInt(1));
                addMoviment();
            }
            else{
                setCountMoviment(1);
            }

            results.close();
            stmt.close();
        }
        catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }

    }

    /** Insert accounts */
    public int createAccount(String password,int valor){
        int r = -1;
        try
        {
            String sql = "Insert into ACCOUNTS(accountid,password,balance) values (?,?,?)";

            PreparedStatement ps = conn.prepareStatement(sql);

            r = getCountAccount();

            ps.setInt(1,r);
            ps.setString(2,password);
            ps.setInt(3,valor);
            ps.executeUpdate();

            addAccount();

            ps.close();
        }
        catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
        return r;
    }

    /** Login account */
    public boolean loginAccount(int accountID, String password){
        boolean r = false;
        try
        {
            Statement stmt = conn.createStatement();
            ResultSet results = stmt.executeQuery("select * from ACCOUNTS where accountid = " + accountID);

            if(results.next()){
                if(password.equals(results.getString(2))){
                    r = true;
                }
            }

            results.close();
            stmt.close();
        }
        catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }

        return r;
    }

    /** Deposit or Withdraw */
    public boolean move(int value, Operation op) {
        boolean r = false;
        try
        {
            Statement stmt = conn.createStatement();

            ResultSet result = stmt.executeQuery("select * from ACCOUNTS where accountid = " + op.getOrigin());

            if(result.next()){

                int balance = result.getInt(3);
                int move = balance + op.getAmount();

                if( move >= 0){

                    String operation;
                    if(op.getAmount() > 0){
                        operation = "Deposit: " + value;
                    }
                    else{
                        operation = "Withdraw: " + value;
                    }

                    String sqlUpdate = "update ACCOUNTS set balance = " + move + " where accountid = " +  op.getOrigin();
                    String sqlMove = "insert into MOVIMENTS(id,accountid,msg,operation,balance) values (?,?,?,?,?)";

                    PreparedStatement ps = conn.prepareStatement(sqlUpdate);
                    ps.executeUpdate();

                    ps = conn.prepareStatement(sqlMove);
                    ps.setInt(1,getCountMoviment());
                    ps.setInt(2,op.getOrigin());
                    ps.setInt(3,op.getMsgNumber());
                    ps.setString(4,operation);
                    ps.setInt(5,move);
                    ps.executeUpdate();

                    addMoviment();

                    ps.close();

                    r = true;
                }
            }
            result.close();
            stmt.close();
        }
        catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
        return r;
    }

    /** GetBalance */
    public int getBalance(int accountId) {
        int r = -1;
        try {
            Statement stmt = conn.createStatement();

            ResultSet result = stmt.executeQuery("select * from ACCOUNTS where accountid = " + accountId);

            if(result.next()){
                r = result.getInt(3);
            }

            result.close();
            stmt.close();
        }
        catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }

        return r;
    }

    /** Transfer */
    public boolean transfer(int source, int dest, int amount, Operation op){

        boolean r = false;
        try {
            Statement stmt = conn.createStatement();

            /** Verifies if the second account exists */
            ResultSet result = stmt.executeQuery("select * from ACCOUNTS where accountid = " + dest);

            if(result.next()){

                int balanceAccount2 = result.getInt(3);

                /** Get balance from account one */
                result = stmt.executeQuery("select * from ACCOUNTS where accountid = " + source);

                if(result.next()){

                    int balanceAccount1 = result.getInt(3);
                    int move = balanceAccount1 - amount;

                    if(move >= 0 ){

                        /** Transfer */
                        balanceAccount2 += amount;
                        String operationAccount1 = "Give " + amount + " to " + dest;
                        String operationAccount2 = "Receive " + amount + " from " + source;

                        /** Account 1 */
                        String sqlUpdate = "update ACCOUNTS set balance = " + move + " where accountid = " +  op.getOrigin();
                        String sqlMove = "insert into MOVIMENTS(id,accountid,msg,operation,balance) values (?,?,?,?,?)";

                        PreparedStatement ps = conn.prepareStatement(sqlUpdate);
                        ps.executeUpdate();

                        ps = conn.prepareStatement(sqlMove);
                        ps.setInt(1,getCountMoviment());
                        ps.setInt(2,op.getOrigin());
                        ps.setInt(3,op.getMsgNumber());
                        ps.setString(4,operationAccount1);
                        ps.setInt(5,move);
                        ps.executeUpdate();

                        addMoviment();

                        /** Account 2 */
                        sqlUpdate = "update ACCOUNTS set balance = "
                                + balanceAccount2 + " where accountid = " +  op.getDestination();
                        sqlMove = "insert into MOVIMENTS(id,accountid,msg,operation,value) values (?,?,?,?,?)";

                        ps = conn.prepareStatement(sqlUpdate);
                        ps.executeUpdate();

                        ps = conn.prepareStatement(sqlMove);
                        ps.setInt(1,getCountMoviment());
                        ps.setInt(2,op.getDestination());
                        ps.setInt(3,op.getMsgNumber());
                        ps.setString(4,operationAccount2);
                        ps.setInt(5,balanceAccount2);
                        ps.executeUpdate();

                        addMoviment();

                        ps.close();

                        r = true;
                    }
                }
            }
            result.close();
            stmt.close();
        }
        catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
        return r;
    }


    /** Get last N moviments */
    public String moveList(int accountId, int nMoviments){
        String r = "";
        try {
            Statement stmt = conn.createStatement();

            /** Get moviments from accountId */
            ResultSet result = stmt.executeQuery("select * from MOVIMENTS where accountid = " + accountId + " order by id DESC");

            StringBuilder s = new StringBuilder();
            s.append("\n--- Moviments ---\n");

            /** Moviments */
            while( (nMoviments != 0) && result.next()) {
                s.append("Move id: " + result.getInt(3));
                s.append("\nOperation: " + result.getString(4));
                s.append("\nBalance: " + result.getInt(5));
                s.append("- - - - - - - - -\n");
                --nMoviments;
            }
            s.append("-----------------\n");
            r =  s.toString();

            result.close();
            stmt.close();
        }
        catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }

        return r;
    }

    /** Last account inserted */
    public int lastAccountInserted(){
        if(getCountAccount() == 1){
            return -1;
        }
        else {
            return getCountAccount() - 1;
        }
    }

    /** Last moviment inserted */
    public int lastMovimentInserted(){
       if(getCountMoviment() == 1) {
           return -1;
       }
       else {
           return getCountMoviment() - 1;
       }
    }

    /** Operation realized */
    public boolean operationRealized(int accountId, int msg){
        boolean r = false;
        try {
            Statement stmt = conn.createStatement();
            ResultSet results = stmt.executeQuery("select * from MOVIMENTS where accountid = " + accountId + " and msg = " + msg);

            if(results.next()){
                r = true;
            }

            results.close();
            stmt.close();
        }
        catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
        return r;
    }

    /** Creates Data to send to another server */
    public synchronized Data exportData(int cl, int op) {

        Data data = new Data();

        /** Get accounts*/
        try {
            Statement stmt = conn.createStatement();

            ResultSet result = stmt.executeQuery("select * from ACCOUNTS where accountid > " + cl);

            while (result.next()) {
                data.InsertAccounts(result.getInt(1), result.getString(2), result.getInt(3));
            }

            result.close();
            stmt.close();
        } catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }

        /** Get Moviments */
        try {
            Statement stmt = conn.createStatement();

            ResultSet result = stmt.executeQuery("select * from MOVIMENTS where id > " + op);

            while (result.next()) {
                Operation newop = new Operation(result.getInt(1), result.getInt(2), result.getInt(3), result.getString(4), result.getInt(5));
                data.insertMoviments(newop);
            }
            result.close();
            stmt.close();
        } catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
        return data;
    }

    /** Update Database with data from other server */
    public synchronized void updateData(Data data){

        /** Accounts */
        Vector<String> accounts = data.getAccounts();

        PreparedStatement ps;
        String sql;
        String parts[];

        for(String str : accounts){
            parts = str.split(":");

            sql = "INSERT into ACCOUNTS(accountid,password,balance) values (?,?,?)";
            try {
                ps = conn.prepareStatement(sql);
                ps.setInt(1,Integer.parseInt(parts[0]));
                ps.setString(2,parts[1]);
                ps.setInt(3,Integer.parseInt(parts[2]));
                ps.executeUpdate();
                addAccount();
            }
            catch (SQLException sqlExcept){
                sqlExcept.printStackTrace();
            }
        }

        /** Moviments */
        Vector<Operation> moviments = data.getOperations();

        for(Operation op : moviments){
            sql = "Insert into MOVIMENTS(id,accountid,msg,operation,balance) values (?,?,?,?,?)";
            try {
                doUpdate(op.getBalance(), op.getAccountid());
                ps = conn.prepareStatement(sql);
                ps.setInt(1,op.getId());
                ps.setInt(2,op.getAccountid());
                ps.setInt(3,op.getMsgNumber());
                ps.setString(4,op.getOperation());
                ps.setInt(5,op.getBalance());
                ps.executeUpdate();
                addMoviment();
            }
            catch (SQLException sqlExcept){
                sqlExcept.printStackTrace();
            }
        }
    }

    /** Update de balance from the account */
    private void doUpdate(int bal, int id) throws SQLException {
        Statement stmt = conn.createStatement();
        String sqlUpdate = "UPDATE ACCOUNTS set balance = " + bal + " where accountid = " +  id;
        stmt.execute(sqlUpdate);
    }
}
