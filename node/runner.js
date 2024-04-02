const fs = require("fs")
const path = require("path")
const commander = require("commander")
const { spawn } = require("child_process")

commander
    .version("0.0.1", "-v, --version")
    .usage("[options] <command> [...]")
    .option("-f, --file <file>", "script file path")
    .option("-e, --env <env>", "path to the .env file")
    .option("-d, --debug", "output extra debugging info")
    .option("-r, --remove", "remove output")
    .option("-l, --localBuild <localBuild>", "build local sdk version")
    .parse(process.argv)

const options = commander.opts()

if (options.file === null) {
    console.log("Please specify the script file path")
    process.exit(1)
}

if (options.env === null) {
    console.log("Please specify the .env file path")
    process.exit(1)
}

const env = fs.readFileSync(options.env, "utf8")
var script = fs.readFileSync(options.file, "utf8")

env.split("\n").map(line => {
    const key  = line.slice(0, line.indexOf("="))
    const value = line.slice(line.indexOf("=") + 1)
    script = script.replace(new RegExp(`"${key}"`, "g"), value)
})

if (options.localBuild) {
    script = script.replace("@zilliz/milvus2-sdk-node", options.localBuild)
}

if (options.remove) {
    script = script.replace(/[^\S\r\n]*\/\/ Output\n(?:[^\S\r\n]*\/\/[^\S\r\n].*\n)*/g, "")
}

const logs = []

const nodeModulesPath = path.resolve(__dirname, "node_modules")

var proc = spawn("node", ["-e", script], {
    env: {
        ...process.env,
        NODE_PATH: nodeModulesPath
    }
})

proc.stdout.on("data", data => {
    logs.push(data)
})

proc.stdout.on("end", () => {
    if (options.debug) {
        console.log(logs.join("\n"))
    }

    script = script.split("\n").map(line => {
        if (options.localBuild && line.includes(options.localBuild)) {
            return line.replace(options.localBuild, "@zilliz/milvus2-sdk-node")
        }

        if (line.includes("console.log")) {
            var indent = line.match(/^\s*/)[0].length
            var output = "Output\n\n" + logs.shift()
            output = output.split("\n").map(line => " ".repeat(indent) + "// " + line).join("\n")
            line += '\n\n' + output
        }

        return line
    }).join("\n")

    env.split("\n").map(line => {
        const key  = line.slice(0, line.indexOf("="))
        const value = line.slice(line.indexOf("=") + 1)
        script = script.replace(value, `"${key}"`)
    })

    fs.writeFileSync(options.file.split('.')[0] + "_copy.js", script)
})

