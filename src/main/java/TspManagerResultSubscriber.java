import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.*;

import java.util.List;

public class TspManagerResultSubscriber implements MqttCallback {

    private final TspManager manager;
    private final MqttClient client;
    private final ObjectMapper mapper = new ObjectMapper();

    private static final String RESULT_TOPIC = "jobs/result";

    public TspManagerResultSubscriber(String broker, TspManager manager) throws MqttException {
        this.manager = manager;

        String clientId = MqttClient.generateClientId();
        client = new MqttClient(broker, clientId);
        client.setCallback(this);
        client.connect();

        client.subscribe(NearestNeighborSolverPublisher.resultTopic, 2);
        System.out.println("TspManagerResultSubscriber subscribed to: " + RESULT_TOPIC);
    }

    static class Payload {
        public List<Integer> tour;
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        try {
            Payload payload = mapper.readValue(message.getPayload(), Payload.class);
            manager.handleResult(payload.tour);
            System.out.println("Result received");
        } catch (Exception e) {
            System.out.println("Invalid result payload: " + new String(message.getPayload()));
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        System.out.println("Manager result subscriber lost connection");
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {}
}
