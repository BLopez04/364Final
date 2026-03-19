import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;

public class TspManager {

    private final int numCities;
    private final CountDownLatch doneLatch;

    private final Queue<Integer> jobQueue = new LinkedList<>();

    private final TspManagerPublisher pub;

    public TspManager(String broker) throws MqttException {

        // number of jobs = number of starting cities
        this.numCities = TspBlackboard.getInstance().getCities().size();
        this.doneLatch = new CountDownLatch(numCities);

        // fill job queue with start indices
        for (int i = 0; i < numCities; i++) {
            jobQueue.add(i);
        }

        // publisher for assigning jobs
        this.pub = new TspManagerPublisher(broker);

        // subscribers for requests + results
        new TspManagerRequestSubscriber(broker, this);
        new TspManagerResultSubscriber(broker, this);

        System.out.println("TspManager initialized with " + numCities + " jobs.");
    }
    // when a worker requests work, assign jobs up to its capacity
    public synchronized void handleRequest(String workerId, int capacity) {
        for (int i = 0; i < capacity; i++) {
            Integer job = jobQueue.poll();
            if (job == null) {
                return; // no more jobs to assign
            }
            pub.assignJob(workerId, job);
        }
    }

    // when a solver returns check its cost and update best if possible
    public void handleResult(List<Integer> tour) {
        List<City> cities = TspBlackboard.getInstance().getCities();
        double cost = NearestNeighborSolver.length(cities, tour);

        TspBlackboard.getInstance().updateBest(tour, cost);

        doneLatch.countDown();
        System.out.println("Manager received result. Remaining: " + doneLatch.getCount());
    }

    // block until results done
    public void waitForAll() {
        try {
            doneLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public int getNumCities() {
        return numCities;
    }
}
