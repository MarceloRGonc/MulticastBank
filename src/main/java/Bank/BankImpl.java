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
    private static XAConnection xaconn = null;
    private static Connection conn = null;
    private static Statement stmt = null;

    /** Contador */
    private int countAccount;
    private void addAccount(){ this.countAccount++; }
    private int getCountAccount() { return this.countAccount; }
    private void setCountAccount(int c){ this.countAccount = c; }

    private int countMoviment;
    private void addMoviment(){ this.countMoviment++; }
    private int getCountMoviment() { return this.countMoviment; }
    private void setCountMoviment(int c){ this.countMoviment = c; }

    /** Construtor */
    public BankImpl(String nameDatabase){
        this.nameDatabase = nameDatabase;
        initBD();
    }

    /** Create and Connect Database */

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
            return false;
        }
    }

    /** Create schema DataBase, If Exists = FALSE */
    private boolean createSchema(){
        try{
            stmt = conn.createStatement();
            stmt.execute("CREATE SCHEMA BANK");
            stmt.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** Set Schema DataBase If Exists = TRUE*/
    private boolean setSchema(){
        try
        {
            stmt = conn.createStatement();
            stmt.execute("SET SCHEMA BANK");
            stmt.close();
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    /** Create Table Account If Exists = FALSE*/
    private boolean createTableAccount() {
        try
        {
            stmt = conn.createStatement();
            stmt.execute("CREATE TABLE ACCOUNTS (accountid INTEGER not NULL," +
                    "password VARCHAR(30) not NULL, balance INTEGER not NULL, PRIMARY KEY ( accountid ))");
            stmt.close();
            return true;
        }
        catch (Exception e)
        {
            System.out.println(e);
            return false;
        }
    }

    /** Create Table Moviments If Exists = FALSE*/
    private boolean createTableMoviments() {
        try
        {
            stmt = conn.createStatement();
            stmt.execute("CREATE TABLE MOVIMENTS (id INTEGER not NULL, " +
                    "accountid INTEGER not NULL, msg INTEGER not NULL, operation VARCHAR(40) not NULL, " +
                    "balance INTEGER not NULL, PRIMARY KEY ( id ),FOREIGN KEY (accountid) References ACCOUNTS (accountid))");
            stmt.close();
            return true;
        }
        catch (Exception e)
        {
            System.out.println(e);
            return false;
        }
    }

    /** Shutdown connection */
    public void shutdown(){
        try
        {
            if (stmt != null)
            {
                stmt.close();
            }
            if (conn != null)
            {
                conn.close();
            }
        }
        catch (SQLException sqlExcept) {}
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
                boolean tabela1 = createTableAccount();
                System.out.print("Created Table Account: ");
                System.out.println(tabela1);
                boolean tabela2 = createTableMoviments();
                System.out.print("Created Account Moviments: ");
                System.out.println(tabela2);
            }
            obterCounters();
            p("Counter Account: " + getCountAccount());
            p("\nCounter Moviment: " + getCountMoviment() +"\n");
        }
    }

    /** Obter Contadores */
    private void obterCounters() {
        try
        {
            stmt = conn.createStatement();

            /** Obter last account inserted */
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
        catch (SQLException sqlExcept) { p(sqlExcept.toString()); };

        try
        {
            stmt = conn.createStatement();

            /** Obter last moviment inserted */
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
        catch (SQLException sqlExcept) { p(sqlExcept.toString()); };

    }

    /** Insert accounts */
    public int createAccount(String password,int valor){
        int r = -1;
        try
        {
            String sql = "Insert into ACCOUNTS(accountid,password,balance) values " +
                    "(?,?,?)";

            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setInt(1,getCountAccount());
            ps.setString(2,password);
            ps.setInt(3,valor);

            ps.executeUpdate();

            r= getCountAccount();
            addAccount();

            ps.close();
        }
        catch (SQLException sqlExcept) {
            System.out.println(sqlExcept);
        }
        return r;
    }

    /** Login account */
    public boolean loginAccount(int accountID, String password){
        boolean r = false;
        try
        {
            stmt = conn.createStatement();
            ResultSet results = stmt.executeQuery("select * from ACCOUNTS where accountid = " + accountID);

            if(results.next()){
                if(password.equals(results.getString(2))){
                    r = true;
                }
            }

            results.close();
            stmt.close();
        }
        catch (SQLException sqlExcept) {}

        return r;
    }

    /** Deposit or Withdraw */
    public boolean move(int value, Operation op) {
        boolean r = false;
        try
        {
            stmt = conn.createStatement();

            ResultSet result = stmt.executeQuery("select * from ACCOUNTS where accountid = " + op.getOrigin());

            if(result.next()){

                // GetBalance
                int balance = result.getInt(3);
                int move = balance + op.getAmount();

                // Move okay
                if( move >= 0){

                    String operation = "";
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
                    addMoviment();
                    ps.setInt(2,op.getOrigin());
                    ps.setInt(3,op.getMsgNumber());
                    ps.setString(4,operation);
                    ps.setInt(5,move);
                    ps.executeUpdate();

                    ps.close();

                    r = true;

                }
            }

            result.close();
            stmt.close();
        }
        catch (SQLException sqlExcept) {}

        return r;
    }

    /** GetBalance */
    public int getBalance(int accountId) {
        int r = -1;
        try
        {
            stmt = conn.createStatement();

            ResultSet result = stmt.executeQuery("select * from ACCOUNTS where accountid = " + accountId);

            if(result.next()){

                // GetBalance
                r = result.getInt(3);
            }

            result.close();
            stmt.close();
        }
        catch (SQLException sqlExcept) {}

        return r;
    }

    /** Transfer */
    public boolean transfer(int source, int dest, int amount, Operation op){

        boolean r = false;
        try
        {
            stmt = conn.createStatement();

            /** Verificar 2 conta existe */
            ResultSet result = stmt.executeQuery("select * from ACCOUNTS where accountid = " + dest);

            if(result.next()){

                int balanceAccount2 = result.getInt(3);

                /** Obter saldo conta 1*/
                result = stmt.executeQuery("select * from ACCOUNTS where accountid = " + source);

                if(result.next()){

                    /** GetBalance */
                    int balanceAccount1 = result.getInt(3);
                    int move = balanceAccount1 - amount;

                    p("aqui");

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
                        addMoviment();
                        ps.setInt(2,op.getOrigin());
                        ps.setInt(3,op.getMsgNumber());
                        ps.setString(4,operationAccount1);
                        ps.setInt(5,move);
                        ps.executeUpdate();

                        /** Account 2 */
                        sqlUpdate = "update ACCOUNTS set balance = " + balanceAccount2 + " where accountid = " +  op.getDestination();
                        sqlMove = "insert into MOVIMENTS(id,accountid,msg,operation,value) values (?,?,?,?,?)";

                        ps = conn.prepareStatement(sqlUpdate);
                        ps.executeUpdate();


                        ps = conn.prepareStatement(sqlMove);
                        ps.setInt(1,getCountMoviment());
                        addMoviment();
                        ps.setInt(2,op.getDestination());
                        ps.setInt(3,op.getMsgNumber());
                        ps.setString(4,operationAccount2);
                        ps.setInt(5,balanceAccount2);
                        ps.executeUpdate();

                        ps.close();

                        r = true;
                    }
                }


            }

            result.close();
            stmt.close();
        }
        catch (SQLException sqlExcept) {}

        return r;
    }


    /** Get last X moviments */
    public String moveList(int accountId, int nMoviments){
        String r = "";
        try
        {
            stmt = conn.createStatement();

            /** Obter movimentos do accountId */
            ResultSet result = stmt.executeQuery("select * from MOVIMENTS where accountid = " + accountId + " order by id DESC");

            StringBuilder s = new StringBuilder();
            s.append("---\nMoviments\n");

            /** Moviments */
            while( (nMoviments != 0) && result.next()) {
                s.append("Number Move: " + result.getInt(3));
                s.append("\nOperation: " + result.getString(4));
                s.append("\nBalance: " + result.getInt(5));
                s.append("\n");
                --nMoviments;
            }

            s.append("---\n");
            r =  s.toString();

            result.close();
            stmt.close();
        }
        catch (SQLException sqlExcept) { }

        return r;
    }

    /** Last account inserted */
    public int lastAccountInserted(){
        if(getCountAccount() == 1) return -1;
        else return getCountAccount() - 1;
    }

    /** Last moviment inserted */
    public int lastMovimentInserted(){
       if(getCountMoviment() == 1) return -1;
        else return getCountMoviment() - 1;
    }

    /** Operation realized */
    public boolean operationRealized(int accountId, int msg){
        boolean r = false;
        try
        {
            stmt = conn.createStatement();
            ResultSet results = stmt.executeQuery("select * from MOVIMENTS where accountid = " + accountId + " and msg = " + msg);

            if(results.next()){
                r = true;
            }

            results.close();
            stmt.close();
        }
        catch (SQLException sqlExcept) {


        }

        return r;
    }

    /** Data a enviar para outros servidores */
    public Data exportData(int cl, int op) {

        /** Dados */
        Data data = new Data();

        /** update accounts*/
        try {
            stmt = conn.createStatement();

            /** Obter accounts */
            ResultSet result = stmt.executeQuery("select * from ACCOUNTS where accountid > " + cl);

            /** Accounts */
            while (result.next()) {
                data.InsertAccounts(result.getInt(1), result.getString(2), result.getInt(3));
            }

            result.close();
            stmt.close();
        } catch (SQLException sqlExcept) { p(sqlExcept.toString()); }


        /** Update Moviments */
        try {
                stmt = conn.createStatement();

                /** Obter movimentos */
                ResultSet result = stmt.executeQuery("select * from MOVIMENTS where id > " + op);

                /** Moviments */
                while (result.next()) {
                    Operation newop = new Operation(result.getInt(1), result.getInt(2), result.getInt(3), result.getString(4), result.getInt(5));
                    data.insertMoviments(newop.clone());
                }

                result.close();
                stmt.close();
            } catch (SQLException sqlExcept) { p(sqlExcept.toString()); }

        return data;

    }

    /** Update Database */
    public void updateData(Data data){

        /** Accounts */
        Vector<String> accounts = data.getAccounts();

        PreparedStatement ps;
        String sql;
        String parts[];

        for(String str : accounts){
            parts = str.split(":");
            sql = "Insert into ACCOUNTS(accountid,password,balance) values " +
                    "(?,?,?)";
            try {
                ps = conn.prepareStatement(sql);
                ps.setInt(1,Integer.parseInt(parts[0]));
                addAccount();
                ps.setString(2,parts[1]);
                ps.setInt(3,Integer.parseInt(parts[2]));
                ps.executeUpdate();

            }
            catch (SQLException sqlExcept){
                    p(sqlExcept.toString());
            }
        }

        Vector<Operation> moviments = data.getOperations();

        for(Operation op : moviments){

            sql = "Insert into MOVIMENTS(id,accountid,msg,operation,balance) values " +
                    "(?,?,?,?,?)";
            try {
                ps = conn.prepareStatement(sql);
                ps.setInt(1,op.getId());
                addMoviment();
                ps.setInt(2,op.getAccountid());
                ps.setInt(3,op.getMsgNumber());
                ps.setString(4,op.getOperation());
                ps.setInt(5,op.getBalance());
                ps.executeUpdate();

            }
            catch (SQLException sqlExcept){

            }
        }
    }
    private void p(String s){
        System.out.print(s);
    }
}
