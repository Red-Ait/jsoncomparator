package com.berexia.jsonreader;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.ClassUtils;

import java.io.File;
import java.util.*;

@SpringBootApplication
public class JsonreaderApplication {

    public static void main(String[] args) {
        SpringApplication.run(JsonreaderApplication.class, args);
        try {
            File oldFile = new File("./oldJson.json");
            File newFile = new File("./newJson.json");

            String oldContent = FileUtils.readFileToString(oldFile, "utf-8");
            String newContent = FileUtils.readFileToString(newFile, "utf-8");

            JSONObject oldObject = new JSONObject(oldContent);
            JSONObject newObject = new JSONObject(newContent);

            JsonCompare.setIdentifierKeyName("id");

            JsonCompare.compare(oldObject, newObject).forEach(r -> System.out.println(r));

        } catch (Exception e)  {
            e.printStackTrace();
        }
    }


}
