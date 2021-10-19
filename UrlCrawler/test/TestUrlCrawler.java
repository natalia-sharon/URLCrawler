import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

public class TestUrlCrawler {

    String       PARENT_URL = "https://monzo.com/";
    UrlProcessor urlProcessor;

    @Test
    public void TestUrlProcessor() {
        init();

        // correct input
        processUrl("https://monzo.com/i/business", true);

        // duplicate
        processUrl("https://monzo.com/i/business", false);

        // duplicate links disguised as new ones
        processUrl("https://monzo.com/i/business#fragment", false);
        processUrl("https://monzo.com/i/business/", false);

        // new links with # or /
        processUrl("https://monzo.com/i/savingwithmonzo#fragment", true);
        processUrl("https://monzo.com/i/savingwithmonzo", false);
        processUrl("https://monzo.com/features/savings/", true);
        processUrl("https://monzo.com/features/savings/", false);

        // different domains
        processUrl("https://www.google.com", false);
        processUrl("https://www.community.monzo.com", false);
        processUrl("https://monzo", false);
    }

    private void processUrl(String url, boolean insertToQueue) {
        String processedUrl = urlProcessor.validateUrl(url);

        if (insertToQueue) {
            Assertions.assertNotEquals(null, processedUrl);
        } else {
            Assertions.assertEquals(null, processedUrl);
        }
    }

    private void init() {
        urlProcessor = new UrlProcessor(new LinkedBlockingQueue<String>(), new CountDownLatch(1));
        urlProcessor.pushParentUrl(PARENT_URL);
    }
}
