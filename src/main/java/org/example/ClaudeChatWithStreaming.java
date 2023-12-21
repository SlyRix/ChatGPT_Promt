package org.example;

import org.json.JSONObject;
import org.util.BedrockRequestBody;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeAsyncClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelWithResponseStreamRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelWithResponseStreamResponseHandler;
import software.amazon.awssdk.services.bedrockruntime.model.PayloadPart;

import java.nio.charset.Charset;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

public class ClaudeChatWithStreaming {




    private static String answer = "";
    private static final String MODEL_ID = "anthropic.claude-v2";

    private static final String PROMPT = """
    give me a Example code for a AWS SDK for Java v2 client that uses the InvokeModelWithResponseStream API maven dependency for AWS Bedrock 
    """;

    public  String claudChat(String promt) throws Exception {

        final CountDownLatch countDownLatch = new CountDownLatch(1);

        try (BedrockRuntimeAsyncClient bedrockClient = BedrockRuntimeAsyncClient.builder()
                .region(Region.EU_CENTRAL_1)
                .credentialsProvider(ProfileCredentialsProvider.create())
                .build()) {

            try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {

                executor.submit(() -> {

                    String bedrockBody = BedrockRequestBody.builder()
                            .withModelId(MODEL_ID)
                            .withPrompt(promt)
                            .withInferenceParameter("max_tokens_to_sample", 2048)
                            .withInferenceParameter("temperature", 0.5)
                            .withInferenceParameter("top_k", 250)
                            .withInferenceParameter("top_p", 1)
                            .build();

                    InvokeModelWithResponseStreamRequest invokeModelRequest = InvokeModelWithResponseStreamRequest
                            .builder()
                            .modelId(MODEL_ID)
                            .body(SdkBytes.fromString(bedrockBody, Charset.defaultCharset()))
                            .build();

                    bedrockClient.invokeModelWithResponseStream(invokeModelRequest,
                            InvokeModelWithResponseStreamResponseHandler.builder()
                                    .onResponse(response -> {
                                        System.out.println("🤖 Response: ");
                                    })
                                    .subscriber(eventConsumer -> {
                                        eventConsumer.accept(new InvokeModelWithResponseStreamResponseHandler.Visitor() {
                                            public void visitChunk(PayloadPart payloadPart) {
                                                String payloadAsString = payloadPart.bytes().asUtf8String();
                                                JSONObject payloadAsJson = new JSONObject(payloadAsString);
                                                System.out.print(payloadAsJson.getString("completion"));
                                                answer += payloadAsJson.getString("completion");
                                            }
                                        });
                                    })
                                    .onComplete(() -> {
                                        System.out.println();
                                        countDownLatch.countDown();
                                    })
                                    .build());

                });

                countDownLatch.await();

            }

            answer = answer.replace("Human: \n Human: ", "");

        }


        return answer;
    }



}