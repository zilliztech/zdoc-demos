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

- Fill in the necessary parameters, such as `YOUR_CLUSTER_ENDPOINT` and `YOUR_CLUSTER_TOKEN` in the file.

- Run the following command in the terminal.

    ```shell
    # replace this with the one you are interested in.
    > python 00_quick_start.py 
    ```

#### Doc content

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
            <td rowspan="2"><a href="https://docs.zilliz.com/docs/quick-start-1">Quick start</a></td>
            <td><a href="https://github.com/zilliztech/zdoc-demos/blob/master/python/00_quick_start.py">python/00_quick_start.py</a></td>
            <td>Ready</td>
        </tr>
        <tr>
            <td><a href="https://github.com/zilliztech/zdoc-demos/blob/master/python/00_quick_start.ipynb">python/00_quick_start.ipynb</a></td>
            <td>Ready</td>
        </tr>
        <tr>
            <td rowspan=2><a href="https://docs.zilliz.com/docs/use-customized-schema">Use Customized Schema</a></td>
            <td><a href="https://github.com/zilliztech/zdoc-demos/blob/master/python/01_use_customized_schema.py">python/01_use_customized_schema.py</a></td>
            <td>Ready</td>
        </tr>
        <tr>
            <td><a href="https://github.com/zilliztech/zdoc-demos/blob/master/python/01_use_customized_schema.ipynb">python/01_use_customized_schema.ipynb</a></td>
            <td>Ready</td>
        </tr>
        <tr>
            <td rowspan=2><a href="https://docs.zilliz.com/docs/enable_dynamic_schema">Enable Dynamic Schema</a></td>
            <td><a href="https://github.com/zilliztech/zdoc-demos/blob/master/python/02_enable_dynamic_schema.py">python/02_enable_dynamic_schema.py</a></td>
            <td>Ready</td>
        </tr>
        <tr>
            <td><a href="https://github.com/zilliztech/zdoc-demos/blob/master/python/02_enable_dynamic_schema.ipynb">python/02_enable_dynamic_schema.ipynb</a></td>
            <td>Ready</td>            
        </tr>
        <tr>
            <td rowspan=2><a href="https://docs.zilliz.com/docs/use_partition_key">Use Partition Key</a></td>
            <td><a href="https://github.com/zilliztech/zdoc-demos/blob/master/python/03_use_partition_key.py">python/03_use_partition_key.py</a></td>
            <td>Ready</td>
        </tr>
        <tr>
            <td><a href="https://github.com/zilliztech/zdoc-demos/blob/master/python/03_use_partition_key.ipynb">python/03_use_partition_key.ipynb</a></td>
            <td>Ready</td>            
        </tr>
        <tr>
            <td rowspan=2><a href="https://docs.zilliz.com/docs/javascript-object-notation-json-1">Use JSON Fields</a></td>
            <td><a href="https://github.com/zilliztech/zdoc-demos/blob/master/python/04_use_json_field.py">python/04_use_json_field.py</a></td>
            <td>Ready</td>
        </tr>
        <tr>
            <td><a href="https://github.com/zilliztech/zdoc-demos/blob/master/python/04_use_json_field.ipynb">python/04_use_json_field.ipynb</a></td>
            <td>Ready</td>            
        </tr>
        <tr>
            <td rowspan=6><a href="https://docs.zilliz.com/docs/use-bulkwriter-for-data-import">Use BulkWriter for Data Import</a></td>
            <td><a href="https://github.com/zilliztech/zdoc-demos/blob/master/python/05_use_local-bulk-writer.py">python/05_use_local-bulk-writer.py</a></td>
            <td>Ready</td>
        </tr>
        <tr>
            <td><a href="https://github.com/zilliztech/zdoc-demos/blob/master/python/05_use_local-bulk-writer.ipynb">python/05_use_local-bulk-writer.ipynb</a></td>
            <td>Ready</td>            
        </tr>
        <tr>
            <td><a href="https://github.com/zilliztech/zdoc-demos/blob/master/python/06_use_remote-bulk-writer.py">python/06_use_remote-bulk-writer.py</a></td>
            <td>Ready</td>
        </tr>
        <tr>
            <td><a href="https://github.com/zilliztech/zdoc-demos/blob/master/python/06_use_remote-bulk-writer.ipynb">python/06_use_remote-bulk-writer.ipynb</a></td>
            <td>Ready</td>            
        </tr>
        <tr>
            <td><a href="https://github.com/zilliztech/zdoc-demos/blob/master/python/07_use_bulk_import.py">python/07_use_bulk_import.py</a></td>
            <td>Ready</td>
        </tr>
        <tr>
            <td><a href="https://github.com/zilliztech/zdoc-demos/blob/master/python/07_use_bulk_import.ipynb">python/07_use_bulk_import.ipynb</a></td>
            <td>Ready</td>            
        </tr>
        <tr>
            <td rowspan="2"><a href="https://docs.zilliz.com/docs/upsert-entities">Upsert Entities</a></td>
            <td><a href="https://github.com/zilliztech/zdoc-demos/blob/master/python/08_upsert_entities.py">python/08_upsert_entities.py</a></td>
            <td>Ready</td>
        </tr>
        <tr>
            <td><a href="https://github.com/zilliztech/zdoc-demos/blob/master/python/08_upsert_entities.ipynb">python/08_upsert_entities.ipynb</a></td>
            <td>Ready</td>
        </tr>
        <tr>
            <td rowspan="2"><a href="https://docs.zilliz.com/docs/search-query-iterators">Search & Query with Iterators</a></td>
            <td><a href="https://github.com/zilliztech/zdoc-demos/blob/master/python/09_search_query_iterators.py">python/09_search_query_iterators.py</a></td>
            <td>Ready</td>
        </tr>
        <tr>
            <td><a href="https://github.com/zilliztech/zdoc-demos/blob/master/python/09_search_query_iterators.ipynb">python/09_search_query_iterators.ipynb</a></td>
            <td>Ready</td>
        </tr>
        <tr>
            <td rowspan="2"><a href="https://docs.zilliz.com/docs/search-query-advanced-ops">Search & Query with Advanced Ops</a></td>
            <td><a href="https://github.com/zilliztech/zdoc-demos/blob/master/python/10_search_query_advanced_ops.py">python/10_search_query_advanced_ops.py</a></td>
            <td>Ready</td>
        </tr>
        <tr>
            <td><a href="https://github.com/zilliztech/zdoc-demos/blob/master/python/10_search_query_advanced_ops.ipynb">python/10_search_query_advanced_ops.ipynb</a></td>
            <td>Ready</td>
        </tr>
        <tr>
            <td rowspan="2"><a href="https://docs.zilliz.com/docs/conduct-range-search">Conduct a Range Search</a></td>
            <td><a href="https://github.com/zilliztech/zdoc-demos/blob/master/python/11_conduct_a_range_search.py">python/11_conduct_a_range_search.py</a></td>
            <td>Ready</td>
        </tr>
        <tr>
            <td><a href="https://github.com/zilliztech/zdoc-demos/blob/master/python/11_conduct_a_range_search.ipynb">python/11_conduct_a_range_search.ipynb</a></td>
            <td>Ready</td>
        </tr>
    </tbody>
</table>

#### Integrations

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
            <td rowspan="2"><a href="https://docs.zilliz.com/docs/quick-start-1">Integartions with OpenAI</a></td>
            <td><a href="https://github.com/zilliztech/zdoc-demos/blob/master/python/80_integrations_openai.py">python/80_integrations_openai.py</a></td>
            <td>Ready</td>
        </tr>
        <tr>
            <td><a href="https://github.com/zilliztech/zdoc-demos/blob/master/python/80_integrations_openai.ipynb">python/80_integrations_openai.ipynb</a></td>
            <td>Ready</td>
        </tr>   
        <tr>
            <td rowspan="2"><a href="https://docs.zilliz.com/docs/quick-start-1">Integartions with HuggingFace</a></td>
            <td><a href="https://github.com/zilliztech/zdoc-demos/blob/master/python/81_integrations_huggingface.py">python/81_integrations_huggingface.py</a></td>
            <td>Ready</td>
        </tr>
        <tr>
            <td><a href="https://github.com/zilliztech/zdoc-demos/blob/master/python/81_integrations_huggingface.ipynb">python/81_integrations_huggingface.ipynb</a></td>
            <td>Ready</td>
        </tr>
        <tr>
            <td rowspan="2"><a href="https://docs.zilliz.com/docs/quick-start-1">Integartions with Cohere</a></td>
            <td><a href="https://github.com/zilliztech/zdoc-demos/blob/master/python/82_integrations_cohere.py">python/82_integrations_cohere.py</a></td>
            <td>Ready</td>
        </tr>
        <tr>
            <td><a href="https://github.com/zilliztech/zdoc-demos/blob/master/python/82_integrations_cohere.ipynb">python/82_integrations_cohere.ipynb</a></td>
            <td>Ready</td>
        </tr>    
        <tr>
            <td rowspan="2"><a href="https://docs.zilliz.com/docs/quick-start-1">Integartions with Langchain</a></td>
            <td><a href="https://github.com/zilliztech/zdoc-demos/blob/master/python/83_integrations_langchain.py">python/83_integrations_langchain.py</a></td>
            <td>Ready</td>
        </tr>
        <tr>
            <td><a href="https://github.com/zilliztech/zdoc-demos/blob/master/python/83_integrations_langchain.ipynb">python/83_integrations_langchain.ipynb</a></td>
            <td>Ready</td>
        </tr>  
        <tr>
            <td rowspan="2"><a href="https://docs.zilliz.com/docs/quick-start-1">Integartions with PyTorch</a></td>
            <td><a href="https://github.com/zilliztech/zdoc-demos/blob/master/python/84_integrations_pytorch.py">python/84_integrations_pytorch.py</a></td>
            <td>Ready</td>
        </tr>
        <tr>
            <td><a href="https://github.com/zilliztech/zdoc-demos/blob/master/python/84_integrations_pytorch.ipynb">python/84_integrations_pytorch.ipynb</a></td>
            <td>Ready</td>
        </tr>  
        <tr>
            <td rowspan="2"><a href="https://docs.zilliz.com/docs/quick-start-1">Integartions with LLamaIndex</a></td>
            <td><a href="https://github.com/zilliztech/zdoc-demos/blob/master/python/85_integrations_llamaindex.py">python/85_integrations_llamaindex.py</a></td>
            <td>Ready</td>
        </tr>
        <tr>
            <td><a href="https://github.com/zilliztech/zdoc-demos/blob/master/python/85_integrations_llamaindex.ipynb">python/85_integrations_llamaindex.ipynb</a></td>
            <td>Ready</td>
        </tr>   
        <tr>
            <td rowspan="2"><a href="https://docs.zilliz.com/docs/quick-start-1">Integartions with SentenceTransformers</a></td>
            <td><a href="https://github.com/zilliztech/zdoc-demos/blob/master/python/86_integrations_sentencetransformers.py">python/86_integrations_sentencetransformers.py</a></td>
            <td>Ready</td>
        </tr>
        <tr>
            <td><a href="https://github.com/zilliztech/zdoc-demos/blob/master/python/86_integrations_sentencetransformers.ipynb">python/86_integrations_sentencetransformers.ipynb</a></td>
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
    > mvn clean compile exec:java
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
            <td><a href="https://github.com/zilliztech/zdoc-demos/blob/master/java/QuickStartDemo/src/main/java/com/zilliz/docs/QuickStartDemo.java">java/QuickStartDemo</a></td>
            <td>Ready</td>
        </tr>
        <tr>
            <td><a href="https://docs.zilliz.com/docs/use-customized-schema">Use Customized Schema</a></td>
            <td><a href="https://github.com/zilliztech/zdoc-demos/blob/master/java/UseCustomizedSchemaDemo/src/main/java/com/zilliz/docs/UseCustomizedSchemaDemo.java">java/use_customized_schema</a></td>
            <td>Ready</td>
        </tr>
        <tr>
            <td><a href="https://docs.zilliz.com/docs/enable_dynamic_schema">Enable Dynamic Schema</a></td>
            <td><a href="https://github.com/zilliztech/zdoc-demos/blob/master/java/EnableDynamicSchemaDemo/src/main/java/com/zilliz/docs/EnableDynamicSchemaDemo.java">java/EnableDynamicSchemaDemo</td>
            <td>Ready</td>
        </tr>
        <tr>
            <td><a href="https://docs.zilliz.com/docs/use_partition_key">Use Partition Key</a></td>
            <td><a href="https://github.com/zilliztech/zdoc-demos/blob/master/java/UsePartitionKeyDemo/src/main/java/com/zilliz/docs/UsePartitionKeyDemo.java">java/UsePartitionKeyDemo</td>
            <td>Ready</td>
        </tr>
        <tr>
            <td><a href="https://docs.zilliz.com/docs/javascript-object-notation-json-1">Use JSON Fields</a></td>
            <td><a href="https://github.com/zilliztech/zdoc-demos/blob/master/java/UseJsonFieldDemo/src/main/java/com/zilliz/docs/UseJsonFieldDemo.java">java/UseJsonFieldDemo</td>
            <td>Ready</td>
        </tr>
        <tr>
            <td><a href="https://docs.zilliz.com/docs/upsert_entities">Upsert Entities</a></td>
            <td><a href="https://github.com/zilliztech/zdoc-demos/blob/master/java/UpsertEntitiesDemo/src/main/java/com/zilliz/docs/UpsertEntitiesDemo.java">java/UpsertEntities</a></td>
            <td>Ready</td>
        </tr>
        <tr>
            <td><a href="https://docs.zilliz.com/docs/search-query-advanced-ops">Search & Query with Advanced Ops</a></td>
            <td><a href="https://github.com/zilliztech/zdoc-demos/blob/master/java/SearchQueryWithAdvancedOpsDemo/src/main/java/com/zilliz/docs/SearchQueryWithAdvancedOpsDemo.java">java/SearchQueryWithAdvancedOpsDemo</a></td>
            <td>Ready</td>
        </tr>
        <tr>
            <td><a href="https://docs.zilliz.com/docs/conduct-range-search">Conduct a Range Search</a></td>
            <td><a href="https://github.com/zilliztech/zdoc-demos/blob/master/java/ConductaRangeSearchDemo/src/main/java/com/zilliz/docs/ConductaRangeSearchDemo.java">java/ConductaRangeSearchDemo</a></td>
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
            <td><a href="https://github.com/zilliztech/zdoc-demos/blob/master/node/01_use_customized_schema.js">node/01_use_customized_schema.js</a></td>
            <td>Ready</td>
        </tr>
        <tr>
            <td><a href="https://docs.zilliz.com/docs/enable_dynamic_schema">Enable Dynamic Schema</a></td>
            <td><a href="https://github.com/zilliztech/zdoc-demos/blob/master/node/02_enable_dynamic_schema.js">node/02_enable_dynamic_schema.js</a></td>
            <td>Ready</td>
        </tr>
        <tr>
            <td><a href="https://docs.zilliz.com/docs/use_partition_key">Use Partition Key</a></td>
            <td><a href="https://github.com/zilliztech/zdoc-demos/blob/master/node/03_use_partition_key.js">node/03_use_partition_key.js</a></td>
            <td>Ready</td>
        </tr>
        <tr>
            <td><a href="https://docs.zilliz.com/docs/javascript-object-notation-json-1">Use JSON Fields</a></td>
            <td><a href="https://github.com/zilliztech/zdoc-demos/blob/master/node/04_use_json_field.js">node/04_use_json_field.js</a></td>
            <td>Ready</td>
        </tr>
        <tr>
            <td><a href="https://docs.zilliz.com/docs/search-query-advanced-ops">Upsert Entities</a></td>
            <td><a href="https://github.com/zilliztech/zdoc-demos/blob/master/node/08_upsert_entities.js">node/08_upsert_entities.js</a></td>
            <td>Ready</td>
        </tr>
        <tr>
            <td><a href="https://docs.zilliz.com/docs/search-query-advanced-ops">Search & Query with Advanced Ops</a></td>
            <td><a href="https://github.com/zilliztech/zdoc-demos/blob/master/node/10_search_query_advanced_ops.js">node/10_search_query_advanced_ops.js</a></td>
            <td>Ready</td>
        </tr>
        <tr>
            <td><a href="https://docs.zilliz.com/docs/conduct-range-search">Conduct a Range Search</a></td>
            <td><a href="https://github.com/zilliztech/zdoc-demos/blob/master/node/11_conduct_a_range_search.js">node/11_conduct_a_range_search.js</a></td>
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
            <td><a href="https://github.com/zilliztech/zdoc-demos/blob/master/go/enable_dynamic_schema/main.go">go/enable_dynamic_schema/main.go</a></td>
            <td>Ready</td>
        </tr>
        <tr>
            <td><a href="https://docs.zilliz.com/docs/use_partition_key">Use Partition Key</a></td>
            <td><a href="https://github.com/zilliztech/zdoc-demos/blob/master/go/use_partition_key/main.go">go/use_partition_key/main.go</a></td>
            <td>Ready</td>
        </tr>
        <tr>
            <td><a href="https://docs.zilliz.com/docs/javascript-object-notation-json-1">Use JSON Fields</a></td>
            <td><a href="https://github.com/zilliztech/zdoc-demos/blob/master/go/use_json_field/main.go">go/use_json_field/main.go</a></td>
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
            <td rowspan=2><a href="https://docs.zilliz.com/docs/quick-start-1">Quick start</a></td>
            <td><a href="https://github.com/zilliztech/zdoc-demos/blob/master/curl/00_quick_start.sh">curl/00_quick_start.sh</a></td>
            <td>Ready</td>
        </tr>
        <tr>
            <td><a href="https://github.com/zilliztech/zdoc-demos/blob/master/curl/00_quick_start.ipynb">curl/00_quick_start.ipynb</a></td>
            <td>Ready</td>
        </tr>
    </tbody>
</table>
