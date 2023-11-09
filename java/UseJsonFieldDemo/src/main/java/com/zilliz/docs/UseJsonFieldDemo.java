package com.zilliz.docs;

import io.milvus.param.*;
import io.milvus.client.*;
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
import io.milvus.common.clientenum.ConsistencyLevelEnum;
import io.milvus.param.collection.LoadCollectionParam;
import io.milvus.grpc.GetLoadingProgressResponse;
import io.milvus.param.collection.GetLoadingProgressParam;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Hello world!
 */
public final class UseJsonFieldDemo {
    private UseJsonFieldDemo() {
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

        ConnectParam connectParam = ConnectParam.newBuilder()
            .withUri(clusterEndpoint)
            .withToken(token)
            .build();

        MilvusServiceClient client = new MilvusServiceClient(connectParam);

        System.out.println("Connected to Zilliz Cloud!");

        // Output:
        // Connected to Zilliz Cloud!





        
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

        // The following field is a JSON field.
        FieldType article_meta = FieldType.newBuilder()
            .withName("article_meta")
            .withDataType(DataType.JSON)
            .build();
            
        CreateCollectionParam createCollectionParam = CreateCollectionParam.newBuilder()
            .withCollectionName(collectionName)
            .withDescription("Schema of Medium articles")
            .addFieldType(id)
            .addFieldType(title)
            .addFieldType(title_vector)
            .addFieldType(article_meta)
            .build();

        R<RpcStatus> collection = client.createCollection(createCollectionParam);

        if (collection.getException() != null) {
            System.err.println("Failed to create collection: " + collection.getException().getMessage());
            return;
        }

        String content;

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

        LoadCollectionParam loadCollectionParam = LoadCollectionParam.newBuilder()
            .withCollectionName(collectionName)
            .build();

        R<RpcStatus> loadCollectionRes = client.loadCollection(loadCollectionParam);

        if (loadCollectionRes.getException() != null) {
            System.err.println("Failed to load collection: " + loadCollectionRes.getException().getMessage());
            return;
        }

        GetLoadingProgressParam getLoadingProgressParam = GetLoadingProgressParam.newBuilder()
            .withCollectionName(collectionName)
            .build();

        R<GetLoadingProgressResponse> getLoadingProgressRes = client.getLoadingProgress(getLoadingProgressParam);

        if (getLoadingProgressRes.getException() != null) {
            System.err.println("Failed to get loading progress: " + getLoadingProgressRes.getException().getMessage());
            return;
        }

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
        // Successfully insert entities: 5979





        // wait for a while
        try {
            // pause execution for 5 seconds
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            // handle the exception
            Thread.currentThread().interrupt();
        }

        // prepare query vector
        List<List<Float>> queryVectors = new ArrayList<>();
        List<Float> queryVector1 = rows.get(0).getJSONArray("title_vector").toJavaList(Float.class);
        queryVectors.add(queryVector1);

        // prepare output field
        List<String> outputFields = new ArrayList<>();
        outputFields.add("title");
        outputFields.add("article_meta");   

        // Search vectors in a collection

        SearchParam searchParam = SearchParam.newBuilder()
            .withCollectionName(collectionName)
            .withVectorFieldName("title_vector")
            .withVectors(queryVectors)
            .withTopK(5)   
            .withMetricType(MetricType.L2)  
            .withParams("{\"nprobe\":10,\"offset\":2, \"limit\":3}")
            .withConsistencyLevel(ConsistencyLevelEnum.BOUNDED)
            .withOutFields(outputFields)
            .withExpr("article_meta[\"claps\"] > 30 and article_meta['reading_time'] < 10")
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
                entity.put("reading_time", ((JSONObject) scores.get(j).get("article_meta")).getLong("reading_time"));
                entity.put("claps", ((JSONObject) scores.get(j).get("article_meta")).getLong("claps"));
                entities.add(entity);
            }
            
            results.add(entities);
        }

        System.out.println(results);

        // Output:
        // [[
        //     {
        //         "reading_time": 4,
        //         "distance": 0.41629803,
        //         "id": 445297206350527638,
        //         "title": "Coronavirus shows what ethical Amazon could look like",
        //         "claps": 51
        //     },
        //     {
        //         "reading_time": 6,
        //         "distance": 0.4360938,
        //         "id": 445297206350525135,
        //         "title": "Mortality Rate As an Indicator of an Epidemic Outbreak",
        //         "claps": 65
        //     },
        //     {
        //         "reading_time": 9,
        //         "distance": 0.48886314,
        //         "id": 445297206350528472,
        //         "title": "How Can AI Help Fight Coronavirus?",
        //         "claps": 255
        //     },
        //     {
        //         "reading_time": 5,
        //         "distance": 0.49283177,
        //         "id": 445297206350528342,
        //         "title": "Will Coronavirus Impact Freelancers\u2019 Ability to Rent?",
        //         "claps": 63
        //     },
        //     {
        //         "reading_time": 9,
        //         "distance": 0.4944387,
        //         "id": 445297206350525039,
        //         "title": "Choosing the right performance metrics can save lives against Coronavirus",
        //         "claps": 202
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
            JSONObject json_row = new JSONObject(1, true);
            JSONObject article_meta = new JSONObject(1, true);
            JSONObject original_row = dataset.getJSONObject(i);

            String title = original_row.getString("title");
            String link = original_row.getString("link");
            String publication = original_row.getString("publication");
            Long reading_time = original_row.getLong("reading_time");
            Long claps = original_row.getLong("claps");
            Long responses = original_row.getLong("responses");
            List<Float> vectors = original_row.getJSONArray("title_vector").toJavaList(Float.class);

            article_meta.put("link", link);
            article_meta.put("publication", publication);
            article_meta.put("reading_time", reading_time);
            article_meta.put("claps", claps);
            article_meta.put("responses", responses);
            json_row.put("title", title);
            json_row.put("title_vector", vectors);
            json_row.put("article_meta", article_meta);
            rows.add(json_row);
        }
        return rows;
    }
}