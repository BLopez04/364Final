import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;


public class NearestNeighborSolverPublisher {
    private final MqttClient client;
    private String workerId;

    public NearestNeighborSolverPublisher(String broker) throws MqttException {
        this.workerId = MqttClient.generateClientId();
        client = new MqttClient(broker, workerId);
        client.connect();
        //System.out.println("outsourcer publisher connected");
    }

    public String getWorkerId(){return workerId;}

    public void sendRequest(){
        try {
            String topic = "jobs/request";

            String json = String.format(
                    "{\"workerId\":\"%s\",\"capacity\":\"%d\"}",
                    workerId, 1
            );

            MqttMessage msg = new MqttMessage(json.getBytes());
            msg.setQos(2);

            client.publish(topic, msg);
            System.out.println("sent request from " + workerId);

        } catch (Exception e) {}
    }
}
