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
import lombok.Getter;
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
    @Getter
    private static JSONObject supplierObject = new JSONObject();
    @Getter
    private static JSONObject JobObject = new JSONObject();

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

        assert jsonObject != null;
        createObject(jsonObject);
        createJobObject(jsonObject);
        String[] chatGPT_Prompt = createPromt(supplierObject);
        for (String s : chatGPT_Prompt) {
            System.out.println(s);
        }

        System.out.println(supplierObject.toJSONString());
        System.out.println(JobObject.toJSONString());

    }

    private static String[] createPromt(JSONObject promtObject) {
        String[] prompts;
        JSONArray jsonArray = (JSONArray) promtObject.get("Suppliers");
        prompts = new String[jsonArray.size()];
        for (int i = 0; i < jsonArray.size(); i++)
        {
            JSONObject jsonObject = (JSONObject) jsonArray.get(i);
            JSONArray skills = (JSONArray) jsonObject.get("Skills");
            JSONArray timeFrame = (JSONArray) jsonObject.get("WorkingHour");
            StringBuilder prompt = new StringBuilder();
            prompt.append("Supplier: ").append(jsonObject.get("SupplierID")).append("\n");
            // ADD Location
            prompt.append("Lat:").append(jsonObject.get("Lat")).append(", Lon:").append(jsonObject.get("Lon")).append("\n");
//            // ADD Skills
            prompt.append("Skills:");
            int count = 0;
            for (Object o : skills) {
                if (count < 6) {
                    JSONObject skill = (JSONObject) o;
                    prompt.append("\n  Skill Name: ").append(skill.get("Skill_NAME")).append(" -");
                    prompt.append("Skill Level: ").append(skill.get("Skill_LEVEL"));
                    count++;
                }
            }
            prompt.append("\n");
            // ADD TimeFrame
            prompt.append("WorkingHour:");
            count = 0;
            for (Object o : timeFrame) {
                if (count < 6) {
                    JSONObject time = (JSONObject) o;
                    prompt.append("\n  Start Date: ").append(time.get("Working_StartDate")).append("\n");
                    prompt.append("  End Date: ").append(time.get("Working_EndDate")).append("\n");
                    prompt.append("  Start Time: ").append(time.get("Working_StartTime")).append("\n");
                    prompt.append("  End Time: ").append(time.get("Working_EndTime"));
                    count++;
                }
            }
            prompts[i] = prompt.toString();
        }

        return prompts;
    }
    private static void createJobObject(JSONObject jsonObject) {
        getJobObject().put("Jobs", new JSONArray());
        JSONArray array = (JSONArray) getJobObject().get("Jobs");
        JSONArray jsonArray = (JSONArray) jsonObject.get("IT_REQUISITIONER");
        for (int i = 0; i < 6; i++)
        {
            JSONObject jsonObject1 = (JSONObject) jsonArray.get(i);
            JSONObject job = new JSONObject();

            job.put("Job_Id", jsonObject1.get("ID"));
            job.put("Start_Date", jsonObject1.get("PREDEFINED_START_DATE"));
            job.put("End_Date", jsonObject1.get("PREDEFINED_END_DATE"));
            job.put("Lon", jsonObject1.get("LONGITUDE"));
            job.put("Lat", jsonObject1.get("LATITUDE"));
            job.put("Skills", new JSONArray());
            array.add(job);
        }
        getJobObject().put("Jobs", array);

        //ADD SkillGroup
        jsonArray = (JSONArray) jsonObject.get("IT_REQUIRED_HARDSKILLGROUP");
        for (Object o : jsonArray){
            for (int j = 0; j < array.size(); j++) {
                JSONObject jsonObject1 = (JSONObject) o;
                JSONObject job = (JSONObject) array.get(j);
                if (jsonObject1.get("ID").equals(job.get("Job_Id"))) {
                    JSONArray skills = (JSONArray) job.get("Skills");
                    JSONObject skillObject = new JSONObject();

                    if (skills.size() < 6) {

                        skillObject.put("Skill_NAME", jsonObject1.get("SKILL_NAME"));
                        skills.add(skillObject);
                        job.put("Skills", skills);
                        array.set(j, job);
                    }
                }
            }
        }
    }
    private static void createObject(JSONObject jsonObject) {
        //Add Supplier
        getSupplierObject().put("Suppliers", new JSONArray());
        JSONArray array = (JSONArray) getSupplierObject().get("Suppliers");
        JSONArray jsonArray = (JSONArray) jsonObject.get("IT_SUPPLIER");
        for (int i = 10; i < 16; i++)
        {
            JSONObject jsonObject1 = (JSONObject) jsonArray.get(i);
            JSONObject supplier = new JSONObject();

            supplier.put("SupplierID", jsonObject1.get("ID_REF"));
            supplier.put("Lat", jsonObject1.get("LATITUDE"));
            supplier.put("Lon", jsonObject1.get("LONGITUDE"));
            supplier.put("Skills", new JSONArray());
            supplier.put("WorkingHour", new JSONArray());
            array.add(supplier);
        }
        getSupplierObject().put("Suppliers", array);

        //ADD TimeFrame
        jsonArray = (JSONArray) jsonObject.get("IT_SUPPLIED_TIMEFRAMEGROUP");
        for (Object o : jsonArray) {
            for (int j = 0; j < array.size(); j++) {
                JSONObject jsonObject1 = (JSONObject) o;
                JSONObject supplier = (JSONObject) array.get(j);
                if (jsonObject1.get("ID_REF").equals(supplier.get("SupplierID"))) {
                    JSONArray timeFrame = (JSONArray) supplier.get("WorkingHour");
                    if (timeFrame.size() < 6) {
                        JSONObject timeFrameObject = new JSONObject();
                        timeFrameObject.put("Working_StartDate", jsonObject1.get("START_DATE"));
                        timeFrameObject.put("Working_EndDate", jsonObject1.get("END_DATE"));
                        timeFrameObject.put("Working_StartTime", jsonObject1.get("START_TIME"));
                        timeFrameObject.put("Working_EndTime", jsonObject1.get("END_TIME"));
                        timeFrame.add(timeFrameObject);
                        supplier.put("Working_Hours", timeFrame);
                        array.set(j, supplier);
                    }

                }
            }
        }

        //ADD SkillGroup
        jsonArray = (JSONArray) jsonObject.get("IT_SUPPLIED_SKILLGROUP");
        for (Object o : jsonArray) {
            for (int j = 0; j < array.size(); j++) {
                JSONObject jsonObject1 = (JSONObject) o;
                JSONObject supplier = (JSONObject) array.get(j);
                if (jsonObject1.get("ID_REF").equals(supplier.get("SupplierID"))) {
                    JSONArray skills = (JSONArray) supplier.get("Skills");
                    JSONObject skillObject = new JSONObject();

                    if (skills.size() < 6) {

                        skillObject.put("Skill_NAME", jsonObject1.get("SKILL_NAME"));
                        skillObject.put("Skill_LEVEL", jsonObject1.get("SKILL_LEVEL"));
                        skills.add(skillObject);
                        supplier.put("Skills", skills);
                        array.set(j, supplier);
                    }
                }
            }
        }
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
                        suppliers.append("\n\"").append(jsonObject2.get("SKILL_NAME")).append("\"");
                        System.out.print("\n\"" + jsonObject2.get("SKILL_NAME") + "\"");
                        skill = "\"Skill\": \"" + jsonObject2.get("SKILL_NAME") + "\"";
                    }else{
                        suppliers.append(",\n\"").append(jsonObject2.get("SKILL_NAME")).append("\"");
                        System.out.println(",");
                        System.out.print("\"" + jsonObject2.get("SKILL_NAME") + "\"");
                        //                      System.out.println("Skill Level: " + jsonObject2.get("SKILL_LEVEL"));
                }
                }
            }
            suppliers.append("],\"Person\": \"").append(prompt).append("\", \"Lat\":\"").append(lat).append("\", \"Lon\":\"").append(lon).append("\"");
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

    public static void setJobObject(JSONObject jobObject) {
        JobObject = jobObject;
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
