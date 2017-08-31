package works.weave.socks.shipping.controllers;

import com.netflix.config.DynamicPropertyFactory;
import com.rabbitmq.client.Channel;

import io.servicecomb.provider.rest.common.RestSchema;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.ChannelCallback;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import works.weave.socks.shipping.entities.HealthCheck;
import works.weave.socks.shipping.entities.Shipment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static works.weave.socks.shipping.controllers.ApiUtils.*;
import works.weave.socks.shipping.controllers.access.AccessServiceUtilsShipping;
import java.net.URL;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

@RestSchema(schemaId = "shipping")
@RequestMapping(path = "/shipping", produces = MediaType.APPLICATION_JSON_VALUE)
public class ShippingController {
	
    private static String endpointUrl = "";

    private static String region = "";

    private static String serviceName = "";

    private static String aKey = "";

    private static String sKey = "";

    private static String projectId = "";

    private static String queueId = "";

    private static String queueGroupId = "";

    private static ArrayList<String> handlerIds;
    
    /*
     * Read Configure File And Initialize Variables
     */
/*    static
    {
        URL configPath = ClassLoader.getSystemResource("dms-service-config.properties");
        Properties prop = AccessServiceUtils.getPropsFromFile(configPath.getFile());
        serviceName = prop.getProperty(Constants.DMS_SERVICE_NAME);
        region = prop.getProperty(Constants.DMS_SERVICE_REGION);
        aKey = prop.getProperty(Constants.DMS_SERVICE_AK);
        sKey = prop.getProperty(Constants.DMS_SERVICE_SK);
        endpointUrl = prop.getProperty(Constants.DMS_SERVICE_ENDPOINT_URL);
        projectId = prop.getProperty(Constants.DMS_SERVICE_PROJECT_ID);
    }*/

/*    @Autowired
    RabbitTemplate rabbitTemplate;*/

    @RequestMapping(method = RequestMethod.GET)
    public String getShipping() {
        return "GET ALL Shipping Resource.";
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String getShippingById(@PathVariable String id) {
        return "GET Shipping Resource with id: " + id;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(path = "/{username}", method = RequestMethod.POST)
    public @ResponseBody Shipment postShipping(@RequestBody Shipment shipment,@PathVariable("username") String username) {
        System.out.println("Adding shipment to queue..."+username);
    /*    try {
            rabbitTemplate.convertAndSend("shipping-task", shipment);
        } catch (Exception e) {
            System.out
                    .println("Unable to add to queue (the queue is probably down). Accepting anyway. Don't do this " +
                            "for real!");
        }*/
        
        //connect to DMS to push the shippment-task
        boolean bResult = creatQueueAndSendShipment(shipment);
        if (!bResult) {
            System.out.println("Some problem happend while adding to queue, Anyway accespting order");
        }
        shipment.setAmount((float) 0.0);
        return shipment;
    }

 /*   @ResponseStatus(HttpStatus.OK)
    @RequestMapping(method = RequestMethod.GET, path = "/health")
    public @ResponseBody Map<String, List<HealthCheck>> getHealth() {
        Map<String, List<HealthCheck>> map = new HashMap<String, List<HealthCheck>>();
        List<HealthCheck> healthChecks = new ArrayList<HealthCheck>();
        Date dateNow = Calendar.getInstance().getTime();

        HealthCheck rabbitmq = new HealthCheck("shipping-rabbitmq", "OK", dateNow);
        HealthCheck app = new HealthCheck("shipping", "OK", dateNow);

        try {
            this.rabbitTemplate.execute(new ChannelCallback<String>() {
                @Override
                public String doInRabbit(Channel channel) throws Exception {
                    Map<String, Object> serverProperties = channel.getConnection().getServerProperties();
                    return serverProperties.get("version").toString();
                }
            });
        } catch (AmqpException e) {
            rabbitmq.setStatus("err");
        }

        healthChecks.add(rabbitmq);
        healthChecks.add(app);

        map.put("health", healthChecks);
        return map;
    }*/
    
    /*
     * run all api methods
     */
    public static boolean creatQueueAndSendShipment(Shipment shipment)
    {
        String[] groupNames = {"gName_" + System.currentTimeMillis()};

        //Read all DMS properties here.
        serviceName = DynamicPropertyFactory.getInstance()
        		       .getStringProperty(Constants.DMS_SERVICE_NAME, "").get();
        region = DynamicPropertyFactory.getInstance()
 		       .getStringProperty(Constants.DMS_SERVICE_REGION, "").get();
        aKey = DynamicPropertyFactory.getInstance()
  		       .getStringProperty(Constants.DMS_SERVICE_AK, "").get();
        sKey = DynamicPropertyFactory.getInstance()
  		       .getStringProperty(Constants.DMS_SERVICE_SK, "").get();
        endpointUrl = DynamicPropertyFactory.getInstance()
  		       .getStringProperty(Constants.DMS_SERVICE_ENDPOINT_URL, "").get();
        projectId = DynamicPropertyFactory.getInstance()
  		       .getStringProperty(Constants.DMS_SERVICE_PROJECT_ID, "").get();
        
        System.out.println("serviceName" +serviceName);
        System.out.println("region" +region);
        System.out.println("aKey" +aKey);
        System.out.println("sKey" +sKey);
        System.out.println("endpointUrl" +endpointUrl);
        System.out.println("projectId" +projectId);

        
        //List All Queues
        listQueues(projectId, endpointUrl, serviceName, region, aKey, sKey);

        //Get Quota
        getQuota(projectId, endpointUrl, serviceName, region, aKey, sKey);

        //Create Queue
        String queueName = "shipment-task" + System.currentTimeMillis();
        String queueDes = "QueueCreatedForShippmentTask";
        ResponseMessage createQueueResMsg =
                createQueue(queueName, queueDes, projectId, endpointUrl, serviceName, region, aKey, sKey);
        queueId = parseQueueId(createQueueResMsg);
        if (queueId.trim() == "")
        {
            System.out
            .println("Unable to add to queue (the queue is probably down).");
            return false;
        }
        
        //Create Group
        ResponseMessage createGroupResMsg =
                createGroup(groupNames, queueId, projectId, endpointUrl, serviceName, region, aKey, sKey);
        queueGroupId = parseQueueGroupId(createGroupResMsg);

        //Retrieve Specific Queue
        retrieveQueue(queueId, projectId, endpointUrl, serviceName, region, aKey, sKey);
        //Wait 2 seconds, then send,consume and acknowledge consume message,etc..
        Timer timer = new Timer();
        timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                //Send Message
                String messages = constructMessages(shipment);
                sendMessages(messages, queueId, projectId, endpointUrl, serviceName, region, aKey, sKey);

 /*               //Consume Message
                ResponseMessage consumeMessagesResMsg = consumeMessages(queueId,
                        queueGroupId,
                        10,
                        projectId,
                        endpointUrl,
                        serviceName,
                        region,
                        aKey,
                        sKey);
                handlerIds = parseHandlerIds(consumeMessagesResMsg);

                //Acknowledge Message
                if(handlerIds.size() > 0)
                {
                    acknowledgeMessages(handlerIds,
                            queueGroupId,
                            queueId,
                            projectId,
                            endpointUrl,
                            serviceName,
                            region,
                            aKey,
                            sKey);
                }

                //Get All Groups
                getGroups(queueId, projectId, endpointUrl, serviceName, region, aKey, sKey);*/
                
                //TODO: delete queue should be at queuemaster side.
                if (queueId.trim() != "")
                {
                    //User Timer to delete the created group and queue
                    Timer deleteTimer = new Timer();
                    deleteTimer.schedule(new TimerTask()
                    {
                        @Override
                        public void run()
                        {
                            deleteQueueAndGroup(queueId, queueGroupId);
                        }
                    }, 2000);// wait for 2 second and delete
                }
                else
                {
                    System.exit(0);
                }
            }
        }, 2000);
        
        return true;
    }

    /*
     * delete queue or consumer group
     *
     * @param queueId           the delete queue id
     * @param queueGroupId      the delete group id
     */
    private static void deleteQueueAndGroup(String queueId, String queueGroupId)
    {
        if (queueGroupId.trim() != "")
        {
            //Delete Group
            deleteGroup(queueGroupId, queueId, projectId, endpointUrl, serviceName, region, aKey, sKey);
        }

        if (queueId.trim() != "")
        {
            //Delete Queue
            deleteQueue(queueId, projectId, endpointUrl, serviceName, region, aKey, sKey);
        }
    }
}
