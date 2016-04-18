package Client;

import java.io.*;

public class Client {

    private static float balance = 0;

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

    private static void printMenu2() throws IOException {
        bwConsole.write("Welcome\n");
        bwConsole.flush();
        bwConsole.write("1 - Deposit money\n");
        bwConsole.flush();
        bwConsole.write("2 - Withdraw money\n");
        bwConsole.flush();
        bwConsole.write("3 - Bank balance\n");
        bwConsole.flush();
        bwConsole.write("4 - Last moviments\n");
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

                    // Login
                    case '1':
                        bwConsole.write("Account: ");
                        bwConsole.flush();
                        int accountNumber = Integer.parseInt(brConsole.readLine().trim());
                        bwConsole.write("Password: ");
                        bwConsole.flush();
                        String accountPassword = brConsole.readLine().trim();

                        // Não existe Conta
                        if(!bank.login(accountNumber,accountPassword)){
                            bwConsole.write("[Response] Something went wrong. Try again.\n");
                            bwConsole.flush();
                            break;
                        }

                        // Existe conta
                        printMenu2();
                        String amount;
                        bank.setAccountId(accountNumber);

                        while (!shutdown) {
                            opt = brConsole.readLine().trim();

                            switch(opt.charAt(0)){
                                case '1':
                                    bwConsole.write("Amount: \n");
                                    bwConsole.flush();
                                    amount = brConsole.readLine().trim();

                                    if (bank.move(Float.parseFloat(amount))) {
                                        balance += Float.parseFloat(amount);
                                    } else {
                                        bwConsole.write("[Response] Error\n");
                                        bwConsole.flush();
                                    }
                                    break;

                                case '2':
                                    bwConsole.write("Amount: \n");
                                    bwConsole.flush();
                                    amount = brConsole.readLine().trim();

                                    if (bank.move(0 - Float.parseFloat(amount))) {
                                        balance -= Float.parseFloat(amount);
                                    } else {
                                        bwConsole.write("[Response] Error\n");
                                        bwConsole.flush();
                                    }
                                    break;

                                case '3':
                                    bwConsole.write("[Response] Real Balance: " + bank.getBalance() + "\n");
                                    bwConsole.flush();
                                    break;

                                case '4':
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

                    // Criar conta
                    case '2':
                        bwConsole.write("Password: ");
                        bwConsole.flush();
                        String accountPassword1 = brConsole.readLine().trim();
                        bwConsole.write("Repeat Password: ");
                        bwConsole.flush();
                        String accountPassword2 = brConsole.readLine().trim();

                        // Servidor devolve numero de conta
                        if(accountPassword1.equals(accountPassword2)){
                            int accountId = bank.createAccount(accountPassword1);
                            if(accountId != -1){
                                bwConsole.write("[Response] Your number account: " + accountId + "\n");
                                bwConsole.flush();
                            }
                            else{
                                bwConsole.write("[Response] Something went wrong. Try again.\n");
                                bwConsole.flush();
                            }
                        }
                        // Error
                        else{
                            bwConsole.write("[Response] Password does not match\n");
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