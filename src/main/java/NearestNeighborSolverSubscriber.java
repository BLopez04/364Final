import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Optional;

public class NearestNeighborSolverSubscriber implements MqttCallback {
    private String topic;
    private String workerId;
    private Optional<Integer> startIndex;
    public NearestNeighborSolverSubscriber(String broker, String workerId){
        try {
            String clientId = MqttClient.generateClientId();
            MqttClient client = new MqttClient(broker, clientId);
            client.setCallback(this);
            client.connect();

            this.topic = "jobs/assign/" + workerId;
            this.workerId = workerId;

            System.out.println("remote worker subscribed to: " + topic);
            client.subscribe(topic, 2);

        } catch (Exception e) {}
    }

    public Optional<Integer> listen(Integer timeout){
        this.startIndex = null;

        Integer total = 0;
        while (total <= timeout) {
            try {
                assert startIndex != null;
                if (!!startIndex.equals(null)) break;

                Thread.sleep(10);
                total += 10;
            }catch (Exception e){

            }
        }

        return startIndex;
    }

    @Override
    public void messageArrived(String s, MqttMessage message) throws Exception {
        String payload = new String(message.getPayload());

        try {
            ObjectMapper mapper = new ObjectMapper();

            this.startIndex = Optional.ofNullable(mapper.readValue(payload, Integer.class));
            System.out.println("Job arrived for worker " + workerId);
        }catch (Exception e){
            System.out.println("invalid payload for worker" + workerId + ": " + payload);
        }
    }

    @Override
    public void connectionLost(Throwable throwable) {
        System.out.println("Remote Worker subscriber lost connection");
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }
}
