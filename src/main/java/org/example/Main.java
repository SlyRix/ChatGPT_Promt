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

class Main
{
    private static JSONObject supplierObject = new JSONObject();
    private String TOKEN = "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";

    public static void main(String... args) throws InterruptedException
    {

        String token = "ZgiXRZupInt7HlViV75XsJXqzVnMLv_7NXSjS1sDvn6cfHfHFWXkDAGVOPYLenMWdqK6pA.;sidts-CjEBSAxbGb3wO-rlV6T6xS5pxzy7VXJe0F7GgV5_IfUMQ72PMJ5fQY3c6NwcYbgnQ8xdEAA";
//        AIClient client = new GoogleBardClient(token);
        //        openAiChat(TOKEN);
        JSONObject jsonObject = getJson();
//        getFullJson(jsonObject);
//        String sup = getItSupplier(jsonObject);
        Answer answer = null;
//        answer = client.ask("remember what i send you now !");
//        String chatGPT_Prompt = workingHours(jsonObject);

        JSONObject promtObject = createObject(jsonObject);
        String[] chatGPT_Prompt = createPromt(promtObject);
        for(int i = 0; i < chatGPT_Prompt.length; i++)
        {
            System.out.println(chatGPT_Prompt[i]);
        }
    }

    private static String[] createPromt(JSONObject promtObject) {
        String[] prompts;
        JSONArray jsonArray = (JSONArray) promtObject.get("Suppliers");
        prompts = new String[jsonArray.size()];
        for (int i = 0; i < jsonArray.size(); i++)
        {
            JSONObject jsonObject = (JSONObject) jsonArray.get(i);
            JSONArray skills = (JSONArray) jsonObject.get("Skills");
            JSONArray timeFrame = (JSONArray) jsonObject.get("TimeFrame");
            StringBuilder prompt = new StringBuilder();
            prompt.append("Supplier: " + jsonObject.get("SupplierID") + "\n");
            prompt.append("Skills:");
            for (int j = 0; j < skills.size(); j++)
            {
                JSONObject skill = (JSONObject) skills.get(j);
                prompt.append("\n  Skill Name "+skill.get("Skill_NAME") + "\n");
                prompt.append("  Skill Level "+skill.get("Skill_LEVEL") + "");
            }
            prompt.append("\n");
            prompt.append("TimeFrame:");
            for (int j = 0; j < timeFrame.size(); j++)
            {
                JSONObject time = (JSONObject) timeFrame.get(j);
                prompt.append("\n  Start Date: "+time.get("TimeFrameStartDate") + "\n");
                prompt.append("  End Date: "+time.get("TimeFrameEndDate") + "\n");
                prompt.append("  Start Time: "+time.get("TimeFrameStartTime") + "\n");
                prompt.append("  End Time: "+time.get("TimeFrameEndTime") + "");
            }
            prompts[i] = prompt.toString();
        }

        return prompts;
    }

    private static JSONObject createObject(JSONObject jsonObject) {
        //Add Supplier
        getSupplierObject().put("Suppliers", new JSONArray());
        JSONArray array = (JSONArray) getSupplierObject().get("Suppliers");
        JSONArray jsonArray = (JSONArray) jsonObject.get("IT_SUPPLIER");
        for (int i = 0; i < jsonArray.size(); i++)
        {
            JSONObject jsonObject1 = (JSONObject) jsonArray.get(i);
            JSONObject supplier = new JSONObject();

            supplier.put("SupplierID", jsonObject1.get("ID_REF"));
            supplier.put("SupplierLat", jsonObject1.get("LATITUDE"));
            supplier.put("SupplierLon", jsonObject1.get("LONGITUDE"));
            supplier.put("Skills", new JSONArray());
            supplier.put("TimeFrame", new JSONArray());
            array.add(supplier);
        }
        getSupplierObject().put("Suppliers", array);

        //ADD TimeFrame
        jsonArray = (JSONArray) jsonObject.get("IT_SUPPLIED_TIMEFRAMEGROUP");
        for (int i = 0; i < jsonArray.size(); i++)
        {
            for(int j = 0; j < array.size(); j++)
            {
                JSONObject jsonObject1 = (JSONObject) jsonArray.get(i);
                JSONObject supplier = (JSONObject) array.get(j);
                if(jsonObject1.get("ID_REF").equals(supplier.get("SupplierID")))
                {
                    JSONArray timeFrame = (JSONArray) supplier.get("TimeFrame");
                    JSONObject timeFrameObject = new JSONObject();
                    timeFrameObject.put("TimeFrameStartDate", jsonObject1.get("START_DATE"));
                    timeFrameObject.put("TimeFrameEndDate", jsonObject1.get("END_DATE"));
                    timeFrameObject.put("TimeFrameStartTime", jsonObject1.get("START_TIME"));
                    timeFrameObject.put("TimeFrameEndTime", jsonObject1.get("END_TIME"));
                    timeFrame.add(timeFrameObject);
                    supplier.put("TimeFrame", timeFrame);
                    array.set(j, supplier);
                }
            }
        }

        //ADD SkillGroup
        jsonArray = (JSONArray) jsonObject.get("IT_SUPPLIED_SKILLGROUP");
        for (int i = 0; i < jsonArray.size(); i++)
        {
            for(int j = 0; j < array.size(); j++)
            {
                JSONObject jsonObject1 = (JSONObject) jsonArray.get(i);
                JSONObject supplier = (JSONObject) array.get(j);
                if(jsonObject1.get("ID_REF").equals(supplier.get("SupplierID")))
                {
                    JSONArray skills = (JSONArray) supplier.get("Skills");
                    JSONObject skillObject = new JSONObject();
                    skillObject.put("Skill_NAME", jsonObject1.get("SKILL_NAME"));
                    skillObject.put("Skill_LEVEL", jsonObject1.get("SKILL_LEVEL"));
                    skills.add(skillObject);
                    supplier.put("Skills", skills);
                    array.set(j, supplier);
                }
            }
        }
        return getSupplierObject();
    }

    public static JSONObject getSupplierObject() {
        return supplierObject;
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
            Object obj = parser.parse(new FileReader("src/main/resources/request-test.json"));

            return (JSONObject) obj;
        } catch (IOException | ParseException e)
        {
            e.printStackTrace();
        }
        return null;
    }

}
