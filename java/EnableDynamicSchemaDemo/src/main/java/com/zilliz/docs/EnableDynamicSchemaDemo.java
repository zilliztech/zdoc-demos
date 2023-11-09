package com.zilliz.docs;

import io.milvus.client.*;
import io.milvus.param.*;
import io.milvus.param.collection.FieldType;
import io.milvus.param.index.CreateIndexParam;
import io.milvus.param.collection.CreateCollectionParam;
import io.milvus.param.collection.DropCollectionParam;
import io.milvus.grpc.DataType;
import io.milvus.param.dml.InsertParam;
import io.milvus.grpc.MutationResult;
import io.milvus.response.MutationResultWrapper;
import io.milvus.param.dml.SearchParam;
import io.milvus.grpc.SearchResults;
import io.milvus.response.SearchResultsWrapper;
import io.milvus.param.collection.LoadCollectionParam;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.List;
import java.util.ArrayList;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Hello world!
 */
public final class EnableDynamicSchemaDemo {
    private EnableDynamicSchemaDemo() {
    }

    /**
     * Says hello to the world.
     * @param args The arguments of the program.
     */
    public static void main(String[] args) {
        String clusterEndpoint = "YOUR_CLUSTER_ENDPOINT";
        String token = "YOUR_CLUSTER_TOKEN";
        String collectionName = "medium_articles";
        String data_file = System.getProperty("user.dir") + "/medium_articles_2020_dpr.json";

        // 1. Connect to Zilliz Cloud cluster
        ConnectParam connectParam = ConnectParam.newBuilder()
            .withUri(clusterEndpoint)
            .withToken(token)
            .build();

        MilvusServiceClient client = new MilvusServiceClient(connectParam);

        System.out.println("Connected to Zilliz Cloud!");

        // Output:
        // Connected to Zilliz Cloud!





        // 2. Define fields

        FieldType id = FieldType.newBuilder()
            .withName("id")
            .withDataType(DataType.Int64)
            .withPrimaryKey(true)
            .withAutoID(true)
            .build();

        FieldType title = FieldType.newBuilder()
            .withName("title")
            .withDataType(DataType.VarChar)
            .withMaxLength(512)
            .build();

        FieldType title_vector = FieldType.newBuilder()
            .withName("title_vector")
            .withDataType(DataType.FloatVector)
            .withDimension(768)
            .build();

        // 3. Create collection

        CreateCollectionParam createCollectionParam = CreateCollectionParam.newBuilder()
            .withCollectionName(collectionName)
            .withDescription("Schema of Medium articles")
            .addFieldType(id)
            .addFieldType(title)
            .addFieldType(title_vector)
            // Enable dynamic schema
            .withEnableDynamicField(true)
            .build();

        R<RpcStatus> collection = client.createCollection(createCollectionParam);

        if (collection.getException() != null) {
            System.err.println("Failed to create collection: " + collection.getException().getMessage());
            return;
        }

        System.out.println("Collection created!");

        // Output:
        // Collection created!





        // 4. Create index

        CreateIndexParam createIndexParam = CreateIndexParam.newBuilder()
            .withCollectionName(collectionName)
            .withFieldName("title_vector")
            .withIndexName("title_vector_index")
            .withIndexType(IndexType.AUTOINDEX)
            .withMetricType(MetricType.L2)
            .build();

        R<RpcStatus> res = client.createIndex(createIndexParam);

        if (res.getException() != null) {
            System.err.println("Failed to create index: " + res.getException().getMessage());
            return;
        }

        System.out.println("Index created!");

        // Output:
        // Index created!





        // 5. Load collection

        LoadCollectionParam loadCollectionParam = LoadCollectionParam.newBuilder()
            .withCollectionName(collectionName)
            .build();

        R<RpcStatus> loadCollectionRes = client.loadCollection(loadCollectionParam);

        if (loadCollectionRes.getException() != null) {
            System.err.println("Failed to load collection: " + loadCollectionRes.getException().getMessage());
            return;
        }

        System.out.println("Collection loaded!");

        // Output:
        // Collection loaded!





        // 6. Insert vectors

        String content;

        // read a local file
        Path file = Path.of(data_file);
        try {
            content = Files.readString(file);
        } catch (Exception e) {
            System.err.println("Failed to read file: " + e.getMessage());
            return;
        }

        System.out.println("Successfully read file");

        // Output:
        // Successfully read file





        // Load dataset
        JSONObject dataset = JSON.parseObject(content);

        // Insert your data in rows, all the fields not pre-defined in the schema 
        // are recognized as pre-defined schema
        List<JSONObject> rows = getRows(dataset.getJSONArray("rows"), 1000);

        InsertParam insertParam = InsertParam.newBuilder()
            .withCollectionName(collectionName)
            .withRows(rows)
            .build();

        R<MutationResult> insertResponse = client.insert(insertParam);

        if (insertResponse.getStatus() != R.Status.Success.getCode()) {
            System.err.println(insertResponse.getMessage());
        }

        MutationResultWrapper mutationResultWrapper = new MutationResultWrapper(insertResponse.getData());

        System.out.println("Successfully insert entities: " + mutationResultWrapper.getInsertCount());  

        // Output:
        // Successfully insert entities: 1000





        
        // wait for a while
        try {
            // pause execution for 5 seconds
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            // handle the exception
            Thread.currentThread().interrupt();
        }          

        // 7. Search vectors

        List<List<Float>> queryVectors = new ArrayList<>();
        List<Float> queryVector1 = rows.get(0).getJSONArray("title_vector").toJavaList(Float.class);
        queryVectors.add(queryVector1);

        List<String> outputFields = new ArrayList<>();
        outputFields.add("title");
        // The following two fields are dynamic fields.
        outputFields.add("claps");
        outputFields.add("reading_time");

        // Search vectors in a collection

        SearchParam searchParam = SearchParam.newBuilder()
            .withCollectionName(collectionName)
            .withVectorFieldName("title_vector")
            .withVectors(queryVectors)
            .withTopK(5)   
            .withMetricType(MetricType.L2)  
            .withParams("{\"nprobe\":10,\"offset\":2, \"limit\":3}")
            .withOutFields(outputFields)
            .withExpr("$meta[\"claps\"] > 30 and reading_time < 10")
            .build();

        R<SearchResults> response = client.search(searchParam);

        SearchResultsWrapper wrapper = new SearchResultsWrapper(response.getData().getResults());
        
        List<List<JSONObject>> results = new ArrayList<>();

        for (int i = 0; i < queryVectors.size(); ++i) {
            List<SearchResultsWrapper.IDScore> scores = wrapper.getIDScore(i);
            List<JSONObject> entities = new ArrayList<>();
            for (int j = 0; j < scores.size(); ++j) {
                SearchResultsWrapper.IDScore score = scores.get(j);
                JSONObject entity = new JSONObject();
                entity.put("id", score.getLongID());
                entity.put("distance", score.getScore());
                entity.put("title", scores.get(j).get("title"));
                // The following are dynamic fields.
                entity.put("claps", scores.get(j).get("claps"));
                entity.put("reading_time", scores.get(j).get("reading_time"));
                entities.add(entity);
            }
            
            results.add(entities);
        }

        System.out.println(results);

        // Output:
        // [[
        //     {
        //         "reading_time": 6,
        //         "distance": 0.494843,
        //         "id": 445297206350523266,
        //         "title": "How bad will the Coronavirus Outbreak get? \u2014 Predicting the outbreak figures",
        //         "claps": 1100
        //     },
        //     {
        //         "reading_time": 7,
        //         "distance": 0.50522643,
        //         "id": 445297206350523757,
        //         "title": "What Does Coronavirus Mean For Your Startup?",
        //         "claps": 111
        //     },
        //     {
        //         "reading_time": 4,
        //         "distance": 0.50953895,
        //         "id": 445297206350524005,
        //         "title": "The Definitive Guide to Leading Remote Work Teams During Coronavirus",
        //         "claps": 753
        //     },
        //     {
        //         "reading_time": 6,
        //         "distance": 0.53836524,
        //         "id": 445297206350523435,
        //         "title": "The Relocation Problem of Field-Calibrated Low-Cost Air Quality Monitoring Systems",
        //         "claps": 51
        //     },
        //     {
        //         "reading_time": 7,
        //         "distance": 0.57358503,
        //         "id": 445297206350523356,
        //         "title": "The Funeral Industry is a Killer",
        //         "claps": 407
        //     }
        // ]]





        // Drop collection

        DropCollectionParam dropCollectionParam = DropCollectionParam.newBuilder()
            .withCollectionName(collectionName)
            .build();

        R<RpcStatus> dropCollectionRes = client.dropCollection(dropCollectionParam);

        if (dropCollectionRes.getException() != null) {
            System.err.println("Failed to drop collection: " + dropCollectionRes.getException().getMessage());
            return;
        }

        System.out.println("Successfully drop collection");

        // Output:
        // Successfully drop collection





    }

    public static List<JSONObject> getRows(JSONArray dataset, int counts) {
        List<JSONObject> rows = new ArrayList<JSONObject>();
        for (int i = 0; i < counts; i++) {
            JSONObject row = dataset.getJSONObject(i);
            List<Float> vectors = row.getJSONArray("title_vector").toJavaList(Float.class);
            Long reading_time = row.getLong("reading_time");
            Long claps = row.getLong("claps");
            Long responses = row.getLong("responses");
            row.put("title_vector", vectors);
            row.put("reading_time", reading_time);
            row.put("claps", claps);
            row.put("responses", responses);
            row.remove("id");
            rows.add(row);
        }
        return rows;
    }
}