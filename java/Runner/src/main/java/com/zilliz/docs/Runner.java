package com.zilliz.docs;

import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.Arrays;
import java.util.regex.*;
import org.json.JSONObject;
import org.json.JSONArray;

/**
 * Hello world!
 */
public final class Runner {
    private Runner() {
    }

    /**
     * Says hello to the world.
     * @param args The arguments of the program.
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        String mainClass = "com.zilliz.docs." + args[0];
        String classPath = "java/JavaSDKDemos";

        Path scriptPath = Paths.get(classPath + "/src/main/java/com/zilliz/docs/" + args[0] + ".java");
        String script = new String(Files.readAllBytes(scriptPath));

        script = script.replaceAll("(?m)^[ \t]*// Output:.*(?:\n[ \t]*//.*)*", "");
        script = script.replaceAll("(?m)^\n{3,}", "\n\n");
        
        Path envPath = Paths.get(args[1]);
        String env = new String(Files.readAllBytes(envPath));

        String[] envs = env.split("\n");
        
        for (String e : envs) {
            String[] kv = e.split("=");
            String key = kv[0];
            String value = kv[1];
            script = script.replaceAll(key, value.substring(1, value.length() - 1));
        }

        Files.write(scriptPath, script.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);

        ProcessBuilder processBuilder = new ProcessBuilder("mvn", "-f", classPath, "clean", "compile", "exec:java", "-Dexec.mainClass=" + mainClass);
        Process process = processBuilder.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String line;
        StringBuilder output = new StringBuilder();

        while ((line = reader.readLine()) != null) {
            if (
                line.startsWith("[INFO]") || 
                line.startsWith("[ERROR]") || 
                line.startsWith("[WARNING]") ||
                line.startsWith("INFO:") ||
                line.matches("^\\d{4}-\\d{2}-\\d{2}.*")
            ) {
                continue;
            }
            output.append(line + "\n");
        }

        List<String> outputLines = Arrays.asList(output.toString().split("\\n"));

        List<String> scriptLines = Arrays.asList(script.split("\\n"));

        int idx = 0;

        for (int i = 0; i < scriptLines.size(); i++) {
            String line1 = scriptLines.get(i);
            if (line1.contains(args[0])) {
                scriptLines.set(i, line1.replace(args[0], args[0]+"Copy"));
            }

            if (line1.contains("System.out.println")) {
                String line2 = outputLines.get(idx);
                if (args[2] == "debug") {
                    System.out.println(line2);
                }
                String pretty;
                if (line2.trim().startsWith("{")) {
                    JSONObject originalObject = new JSONObject(line2);
                    pretty = ((JSONObject) limitArraySize(originalObject, 10)).toString(4);
                } else if (line2.trim().startsWith("[")) {
                    JSONArray originalArray = new JSONArray(line2);
                    pretty = ((JSONArray) limitArraySize(originalArray, 10)).toString(4);
                } else {
                    pretty = line2;
                }
                String indent = line1.substring(0, line1.indexOf("System.out.println"));
                StringBuilder indented = new StringBuilder();
                for (String split : pretty.split("\n")) {
                    indented.append(indent + "// " + split + "\n");
                }
                pretty = indented.toString();
                scriptLines.set(i, line1 + "\n\n" + indent + "// Output:\n" + pretty + "\n\n");
                idx++;
            }
        }

        script = String.join("\n", scriptLines);

        for (String e : envs) {
            String[] kv = e.split("=");
            String key = kv[0];
            String value = kv[1];
            script = script.replaceAll(value.substring(1, value.length() - 1), key);
        }

        Path newScriptPath = Paths.get(classPath + "/src/main/java/com/zilliz/docs/" + args[0] + "Copy.java");

        Files.write(newScriptPath, script.getBytes());
    }

    public static Object limitArraySize(Object obj, int maxElements) {
        if (obj instanceof JSONArray) {
            JSONArray originalArray = (JSONArray) obj;
            JSONArray newArray = new JSONArray();
    
            for (int i = 0; i < Math.min(originalArray.length(), maxElements); i++) {
                newArray.put(limitArraySize(originalArray.get(i), maxElements));
            }

            if (originalArray.length() > maxElements) {
                int hiddenElements = originalArray.length() - maxElements;
                String hiddenElement = "(" + hiddenElements + " elements are hidden)";
                newArray.put(hiddenElement);
            }
    
            return newArray;
        } else if (obj instanceof JSONObject) {
            JSONObject originalObject = (JSONObject) obj;
            JSONObject newObject = new JSONObject();
    
            for (String key : originalObject.keySet()) {
                newObject.put(key, limitArraySize(originalObject.get(key), maxElements));
            }
    
            return newObject;
        } else {
            return obj;
        }
    }
}
