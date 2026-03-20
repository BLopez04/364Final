import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class TspBlackboard {
    private static TspBlackboard instance = new TspBlackboard();
    private List<City> cities;
    private List<Integer> bestTour;
    private double bestCost;
    private final ReentrantLock lock;

    private TspBlackboard(){
        cities = new ArrayList<>();
        bestTour = new ArrayList<>();
        bestCost = Double.POSITIVE_INFINITY;
        lock = new ReentrantLock();
    }

    public static TspBlackboard getInstance() {
        return instance;
    }

    public List<City> getCities() {
        try {
            lock.lock();
            return cities;
        } finally {
            lock.unlock();
        }
    }

    public void setCities(List<City> cities){
        try{
            lock.lock();
            this.cities = cities;

            this.bestTour = new ArrayList<>();
            this.bestCost = Double.POSITIVE_INFINITY;
        }catch (Exception e){
            Thread.currentThread().interrupt();
        }finally {
            lock.unlock();
        }
    }

    public List<Integer> getBestTour() {
        try {
            lock.lock();
            return bestTour;
        } finally {
            lock.unlock();
        }
    }

    public double getBestCost() {
        try {
            lock.lock();
            return bestCost;
        } finally {
            lock.unlock();
        }
    }

    public void updateBest(List<Integer> tour, double cost) {
        try {
            lock.lock();

            if(tour.size() == 0) System.out.printf("cost: %d\n\n\n", cost);
            if (cost < bestCost) {
                bestCost = cost;
                bestTour = new ArrayList<>(tour);
            }
        } finally {
            lock.unlock();
        }
    }
}
