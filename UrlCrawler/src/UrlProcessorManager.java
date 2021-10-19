import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.*;

public class UrlProcessorManager {

    ExecutorService executor;
    UrlProcessor urlProcessor;

    public void run(int threadNum)
    {
        BufferedReader br      = new BufferedReader(new InputStreamReader(System.in));
        String url             = null;
        boolean exit           = false;
        boolean valid          = false;
        Future<Set<String>> future = null;
        Set<String>  retrievedUrls = new HashSet<>();

        while (!exit) {
            // receive input for url
            while (!valid) {
                init(threadNum);
                System.out.print("Please enter a URL.\n");
                try {
                    url = br.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                valid = validateUrl(url);
            }

            // add the url to the processing queue
            urlProcessor.pushParentUrl(url);

            // print out the urls
            try {
                // start the processing threads, future will wait for threads to finish.
                for (int threadCount = 0;  threadCount < threadNum; threadCount++) {
                    future = executor.submit(urlProcessor);
                }

                retrievedUrls = future.get();

                // sort them
                Collection<String> sortedUrls = new TreeSet<>(retrievedUrls);

                System.out.print("Printing crawl results, urls crawled: " + sortedUrls.size() );

                for (String urlString : sortedUrls) {
                    System.out.print("\n" + urlString);
                }
            } catch (Exception ex) {
                System.err.print("Error retrieving urls from urlProcessor - " + ex);
                ex.printStackTrace();
            }

            // finished
            try {
                executor.shutdown();
                executor.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                System.err.println("Cancelling of tasks interrupted - " + ex);
                ex.printStackTrace();
            }

            System.out.print("\nPress x to exit, or anything else to go again\n");

            try {
                if (br.readLine().equals("x")) {
                    exit = true;
                } else {
                    // reset vars
                    valid = false;
                    url   = "";
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // checks if this is a valid URL
    private boolean validateUrl(String url) {
        if ((null == url) || (url.equals(""))) {
            System.err.print("URL cannot be empty\n");
            return false;
        }
        return true;
    }

    private void init(int threadNum) {
        executor = Executors.newFixedThreadPool(threadNum);
        urlProcessor = new UrlProcessor(new LinkedBlockingQueue<>(), new CountDownLatch(threadNum - 1));
    }
}
