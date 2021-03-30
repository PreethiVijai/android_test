public class MessageAttributesExample {

    private static final Logger logger = LogManager.getLogger(MessageAttributesExample.class);

    /**
     * Hard-coded constants for example purpose only
     */
    private static final String REGION = "us-east-1";
    private static final Integer LONG_POLLING_SECONDS = 50;
    private static final String QUEUE_NAME = "Android_OTA.fifo";
    private static final String TRACE_ID_NAME = "trace-id";

    private static final SqsClient sqsClient = SqsClient.builder().region(Region.of("us-east-1")).build();

    public static void main(String[] args) {


        ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
                .queueUrl("https://sqs.us-east-1.amazonaws.com/086435422519/Android_OTA.fifo")
                .waitTimeSeconds(20)  // forces long polling
                .build();
        List<Message> messages = sqsClient.receiveMessage(receiveMessageRequest).messages();

            logger.info("test");
            logger.info("{} messages received.", messages.size());


            // process and delete message
            for (Message message : messages) {
                logger.info(message);
            }
        }

    }
