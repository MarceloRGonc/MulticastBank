package Bank;

import Communication.Operation;
import org.apache.derby.jdbc.EmbeddedXADataSource;
import javax.sql.XAConnection;
import java.io.Serializable;
import java.sql.*;
import java.util.Vector;

/**
 * Bank implementation to be used by the server
 * */
public class BankImpl implements Bank, Serializable{

    /** Database name, identificates the server */
    private String nameDatabase;

    /** Connection BD */
    private static XAConnection xaconn;
    private static Connection conn;

    /** Current account number */
    private int countAccount;
    private synchronized void addAccount(){ this.countAccount++; }
    private synchronized int getCountAccount() { return this.countAccount; }
    private synchronized void setCountAccount(int c){ this.countAccount = c; }

    /** Current movement number */
    private int countMovement;
    private synchronized void addMovement(){ this.countMovement++; }
    private synchronized int getCountMovement() { return this.countMovement; }
    private synchronized void setCountMovement(int c){ this.countMovement = c; }

    /** Construtor
     * @param nameDatabase - server name
     * */
    public BankImpl(String nameDatabase){
        this.nameDatabase = nameDatabase;
        initBD();
    }

    /** Create Connection,
     * @return false - error, true - success
     * */
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

    /** Create schema DataBase,
     * @return false - if schema exists, true - create with success
     */
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

    /** Set Schema DataBase,
     * @return true - if schema exists
     * */
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

    /** Create Table Account,
     * @return false - if table exists, true - create with success
     */
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

    /** Create Table Movements,
     * @return false - if table exists, true - create with success
     */
    private boolean createTableMovements() {
        try {
            Statement stmt = conn.createStatement();
            stmt.execute("CREATE TABLE MOVEMENTS (id INTEGER not NULL, " +
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

    /** Initialize database */
    private void initBD(){
        if(createConnection() == true){
            boolean flag = false;
            if(!createSchema()){
                flag = true;
            }
            setSchema();
            if(!flag){
                createTableAccount();
                createTableMovements();
            }
            getCounters();
            getCounters();
        }
    }

    /** Accesses to the database add update the counters in memory */
    private void getCounters() {
        try {
            Statement stmt = conn.createStatement();

            /** Get last account inserted */
            ResultSet results = stmt.executeQuery("select * from ACCOUNTS Order by accountid DESC");

            if(results.next()){
                /** Set new value */
                setCountAccount(results.getInt(1));
                addAccount();
            } else{
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

            /** Get last movement inserted */
            ResultSet results = stmt.executeQuery("select * from MOVEMENTS Order by id DESC");

            if(results.next()){
                /** Set new value */
                setCountMovement(results.getInt(1));
                addMovement();
            } else{
                setCountMovement(1);
            }

            results.close();
            stmt.close();
        }
        catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }

    }

    /** Creates a new account
     * @param password - user password
     * @param value - opening balance
     * @return if successful account id, else -1
     */
    public int createAccount(String password, int value){
        int r = -1;
        try {
            String sql = "Insert into ACCOUNTS(accountid,password,balance) values (?,?,?)";

            PreparedStatement ps = conn.prepareStatement(sql);

            r = getCountAccount();

            ps.setInt(1,r);
            ps.setString(2,password);
            ps.setInt(3,value);
            ps.executeUpdate();

            addAccount();

            ps.close();
        }
        catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
        return r;
    }

    /** Login Account
     * @param accountID - account id
     * @param password - user password
     * @return if successful true, else false
     */
    public boolean loginAccount(int accountID, String password){
        boolean r = false;
        try {
            Statement stmt = conn.createStatement();
            ResultSet results = stmt.executeQuery("select * from ACCOUNTS where accountid = " + accountID);

            if(results.next()){
                if(password.equals(results.getString(2))){
                    r = true;
                }
            }

            results.close();
            stmt.close();
        } catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }

        return r;
    }

    /** Deposit or Withdraw
     * @param value - movement value
     * @param op - operation data to perform
     * @return if successful true, else false
     */
    public boolean move(int value, Operation op) {
        boolean r = false;
        try {
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
                    String sqlMove = "insert into MOVEMENTS(id,accountid,msg,operation,balance) values (?,?,?,?,?)";

                    PreparedStatement ps = conn.prepareStatement(sqlUpdate);
                    ps.executeUpdate();

                    ps = conn.prepareStatement(sqlMove);
                    ps.setInt(1,getCountMovement());
                    ps.setInt(2,op.getOrigin());
                    ps.setInt(3,op.getMsgNumber());
                    ps.setString(4,operation);
                    ps.setInt(5,move);
                    ps.executeUpdate();

                    addMovement();

                    ps.close();

                    r = true;
                }
            }
            result.close();
            stmt.close();
        } catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
        return r;
    }


    /** Get Balance
     * @param accountId - account id
     * @return if successful account balance, else -1
     */
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

    /** Transfer
     * @param source - origin account id
     * @param dest - destination account id
     * @param amount - value to transfer
     * @param op - operation data to perform
     * @return if successful true, else false
     */
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
                        String sqlMove = "insert into MOVEMENTS(id,accountid,msg,operation,balance) values (?,?,?,?,?)";

                        PreparedStatement ps = conn.prepareStatement(sqlUpdate);
                        ps.executeUpdate();

                        ps = conn.prepareStatement(sqlMove);
                        ps.setInt(1,getCountMovement());
                        ps.setInt(2,op.getOrigin());
                        ps.setInt(3,op.getMsgNumber());
                        ps.setString(4,operationAccount1);
                        ps.setInt(5,move);
                        ps.executeUpdate();

                        addMovement();

                        /** Account 2 */
                        sqlUpdate = "update ACCOUNTS set balance = "
                                + balanceAccount2 + " where accountid = " +  op.getDestination();
                        sqlMove = "insert into MOVEMENTS(id,accountid,msg,operation,balance) values (?,?,?,?,?)";

                        ps = conn.prepareStatement(sqlUpdate);
                        ps.executeUpdate();

                        ps = conn.prepareStatement(sqlMove);
                        ps.setInt(1,getCountMovement());
                        ps.setInt(2,op.getDestination());
                        ps.setInt(3,op.getMsgNumber());
                        ps.setString(4,operationAccount2);
                        ps.setInt(5,balanceAccount2);
                        ps.executeUpdate();

                        addMovement();

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


    /** Get last N movements
     * @param accountId - account id
     * @param nMovements - number of movements to return
     * @return made movements
     */
    public String moveList(int accountId, int nMovements){
        String r = "";
        try {
            Statement stmt = conn.createStatement();

            /** Get movements from accountId */
            ResultSet result = stmt.executeQuery("select * from MOVEMENTS where accountid = " + accountId + " order by id DESC");

            StringBuilder s = new StringBuilder();
            s.append("\n--- Movements ---\n");

            /** Movements */
            while( (nMovements != 0) && result.next()) {
                s.append("Move id: " + result.getInt(3));
                s.append("\nOperation: " + result.getString(4));
                s.append("\nBalance: " + result.getInt(5));
                s.append("\n- - - - - - - - -\n");
                --nMovements;
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

    /** Last movement inserted */
    public int lastMovementInserted(){
       if(getCountMovement() == 1) {
           return -1;
       }
       else {
           return getCountMovement() - 1;
       }
    }

    /** Verifies if operation has been performed
     * @param accountId - account id
     * @param msgNumber - message number
     * @return if successful true, else false
     */
    public boolean operationRealized(int accountId, int msgNumber){
        boolean r = false;
        try {
            Statement stmt = conn.createStatement();
            ResultSet results = stmt.executeQuery("select * from MOVEMENTS where accountid = " + accountId + " and msg = " + msgNumber);

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

    /** Creates Data to send to another server
     * @param cl - last account created
     * @param op - last operation made
     * @return Data object - with all the missing data
     */
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

        /** Get Movements */
        try {
            Statement stmt = conn.createStatement();

            ResultSet result = stmt.executeQuery("select * from MOVEMENTS where id > " + op);

            while (result.next()) {
                Operation newop = new Operation(result.getInt(1), result.getInt(2), result.getInt(3), result.getString(4), result.getInt(5));
                data.insertMovements(newop);
            }
            result.close();
            stmt.close();
        } catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
        return data;
    }

    /** Update Database with data from other server
     * @param data - Data object with all the missing data
     */
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

        /** Movements */
        Vector<Operation> movements = data.getOperations();

        for(Operation op : movements){
            sql = "Insert into MOVEMENTS(id,accountid,msg,operation,balance) values (?,?,?,?,?)";
            try {
                doUpdate(op.getBalance(), op.getAccountid());
                ps = conn.prepareStatement(sql);
                ps.setInt(1,op.getId());
                ps.setInt(2,op.getAccountid());
                ps.setInt(3,op.getMsgNumber());
                ps.setString(4,op.getOperation());
                ps.setInt(5,op.getBalance());
                ps.executeUpdate();
                addMovement();
            }
            catch (SQLException sqlExcept){
                sqlExcept.printStackTrace();
            }
        }
    }

    /** Update de balance from the account
     * @param bal - new balance
     * @param id - account id
     * @throws SQLException
     */
    private void doUpdate(int bal, int id) throws SQLException {
        Statement stmt = conn.createStatement();
        String sqlUpdate = "UPDATE ACCOUNTS set balance = " + bal + " where accountid = " +  id;
        stmt.execute(sqlUpdate);
    }
}
