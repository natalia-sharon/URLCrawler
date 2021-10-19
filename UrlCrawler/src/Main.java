import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Main {

    public static void main(String[] args) {
        // init
        int threadNum      = 1;
        boolean valid    = false;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        System.out.print("Welcome to the Web Crawler! ");

        // receive input for thread number
        while (!valid) {
            System.out.print("How many threads should I use?\n");

            try {
                threadNum = Integer.parseInt(br.readLine());

                if (threadNum > 0) {
                    valid = true;
                }
                else {
                    System.err.print("That's not allowed.\n");
                }

            } catch (Exception e) {
                System.err.print("That's not allowed.\n");
            }

        }

        // now begin!
        new UrlProcessorManager().run(threadNum);

        //exit
        System.out.print("Goodbye!\n");
    }
}
