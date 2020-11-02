import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ResponseTimeTest {
    private final String url;
    private final int requestCount;
    private final ExecutorService pool;
    private List<Long> responseTimeList;
    

    public ResponseTimeTest(final String url, final int requestCount, final ExecutorService pool) {
        this.url = url;
        this.requestCount = requestCount;
        this.pool = pool;   
    }

    private void run() {
        final CountDownLatch doneSignal = new CountDownLatch(requestCount);
        final Queue<Long> responseTimeQueue = new ConcurrentLinkedDeque<>();

        for (int i = 0; i < requestCount; i++) {
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    HttpURLConnection urlConnection = null;
                    final long startTime = System.currentTimeMillis();
                    try {
                        final URL link = new URL(url);
                        urlConnection = (HttpURLConnection) link.openConnection();
                        urlConnection.setRequestMethod("GET");
                        InputStream response = urlConnection.getInputStream();
                        response.close();
                    } catch (final Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (urlConnection != null) {
                            urlConnection.disconnect();
                        }
                    }

                    final long endTime = System.currentTimeMillis();
                    responseTimeQueue.add(endTime - startTime);

                    doneSignal.countDown();
                }
            });
        }

        try {
          doneSignal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        responseTimeList = new ArrayList<>(responseTimeQueue);
    }

    private long getAvgResponseTime() {
        long total = 0;
        for (Long responseTime : responseTimeList) {
            total += responseTime;
        }
        return total / responseTimeList.size();
    }

    private long get95PercentResponseTime() {
        responseTimeList.sort(Long::compareTo);
        int index = (int) Math.ceil(95 / 100.0 * responseTimeList.size());
        return responseTimeList.get(index-1);
    }

    public static void main(final String[] args) {
        final String URL = "https://baidu.com";
        final int CONCURRENT_COUNT = 10;
        final int REQUEST_COUNT = 100;

        final ExecutorService pool = Executors.newFixedThreadPool(CONCURRENT_COUNT);
        final ResponseTimeTest responseTimeTest = new ResponseTimeTest(URL, REQUEST_COUNT, pool);
        responseTimeTest.run();

        System.out.println("Concurrent Count: " + CONCURRENT_COUNT);
        System.out.println("Request Count: " + REQUEST_COUNT);
        System.out.println("Average Response Time: " + responseTimeTest.getAvgResponseTime() + " ms");
        System.out.println("95% Response Time: " + responseTimeTest.get95PercentResponseTime() + " ms");
        
        pool.shutdown();
    }
}