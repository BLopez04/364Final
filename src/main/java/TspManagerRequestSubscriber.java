import org.eclipse.paho.client.mqttv3.*;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TspManagerRequestSubscriber implements MqttCallback {

    private final TspManager manager;
    private final MqttClient client;
    private final ObjectMapper mapper = new ObjectMapper();

    private static final String REQUEST_TOPIC = "jobs/request";

    public TspManagerRequestSubscriber(String broker, TspManager manager) throws MqttException {
        this.manager = manager;

        String clientId = MqttClient.generateClientId();
        client = new MqttClient(broker, clientId);
        client.setCallback(this);
        client.connect();

        client.subscribe(REQUEST_TOPIC, 2);
        System.out.println("TspManagerRequestSubscriber subscribed to: " + REQUEST_TOPIC);
    }

    static class RequestPayload {
        public String workerId;
        public String capacity;
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        try {
            RequestPayload req = mapper.readValue(message.getPayload(), RequestPayload.class);

            String workerId = req.workerId;
            int capacity = Integer.parseInt(req.capacity); // convert string → int

            manager.handleRequest(workerId, capacity);

        } catch (Exception e) {
            System.out.println("Invalid request payload: " + new String(message.getPayload()));
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        System.out.println("Manager request subscriber lost connection");
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {}
}
