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

            JSONArray oldActorsList = new JSONObject(oldContent).getJSONArray("Actors");
            JSONArray newActorsList = new JSONObject(newContent).getJSONArray("Actors");


//            compareJsonObjetcts(new JSONObject(oldContent), new JSONObject(newContent));

            for (Object oldActor : oldActorsList) {
                JSONObject oldActorObj = (JSONObject) oldActor;

                int id = oldActorObj.getInt("id"); // id dans oldJson
                boolean isExist = false; //

                List<String> actions = new ArrayList<>();
                for (Object newActor : newActorsList) {

                    JSONObject newActorObj = (JSONObject) newActor;
                    // rechercher l'objet dans "newJson"
                    if (newActorObj.getInt("id") == id) {
                        isExist = true;

                        List<String> aux = compareJsonObjetcts(oldActor, newActor, "actors");
                        for (String i : aux) {
                            aux.set(aux.indexOf(i), "[Actor: " + id + "]" + i);
                        }
                        aux.forEach(r -> {
                            System.out.println(r);
                        });
                    }
                }
                if(!isExist) {
                    System.out.println("[Actor: " + id + "] supprimé: " );
                }
            }
            for (Object newActor : newActorsList) {
                JSONObject newActorObj = (JSONObject) newActor;

                int id = newActorObj.getInt("id"); // id dans newJson
                boolean isExist = false; //

                for (Object oldActor : oldActorsList) {

                    JSONObject oldActorObj = (JSONObject) oldActor;
                    // rechercher l'objet dans "newJson"
                    if (oldActorObj.getInt("id") == id) {
                        isExist = true;
                    }
                }
                if(!isExist) {
                    System.out.println("[Actor: " + id + "] ajouté: " );
                }
            }
       } catch (Exception e)  {
            e.printStackTrace();
        }
    }

    public static List<String> comparJsonArray(JSONArray oldJsonArray, JSONArray newJsonArray, String proprieteName) {

        List<String> results = new ArrayList<>();

        for (Object oldItem: oldJsonArray) {
            boolean flag = false;
            for (Object newItem: newJsonArray) {
                if(compareJsonObjetcts(oldItem, newItem, proprieteName).isEmpty()) {
                    flag = true;
                }
            }
            if (!flag) {
                results.add("\" " + oldItem + " \" est supprimé de " + proprieteName) ;
            }
        }
        for (Object newItem: newJsonArray) {
            boolean flag = false;
            for (Object oldItem: oldJsonArray) {
                if(compareJsonObjetcts(oldItem, newItem, proprieteName).isEmpty()) {
                    flag = true;
                }
            }
            if (!flag) {
                results.add("\" " + newItem + " \" est ajouté dans " + proprieteName) ;
            }
        }
        return results;
    }

    public static List<String> compareJsonObjetcts(Object oldJsonObject, Object newJsonObject, String proprieteName) {

        List<String> results = new ArrayList<>();

        if(ClassUtils.isPrimitiveOrWrapper(oldJsonObject.getClass()) | oldJsonObject instanceof String
                | newJsonObject instanceof String) {
            if (!newJsonObject.equals(oldJsonObject)) {
                results.add(" est changé de: " + oldJsonObject + " à: " + newJsonObject) ;
            }
        } else {
            if (oldJsonObject instanceof JSONArray)
                return comparJsonArray((JSONArray) oldJsonObject, (JSONArray) newJsonObject, proprieteName);

            Map<String, Object> oldActorProperties = new HashMap<>();

            // stocker les ancienns proprietés dans une map
            for (String key : JSONObject.getNames((JSONObject) oldJsonObject)) {
                oldActorProperties.put(key,((JSONObject) oldJsonObject).get(key));
            }

            Map<String, Object> newActorProperties = new HashMap<>();

            // stocker les ancienns proprietés dans une map
            for (String key : JSONObject.getNames((JSONObject) newJsonObject)) {
                newActorProperties.put(key,((JSONObject) newJsonObject).get(key));
            }

            for (Map.Entry<String, Object> entry : oldActorProperties.entrySet()) {
                if (newActorProperties.containsKey(entry.getKey())) {
                    List<String> aux = compareJsonObjetcts( entry.getValue(),  newActorProperties.get(entry.getKey()), entry.getKey());
                    for (String i : aux) {
                        aux.set(aux.indexOf(i), "/[" + entry.getKey() + "]" + i);
                    }
                    results.addAll(aux);
                } else {
                    results.add(" La propriété \" "+ entry.getKey() + "\"  est supprimée") ;
                }
            }
            for (Map.Entry<String, Object> entry : newActorProperties.entrySet()) {
                if (!oldActorProperties.containsKey(entry.getKey())) {
                    results.add(" La propriété \" "+ entry.getKey() + "\"  est ajoutée avec valeur: " + entry.getValue()) ;
                }
            }
        }

        return results;
    }
}
