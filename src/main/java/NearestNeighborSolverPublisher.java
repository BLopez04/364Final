import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import java.util.List;


public class NearestNeighborSolverPublisher {
    private final MqttClient client;
    private String workerId;

    private static final String resultTopic = "jobs/result";

    public NearestNeighborSolverPublisher(String broker) throws MqttException {
        this.workerId = MqttClient.generateClientId();
        client = new MqttClient(broker, workerId);
        client.connect();
        //System.out.println("outsourcer publisher connected");
    }

    static class Payload {
        public List<Integer> tour;

        public Payload(List<Integer> items) {
            this.tour = items;
        }
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
    public void sendResult(List<Integer> result) throws JsonProcessingException, MqttException {
        ObjectMapper mapper = new ObjectMapper();

        Payload payload = new Payload(result);
        byte[] bytes = mapper.writeValueAsBytes(payload);

        MqttMessage message = new MqttMessage(bytes);
        client.publish(resultTopic, message);
    }

    /*
        below is how to extract the "Payload" class as the resulting List<City> tour objec
        from the subscriber side

        ObjectMapper mapper = new ObjectMapper();

        client.subscribe(NearestNeighborSolverPublisher.resultTopic, (topic, message) -> {
            Payload payload = mapper.readValue(message.getPayload(), NearestNeighborSolverPublisher.Payload.class);

            List<Integer> result = payload.tour; // or whatever you do with the result
        });
     */
}
