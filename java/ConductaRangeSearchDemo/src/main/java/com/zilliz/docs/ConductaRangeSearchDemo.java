package com.zilliz.docs;

import io.milvus.client.*;
import io.milvus.param.*;
import io.milvus.param.collection.FieldType;
import io.milvus.param.index.CreateIndexParam;
import io.milvus.param.collection.CreateCollectionParam;
import io.milvus.param.collection.DropCollectionParam;
import io.milvus.grpc.DataType;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.dml.InsertParam.Field;
import io.milvus.grpc.MutationResult;
import io.milvus.response.MutationResultWrapper;
import io.milvus.param.dml.SearchParam;
import io.milvus.grpc.SearchResults;
import io.milvus.response.SearchResultsWrapper;
import io.milvus.common.clientenum.ConsistencyLevelEnum;
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
public final class ConductaRangeSearchDemo {
    private ConductaRangeSearchDemo() {
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

        FieldType link = FieldType.newBuilder()
            .withName("link")
            .withDataType(DataType.VarChar)
            .withMaxLength(512)
            .build();

        FieldType reading_time = FieldType.newBuilder()
            .withName("reading_time")
            .withDataType(DataType.Int64)
            .build();

        FieldType publication = FieldType.newBuilder()
            .withName("publication")
            .withDataType(DataType.VarChar)
            .withMaxLength(512)
            // This field is set as the partition key.
            .withPartitionKey(true)
            .build();

        FieldType claps = FieldType.newBuilder()
            .withName("claps")
            .withDataType(DataType.Int64)
            .build();

        FieldType responses = FieldType.newBuilder()
            .withName("responses")
            .withDataType(DataType.Int64)
            .build();

        // 3. Create collection

        CreateCollectionParam createCollectionParam = CreateCollectionParam.newBuilder()
            .withCollectionName(collectionName)
            .addFieldType(id)
            .addFieldType(title)
            .addFieldType(title_vector)
            .addFieldType(link)
            .addFieldType(reading_time)
            .addFieldType(publication)
            .addFieldType(claps)
            .addFieldType(responses)
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
        List<JSONObject> rows = getRows(dataset.getJSONArray("rows"), 5979);
        List<Field> fields = getFields(dataset.getJSONArray("rows"), 5979);

        InsertParam insertParam = InsertParam.newBuilder()
            .withCollectionName(collectionName)
            .withFields(fields)
            .build();

        R<MutationResult> insertResponse = client.insert(insertParam);

        if (insertResponse.getStatus() != R.Status.Success.getCode()) {
            System.err.println(insertResponse.getMessage());
        }

        MutationResultWrapper mutationResultWrapper = new MutationResultWrapper(insertResponse.getData());

        System.out.println("Successfully insert entities: " + mutationResultWrapper.getInsertCount());   

        // Output:
        // Successfully insert entities: 5979





        
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
        outputFields.add("link");

        // Search vectors in a collection

        SearchParam searchParam = SearchParam.newBuilder()
            .withCollectionName(collectionName)
            .withVectorFieldName("title_vector")
            .withVectors(queryVectors)
            .withTopK(5)   
            .withMetricType(MetricType.L2) 
            // Set the 'radius' and 'range_filter' here! 
            .withParams("{\"nprobe\":10,\"radius\":1.0, \"range_filter\":0.8}")
            .withConsistencyLevel(ConsistencyLevelEnum.BOUNDED)
            .withOutFields(outputFields)
            .withExpr("(publication == \"Towards Data Science\") and ((claps > 1500 and responses > 15) or (10 < reading_time < 15))")
            .build();

        R<SearchResults> response = client.search(searchParam);

        SearchResultsWrapper wrapper = new SearchResultsWrapper(response.getData().getResults());
        
        List<List<JSONObject>> results = new ArrayList<>();

        for (int i = 0; i < queryVectors.size(); ++i) {
            List<SearchResultsWrapper.IDScore> scores = wrapper.getIDScore(i);
            List<JSONObject> entities = new ArrayList<>();
            for (int j = 0; j < scores.size(); ++j) {
                SearchResultsWrapper.IDScore score = scores.get(j);
                JSONObject entity = new JSONObject(1, true);
                entity.put("id", score.getLongID());
                entity.put("distance", score.getScore());
                entity.put("title", scores.get(j).get("title"));
                entity.put("link", scores.get(j).get("link"));
                entities.add(entity);
            }
            
            results.add(entities);
        } 

        System.out.println(results);

        // Output:
        // [[
        //     {
        //         "distance": 0.800161,
        //         "link": "https://towardsdatascience.com/binary-cross-entropy-and-logistic-regression-bf7098e75559",
        //         "id": 445494450042705136,
        //         "title": "Binary cross-entropy and logistic regression"
        //     },
        //     {
        //         "distance": 0.80130583,
        //         "link": "https://towardsdatascience.com/what-i-learnt-from-taking-a-masters-in-computer-vision-and-machine-learning-69f0c6dfe9df",
        //         "id": 445494450042703937,
        //         "title": "What I Learnt From Taking A Masters In Computer Vision And Machine Learning"
        //     },
        //     {
        //         "distance": 0.8042611,
        //         "link": "https://towardsdatascience.com/do-not-use-to-join-strings-in-python-f89908307273",
        //         "id": 445494450042703425,
        //         "title": "Do Not Use \u201c+\u201d to Join Strings in Python"
        //     },
        //     {
        //         "distance": 0.8053469,
        //         "link": "https://towardsdatascience.com/using-data-science-to-study-economic-inequality-in-the-united-states-1101e9350c3d",
        //         "id": 445494450042703983,
        //         "title": "Using Data Science to Study Economic Inequality in the United States"
        //     },
        //     {
        //         "distance": 0.80535966,
        //         "link": "https://towardsdatascience.com/learn-ai-today-01-getting-started-with-pytorch-2e3ba25a518",
        //         "id": 445494450042703567,
        //         "title": "Learn AI Today: 01 \u2014 Getting started with Pytorch"
        //     }
        // ]]





        // Drop collection

        DropCollectionParam dropCollectionParam = DropCollectionParam.newBuilder()
            .withCollectionName("medium_articles")
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

    public static List<Field> getFields(JSONArray dataset, int counts) {
        List<Field> fields = new ArrayList<Field>();
        List<String> titles = new ArrayList<String>();
        List<List<Float>> title_vectors = new ArrayList<List<Float>>();
        List<String> links = new ArrayList<String>();
        List<Long> reading_times = new ArrayList<Long>();
        List<String> publications = new ArrayList<String>();
        List<Long> claps_list = new ArrayList<Long>();
        List<Long> responses_list = new ArrayList<Long>();

        for (int i = 0; i < counts; i++) {
            JSONObject row = dataset.getJSONObject(i);
            titles.add(row.getString("title"));
            title_vectors.add(row.getJSONArray("title_vector").toJavaList(Float.class));
            links.add(row.getString("link"));
            reading_times.add(row.getLong("reading_time"));
            publications.add(row.getString("publication"));
            claps_list.add(row.getLong("claps"));
            responses_list.add(row.getLong("responses"));
        }

        fields.add(new Field("title", titles));
        fields.add(new Field("title_vector", title_vectors));
        fields.add(new Field("link", links));
        fields.add(new Field("reading_time", reading_times));
        fields.add(new Field("publication", publications));
        fields.add(new Field("claps", claps_list));
        fields.add(new Field("responses", responses_list));

        return fields;        
    }
}