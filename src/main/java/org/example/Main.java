package org.example;

import com.pkslow.ai.AIClient;
import com.pkslow.ai.GoogleBardClient;
import com.pkslow.ai.domain.Answer;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.image.CreateImageRequest;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.print.DocFlavor;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

class OpenAiApiExample
{
    private String TOKEN = "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";

    public static void main(String... args) throws InterruptedException
    {

        String token = "ZgiXRZupInt7HlViV75XsJXqzVnMLv_7NXSjS1sDvn6cfHfHFWXkDAGVOPYLenMWdqK6pA.;sidts-CjEBSAxbGb3wO-rlV6T6xS5pxzy7VXJe0F7GgV5_IfUMQ72PMJ5fQY3c6NwcYbgnQ8xdEAA";
        AIClient client = new GoogleBardClient(token);
        //        openAiChat(TOKEN);
        JSONObject jsonObject = getJson();
//        getFullJson(jsonObject);
        String sup = getItSupplier(jsonObject);
//        Answer answer = null;
//        answer = client.ask("remember what i send you now !");
        String chatGPT_Prompt = workingHours(jsonObject);
    }

    private static String workingHours(JSONObject jsonObject)
    {
        String output = "{ \"WorkingHours\": [";
        JSONArray workingHours = (JSONArray) jsonObject.get("IT_SUPPLIED_TIMEFRAMEGROUP");

        for(int i = 0; i < workingHours.size(); i++)
        {
            JSONObject jsonObject1 = (JSONObject) workingHours.get(i);
            String prompt = (String) jsonObject1.get("ID_REF");
            String start = (String) jsonObject1.get("START_TIME");
            String end = (String) jsoget("END_TIME");
            String du
            output += "{\"Person\": \"" + prompt + "\", \"Start\":\"" + start + "\", \"End\":\"" + end + "\"}";
            if(i < workingHours.size() - 1)
            {
                output += ",";
            }

        }
        return null;
    }

    private static String getItSupplier(JSONObject jsonObject)
    {
        StringBuilder suppliers = new StringBuilder("{ \\\"Supplier\\\": [{\" ");
        System.out.println("{");
        System.out.println("\"Supplier\": [{");
        JSONArray jsonArray = (JSONArray) jsonObject.get("IT_SUPPLIER");
        for (int i = 0; i < jsonArray.size(); i++)
        {
            JSONObject jsonObject1 = (JSONObject) jsonArray.get(i);
            String prompt = (String) jsonObject1.get("ID_REF");
            String lat = (String) jsonObject1.get("LATITUDE");
            String lon = (String) jsonObject1.get("LONGITUDE");
            JSONArray skillGroup = (JSONArray) jsonObject.get("IT_SUPPLIED_SKILLGROUP");
            suppliers.append("\"Skills\": [");
            System.out.println("\"Skills\": [");
            String skill = null;
            for (int j = 0; j < skillGroup.size(); j++)
            {
                JSONObject jsonObject2 = (JSONObject) skillGroup.get(j);
                String skillGroupPrompt = (String) jsonObject2.get("ID_REF");
                if (skillGroupPrompt.equals(prompt))
                {
                    if (skill == null){
                        suppliers.append("\n\"" + jsonObject2.get("SKILL_NAME") + "\"");
                        System.out.print("\n\"" + jsonObject2.get("SKILL_NAME") + "\"");
                        skill = "\"Skill\": \"" + jsonObject2.get("SKILL_NAME") + "\"";
                    }else{
                        suppliers.append(",\n\"" + jsonObject2.get("SKILL_NAME") + "\"");
                        System.out.println(",");
                        System.out.print("\"" + jsonObject2.get("SKILL_NAME") + "\"");
                        //                      System.out.println("Skill Level: " + jsonObject2.get("SKILL_LEVEL"));
                }
                }
            }
            suppliers.append("],\"Person\": \"" + prompt + "\", \"Lat\":\"" + lat+ "\", \"Lon\":\"" + lon+ "\"");
            System.out.println("],");
            System.out.println("\"Person\": \"" + prompt + "\",");
            System.out.println("\"Lat\":\"" + lat+ "\",");
            System.out.println("\"Lon\":\"" + lon+ "\"");
            if(i < jsonArray.size() - 1)
            {
                suppliers.append("},{");
                System.out.println("},{");
            }else{
                suppliers.append("}]");
                System.out.println("}]");
            }

        }
        System.out.println("}");
        suppliers.append("}}");
    return suppliers.toString();
    }

    private static void getFullJson(JSONObject jsonObject)
    {
        for (Iterator iterator = jsonObject.keySet().iterator(); iterator.hasNext(); )
        {
            String key = (String) iterator.next();
            System.out.println("--" + key);
            for(int i = 0; jsonObject.get(key) instanceof JSONArray && i < ((JSONArray) jsonObject.get(key)).size(); i++)
            {
                JSONObject jsonObject1 = (JSONObject) ((JSONArray) jsonObject.get(key)).get(i);
                System.out.println("--");
                for (Iterator iterator1 = jsonObject1.keySet().iterator(); iterator1.hasNext(); )
                {
                    String key1 = (String) iterator1.next();
                    System.out.println("----" + key1 + ": " + jsonObject1.get(key1));
                }
            }
        }
    }

    private void openAiChat(String token)
    {
        OpenAiService service = new OpenAiService(token);
        CompletionRequest completionRequest = CompletionRequest.builder().prompt("Somebody once told me the world is gonna roll me").model("ada").echo(true).build();
        service.createCompletion(completionRequest).getChoices().forEach(System.out::println);

    }

    private static JSONObject getJson()

    {
        JSONParser parser = new JSONParser();
        try
        {
            Object obj = parser.parse(new FileReader("/System/Volumes/Data/mps/prj/ChatGPT-PromptTest/src/main/resources/request-test.json"));

            return (JSONObject) obj;
        } catch (IOException | ParseException e)
        {
            e.printStackTrace();
        }
        return null;
    }

}
