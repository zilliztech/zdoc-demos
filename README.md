# Demos in Zilliz Cloud Documents

This repository provides all executable demos in Zilliz Cloud documents, including those in Python, Java, Golang, Node.js, and RESTful API.

## Before you start

Running any demo in this repository requires the installation of corresponding SDKs, and you are advised to keep these SDKs update to date.

## Roadmaps

### PyMilvus: Python SDK

Before running the demos in the `python` folder, ensure that you have run the following code snippets.

```shell
> cd python
> pip install -r requirements.txt
```

To run the demos, do as following in the `python` folder:

- Fill in necessary parameters, such as `YOUR_CLUSTER_ENDPOINT` and `YOUR_CLUSTER_TOKEN` in the file.

- Run the following command in the terminal.

    ```shell
    # replace this with the one you are interested in.
    > python 00_quick_start.py 
    ```

<table>
    <thead>
        <tr>
            <th>Case</th>
            <th>Code File</th>
            <th>Status</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td><a href="https://docs.zilliz.com/docs/quick-start-1">Quick start</a></td>
            <td><a href="https://github.com/zilliztech/zdoc-demos/blob/master/python/00_quick_start.py">python/00_quick_start.py</a></td>
            <td>Ready</td>
        </tr>
        <tr>
            <td><a href="https://docs.zilliz.com/docs/use-customized-schema">Use Customized Schema</a></td>
            <td><a href="https://github.com/zilliztech/zdoc-demos/blob/master/python/01_use_customized_schema.py">python/01_use_customized_schema.py</a></td>
            <td>Ready</td>
        </tr>
        <tr>
            <td><a href="https://docs.zilliz.com/docs/enable_dynamic_schema">Enable Dynamic Schema</a></td>
            <td>01_enable_dynamic_schema.py</td>
            <td>Ready</td>
        </tr>
        <tr>
            <td><a href="https://docs.zilliz.com/docs/enable_dynamic_schema">Enable Dynamic Schema</a></td>
            <td>01_enable_dynamic_schema.py</td>
            <td>Ready</td>
        </tr>
        <tr>
            <td><a href="https://docs.zilliz.com/docs/use_partition_key">Use Partition Key</a></td>
            <td>01_use_partition_key.py</td>
            <td>Ready</td>
        </tr>
        <tr>
            <td><a href="https://docs.zilliz.com/docs/javascript-object-notation-json-1">JavaScript Object Notation (JSON)</a></td>
            <td>01_use_json_field.py</td>
            <td>Ready</td>
        </tr>
        <tr>
            <td><a href="https://docs.zilliz.com/docs/use-bulkwriter-for-data-import">Use BulkWriter for Data Import</a></td>
            <td>01_use_bulk_writer_and_import.py</td>
            <td>Ready</td>
        </tr>
    </tbody>
</table>


### Java SDK

To run the demos in the `java` folder, run the following commands:

- Fill in necessary parameters, such as `YOUR_CLUSTER_ENDPOINT` and `YOUR_CLUSTER_TOKEN` in the file.

- Run the following commands in the terminal.

    ```shell
    # replace the folder with the one you are interested in.
    > cd java/quick_start 
    > mvn compile exec:java -Dexec.mainClass="com.zilliz.docs.QuickStartDemo"
    ```

<table>
    <thead>
        <tr>
            <th>Case</th>
            <th>Code File</th>
            <th>Status</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td><a href="https://docs.zilliz.com/docs/quick-start-1">Quick start</a></td>
            <td><a href="https://github.com/zilliztech/zdoc-demos/blob/master/java/quick_start/src/main/java/com/zilliz/docs/QuickStartDemo.java">java/quick_start</a></td>
            <td>Ready</td>
        </tr>
        <tr>
            <td><a href="https://docs.zilliz.com/docs/use-customized-schema">Use Customized Schema</a></td>
            <td><a href="https://github.com/zilliztech/zdoc-demos/blob/master/java/quick_start/src/main/java/com/zilliz/docs/UseCustomizedSchemaDemo.java">java/use_customized_schema</a></td>
            <td>Ready</td>
        </tr>
        <tr>
            <td><a href="https://docs.zilliz.com/docs/enable_dynamic_schema">Enable Dynamic Schema</a></td>
            <td>java/enable_dynamic_schema</td>
            <td>Ready</td>
        </tr>
        <tr>
            <td><a href="https://docs.zilliz.com/docs/enable_dynamic_schema">Enable Dynamic Schema</a></td>
            <td>java/enable_dynamic_schema</td>
            <td>Ready</td>
        </tr>
        <tr>
            <td><a href="https://docs.zilliz.com/docs/use_partition_key">Use Partition Key</a></td>
            <td>java/use_partition_key</td>
            <td>Ready</td>
        </tr>
        <tr>
            <td><a href="https://docs.zilliz.com/docs/javascript-object-notation-json-1">JavaScript Object Notation (JSON)</a></td>
            <td>java/use_json_field</td>
            <td>Ready</td>
        </tr>
        <tr>
            <td><a href="https://docs.zilliz.com/docs/use-bulkwriter-for-data-import">Use BulkWriter for Data Import</a></td>
            <td>java/use_bulk_writer_and_import</td>
            <td>Ready</td>
        </tr>
    </tbody>
</table>

### Node.js SDK

Before running the demos in the `node` folder, ensure that you have run the following code snippets.

```shell
> cd node
> npm install
```

To run the demos, do the following in the `node` folder:

- Fill in necessary parameters, such as `YOUR_CLUSTER_ENDPOINT` and `YOUR_CLUSTER_TOKEN` in the file.

- Run the following commands in the terminal.

    ```shell
    # replace this with the one you are interested in.
    > node 00_quick_start.js
    ```

<table>
    <thead>
        <tr>
            <th>Case</th>
            <th>Code File</th>
            <th>Status</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td><a href="https://docs.zilliz.com/docs/quick-start-1">Quick start</a></td>
            <td><a href="https://github.com/zilliztech/zdoc-demos/blob/master/node/00_quick_start.js">node/00_quick_start.js</a></td>
            <td>Ready</td>
        </tr>
        <tr>
            <td><a href="https://docs.zilliz.com/docs/use-customized-schema">Use Customized Schema</a></td>
            <td>node/01_use_customized_schema.js</td>
            <td>Ready</td>
        </tr>
        <tr>
            <td><a href="https://docs.zilliz.com/docs/enable_dynamic_schema">Enable Dynamic Schema</a></td>
            <td>node/01_enable_dynamic_schema.js</td>
            <td>Ready</td>
        </tr>
        <tr>
            <td><a href="https://docs.zilliz.com/docs/enable_dynamic_schema">Enable Dynamic Schema</a></td>
            <td>node/01_enable_dynamic_schema.js</td>
            <td>Ready</td>
        </tr>
        <tr>
            <td><a href="https://docs.zilliz.com/docs/use_partition_key">Use Partition Key</a></td>
            <td>node/01_use_partition_key.js</td>
            <td>Ready</td>
        </tr>
        <tr>
            <td><a href="https://docs.zilliz.com/docs/javascript-object-notation-json-1">JavaScript Object Notation (JSON)</a></td>
            <td>node/01_use_json_field.js</td>
            <td>Ready</td>
        </tr>
    </tbody>
</table>

### Go SDK

Before running the demos in the `go` folder, ensure that you have run the following code snippets.

```shell
# replace the folder with the one you are interested in.
> cd go/use_customized_schema
> go mod tidy
```

To run the demos, do the following in the `go` project folder:

- Fill in necessary parameters, such as `YOUR_CLUSTER_ENDPOINT` and `YOUR_CLUSTER_TOKEN` in the file.

- Run the following commands in the terminal.

    ```shell
    # replace the folder and file with the one you are interested in.
    > cd go/use_customized_schema
    > go run use_customized_schema.go
    ```

<table>
    <thead>
        <tr>
            <th>Case</th>
            <th>Code File</th>
            <th>Status</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td><a href="https://docs.zilliz.com/docs/use-customized-schema">Use Customized Schema</a></td>
            <td><a href="https://github.com/zilliztech/zdoc-demos/blob/master/go/use_customized_schema/main.go">go/use_customized_schema/main.go</a></td>
            <td>Ready</td>
        </tr>
        <tr>
            <td><a href="https://docs.zilliz.com/docs/enable_dynamic_schema">Enable Dynamic Schema</a></td>
            <td>go/enable_dynamic_schema/main.go</td>
            <td>Ready</td>
        </tr>
        <tr>
            <td><a href="https://docs.zilliz.com/docs/enable_dynamic_schema">Enable Dynamic Schema</a></td>
            <td>go/enable_dynamic_schema/main.go</td>
            <td>Ready</td>
        </tr>
        <tr>
            <td><a href="https://docs.zilliz.com/docs/use_partition_key">Use Partition Key</a></td>
            <td>go/use_partition_key/main.go</td>
            <td>Ready</td>
        </tr>
        <tr>
            <td><a href="https://docs.zilliz.com/docs/javascript-object-notation-json-1">JavaScript Object Notation (JSON)</a></td>
            <td>go/use_json_field/main.go</td>
            <td>Ready</td>
        </tr>
    </tbody>
</table>

### RESTful API

To run the demos, do the following in the `curl` folder:

- Fill in necessary parameters, such as `YOUR_CLUSTER_ENDPOINT` and `YOUR_CLUSTER_TOKEN` in the file.

- Run the following commands in the terminal.

    ```shell
    > cd curl
    # replace this with the one you are interested.
    > sh ./00_quick_start.sh
    ```

<table>
    <thead>
        <tr>
            <th>Case</th>
            <th>Code File</th>
            <th>Status</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td><a href="https://docs.zilliz.com/docs/quick-start-1">Quick start</a></td>
            <td><a href="https://github.com/zilliztech/zdoc-demos/blob/master/node/00_quick_start.js">curl/00_quick_start.sh</a></td>
            <td>Ready</td>
        </tr>
        <tr>
            <td><a href="https://docs.zilliz.com/docs/use-customized-schema">Use Customized Schema</a></td>
            <td>curl/01_use_customized_schema.sh</td>
            <td>Ready</td>
        </tr>
        <tr>
            <td><a href="https://docs.zilliz.com/docs/enable_dynamic_schema">Enable Dynamic Schema</a></td>
            <td>curl/01_enable_dynamic_schema.sh</td>
            <td>Ready</td>
        </tr>
        <tr>
            <td><a href="https://docs.zilliz.com/docs/enable_dynamic_schema">Enable Dynamic Schema</a></td>
            <td>curl/01_enable_dynamic_schema.sh</td>
            <td>Ready</td>
        </tr>
        <tr>
            <td><a href="https://docs.zilliz.com/docs/use_partition_key">Use Partition Key</a></td>
            <td>curl/01_use_partition_key.sh</td>
            <td>Ready</td>
        </tr>
        <tr>
            <td><a href="https://docs.zilliz.com/docs/javascript-object-notation-json-1">JavaScript Object Notation (JSON)</a></td>
            <td>curl/01_use_json_field.sh</td>
            <td>Ready</td>
        </tr>
    </tbody>
</table>
