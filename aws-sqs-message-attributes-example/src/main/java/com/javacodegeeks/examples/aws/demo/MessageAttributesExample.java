package com.javacodegeeks.examples.aws.demo;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.DeleteQueueRequest;
import software.amazon.awssdk.services.sqs.model.DeleteQueueResponse;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlResponse;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

public class MessageAttributesExample {

    private static final Logger logger = LogManager.getLogger(MessageAttributesExample.class);

    /**
     * Hard-coded constants for example purpose only
     */
    private static final String REGION = "ap-southeast-2";
    private static final Integer LONG_POLLING_SECONDS = 50;
    private static final String QUEUE_NAME = "Android_OTA.fifo";
    private static final String TRACE_ID_NAME = "trace-id";

    /**
     * The JSON object mapper.
     */
    private static ObjectMapper objectMapper;
    static {
        objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.registerModule(new JavaTimeModule());
    }

    private SqsClient sqsClient;

    public static void main(String[] args) {
        MessageAttributesExample example = new MessageAttributesExample();

        // create the queue
        //example.createQueue(QUEUE_NAME);

        // send a message
        //ExecutorService producerExecutorService = Executors.newSingleThreadExecutor();
        /*MyProducer producer = new MyProducer(QUEUE_NAME, example.getSqsClient());
        Future<?> producerResult = producerExecutorService.submit(producer);
        // block util the producer finishes
        try {
            producerResult.get();
        } catch (InterruptedException | ExecutionException e) {
            // do nothing
        }*/

        // start the consumer
        ExecutorService consumerExecutorService = Executors.newSingleThreadExecutor();
        MyConsumer myConsumer = new MyConsumer(QUEUE_NAME, example.getSqsClient());
        Future<?> consumerResult = consumerExecutorService.submit(myConsumer);
        // block util the consumer finishes
        try {
            consumerResult.get();
        } catch (InterruptedException | ExecutionException e) {
            // do nothing
        }

        // delete the queue
        //example.deleteQueue(QUEUE_NAME);

        // shut down thread pools
       /* producerExecutorService.shutdown();
        try {
            if (!producerExecutorService.awaitTermination(60, TimeUnit.SECONDS)) {
                producerExecutorService.shutdownNow();
            }
        } catch (InterruptedException ex) {
            producerExecutorService.shutdownNow();
            Thread.currentThread().interrupt();
        }*/

        consumerExecutorService.shutdown();
        try {
            if (!consumerExecutorService.awaitTermination(60, TimeUnit.SECONDS)) {
                consumerExecutorService.shutdownNow();
            }
        } catch (InterruptedException ex) {
            consumerExecutorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Constructor.
     */
    public MessageAttributesExample() {
        // create a SQS client
        this.sqsClient = SqsClient.builder().region(Region.of(REGION)).build();
    }

    /**
     * Create a standard/fifo queue.
     *
     * @param queueName          the queue name. FIFO queue name must end with
     *                           ".fifo".
     * @return The queue URL
     */
    public String createQueue(String queueName) {
        logger.info("Create queue {} in region {}", queueName, REGION);
        CreateQueueRequest request = CreateQueueRequest.builder().queueName(queueName).build();
        this.sqsClient.createQueue(request);
        String queueUrl = getQueueUrl(queueName);
        logger.info("Queue URL: {}", queueUrl);
        return queueUrl;
    }

    /**
     * Delete the given queue.
     *
     * @param queueName the queue name
     */
    public void deleteQueue(String queueName) {
        logger.info("Delete queue {}", queueName);
        String queueUrl = getQueueUrl(queueName);
        DeleteQueueRequest deleteQueueRequest = DeleteQueueRequest.builder().queueUrl(queueUrl).build();
        DeleteQueueResponse deleteQueueResponse = this.sqsClient.deleteQueue(deleteQueueRequest);
        if (deleteQueueResponse.sdkHttpResponse().isSuccessful()) {
            logger.info("Queue {} deleted.", queueName);
        } else {
            logger.error("Failed to delete queue {}: {} - {}", queueName, deleteQueueResponse.sdkHttpResponse().statusCode(),
                    deleteQueueResponse.sdkHttpResponse().statusText());
        }
    }

    /**
     * Get the queue url by queue name.
     *
     * @param queueName
     * @return the queue url
     */
    private String getQueueUrl(String queueName) {
        /*GetQueueUrlRequest getQueueUrlRequest = GetQueueUrlRequest.builder().queueName(queueName).build();
        GetQueueUrlResponse getQueueUrlResponse = this.sqsClient.getQueueUrl(getQueueUrlRequest);*/
        return "https://sqs.us-east-1.amazonaws.com/086435422519/Android_OTA.fifo";
    }

    /**
     * @return the sqsClient
     */
    public SqsClient getSqsClient() {
        return sqsClient;
    }

    /**
     * MyEvent POJO.
     */
    private static class MyEvent {
        private String id;
        private Instant timeStamp;
        private String source;
        private String payload;

        public MyEvent() {
            this.timeStamp = Instant.now();
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Instant getTimeStamp() {
            return timeStamp;
        }

        public void setTimeStamp(Instant timeStamp) {
            this.timeStamp = timeStamp;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public String getPayload() {
            return payload;
        }

        public void setPayload(String payload) {
            this.payload = payload;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", MyEvent.class.getSimpleName() + "[", "]")
                    .add("id='" + id + "'")
                    .add("timeStamp=" + timeStamp)
                    .add("source='" + source + "'")
                    .add("payload='" + payload + "'")
                    .toString();
        }
    }

    /**
     * An AWS SQS message producer.
     */
    private static class MyProducer implements Runnable {
        private static final Logger logger = LogManager.getLogger(MyProducer.class);
        private final String queueName;
        private final SqsClient sqsClient;

        public MyProducer(String queueName, SqsClient sqsClient) {
            this.queueName = queueName;
            this.sqsClient = sqsClient;
        }

        @Override
        public void run() {
            // sample message
            MyEvent myEvent = new MyEvent();
            myEvent.setId(UUID.randomUUID().toString());
            myEvent.setSource(Thread.currentThread().getName());
            myEvent.setPayload("AWS SQS message attributes example.");

            String message = null;
            try {
                message = objectMapper.writeValueAsString(myEvent);
            } catch (JsonProcessingException e) {
                logger.error(e);
            }

            if (message != null) {
                String queueUrl = getQueueUrl(this.queueName);
                // generates a UUID as the traceId
                String traceId = UUID.randomUUID().toString();
                final Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
                // add traceId as a message attribute
                messageAttributes.put(TRACE_ID_NAME, MessageAttributeValue.builder().dataType("String").stringValue(traceId).build());
                SendMessageRequest.Builder builder = SendMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .messageBody(message)
                        .messageAttributes(messageAttributes);
                // send the message
                logger.info("Sending message to queue {} with {}={}", this.queueName, TRACE_ID_NAME, traceId);
                this.sqsClient.sendMessage(builder.build());
            }
        }

        /**
         * Get the queue url by queue name.
         *
         * @param queueName
         * @return the queue url
         */
        private String getQueueUrl(String queueName) {
            GetQueueUrlRequest getQueueUrlRequest = GetQueueUrlRequest.builder().queueName(queueName).build();
            GetQueueUrlResponse getQueueUrlResponse = this.sqsClient.getQueueUrl(getQueueUrlRequest);
            return getQueueUrlResponse.queueUrl();
        }
    }

    /**
     * A runnable task run by a thread to simulate a user application consuming
     * messages from the queue.
     */
    private static class MyConsumer implements Runnable {
        private static final Logger logger = LogManager.getLogger(MyConsumer.class);
        private final String queueName;
        private final SqsClient sqsClient;

        /**
         * Extract message attribute.
         *
         * @param message       The message
         * @param attributeName The attribute name
         * @return The attribute value
         */
        private static String extractAttribute(Message message, String attributeName) {
            if (message.hasMessageAttributes()) {
                Map<String, MessageAttributeValue> messageAttributes = message.messageAttributes();
                MessageAttributeValue attributeValue = messageAttributes.get(attributeName);
                if (attributeValue != null) {
                    return attributeValue.stringValue();
                }
            }
            return null;
        }

        /**
         * Constructor.
         *
         * @param queueName
         * @param sqsClient
         */
        public MyConsumer(String queueName, SqsClient sqsClient) {
            this.queueName = queueName;
            this.sqsClient = sqsClient;
        }

        @Override
        public void run() {
            logger.info("Receiving messages from {}...", this.queueName);
            logger.info("Preethi");

            // long polling and wait for waitTimeSeconds before timed out
            String queueUrl = getQueueUrl(queueName);
            ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
                    .queueUrl("https://sqs.us-east-1.amazonaws.com/086435422519/Android_OTA.fifo")
                    .waitTimeSeconds(30)

                    .build();
            logger.info(this.sqsClient.receiveMessage(receiveMessageRequest).sdkHttpResponse());
            List<Message> messages = this.sqsClient.receiveMessage(receiveMessageRequest).messages();
            logger.info("test");
            logger.info("{} messages received.", messages.size());



            // process and delete message
            for (Message message : messages) {
                processMessage(message);
                deleteMessage(this.queueName, message);
            }
        }

        /**
         * Process message.
         *
         * @param message the message
         */
        private void processMessage(Message message) {
            logger.info("Processing message {}", message.messageId());

            // extract traceId
            String traceId = extractAttribute(message, TRACE_ID_NAME);

            // special handling before parsing the message body
            if (traceId == null || traceId.length() != 36) {
                logger.error("{} is compromised, message {} abandoned", TRACE_ID_NAME, message.messageId());
                return;
            }

            // deserialise message body
            MyEvent myEvent = null;
            try {
                myEvent = objectMapper.readValue(message.body(), MyEvent.class);
            } catch (JsonProcessingException e) {
                logger.error(e);
            }

            logger.info("Message processed: {}={}, MyEvent={}", TRACE_ID_NAME, traceId, myEvent == null ? null : myEvent.toString());
        }

        /**
         * Delete the message from the queue.
         *
         * @param queueName the queue name
         * @param message   the message to be deleted
         */
        private void deleteMessage(String queueName, Message message) {
            String queueUrl = getQueueUrl(queueName);
            logger.info("Deleting message {} from queue: {}", message.messageId(), queueName);
            DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder()
                    .queueUrl(queueUrl).receiptHandle(message.receiptHandle()).build();
            this.sqsClient.deleteMessage(deleteMessageRequest);
        }

        /**
         * Get the queue url by queue name.
         *
         * @param queueName
         * @return the queue url
         */
        private String getQueueUrl(String queueName) {
            /*GetQueueUrlRequest getQueueUrlRequest = GetQueueUrlRequest.builder().queueName(queueName).build();
            GetQueueUrlResponse getQueueUrlResponse = this.sqsClient.getQueueUrl(getQueueUrlRequest);
            return getQueueUrlResponse.queueUrl();*/
            return "https://sqs.us-east-1.amazonaws.com/086435422519/Android_OTA.fifo";
        }
    }

}
