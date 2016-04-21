package Client;

import java.io.*;

public class Client {

    private static BufferedReader brConsole;
    private static BufferedWriter bwConsole;

    private static void printMenu1() throws IOException{
        bwConsole.write("Welcome\n");
        bwConsole.flush();
        bwConsole.write("1 - Login\n");
        bwConsole.flush();
        bwConsole.write("2 - Create Account\n");
        bwConsole.flush();
        bwConsole.write("0 - Shutdown\n");
        bwConsole.flush();
    }

    private static void printMenu2(int accountId) throws IOException {
        bwConsole.write("Welcome: " + accountId + " \n");
        bwConsole.flush();
        bwConsole.write("1 - Deposit money\n");
        bwConsole.flush();
        bwConsole.write("2 - Withdraw money\n");
        bwConsole.flush();
        bwConsole.write("3 - Transfer\n");
        bwConsole.flush();
        bwConsole.write("4 - Bank balance\n");
        bwConsole.flush();
        bwConsole.write("5 - Last moviments\n");
        bwConsole.flush();
        bwConsole.write("0 - Shutdown\n");
        bwConsole.flush();
    }

    public static void main(String[] args) {
        try {

            brConsole = new BufferedReader(new InputStreamReader(System.in));
            bwConsole = new BufferedWriter(new OutputStreamWriter(System.out));

            boolean shutdown = false;
            BankStub bank = new BankStub();
            while (!shutdown) {

                printMenu1();
                String opt = brConsole.readLine().trim();

                switch(opt.charAt(0)) {

                    /** Login */
                    case '1':
                        bwConsole.write("Account: ");
                        bwConsole.flush();
                        int accountId = Integer.parseInt(brConsole.readLine().trim());
                        bwConsole.write("Password: ");
                        bwConsole.flush();
                        String accountPassword = brConsole.readLine().trim();

                        /** Account doesn't exist */
                        if(!bank.login(accountId, accountPassword)){
                            bwConsole.write("[Response] This account doesn't exist.\n");
                            bwConsole.flush();
                            break;
                        }

                        /** Account exist*/
                        String amount;
                        bank.setAccountId(accountId);
                        while (!shutdown) {
                            printMenu2(accountId);
                            opt = brConsole.readLine().trim();

                            switch(opt.charAt(0)){
                                case '1':
                                    bwConsole.write("Deposit Amount: ");
                                    bwConsole.flush();
                                    amount = brConsole.readLine().trim();

                                    if (bank.move(Integer.parseInt(amount), null)) {
                                        bwConsole.write("[Response] Deposit made!\n");
                                        bwConsole.flush();
                                    } else {
                                        bwConsole.write("[Response] Something went wrong!\n");
                                        bwConsole.flush();
                                    }
                                    break;

                                case '2':
                                    bwConsole.write("Withdraw Amount: ");
                                    bwConsole.flush();
                                    amount = brConsole.readLine().trim();

                                    if (bank.move(0 - Integer.parseInt(amount), null)) {
                                        bwConsole.write("[Response] Withdraw made\n");
                                        bwConsole.flush();
                                    } else {
                                        bwConsole.write("[Response] Something went wrong!\n");
                                        bwConsole.flush();
                                    }
                                    break;

                                case '3':
                                    bwConsole.write("Account Destination: ");
                                    bwConsole.flush();
                                    String account1 = brConsole.readLine().trim();
                                    bwConsole.write("Ammount: ");
                                    bwConsole.flush();
                                    amount = brConsole.readLine().trim();

                                    /** Verifies that the account 2 exists and that account 1 have money */
                                    if(accountId == Integer.parseInt(account1)){
                                        bwConsole.write("[Response] It's the same account!\n");
                                        bwConsole.flush();
                                    }
                                    else if (bank.transfer(accountId,Integer.parseInt(account1),Integer.parseInt(amount), null)) {
                                        bwConsole.write("[Response] Transfer made!\n");
                                        bwConsole.flush();
                                    } else {
                                        bwConsole.write("[Response] Something went wrong\n");
                                        bwConsole.flush();
                                    }
                                    break;

                                case '4':
                                    bwConsole.write("[Response] Real Balance: " + bank.getBalance(accountId) + "\n");
                                    bwConsole.flush();
                                    break;

                                case '5':
                                    bwConsole.write("Moviments: ");
                                    bwConsole.flush();
                                    String nMoviments = brConsole.readLine().trim();
                                    int movimentos = Integer.parseInt(nMoviments);
                                    if (movimentos > 0){
                                        bwConsole.write("[Response] \n");
                                        bwConsole.write(bank.moveList(accountId, movimentos));
                                        bwConsole.flush();
                                    }
                                    else{
                                        bwConsole.write("[Response] Something went wrong\n");
                                        bwConsole.flush();
                                    }
                                    break;

                                case '0':
                                    shutdown = true;
                                    bank.leave();
                                    bwConsole.write("[Response] See you next time\n");
                                    bwConsole.flush();
                                    break;

                                default:
                                    bwConsole.write("[Response] Something went wrong\n");
                                    bwConsole.flush();
                            }
                        }
                        shutdown = false;
                        break;

                    /** Create an account */
                    case '2':
                        bwConsole.write("Password: ");
                        bwConsole.flush();
                        String accountPassword1 = brConsole.readLine().trim();
                        bwConsole.write("Repeat Password: ");
                        bwConsole.flush();
                        String accountPassword2 = brConsole.readLine().trim();

                        /** Server returns account number */
                        if(accountPassword1.equals(accountPassword2)){
                            accountId = bank.createAccount(accountPassword1);
                            if(accountId != -1){
                                bwConsole.write("[Response] Your account number is: " + accountId + "\n");
                                bwConsole.flush();
                            }
                            else{
                                bwConsole.write("[Response] Something went wrong. Try again.\n");
                                bwConsole.flush();
                            }
                        }
                        else{
                            bwConsole.write("[Response] Password doesn't match\n");
                            bwConsole.flush();
                        }
                        break;

                    case '0':
                        shutdown = true;
                        bwConsole.write("[Response] See you next time\n");
                        bwConsole.flush();
                        break;
                }
            }

            System.exit(0);

        } catch (IOException ex) {
            System.out.println("[Client] IOexception");
        }
    }
}