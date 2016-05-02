package Client;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 *
 * */
public class MultipleTester {
    private static int ThreadNumber = 20;
    private static int FirstNumber;
    private static int LastNumber;
    private static int n = 0;
    private static boolean partida = false;
    private static double tot = 0;

    public static ConcurrentHashMap<Integer,Integer> accounts;

    public static synchronized int getThreadNumber() {
        return ThreadNumber;
    }

    public static synchronized int getLastNumber() {
        return LastNumber;
    }

    public static synchronized int getFirstNumber() {
        return FirstNumber;
    }

    public static synchronized void inc(long delta) {
		if (partida) {
			n++;
			tot += delta;
		}
	}

	private static synchronized void end(long delta) {
		System.out.println("Throughput: "+ (n/((delta)/1000d)));
		System.out.println("Latency: "+ (tot/n));
	}

	public static void main(String[] args) throws Exception {

        accounts = new ConcurrentHashMap<>();
        BankStub bank = new BankStub();
        ArrayList<Thread> threads = new ArrayList<>();

		for(int i = 0; i < ThreadNumber; i++) {
            int accountNumber = bank.createAccount("1234");
            threads.add(new Thread(new ThreadClient(accountNumber)));
            accounts.put(accountNumber, 0);

            if(i == 0){
                FirstNumber = accountNumber;
            }
            else if (i == (ThreadNumber - 1)){
                LastNumber = accountNumber;
            }
        }

        for(Thread td : threads) {
            td.start();
        }

		Thread.sleep(5000);

		System.out.println("Beginning!");

        partida = true;

        long before = System.currentTimeMillis();
		
		Thread.sleep(5000);

		long after = System.currentTimeMillis();

		end(after-before);

		System.exit(0);
	}

}
