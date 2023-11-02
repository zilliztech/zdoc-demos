package com.zilliz.docs;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

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
        String classPath = "java/" + args[0];

        Path scriptPath = Paths.get(classPath + "/src/main/java/com/zilliz/docs/" + args[0] + ".java");
        String script = new String(Files.readAllBytes(scriptPath));

        Path envPath = Paths.get(args[1]);
        String env = new String(Files.readAllBytes(envPath));

        String[] envs = env.split("\n");
        
        for (String e : envs) {
            String[] kv = e.split("=");
            String key = kv[0];
            String value = kv[1];
            script = script.replaceAll(key, value.substring(1, value.length() - 1));
        }

        Files.write(scriptPath, script.getBytes());

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
                String indent = line1.substring(0, line1.indexOf("System.out.println"));
                scriptLines.set(i, line1 + "\n\n" + indent + "// Output:\n" + indent + "// " + line2 + "\n\n");
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
}
