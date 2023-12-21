
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import dev.langchain4j.chain.ConversationalChain;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.bedrock.BedrockAnthropicChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.SystemPropertyCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import static dev.langchain4j.data.message.AiMessage.aiMessage;

public class main {
    public static void main(String... args) throws Exception {
        System.out.println("Ask AI a question ");
        ConversationalChain chain = new main().createChatmodel();
        String userinput;
        String response;

        do {
            Scanner myObj = new Scanner(System.in);  // Create a Scanner object
            System.out.print("You: ");

            userinput = myObj.nextLine();  // Read user input
            response = chain.execute(userinput);

            System.out.println("AI: " + response);
        } while (!userinput.equals("bye"));


    }


    public ConversationalChain createChatmodel() {
        BedrockAnthropicChatModel bedrockChatModel = BedrockAnthropicChatModel
                .builder()
                .temperature(0.50f)
                .maxTokens(300)
                .region(Region.US_EAST_1)
                .credentialsProvider(ProfileCredentialsProvider.create("default"))
                .model(BedrockAnthropicChatModel.Types.AnthropicClaudeV21)
                .maxRetries(1)
                .build();

        ChatLanguageModel chatLanguageModel = bedrockChatModel;
        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);

        ConversationalChain chain = ConversationalChain.builder()
                .chatLanguageModel(chatLanguageModel)
                .chatMemory(chatMemory)
                .build();
        return chain;
    }
}
