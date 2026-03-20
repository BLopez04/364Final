import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class TspManagerPublisher {

    private final MqttClient client;

    public TspManagerPublisher(String broker) throws MqttException {
        String clientId = MqttClient.generateClientId();
        client = new MqttClient(broker, clientId);
        client.connect();
        System.out.println("TspManagerPublisher has connected");
    }

    // Assign a job to a given workerId
    public void assignJob(String workerId, int startIndex) {
        try {
            String topic = "jobs/assign/" + workerId;

            String json = String.valueOf(startIndex);

            MqttMessage msg = new MqttMessage(json.getBytes());
            msg.setQos(2);

            client.publish(topic, msg);
            System.out.println("Assigned startIndex " + startIndex + " to " + workerId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() throws MqttException {
        client.disconnect();
        client.close();
    }
}
