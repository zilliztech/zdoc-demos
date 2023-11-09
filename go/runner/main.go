package main

import (
	"bytes"
	"encoding/json"
	"os"
	"os/exec"
	"regexp"
	"strings"
)

func main() {
	folder := os.Args[1]
	env := os.Args[2]
	debug := "false"
	if len(os.Args) == 4 {
		debug = os.Args[3]
	}

	scripts_, err := os.ReadFile("go/" + folder + "/main.go")

	re := regexp.MustCompile(`(?m)\t*// Output:.*\n(\t*//.*\n)*`)

	cleanScripts := re.ReplaceAllString(string(scripts_), "")

	if err != nil {
		panic(err)
	}

	envs_, err := os.ReadFile(env)

	if err != nil {
		panic(err)
	}

	scripts := strings.Split(cleanScripts, "\n")
	envs := strings.Split(string(envs_), "\n")

	envsMap := make(map[string]string)

	for _, env := range envs {
		kv := strings.Split(env, "=")
		envsMap[kv[0]] = kv[1]
	}

	for i, line := range scripts {
		for k, v := range envsMap {
			line = strings.ReplaceAll(line, k, v[1:len(v)-1])
		}

		if strings.HasPrefix(strings.Trim(line, "\t"), "DATA_FILE") {
			line = strings.ReplaceAll(line, "../../", "")
		}

		scripts[i] = line
	}

	tmpfile, err := os.Create("go/" + folder + "/mainTemp.go")

	if err != nil {
		panic(err)
	}

	if debug == "true" {
		defer tmpfile.Close()
	} else {
		defer os.Remove(tmpfile.Name())
	}

	if _, err := tmpfile.Write([]byte(strings.Join(scripts, "\n"))); err != nil {
		panic(err)
	}

	cmd := exec.Command("go", "run", tmpfile.Name())
	output, err := cmd.CombinedOutput()

	if err != nil {
		panic(err)
	}

	outputLines := strings.Split(string(output), "\n")

	idx := 0

	for i, line := range scripts {
		indent := strings.Count(line, "\t")

		for k, v := range envsMap {
			line = strings.ReplaceAll(line, v[1:len(v)-1], k)
		}

		if strings.HasPrefix(strings.Trim(line, "\t"), "DATA_FILE") {
			value := strings.Split(line, "=")[1][2 : len(strings.Split(line, "=")[1])-1]
			line = strings.Repeat("\t", indent) + "DATA_FILE := \"../../" + value + "\""
		}

		if strings.Contains(line, "fmt.Println") {
			if strings.HasPrefix(outputLines[idx], "[") || strings.HasPrefix(outputLines[idx], "{") {
				var prettyJSON bytes.Buffer
				err := json.Indent(&prettyJSON, []byte(outputLines[idx]), "", "\t")
				if err != nil {
					panic(err)
				}

				splits := strings.Split(prettyJSON.String(), "\n")

				for i, split := range splits {
					splits[i] = strings.Repeat("\t", indent) + "// " + split
				}

				splits = append([]string{strings.Repeat("\t", indent) + "// Output: "}, splits...)

				line = line + "\n\n" + strings.Join(splits, "\n")
			} else {
				line = line + "\n\n" + strings.Repeat("\t", indent) + "// Output: \n" + strings.Repeat("\t", indent) + "//\n" + strings.Repeat("\t", indent) + "// " + outputLines[idx]
			}
			idx++

		}

		scripts[i] = line
	}

	copyFile, err := os.Create("go/" + folder + "/mainCopy.go")

	if err != nil {
		panic(err)
	}

	defer copyFile.Close()

	if _, err := copyFile.Write([]byte(strings.Join(scripts, "\n"))); err != nil {
		panic(err)
	}

}
