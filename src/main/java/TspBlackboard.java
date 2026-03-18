import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class TspBlackboard {
    private static TspBlackboard instance = new TspBlackboard();
    private List<City> cities;
    private final ReentrantLock lock;

    private TspBlackboard(){
        cities = new ArrayList<>();
        lock = new ReentrantLock();
    }

    public static TspBlackboard getInstance() {
        return instance;
    }

    public List<City> getCities(){
        try{
            lock.lock();

            return cities;
        }catch (Exception e){
            Thread.currentThread().interrupt();
        }finally {
            lock.unlock();
        }
    }

    public void setCities(List<City> cities){
        try{
            lock.lock();

            this.cities = cities;
        }catch (Exception e){
            Thread.currentThread().interrupt();
        }finally {
            lock.unlock();
        }
    }
}
