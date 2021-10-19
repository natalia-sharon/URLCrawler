import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

public class UrlProcessor implements Callable {

    LinkedBlockingQueue<String> urlQueue;
    Set<String> urList; // sets contain no duplicate elements
    CountDownLatch latch;
    boolean continueProcessing;
    String  headUrl;

    public UrlProcessor(LinkedBlockingQueue<String> urlQueue, CountDownLatch latch) {
        this.urlQueue = urlQueue;
        this.urList = new HashSet<>();
        this.latch = latch;
        continueProcessing  = true;
    }

    @Override
    public Set<String> call() throws Exception {
        // it is possible for the queue to be empty whilst a thread is retrieving data
        while (continueProcessing) {
            // the difference between poll() and pop() is that pop will throw NoSuchElementException() on empty list, whereas poll returns null.
            String url = urlQueue.poll();

            if (url != null) {
                //System.out.print(Thread.currentThread().getName() + " retrieved [" + url + "] from queue" + "\n");
                System.out.print(".");
                process(url);
            }

            if (latch.getCount() == 0) {
                continueProcessing = false;
            }
        }

        return urList;
    }

    private void process(String url) throws InterruptedException {
        // get the html of the url and parse
        Document doc = getHtml(url);
        if (null != doc) {
            //System.out.print("Received doc for:  " + url + " length: " + doc.wholeText().length() + "\n");
            parseHtml(doc);
        }

        // use a count down latch to decide if all threads are finished
        if (urlQueue.isEmpty()) {
            latch.countDown();
        }
    }

    private Document getHtml(String url) {
        Document doc = null;
        try {
            doc = Jsoup.connect(url).get();
        } catch (Exception ex) {
            System.err.print("\nCould not retrieve html from url [" + url + "] - " + ex.getMessage() + "\n");

            // if this is the first url and we have an error, then stop this crawl
            if (url.equals(headUrl)) {
                continueProcessing = false;
            }
        }
        return doc;
    }

    private void parseHtml(Document doc) {
        // retrieve all matching tags
        Elements links = doc.select("a[href]");

        links.forEach((link) -> {
            // Use the abs: attribute prefix to resolve the absolute URL
            String childUrl = link.attr("abs:href");

            String url = validateUrl(childUrl);
            if(url != null) {
                // add to the queue for processing
                urlQueue.add(url);
            }
        });
    }

    public String validateUrl(String url) {
        // If this is a fragment link, remove the scroll position so we don't duplicate
        int hashIndex = url.indexOf("#");
        if (hashIndex > -1) {
            url = url.substring(0, hashIndex);
        }

        // If this has / appended, remove
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }

        // check if this url is a subdomain of the parent url
        if ((url.length() >= headUrl.length()) && (headUrl.equals(url.substring(0, headUrl.length())))) {
            // check if we have already processed this
            return checkAddedUrls(url);
        }
        return null;
    }

    // this resource is synchronized to maintain concurrency as multiple threads access the list
    private synchronized String checkAddedUrls(String url) {
        // check if this url already exists in our url list
        if (urList.stream().anyMatch(u -> u.equals(url)))
        {
            return null;
        } else {
            urList.add(url);
            return url;
        }
    }

    // push to queue externally
    public void pushParentUrl(String url) {
        urlQueue.clear();
        this.headUrl = url;

        urList.add(url);
        urlQueue.add(url);
    }
}
