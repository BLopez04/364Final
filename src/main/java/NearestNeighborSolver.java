import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.*;

/**
 * A simple implementation of the Nearest Neighbor heuristic for TSP.
 *
 * @author javiergs
 * @version 2.0
 */
public class NearestNeighborSolver implements Runnable{

  private static final Integer TIMEOUT = 1000;
  private NearestNeighborSolverPublisher pub;
  private NearestNeighborSolverSubscriber sub;

  public NearestNeighborSolver(String broker) throws MqttException {
    this.pub = new NearestNeighborSolverPublisher(broker);
    this.sub = new NearestNeighborSolverSubscriber(broker, pub.getWorkerId());
  }
  public static List<Integer> solve(List<City> cities, int startIndex) {
    // 1) chooses starting city from "startIndex"

    // set n as size of cities given and check if both values given are valid
    int n = cities.size();
    if (n == 0) return List.of();
    if (startIndex < 0 || startIndex >= n) startIndex = 0;

    // 2) create used and resulting/tour lists to correct size
    // also add the 1st city to used and tour list, therefore marking it
    boolean[] used = new boolean[n];
    List<Integer> tour = new ArrayList<>(n + 1);
    int current = startIndex;
    used[current] = true;
    tour.add(current);

    // iterate n cities, since it will guarantee the tour goes to every city
    for (int step = 1; step < n; step++) {

      // start iteration at current city
      int next = -1;
      double best = Double.POSITIVE_INFINITY;
      City curCity = cities.get(current);

      // 3) find closest unvisted/unused city
      // iterate through all cities to find the city with the shortest distance
      for (int j = 0; j < n; j++) {
        if (used[j]) continue;
        double d = curCity.distanceTo(cities.get(j));
        if (d < best) {
          best = d;
          next = j;
        }
      }

      // check if cities still exists, therefore checking if STOP
      // signal was sent
      if(TspBlackboard.getInstance().getCities().equals(City.STOP)) return new ArrayList<>();

      // 4) "move there"
      // add the shortest distance to used and tour lists, then set it to current for
      // next step/iteration
      // also repeats steps 1-2 before repeating 3-4 next iteration
      used[next] = true;
      tour.add(next);
      current = next;
    }

    // 6) return to starting city
    // add starting point to end of tour to make it a loop before returning it
    tour.add(tour.get(0));
    return tour;
  }

  public static double length(List<City> cities, List<Integer> tour) {
    if (tour == null || tour.size() < 2) return 0.0;
    double total = 0.0;
    for (int i = 0; i < tour.size() - 1; i++) {
      City a = cities.get(tour.get(i));
      City b = cities.get(tour.get(i + 1));
      total += a.distanceTo(b);
    }
    return total;
  }

  @Override
  public void run() {
    Optional<Integer> startIndex;
    while(true) {

      // send request then listen for TIMEOUT time until a job is found
      while (true) {
        // send request
        pub.sendRequest();

        // listen for assignment
        startIndex = sub.listen(TIMEOUT);
        if(startIndex.isPresent()) break;
      }

      // solve job
      try {

        // get cities and check if they still exist, terminate otherwise
        List<City> cities = TspBlackboard.getInstance().getCities();
        if(cities.equals(City.STOP)) return;

        // solve job
        List<Integer> res = solve(cities, startIndex.get());
        if(res.isEmpty()) return; // check if it returned with STOP signal, then terminate if true

        // return result to manager using publisher
        pub.sendResult(res);
      }catch (Exception ignored){}
    }
  }
}
