package org.example;


import com.pkslow.ai.domain.Answer;
import lombok.Getter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.SystemPropertyCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

class Main {
    @Getter
    private static JSONObject supplierObject = new JSONObject();
    @Getter
    private static JSONObject JobObject = new JSONObject();

    private static S3Client s3;

    final String usage = """

            Usage:
              <bucketName> <objectKey> <objectPath>\s

            Where:
              bucketName - The Amazon S3 bucket to upload an object into.
              objectKey - The object to upload (for example, book.pdf).
              objectPath - The path where the file is located (for example, C:/AWS/book2.pdf).\s
            """;


    public static void main(String... args) throws Exception {

        JSONObject jsonObject = getJson();

        createObject(jsonObject);
        createJobObject(jsonObject);

        String[] chatGPT_Prompt = createPromt(supplierObject);

        for (String s : chatGPT_Prompt) {
            System.out.println(s);
        }

        System.out.println(supplierObject.toJSONString());
        System.out.println(JobObject.toJSONString());

        writeJson(createObject(jsonObject), "src/main/resources/JSON/Suppliers/", "supplier");
        writeJson(createJobObject(jsonObject), "src/main/resources/JSON/Job/", "job");

        multipartUpload("rs-ml-bucket", "supplier0.json", "src/main/resources/JSON/Suppliers/P.053800940.json");
    }

    private static String[] createPromt(JSONObject promtObject) {
        String[] prompts;
        JSONArray jsonArray = (JSONArray) promtObject.get("Suppliers");
        prompts = new String[jsonArray.size()];
        for (int i = 0; i < jsonArray.size(); i++) {
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

    private static JSONArray createJobObject(JSONObject jsonObject) {
        getJobObject().put("Jobs", new JSONArray());
        JSONArray array = (JSONArray) getJobObject().get("Jobs");
        JSONArray jsonArray = (JSONArray) jsonObject.get("IT_REQUISITIONER");
        for (int i = 0; i < jsonArray.toArray().length - 1; i++) {
            JSONObject jsonObject1 = (JSONObject) jsonArray.get(i);
            JSONObject job = new JSONObject();
            if (jsonObject1.get("ALLOCATED_SUPPLIER").equals("")) {
                job.put("Job_Id", jsonObject1.get("ID"));
                job.put("Start_Date", jsonObject1.get("PREDEFINED_START_DATE"));
                job.put("End_Date", jsonObject1.get("PREDEFINED_END_DATE"));
                job.put("Lon", jsonObject1.get("LONGITUDE"));
                job.put("Lat", jsonObject1.get("LATITUDE"));
                job.put("Skills", new JSONArray());
                array.add(job);
            }
        }
        getJobObject().put("Jobs", array);

        //ADD SkillGroup
        jsonArray = (JSONArray) jsonObject.get("IT_REQUIRED_HARDSKILLGROUP");
        for (Object o : jsonArray) {
            for (int j = 0; j < array.size(); j++) {
                JSONObject jsonObject1 = (JSONObject) o;
                JSONObject job = (JSONObject) array.get(j);
                if (jsonObject1.get("ID").equals(job.get("Job_Id"))) {
                    JSONArray skills = (JSONArray) job.get("Skills");
                    JSONObject skillObject = new JSONObject();

                    skillObject.put("Skill_NAME", jsonObject1.get("SKILL_NAME"));
                    skills.add(skillObject);
                    job.put("Skills", skills);
                    array.set(j, job);
                }
            }
        }
        return array;
    }


    private static JSONArray createObject(JSONObject jsonObject) {
        //Add Supplier
        getSupplierObject().put("Suppliers", new JSONArray());
        JSONArray array = (JSONArray) getSupplierObject().get("Suppliers");
        JSONArray jsonArray = (JSONArray) jsonObject.get("IT_SUPPLIER");
        for (int i = 0; i < jsonArray.toArray().length - 1; i++) {
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

        //ADD SkillGroup
        jsonArray = (JSONArray) jsonObject.get("IT_SUPPLIED_SKILLGROUP");
        for (Object o : jsonArray) {
            for (int j = 0; j < array.size(); j++) {
                JSONObject jsonObject1 = (JSONObject) o;
                JSONObject supplier = (JSONObject) array.get(j);
                if (jsonObject1.get("ID_REF").equals(supplier.get("SupplierID"))) {
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
        return array;
    }

    private static void writeJson(JSONArray array, String path, String filename) throws IOException {
        for (Object o : array) {
            JSONObject jsonObject = (JSONObject) o;
            if (filename.equals("supplier")) {
                filename = jsonObject.get("SupplierID").toString();
            } else if (filename.equals("job")) {
                filename = jsonObject.get("Job_Id").toString();
            }
            FileWriter file = new FileWriter(path + filename + array.indexOf(o) + ".json");
            file.write(jsonObject.toJSONString());
            file.close();
        }
    }

    private static JSONObject getJson() {
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(new FileReader("src/main/resources/request-test.json"));

            return (JSONObject) obj;
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void multipartUpload(String bucketName, String objectKey, String objectPath) throws IOException {

        S3Client s3 = S3Client.builder()
                .region(Region.EU_CENTRAL_1)
                .credentialsProvider(ProfileCredentialsProvider.create("default"))
                .build();

        putS3Object(s3, bucketName, objectKey, objectPath);
        s3.close();
    }

    public static void putS3Object(S3Client s3, String bucketName, String objectKey, String objectPath) {
        try {
            Map<String, String> metadata = new HashMap<>();
            metadata.put("x-amz-meta-myVal", "test");
            PutObjectRequest putOb = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .metadata(metadata)
                    .acl("public-read")
                    .build();

            s3.putObject(putOb, RequestBody.fromFile(new File(objectPath)));
            System.out.println("Successfully placed " + objectKey + " into bucket " + bucketName);

        } catch (S3Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }


    private static ByteBuffer getRandomByteBuffer(int size) throws IOException {
        byte[] b = new byte[size];
        new Random().nextBytes(b);
        return ByteBuffer.wrap(b);
    }
}
