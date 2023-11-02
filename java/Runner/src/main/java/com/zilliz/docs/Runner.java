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

    private static String formatOutput(String line) {
        String newLine = clean(line);

        if (newLine.startsWith("{")) {
            // treat it as an object
            String[] attrs = newLine.substring(1, newLine.length() - 1).split(",");
            newLine = "{" + formatAttrs(attrs) + "}";
        }

        if (newLine.startsWith("[")) {
            // treat it as an array
            String[] elements = newLine.substring(1, newLine.length() - 1).split(",");
            newLine = "[" + formatElements(elements) + "]";
        }

        return newLine;
    }

    private static String formatAttrs(String[] attrs) {
        attrs = validateArrays(attrs);
        System.out.println(Arrays.toString(attrs));
        for (int i = 0; i < attrs.length; i++) {
            String attr = attrs[i];
            System.out.println(attr);
            String[] kv = new String[2];

            if (attr.indexOf(":") > 0) {
                kv = attr.split(":");
            }

            if (attr.indexOf("=") > 0) {
                kv = attr.split("=");
            }

            String key = kv[0].trim();
            String value = kv[1].trim();

            if (value.equals("{}") || value.equals("[]")) {
                continue;
            } else {
                value = clean(value);
                
                if (value.startsWith("'") && value.endsWith("'")) {
                    // treat it as a string
                    value = "\"" + value.substring(1, value.length() - 1) + "\"";
                }

                if (value == "True" || value == "False") {
                    // treat it as a boolean
                    value = value.toLowerCase();
                }

                if (value.startsWith("{") && value.endsWith("}")) {
                    // treat it as an object
                    String[] valueAttrs = value.substring(1, value.length() - 1).split(",");
                    value = "{" + formatAttrs(valueAttrs) + "}";
                }

                if (value.startsWith("[") && value.endsWith("]")) {
                    // treat it as an array
                    String[] valueElements = value.substring(1, value.length() - 1).split(",");
                    value = "[" + formatElements(valueElements) + "]";
                }

                attrs[i] = "\"" + key + "\": " + value;
            }
        }

        return String.join(", ", attrs);
    }

    private static String formatElements(String[] elements) {
        elements = validateArrays(elements);
        System.out.println(Arrays.toString(elements));
        for (int i = 0; i < elements.length; i++) {
            String element = elements[i];
            System.out.println(element);

            if (element.equals("{}") || element.equals("[]")) {
                continue;
            } else {
                element = clean(element);

                if (element.startsWith("{") && element.endsWith("}")) {
                    // treat it as an object
                    String[] elementAttrs = element.substring(1, element.length() - 1).split(",");
                    element = "{" + formatAttrs(elementAttrs) + "}";
                }

                if (element.startsWith("[") && element.endsWith("]")) {
                    // treat it as an array
                    String[] elementElements = element.substring(1, element.length() - 1).split(",");
                    element = "[" + formatElements(elementElements) + "]";
                }

                if (element.startsWith("'") && element.endsWith("'")) {
                    // treat it as a string
                    element = "\"" + element.substring(1, element.length() - 1) + "\"";
                }

                if (element == "True" || element == "False") {
                    // treat it as a boolean
                    element = element.toLowerCase();
                }
            }

            elements[i] = element;
        }

        return String.join(", ", elements);
    }

    private static String clean(String line) {
        if (countIn("{", line) == countIn("}", line) && countIn("[", line) == countIn("]", line)) {
            int firstBracebracket = line.indexOf("{");
            int firstSquarebracket = line.indexOf("[");

            if (line.equals("{}") || line.equals("[]")) {
                return line;
            }

            if (firstBracebracket == -1 && firstSquarebracket == -1) {
                return line;
            }

            if (firstBracebracket == -1) {
                line = line.substring(line.indexOf("["), line.lastIndexOf("]"));
            }

            if (firstSquarebracket == -1) {
                line = line.substring(line.indexOf("{"), line.lastIndexOf("}"));
            }

            if (firstBracebracket < firstSquarebracket) {
                line = line.substring(line.indexOf("{"), line.lastIndexOf("}"));
            } else {
                line = line.substring(line.indexOf("["), line.lastIndexOf("]"));
            }
        }

        return line;
    }

    private static String[] validateArrays(String[] array) {
        for (int i = 0; i < array.length; i++) {
            String element = array[i];
            if (countIn("{", element) != countIn("}", element)) {
                String[] subArray = Arrays.copyOfRange(array, i, array.length);
                String pair = Arrays.stream(subArray).filter(e -> e.contains("}")).toArray(String[]::new)[0];
                int idx = Arrays.asList(array).indexOf(pair);
                String[] subArray2 = Arrays.copyOfRange(array, i, idx + 1);
                array[i] = String.join(",", subArray2);

                for (int j = i; j < idx + 1; j++) {
                    array[j] = "";
                }
            }

            if (countIn("[", element) != countIn("]", element)) {
                String[] subArray = Arrays.copyOfRange(array, i, array.length);
                String pair = Arrays.stream(subArray).filter(e -> e.contains("]")).toArray(String[]::new)[0];
                int idx = Arrays.asList(array).indexOf(pair);
                String[] subArray2 = Arrays.copyOfRange(array, i, idx + 1);
                array[i] = String.join(",", subArray2);

                for (int j = i; j < idx + 1; j++) {
                    array[j] = "";
                }
            }
        }

        return Arrays.stream(array).filter(e -> !e.equals("")).toArray(String[]::new);
    }

    private static int countIn(String target, String line) {
        return line.length() - line.replace(target, "").length();
    }   


}
