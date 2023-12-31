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
import io.milvus.param.dml.QueryParam;
import io.milvus.grpc.SearchResults;
import io.milvus.grpc.QueryResults;
import io.milvus.response.SearchResultsWrapper;
import io.milvus.response.QueryResultsWrapper;
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
import java.util.Random;

/**
 * Hello world!
 */
public final class SearchQueryWithAdvancedOpsDemo {
    private SearchQueryWithAdvancedOpsDemo() {
    }

    /**
     * Says hello to the world.
     * @param args The arguments of the program.
     */
    public static void main(String[] args) {
        String clusterEndpoint = "http://localhost:19530";
        String token = "root:Milvus";
        String collectionName = "medium_articles";
        String data_file = System.getProperty("user.dir") + "/medium_articles_2020_dpr.json";

        // 1. Connect to cluster

        ConnectParam connectParam = ConnectParam.newBuilder()
            .withUri(clusterEndpoint)
            .withToken(token)
            .build();

        MilvusServiceClient client = new MilvusServiceClient(connectParam);

        System.out.println("Connected to Zilliz Cloud!");

        // Output:
        // Connected to Zilliz Cloud!





        // 2. Create cluster
        
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

        System.out.println("Successfully created a collection with name: " + collectionName);

        // Output:
        // Successfully created a collection with name: medium_articles





        // 3. Index and load the collection

        // Create index

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

        System.out.println("Successfully created index");

        // Output:
        // Successfully created index





        // Load collection

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

        System.out.println("Successfully loaded collection");

        // Output:
        // Successfully loaded collection





        // 4. Read a local file
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

        // In addition to the original data, we also need to add some tags fields.
        // The tags fields are used to demonstrate the use of the advanced expressions.
        // For details, examine the getRows function
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





        // Wait for a while
        try {
            // pause execution for 5 seconds
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            // handle the exception
            Thread.currentThread().interrupt();
        }

        // 5. Count the entities using the 'count(*)' field

        List<String> outputFields1 = new ArrayList<>();
        outputFields1.add("count(*)");

        QueryParam queryParam1 = QueryParam.newBuilder()
            .withCollectionName(collectionName)
            .withExpr("")
            .withOutFields(outputFields1)
            .build();

        R<QueryResults> queryResponse1 = client.query(queryParam1);

        if (queryResponse1.getException() != null) {
            System.err.println("Failed to query: " + queryResponse1.getException().getMessage());
            return;
        }

        QueryResultsWrapper queryResultsWrapper1 = new QueryResultsWrapper(queryResponse1.getData());

        int count1 = ((Long) queryResultsWrapper1.getFieldWrapper("count(*)").getFieldData().get(0)).intValue();

        System.out.println("The collection contains exactly " + count1 + " entities!");

        // Output:
        // The collection contains exactly 5979 entities!





        // 6. Count the entities with filters

        List<String> outputFields2 = new ArrayList<>();
        outputFields2.add("count(*)");

        QueryParam queryParam2 = QueryParam.newBuilder()
            .withCollectionName(collectionName)
            .withExpr("article_meta[\"claps\"] > 30 and article_meta[\"reading_time\"] < 10")
            .withOutFields(outputFields2)
            .build();

        R<QueryResults> queryResponse2 = client.query(queryParam2);

        if (queryResponse2.getException() != null) {
            System.err.println("Failed to query: " + queryResponse2.getException().getMessage());
            return;
        }

        QueryResultsWrapper queryResultsWrapper2 = new QueryResultsWrapper(queryResponse2.getData());

        int count2 = ((Long) queryResultsWrapper2.getFieldWrapper("count(*)").getFieldData().get(0)).intValue();

        System.out.println("The collection contains exactly " + count2 + " entities!");

        // Output:
        // The collection contains exactly 4304 entities!





        
        // 7. Search with advanced filters

        // matches all articles with tags_1 having the member 16
        String expr_1 = "JSON_CONTAINS(article_meta[\"tags_1\"], 16)";
        
        // matches all articles with tags_2 having the member [5, 3, 39, 8]
        // String expr_2 = "JSON_CONTAINS(article_meta[\"tags_2\"], [5, 3, 39, 8])";
        
        // matches all articles with tags_1 having a member from [5, 3, 39, 8]
        // String expr_3 = "JSON_CONTAINS_ANY(article_meta[\"tags_1\"], [5, 3, 39, 8])";
        
        // matches all articles with tags_1 having all members from [2, 4, 6]
        // String expr_4 = "JSON_CONTAINS_ALL(article_meta[\"tags_1\"], [2, 4, 6])";

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
            .withParams("{\"nprobe\":10}")
            .withConsistencyLevel(ConsistencyLevelEnum.BOUNDED)
            .withOutFields(outputFields)
            .withExpr(expr_1)
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
                entity.put("tags_1", ((JSONObject) scores.get(j).get("article_meta")).getJSONArray("tags_1").indexOf(16) >= 0);
                entities.add(entity);
            }
            
            results.add(entities);
        }

        System.out.println(results);

        // Output:
        // [[
        //     {
        //         "reading_time": 13,
        //         "tags_1": true,
        //         "distance": 0,
        //         "id": 445535516305953765,
        //         "title": "The Reported Mortality Rate of Coronavirus Is Not Important",
        //         "claps": 1100
        //     },
        //     {
        //         "reading_time": 9,
        //         "tags_1": true,
        //         "distance": 0.37674016,
        //         "id": 445535516305959406,
        //         "title": "Why The Coronavirus Mortality Rate is Misleading",
        //         "claps": 2900
        //     },
        //     {
        //         "reading_time": 4,
        //         "tags_1": true,
        //         "distance": 0.41629797,
        //         "id": 445535516305957206,
        //         "title": "Coronavirus shows what ethical Amazon could look like",
        //         "claps": 51
        //     },
        //     {
        //         "reading_time": 6,
        //         "tags_1": true,
        //         "distance": 0.4360938,
        //         "id": 445535516305954703,
        //         "title": "Mortality Rate As an Indicator of an Epidemic Outbreak",
        //         "claps": 65
        //     },
        //     {
        //         "reading_time": 12,
        //         "tags_1": true,
        //         "distance": 0.45862228,
        //         "id": 445535516305959545,
        //         "title": "Heart Disease Risk Assessment Using Machine Learning",
        //         "claps": 15
        //     }
        // ]]





        QueryParam queryParam = QueryParam.newBuilder()
            .withCollectionName(collectionName)
            .withExpr(expr_1)
            .withOutFields(outputFields)
            .withLimit(5L)
            .build();

        R<QueryResults> queryResponse = client.query(queryParam);

        if (queryResponse.getException() != null) {
            System.err.println("Failed to query: " + queryResponse.getException().getMessage());
            return;
        }

        QueryResultsWrapper queryResultsWrapper = new QueryResultsWrapper(queryResponse.getData());

        List<List<JSONObject>> queryResults = new ArrayList<>();

        List<Long> ids = (List<Long>) queryResultsWrapper.getFieldWrapper("id").getFieldData();
        List<String> titles = (List<String>) queryResultsWrapper.getFieldWrapper("title").getFieldData();
        List<?> articleMetas = queryResultsWrapper.getFieldWrapper("article_meta").getFieldData();
        List<JSONObject> entities = new ArrayList<>();
        for (int j = 0; j < ids.size(); ++j) {
            JSONObject entity = new JSONObject();
            entity.put("id", ids.get(j));
            entity.put("title", titles.get(j));

            byte[] articleMetaBytes = (byte[]) articleMetas.get(j);
            JSONObject articleMeta = JSON.parseObject(new String(articleMetaBytes));

            articleMeta.put("tags_1", articleMeta.getJSONArray("tags_1").indexOf(16) >= 0);
            articleMeta.remove("tags_2");

            entity.put("article_meta", articleMeta);
            entities.add(entity);
        }

        queryResults.add(entities);

        System.out.println(queryResults);        

        // Output:
        // [[
        //     {
        //         "article_meta": {
        //             "reading_time": 13,
        //             "tags_1": true,
        //             "publication": "The Startup",
        //             "link": "https://medium.com/swlh/the-reported-mortality-rate-of-coronavirus-is-not-important-369989c8d912",
        //             "responses": 18,
        //             "claps": 1100
        //         },
        //         "id": 445535516305953765,
        //         "title": "The Reported Mortality Rate of Coronavirus Is Not Important"
        //     },
        //     {
        //         "article_meta": {
        //             "reading_time": 6,
        //             "tags_1": true,
        //             "publication": "The Startup",
        //             "link": "https://medium.com/swlh/how-can-we-best-switch-in-python-458fb33f7835",
        //             "responses": 7,
        //             "claps": 500
        //         },
        //         "id": 445535516305953767,
        //         "title": "How Can We Best Switch in Python?"
        //     },
        //     {
        //         "article_meta": {
        //             "reading_time": 6,
        //             "tags_1": true,
        //             "publication": "The Startup",
        //             "link": "https://medium.com/swlh/science-monday-can-you-drink-as-much-water-as-tom-brady-1a45cb802be8",
        //             "responses": 5,
        //             "claps": 142
        //         },
        //         "id": 445535516305953771,
        //         "title": "Science Monday: Can You Drink As Much Water As Tom Brady?"
        //     },
        //     {
        //         "article_meta": {
        //             "reading_time": 13,
        //             "tags_1": true,
        //             "publication": "The Startup",
        //             "link": "https://medium.com/swlh/building-comprehensible-customer-churn-prediction-models-ca61ecce529d",
        //             "responses": 4,
        //             "claps": 261
        //         },
        //         "id": 445535516305953772,
        //         "title": "Building Comprehensible Customer Churn Prediction Models"
        //     },
        //     {
        //         "article_meta": {
        //             "reading_time": 11,
        //             "tags_1": true,
        //             "publication": "The Startup",
        //             "link": "https://medium.com/swlh/building-high-performance-startup-teams-1a0611c0bdd4",
        //             "responses": 0,
        //             "claps": 188
        //         },
        //         "id": 445535516305953775,
        //         "title": "Building high performance startup teams"
        //     }
        // ]]





        // 8. Drop collection

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
            int[] tags_1 = randomIntArray(40, 40);
            List<int[]> tags_2 = randomMatrix(10, 4, 40);


            article_meta.put("link", link);
            article_meta.put("publication", publication);
            article_meta.put("reading_time", reading_time);
            article_meta.put("claps", claps);
            article_meta.put("responses", responses);
            article_meta.put("tags_1", tags_1);
            article_meta.put("tags_2", tags_2);
            
            json_row.put("title", title);
            json_row.put("title_vector", vectors);
            json_row.put("article_meta", article_meta);
            rows.add(json_row);
        }
        return rows;
    }

    private static int[] randomIntArray(int length, int max) {
        Random rand = new Random();
        int[] arr = new int[length];
        for (int i = 0; i < length; i++) {
            arr[i] = rand.nextInt(max);
        }

        return arr;
    }

    private static List<int[]> randomMatrix(int dim, int length, int max) {
        List<int[]> matrix = new ArrayList<>();
        for (int i = 0; i < dim; i++) {
            matrix.add(randomIntArray(length, max));
        }

        return matrix;
    }
}