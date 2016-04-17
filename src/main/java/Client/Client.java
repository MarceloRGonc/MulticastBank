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
        bwConsole.write("0 - Shutdown\n");
        bwConsole.flush();
    }

    public static void main(String[] args) {
        try {

            brConsole = new BufferedReader(new InputStreamReader(System.in));
            bwConsole = new BufferedWriter(new OutputStreamWriter(System.out));

            boolean shutdown = false;
            BankStub bank = new BankStub(1);

            while (!shutdown) {

                printMenu1();
                String opt = brConsole.readLine().trim();

                switch(opt.charAt(0)) {

                    // Login
                    case '1':
                        bwConsole.write("Account: ");
                        bwConsole.flush();
                        String accountNumber = brConsole.readLine().trim();
                        bwConsole.write("Password: ");
                        bwConsole.flush();
                        String accountPassword = brConsole.readLine().trim();

                        // Verificar se conta existe
                        // Não existe

                        // Se Existe
                        printMenu2();
                        String amount;

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

                        break;

                    // Criar conta
                    case '2':
                        bwConsole.write("Password: ");
                        bwConsole.flush();
                        String accountPassword1 = brConsole.readLine().trim();
                        bwConsole.write("Repeat Password: ");
                        bwConsole.flush();
                        String accountPassword2 = brConsole.readLine().trim();

                        // Devolve número de conta
                        if(accountPassword1.equals(accountPassword2)){
                            bwConsole.write("Account: 10\n");
                            bwConsole.flush();
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