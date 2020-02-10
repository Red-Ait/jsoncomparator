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

            compareJsonObjetcts(oldObject, newObject, "").forEach(r -> {
                System.out.println(r);
            });

        } catch (Exception e)  {
            e.printStackTrace();
        }
    }

    public static List<String> comparJsonArrays(JSONArray oldJsonArray, JSONArray newJsonArray, String proprieteName) {
        if(!oldJsonArray.isEmpty())
            if (!ClassUtils.isPrimitiveOrWrapper(oldJsonArray.get(0).getClass()) && !(oldJsonArray.get(0) instanceof String) ) {
                if(((JSONObject)oldJsonArray.get(0)).has("id"))
                    return compareJsonArraysWithId(oldJsonArray, newJsonArray, proprieteName);
            }
        return comparJsonArrayWithoutId(oldJsonArray, newJsonArray, proprieteName);
    }

    public static List<String> comparJsonArrayWithoutId(JSONArray oldJsonArray, JSONArray newJsonArray, String proprieteName) {

        List<String> results = new ArrayList<>();

        for (Object oldItem: oldJsonArray) {
            boolean flag = false;
            for (Object newItem: newJsonArray) {
                if(compareJsonObjetcts(oldItem, newItem, proprieteName).isEmpty()) {
                    flag = true;
                }
            }
            if (!flag) {
                results.add(proprieteName + " \"" + oldItem + "\" est supprimé") ;
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
                results.add(proprieteName + " \"" + newItem + "\" est ajouté") ;
            }
        }
        return results;
    }

    public static List<String> compareJsonObjetcts(Object oldJsonObject, Object newJsonObject, String proprieteName) {

        List<String> results = new ArrayList<>();


        if(oldJsonObject.getClass().getName() != newJsonObject.getClass().getName()){
            // Les deux objets sont de type déffirent
            results.add(proprieteName + " est changé de: " + oldJsonObject + " à: " + newJsonObject) ;
        } else {
            if(ClassUtils.isPrimitiveOrWrapper(oldJsonObject.getClass()) | oldJsonObject instanceof String  ) {
                // Les deux objets sont de type primitive
                if (!newJsonObject.equals(oldJsonObject)) {
                    // Les valeurs sont de valeur déffirent
                    results.add(proprieteName + " est changé de: " + oldJsonObject + " à: " + newJsonObject) ;
                }
            } else {
                if (oldJsonObject instanceof JSONArray) // Les deux objets sont de type Array
                    return comparJsonArrays((JSONArray) oldJsonObject, (JSONArray) newJsonObject, proprieteName);

                Map<String, Object> oldActorProperties = new HashMap<>();

                // stocker les ancienns proprietés dans une map(nom de propriété, Objet)
                for (String key : JSONObject.getNames((JSONObject) oldJsonObject)) {
                    oldActorProperties.put(key,((JSONObject) oldJsonObject).get(key));
                }

                Map<String, Object> newActorProperties = new HashMap<>();

                // stocker les ancienns proprietés dans une map(nom de propriété, Objet)
                for (String key : JSONObject.getNames((JSONObject) newJsonObject)) {
                    newActorProperties.put(key,((JSONObject) newJsonObject).get(key));
                }

                for (Map.Entry<String, Object> entry : oldActorProperties.entrySet()) {
                    if (newActorProperties.containsKey(entry.getKey())) { // Le nouveau objet contient la propriété
                        results.addAll(compareJsonObjetcts( entry.getValue(),
                                newActorProperties.get(entry.getKey()), proprieteName + "/[" +entry.getKey() + "]"));
                    } else {
                        // Le nouveau objet ne contient pas la propriété
                        results.add(proprieteName + "/[" +entry.getKey() + "] La propriété \" "+ entry.getKey() + "\"  est supprimée") ;
                    }
                }
                for (Map.Entry<String, Object> entry : newActorProperties.entrySet()) {
                    if (!oldActorProperties.containsKey(entry.getKey())) {
                        // une propriété est ajoutée
                        results.add(proprieteName + "/[" +entry.getKey() + "] La propriété \" "+ entry.getKey() + "\"  est ajoutée avec valeur: " + entry.getValue()) ;
                    }
                }
            }
        }

        return results;
    }

    public static List<String> compareJsonArraysWithId(JSONArray oldArray, JSONArray newArray, String proprieteName) {
       List<String > results = new ArrayList<>();
        for(Object oldObject: oldArray) {
            int oldId = ((JSONObject) oldObject).getInt("id");
            boolean flag = false;
            for(Object newObject: newArray) {
                int newId = ((JSONObject) newObject).getInt("id");
                if (newId == oldId) {
                    results.addAll(compareJsonObjetcts(oldObject, newObject, proprieteName + ":" + newId));
                    flag = true;
                }
            }
            if(!flag)
                results.add(proprieteName + ":" + oldId + " supprimé" );
        }
        for(Object newObject: newArray) {
            int newId = ((JSONObject) newObject).getInt("id");
            boolean flag = false;
            for(Object oldObject: newArray) {
                int oldId = ((JSONObject) oldObject).getInt("id");
                if (newId == oldId) {
                    flag = true;
                }
            }
            if(!flag)
            results.add(proprieteName + ":" + newId + "] ajouté" );
        }
       return  results;
    }

}
