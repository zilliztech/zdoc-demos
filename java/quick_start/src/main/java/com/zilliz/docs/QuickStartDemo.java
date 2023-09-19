package com.zilliz.docs;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.DescribeCollectionResponse;
import io.milvus.param.ConnectParam;
import io.milvus.param.R;
import io.milvus.param.RpcStatus;
import io.milvus.param.collection.DescribeCollectionParam;
import io.milvus.param.collection.DropCollectionParam;
import io.milvus.param.highlevel.collection.CreateSimpleCollectionParam;
import io.milvus.param.highlevel.dml.DeleteIdsParam;
import io.milvus.param.highlevel.dml.GetIdsParam;
import io.milvus.param.highlevel.dml.InsertRowsParam;
import io.milvus.param.highlevel.dml.QuerySimpleParam;
import io.milvus.param.highlevel.dml.SearchSimpleParam;
import io.milvus.param.highlevel.dml.response.DeleteResponse;
import io.milvus.param.highlevel.dml.response.GetResponse;
import io.milvus.param.highlevel.dml.response.InsertResponse;
import io.milvus.param.highlevel.dml.response.QueryResponse;
import io.milvus.param.highlevel.dml.response.SearchResponse;
import io.milvus.response.QueryResultsWrapper;

/**
 * Hello world!
 */
public final class QuickStartDemo {
    private QuickStartDemo() {
    }

    /**
     * Says hello to the world.
     * @param args The arguments of the program.
     */
    public static void main(String[] args) {
        String clusterEndpoint = "YOUR_CLUSTER_ENDPOINT";
        String token = "YOUR_CLUSTER_TOKEN";

        // 1. Connect to Zilliz Cloud
        // - For a serverless cluster, use an API key as the token.                                 
        // - For a dedicated cluster, use the cluster credentials as the token in the format of 'user:password'.
        ConnectParam connectParam = ConnectParam.newBuilder()
            .withUri(clusterEndpoint)
            .withToken(token)
            .build();   
            
        MilvusServiceClient client = new MilvusServiceClient(connectParam);

        System.out.println("Connected to Zilliz Cloud!");

        // 2. Create collection

        CreateSimpleCollectionParam createCollectionParam = CreateSimpleCollectionParam.newBuilder()
            .withCollectionName("medium_articles_2020")
            .withDimension(768)
            .build();

        R<RpcStatus> createCollection = client.createCollection(createCollectionParam);

        if (createCollection.getException() != null) {
            System.out.println("Failed to create collection: " + createCollection.getException().getMessage());
            return;            
        }

        // 3. Describe collection
        String collectionName = "medium_articles_2020";

        DescribeCollectionParam describeCollectionParam = DescribeCollectionParam.newBuilder()
            .withCollectionName(collectionName)
            .build();

        R<DescribeCollectionResponse> collectionInfo = client.describeCollection(describeCollectionParam);

        System.out.println("Collection info: " + collectionInfo.toString());

        // 4. Insert a single entity
        String content;

        Path file = Path.of("../../medium_articles_2020_dpr.json");
        try {
            content = Files.readString(file);
        } catch (Exception e) {
            System.out.println("Failed to read file: " + e.getMessage());
            return;
        }

        System.out.println("Successfully read file");

        // Load dataset
        JSONObject dataset = JSON.parseObject(content);

        // Change the second argument of the `getRows` function to limit the number of rows obtained from the dataset.
        List<JSONObject> rows = getRows(dataset.getJSONArray("rows"), 1);

        System.out.println(rows.get(0).toString());

        InsertRowsParam insertRowsParam = InsertRowsParam.newBuilder()
            .withCollectionName(collectionName)
            .withRows(rows)
            .build();

        R<InsertResponse> res = client.insert(insertRowsParam);

        if (res.getException() != null) {
            System.out.println("Failed to insert: " + res.getException().getMessage());
            return;
        }

        System.out.println("Successfully inserted " + res.getData().getInsertCount() + " records");

        // 5. search
        List<List<Float>> queryVectors = new ArrayList<>();
        List<Float> queryVector1 = rows.get(0).getJSONArray("vector").toJavaList(Float.class);
        queryVectors.add(queryVector1);

        List<String> outputFields = new ArrayList<>();
        outputFields.add("title");

        SearchSimpleParam searchSimpleParam = SearchSimpleParam.newBuilder()
            .withCollectionName(collectionName)
            .withVectors(queryVectors)
            .withOutputFields(outputFields)
            .withOffset(0L)
            .withLimit(3L)
            .build();

        R<SearchResponse> searchRes = client.search(searchSimpleParam);

        if (searchRes.getException() != null) {
            System.out.println("Failed to search: " + searchRes.getException().getMessage());
            return;
        }

        for (QueryResultsWrapper.RowRecord rowRecord: searchRes.getData().getRowRecords()) {
            System.out.println(rowRecord);
        }

        // 6. search with filters

        outputFields.add("claps");
        outputFields.add("publication");

        SearchSimpleParam searchSimpleParamWithFilter = SearchSimpleParam.newBuilder()
            .withCollectionName(collectionName)
            .withVectors(queryVectors)
            .withOutputFields(outputFields)
            .withFilter("claps > 100 and publication in ['The Startup', 'Towards Data Science']")
            .withOffset(0L)
            .withLimit(3L)
            .build();

        R<SearchResponse> searchResWithFilter = client.search(searchSimpleParamWithFilter);

        if (searchResWithFilter.getException() != null) {
            System.out.println("Failed to search: " + searchResWithFilter.getException().getMessage());
            return;
        }

        for (QueryResultsWrapper.RowRecord rowRecord: searchResWithFilter.getData().getRowRecords()) {
            System.out.println(rowRecord);
        }

        QuerySimpleParam querySimpleParam = QuerySimpleParam.newBuilder()
            .withCollectionName(collectionName)
            .withFilter("claps > 100 and publication in ['The Startup', 'Towards Data Science']")
            .withOutputFields(outputFields)
            .withOffset(0L)
            .withLimit(3L)
            .build();

        R<QueryResponse> queryRes = client.query(querySimpleParam);

        if (queryRes.getException() != null) {
            System.out.println("Failed to query: " + queryRes.getException().getMessage());
            return;
        }

        for (QueryResultsWrapper.RowRecord rowRecord: queryRes.getData().getRowRecords()) {
            System.out.println(rowRecord);
        }

        List<String> ids = Lists.newArrayList("1");
        // List<String> ids = Lists.newArrayList("1", "2", "3");

        GetIdsParam getParam = GetIdsParam.newBuilder()
                .withCollectionName(collectionName)
                .withPrimaryIds(ids)
                .withOutputFields(Lists.newArrayList("*"))
                .build();

        R<GetResponse> getRes = client.get(getParam);

        if (getRes.getException() != null) {
            System.out.println("Failed to get: " + getRes.getException().getMessage());
            return;
        }

        for (QueryResultsWrapper.RowRecord rowRecord: getRes.getData().getRowRecords()) {
            System.out.println(rowRecord);
        }

        DeleteIdsParam deleteParam = DeleteIdsParam.newBuilder()
                .withCollectionName(collectionName)
                .withPrimaryIds(ids)
                .build();

        R<DeleteResponse> deleteRes = client.delete(deleteParam);

        if (deleteRes.getException() != null) {
            System.out.println("Failed to delete: " + deleteRes.getException().getMessage());
            return;
        }

        System.out.println("Successfully deleted " + deleteRes.getData().getDeleteIds() + " records");

        DropCollectionParam dropCollectionParam = DropCollectionParam.newBuilder()
            .withCollectionName(collectionName)
            .build();

        R<RpcStatus> dropCollection = client.dropCollection(dropCollectionParam);

        if (dropCollection.getException() != null) {
            System.out.println("Failed to drop collection: " + dropCollection.getException().getMessage());
            return;            
        }
    }

    public static List<JSONObject> getRows(JSONArray dataset, int counts) {
        List<JSONObject> rows = new ArrayList<JSONObject>();
        for (int i = 0; i < counts; i++) {
            JSONObject json_row = new JSONObject(1, true);
            JSONObject original_row = dataset.getJSONObject(i);
            
            Long id = original_row.getLong("id");
            String title = original_row.getString("title");
            String link = original_row.getString("link");
            String publication = original_row.getString("publication");
            Long reading_time = original_row.getLong("reading_time");
            Long claps = original_row.getLong("claps");
            Long responses = original_row.getLong("responses");
            List<Float> vectors = original_row.getJSONArray("title_vector").toJavaList(Float.class);
    
            json_row.put("id", id);
            json_row.put("link", link);
            json_row.put("publication", publication);
            json_row.put("reading_time", reading_time);
            json_row.put("claps", claps);
            json_row.put("responses", responses);
            json_row.put("title", title);
            json_row.put("vector", vectors);
            rows.add(json_row);
        }
        return rows;
    }
}
