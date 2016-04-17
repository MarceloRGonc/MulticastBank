package Client;

import java.io.*;

public class Client {

    private static float balance = 0;

    private static BufferedReader brConsole;
    private static BufferedWriter bwConsole;

    private static void printMenu() throws IOException {
        bwConsole.write("Welcome\n");
        bwConsole.flush();
        bwConsole.write("1 - Deposit money\n");
        bwConsole.flush();
        bwConsole.write("2 - Withdraw money\n");
        bwConsole.flush();
        bwConsole.write("3 - Bank balance\n");
        bwConsole.flush();
        bwConsole.write("4 - Shutdown\n");
        bwConsole.flush();
    }

    public static void main(String[] args) {
        try {

            BankStub bank = new BankStub(0);

            brConsole = new BufferedReader(new InputStreamReader(System.in));
            bwConsole = new BufferedWriter(new OutputStreamWriter(System.out));

            boolean shutdown = false;

            while (!shutdown) {

                printMenu();

                String opt = brConsole.readLine().trim();
                String amount;

                if (opt.equals("1")) {
                    bwConsole.write("Amount: \n");
                    bwConsole.flush();
                    amount = brConsole.readLine().trim();

                   if (bank.move(Float.parseFloat(amount))) {
                        balance += Float.parseFloat(amount);
                    } else {
                        bwConsole.write("[Response] Error\n");
                        bwConsole.flush();
                    }
                } else if (opt.equals("2")) {
                    bwConsole.write("Amount: \n");
                    bwConsole.flush();
                    amount = brConsole.readLine().trim();

                    if (bank.move(0 - Float.parseFloat(amount))) {
                        balance -= Float.parseFloat(amount);
                    } else {
                        bwConsole.write("[Response] Error\n");
                        bwConsole.flush();
                    }
                } else if (opt.equals("3")) {
                    bwConsole.write("[Response] Real Balance: " + bank.getBalance() + "\n");
                    bwConsole.flush();
                } else if (opt.equals("4")) {
                    shutdown = true;
                    bank.leave();
                    bwConsole.write("[Response] See you next time\n");
                    bwConsole.flush();

                } else {
                    bwConsole.write("[Response] Something went wrong\n");
                    bwConsole.flush();
                }
            }
        } catch (IOException ex) {
            System.out.println("[Client] IOexception");
        }
    }
}
