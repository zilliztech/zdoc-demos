package com.zilliz.docs;

import io.milvus.client.*;
import io.milvus.param.*;
import io.milvus.param.collection.FieldType;
import io.milvus.param.collection.FlushParam;
import io.milvus.param.index.CreateIndexParam;
import io.milvus.param.collection.CreateCollectionParam;
import io.milvus.param.collection.DropCollectionParam;
import io.milvus.grpc.DataType;
import io.milvus.grpc.FlushResponse;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.dml.QueryParam;
import io.milvus.param.dml.InsertParam.Field;
import io.milvus.param.highlevel.dml.GetIdsParam;
import io.milvus.param.highlevel.dml.response.GetResponse;
import io.milvus.grpc.MutationResult;
import io.milvus.grpc.QueryResults;
import io.milvus.response.MutationResultWrapper;
import io.milvus.response.QueryResultsWrapper;
import io.milvus.response.QueryResultsWrapper.RowRecord;
import io.milvus.param.dml.SearchParam;
import io.milvus.grpc.SearchResults;
import io.milvus.response.SearchResultsWrapper;
import io.milvus.common.clientenum.ConsistencyLevelEnum;
import io.milvus.param.collection.LoadCollectionParam;
import io.milvus.param.collection.ReleaseCollectionParam;

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
public final class UseCustomizedSchemaDemoCopy  {
    private UseCustomizedSchemaDemoCopy () {
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
            .withCollectionName("medium_articles")
            .withDescription("Schema of Medium articles")
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
        List<JSONObject> rows = getRows(dataset.getJSONArray("rows"), 100);
        
        // Also, you can get fields from dataset and insert them
        // List<Field> fields = getFields(dataset.getJSONArray("rows"), 100);

        InsertParam insertParam = InsertParam.newBuilder()
            .withCollectionName(collectionName)
            .withRows(rows)
            // .withFields(fields)
            .build();

        R<MutationResult> insertResponse = client.insert(insertParam);

        if (insertResponse.getStatus() != R.Status.Success.getCode()) {
            System.err.println(insertResponse.getMessage());
        }

        MutationResultWrapper mutationResultWrapper = new MutationResultWrapper(insertResponse.getData());

        System.out.println("Successfully insert entities: " + mutationResultWrapper.getInsertCount());   

        // Output:
        // Successfully insert entities: 100

        
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

        // Search vector in a collection

        SearchParam searchParam1 = SearchParam.newBuilder()
            .withCollectionName(collectionName)
            .withVectorFieldName("title_vector")
            .withVectors(queryVectors)
            .withTopK(5)   
            .withMetricType(MetricType.L2)  
            .withParams("{\"nprobe\":10,\"offset\":2, \"limit\":3}")
            .withConsistencyLevel(ConsistencyLevelEnum.BOUNDED)
            .withOutFields(outputFields)
            .build();

        R<SearchResults> response1 = client.search(searchParam1);

        SearchResultsWrapper wrapper1 = new SearchResultsWrapper(response1.getData().getResults());

        List<List<JSONObject>> results1 = new ArrayList<>();

        for (int i = 0; i < queryVectors.size(); ++i) {
            List<SearchResultsWrapper.IDScore> scores = wrapper1.getIDScore(i);
            List<String> titles = (List<String>) wrapper1.getFieldData("title", i);
            List<String> links = (List<String>) wrapper1.getFieldData("link", i);
            List<JSONObject> entities = new ArrayList<>();
            for (int j = 0; j < scores.size(); ++j) {
                SearchResultsWrapper.IDScore score = scores.get(j);
                JSONObject entity = new JSONObject();
                entity.put("id", score.getLongID());
                entity.put("distance", score.getScore());
                entity.put("title", titles.get(j));
                entity.put("link", links.get(j));
                entities.add(entity);
            }

            results1.add(entities);
        } 

        System.out.println(results1);

        // Output:
        // [[{"distance":0.6560175,"link":"https://medium.com/swlh/what-if-facebook-had-to-pay-you-for-the-profit-they-are-making-7571115139a7","id":445453895050486739,"title":"What if Facebook had to pay you for the profit they are making?"}, {"distance":0.663948,"link":"https://medium.com/swlh/what-happens-when-the-google-cookie-crumbles-a0405ef97bf1","id":445453895050486735,"title":"What Happens When the Google Cookie Crumbles?"}, {"distance":0.7018132,"link":"https://medium.com/personal-growth/a-psychologist-explains-how-anyone-can-beat-social-anxiety-7992014ced88","id":445453895050486756,"title":"A Clinical Psychologist Explains How to Beat Social Anxiety"}, {"distance":0.72767186,"link":"https://medium.com/swlh/building-comprehensible-customer-churn-prediction-models-ca61ecce529d","id":445453895050486695,"title":"Building Comprehensible Customer Churn Prediction Models"}, {"distance":0.7282397,"link":"https://medium.com/swlh/bad-luck-or-bad-strategy-9c3ca2352a3b","id":445453895050486747,"title":"Bad Luck? or Bad Strategy?"}]]


        // Search vector with filters in a collection

        SearchParam searchParam2 = SearchParam.newBuilder()
            .withCollectionName(collectionName)
            .withVectorFieldName("title_vector")
            .withVectors(queryVectors)
            .withTopK(5)   
            .withMetricType(MetricType.L2)  
            .withParams("{\"nprobe\":10,\"offset\":2, \"limit\":3}")
            .withConsistencyLevel(ConsistencyLevelEnum.BOUNDED)
            .withOutFields(outputFields)
            .withExpr("(publication == \"Towards Data Science\") and ((claps > 1500 and responses > 15) or (10 < reading_time < 15))")
            .build();

        R<SearchResults> response2 = client.search(searchParam2);

        SearchResultsWrapper wrapper2 = new SearchResultsWrapper(response2.getData().getResults());

        List<List<JSONObject>> results2 = new ArrayList<>();

        for (int i = 0; i < queryVectors.size(); ++i) {
            List<SearchResultsWrapper.IDScore> scores = wrapper2.getIDScore(i);
            List<String> titles = (List<String>) wrapper2.getFieldData("title", i);
            List<String> links = (List<String>) wrapper2.getFieldData("link", i);
            List<JSONObject> entities = new ArrayList<>();
            for (int j = 0; j < scores.size(); ++j) {
                SearchResultsWrapper.IDScore score = scores.get(j);
                JSONObject entity = new JSONObject();
                entity.put("id", score.getLongID());
                entity.put("distance", score.getScore());
                entity.put("title", titles.get(j));
                entity.put("link", links.get(j));
                entities.add(entity);
            }

            results2.add(entities);
        } 

        System.out.println(results2);

        // Output:
        // [[{"distance":0.8547761,"link":"https://towardsdatascience.com/finding-optimal-nba-physiques-using-data-visualization-with-python-6ce27ac5b68f","id":445453895050486787,"title":"Finding optimal NBA physiques using data visualization with Python"}, {"distance":0.8702323,"link":"https://towardsdatascience.com/understanding-nlp-how-ai-understands-our-languages-77601002cffc","id":445453895050486768,"title":"Understanding Natural Language Processing: how AI understands our languages"}, {"distance":0.91095924,"link":"https://towardsdatascience.com/rage-quitting-cancer-research-5e79cb04801","id":445453895050486767,"title":"Rage Quitting Cancer Research"}, {"distance":0.98407775,"link":"https://towardsdatascience.com/data-cleaning-in-python-the-ultimate-guide-2020-c63b88bf0a0d","id":445453895050486761,"title":"Data Cleaning in Python: the Ultimate Guide (2020)"}, {"distance":1.091625,"link":"https://towardsdatascience.com/top-10-in-demand-programming-languages-to-learn-in-2020-4462eb7d8d3e","id":445453895050486757,"title":"Top 10 In-Demand programming languages to learn in 2020"}]]


        // Search multiple vectors in a collection

        List<Float> queryVector2 = rows.get(1).getJSONArray("title_vector").toJavaList(Float.class);
        queryVectors.add(queryVector2);


        SearchParam searchParam3 = SearchParam.newBuilder()
            .withCollectionName(collectionName)
            .withVectorFieldName("title_vector")
            .withVectors(queryVectors)
            .withTopK(5)   
            .withMetricType(MetricType.L2)  
            .withParams("{\"nprobe\":10,\"offset\":2, \"limit\":3}")
            .withConsistencyLevel(ConsistencyLevelEnum.BOUNDED)
            .withOutFields(outputFields)
            .build();

        R<SearchResults> response3 = client.search(searchParam3);

        SearchResultsWrapper wrapper3 = new SearchResultsWrapper(response3.getData().getResults());

        List<List<JSONObject>> results3 = new ArrayList<>();

        for (int i = 0; i < queryVectors.size(); ++i) {
            List<SearchResultsWrapper.IDScore> scores = wrapper3.getIDScore(i);
            List<String> titles = (List<String>) wrapper3.getFieldData("title", i);
            List<String> links = (List<String>) wrapper3.getFieldData("link", i);
            List<JSONObject> entities = new ArrayList<>();
            for (int j = 0; j < scores.size(); ++j) {
                SearchResultsWrapper.IDScore score = scores.get(j);
                JSONObject entity = new JSONObject();
                entity.put("id", score.getLongID());
                entity.put("distance", score.getScore());
                entity.put("title", titles.get(j));
                entity.put("link", links.get(j));
                entities.add(entity);
            }

            results3.add(entities);
        } 

        System.out.println(results3);

        // Output:
        // [[{"distance":0.6560175,"link":"https://medium.com/swlh/what-if-facebook-had-to-pay-you-for-the-profit-they-are-making-7571115139a7","id":445453895050486739,"title":"What if Facebook had to pay you for the profit they are making?"}, {"distance":0.663948,"link":"https://medium.com/swlh/what-happens-when-the-google-cookie-crumbles-a0405ef97bf1","id":445453895050486735,"title":"What Happens When the Google Cookie Crumbles?"}, {"distance":0.7018132,"link":"https://medium.com/personal-growth/a-psychologist-explains-how-anyone-can-beat-social-anxiety-7992014ced88","id":445453895050486756,"title":"A Clinical Psychologist Explains How to Beat Social Anxiety"}, {"distance":0.72767186,"link":"https://medium.com/swlh/building-comprehensible-customer-churn-prediction-models-ca61ecce529d","id":445453895050486695,"title":"Building Comprehensible Customer Churn Prediction Models"}, {"distance":0.7282397,"link":"https://medium.com/swlh/bad-luck-or-bad-strategy-9c3ca2352a3b","id":445453895050486747,"title":"Bad Luck? or Bad Strategy?"}], [{"distance":0.5267407,"link":"https://medium.com/swlh/blockchain-iot-and-ai-a-perfect-fit-1-e04c6ad73fbc","id":445453895050486705,"title":"Blockchain, IoT and AI — A Perfect Fit"}, {"distance":0.54294807,"link":"https://towardsdatascience.com/how-to-write-movie-reviews-with-ai-d17f758f2ed5","id":445453895050486786,"title":"How To Write Movie Reviews with AI"}, {"distance":0.5753727,"link":"https://towardsdatascience.com/feature-selection-techniques-in-python-predicting-hotel-cancellations-48a77521ee4f","id":445453895050486784,"title":"Feature Selection Techniques in Python: Predicting Hotel Cancellations"}, {"distance":0.5788189,"link":"https://medium.com/swlh/guide-to-nest-js-rabbitmq-microservices-e1e8655d2853","id":445453895050486693,"title":"Guide to Nest JS-RabbitMQ Microservices"}, {"distance":0.57894915,"link":"https://towardsdatascience.com/opengl-in-java-how-to-use-hardware-acceleration-676334f18f11","id":445453895050486773,"title":"OpenGL in Java: how to use hardware acceleration"}]]


        QueryParam queryParam = QueryParam.newBuilder()
            .withCollectionName(collectionName)
            .withExpr("(publication == \"Towards Data Science\") and ((claps > 1500 and responses > 15) or (10 < reading_time < 15))")
            .withOutFields(outputFields)
            .build();
        
        R<QueryResults> queryResponse = client.query(queryParam);

        QueryResultsWrapper queryResultsWrapper = new QueryResultsWrapper(queryResponse.getData());

        List<List<JSONObject>> queryResults = new ArrayList<>();

        for (int i = 0; i < queryResultsWrapper.getRowCount(); ++i) {
            List<Long> ids = (List<Long>) queryResultsWrapper.getFieldWrapper("id").getFieldData();
            List<String> titles = (List<String>) queryResultsWrapper.getFieldWrapper("title").getFieldData();
            List<String> links = (List<String>) queryResultsWrapper.getFieldWrapper("link").getFieldData();
            List<JSONObject> entities = new ArrayList<>();
            for (int j = 0; j < ids.size(); ++j) {
                JSONObject entity = new JSONObject();
                entity.put("id", ids.get(j));
                entity.put("title", titles.get(j));
                entity.put("link", links.get(j));
                entities.add(entity);
            }

            queryResults.add(entities);
        }

        System.out.println(queryResults);

        // Output:
        // [[{"link":"https://towardsdatascience.com/top-10-in-demand-programming-languages-to-learn-in-2020-4462eb7d8d3e","id":445453895050486757,"title":"Top 10 In-Demand programming languages to learn in 2020"}, {"link":"https://towardsdatascience.com/data-cleaning-in-python-the-ultimate-guide-2020-c63b88bf0a0d","id":445453895050486761,"title":"Data Cleaning in Python: the Ultimate Guide (2020)"}, {"link":"https://towardsdatascience.com/top-trends-of-graph-machine-learning-in-2020-1194175351a3","id":445453895050486763,"title":"Top Trends of Graph Machine Learning in 2020"}, {"link":"https://towardsdatascience.com/rage-quitting-cancer-research-5e79cb04801","id":445453895050486767,"title":"Rage Quitting Cancer Research"}, {"link":"https://towardsdatascience.com/understanding-nlp-how-ai-understands-our-languages-77601002cffc","id":445453895050486768,"title":"Understanding Natural Language Processing: how AI understands our languages"}, {"link":"https://towardsdatascience.com/svm-an-optimization-problem-242cbb8d96a8","id":445453895050486778,"title":"SVM: An optimization problem"}, {"link":"https://towardsdatascience.com/finding-optimal-nba-physiques-using-data-visualization-with-python-6ce27ac5b68f","id":445453895050486787,"title":"Finding optimal NBA physiques using data visualization with Python"}], [{"link":"https://towardsdatascience.com/top-10-in-demand-programming-languages-to-learn-in-2020-4462eb7d8d3e","id":445453895050486757,"title":"Top 10 In-Demand programming languages to learn in 2020"}, {"link":"https://towardsdatascience.com/data-cleaning-in-python-the-ultimate-guide-2020-c63b88bf0a0d","id":445453895050486761,"title":"Data Cleaning in Python: the Ultimate Guide (2020)"}, {"link":"https://towardsdatascience.com/top-trends-of-graph-machine-learning-in-2020-1194175351a3","id":445453895050486763,"title":"Top Trends of Graph Machine Learning in 2020"}, {"link":"https://towardsdatascience.com/rage-quitting-cancer-research-5e79cb04801","id":445453895050486767,"title":"Rage Quitting Cancer Research"}, {"link":"https://towardsdatascience.com/understanding-nlp-how-ai-understands-our-languages-77601002cffc","id":445453895050486768,"title":"Understanding Natural Language Processing: how AI understands our languages"}, {"link":"https://towardsdatascience.com/svm-an-optimization-problem-242cbb8d96a8","id":445453895050486778,"title":"SVM: An optimization problem"}, {"link":"https://towardsdatascience.com/finding-optimal-nba-physiques-using-data-visualization-with-python-6ce27ac5b68f","id":445453895050486787,"title":"Finding optimal NBA physiques using data visualization with Python"}], [{"link":"https://towardsdatascience.com/top-10-in-demand-programming-languages-to-learn-in-2020-4462eb7d8d3e","id":445453895050486757,"title":"Top 10 In-Demand programming languages to learn in 2020"}, {"link":"https://towardsdatascience.com/data-cleaning-in-python-the-ultimate-guide-2020-c63b88bf0a0d","id":445453895050486761,"title":"Data Cleaning in Python: the Ultimate Guide (2020)"}, {"link":"https://towardsdatascience.com/top-trends-of-graph-machine-learning-in-2020-1194175351a3","id":445453895050486763,"title":"Top Trends of Graph Machine Learning in 2020"}, {"link":"https://towardsdatascience.com/rage-quitting-cancer-research-5e79cb04801","id":445453895050486767,"title":"Rage Quitting Cancer Research"}, {"link":"https://towardsdatascience.com/understanding-nlp-how-ai-understands-our-languages-77601002cffc","id":445453895050486768,"title":"Understanding Natural Language Processing: how AI understands our languages"}, {"link":"https://towardsdatascience.com/svm-an-optimization-problem-242cbb8d96a8","id":445453895050486778,"title":"SVM: An optimization problem"}, {"link":"https://towardsdatascience.com/finding-optimal-nba-physiques-using-data-visualization-with-python-6ce27ac5b68f","id":445453895050486787,"title":"Finding optimal NBA physiques using data visualization with Python"}], [{"link":"https://towardsdatascience.com/top-10-in-demand-programming-languages-to-learn-in-2020-4462eb7d8d3e","id":445453895050486757,"title":"Top 10 In-Demand programming languages to learn in 2020"}, {"link":"https://towardsdatascience.com/data-cleaning-in-python-the-ultimate-guide-2020-c63b88bf0a0d","id":445453895050486761,"title":"Data Cleaning in Python: the Ultimate Guide (2020)"}, {"link":"https://towardsdatascience.com/top-trends-of-graph-machine-learning-in-2020-1194175351a3","id":445453895050486763,"title":"Top Trends of Graph Machine Learning in 2020"}, {"link":"https://towardsdatascience.com/rage-quitting-cancer-research-5e79cb04801","id":445453895050486767,"title":"Rage Quitting Cancer Research"}, {"link":"https://towardsdatascience.com/understanding-nlp-how-ai-understands-our-languages-77601002cffc","id":445453895050486768,"title":"Understanding Natural Language Processing: how AI understands our languages"}, {"link":"https://towardsdatascience.com/svm-an-optimization-problem-242cbb8d96a8","id":445453895050486778,"title":"SVM: An optimization problem"}, {"link":"https://towardsdatascience.com/finding-optimal-nba-physiques-using-data-visualization-with-python-6ce27ac5b68f","id":445453895050486787,"title":"Finding optimal NBA physiques using data visualization with Python"}], [{"link":"https://towardsdatascience.com/top-10-in-demand-programming-languages-to-learn-in-2020-4462eb7d8d3e","id":445453895050486757,"title":"Top 10 In-Demand programming languages to learn in 2020"}, {"link":"https://towardsdatascience.com/data-cleaning-in-python-the-ultimate-guide-2020-c63b88bf0a0d","id":445453895050486761,"title":"Data Cleaning in Python: the Ultimate Guide (2020)"}, {"link":"https://towardsdatascience.com/top-trends-of-graph-machine-learning-in-2020-1194175351a3","id":445453895050486763,"title":"Top Trends of Graph Machine Learning in 2020"}, {"link":"https://towardsdatascience.com/rage-quitting-cancer-research-5e79cb04801","id":445453895050486767,"title":"Rage Quitting Cancer Research"}, {"link":"https://towardsdatascience.com/understanding-nlp-how-ai-understands-our-languages-77601002cffc","id":445453895050486768,"title":"Understanding Natural Language Processing: how AI understands our languages"}, {"link":"https://towardsdatascience.com/svm-an-optimization-problem-242cbb8d96a8","id":445453895050486778,"title":"SVM: An optimization problem"}, {"link":"https://towardsdatascience.com/finding-optimal-nba-physiques-using-data-visualization-with-python-6ce27ac5b68f","id":445453895050486787,"title":"Finding optimal NBA physiques using data visualization with Python"}], [{"link":"https://towardsdatascience.com/top-10-in-demand-programming-languages-to-learn-in-2020-4462eb7d8d3e","id":445453895050486757,"title":"Top 10 In-Demand programming languages to learn in 2020"}, {"link":"https://towardsdatascience.com/data-cleaning-in-python-the-ultimate-guide-2020-c63b88bf0a0d","id":445453895050486761,"title":"Data Cleaning in Python: the Ultimate Guide (2020)"}, {"link":"https://towardsdatascience.com/top-trends-of-graph-machine-learning-in-2020-1194175351a3","id":445453895050486763,"title":"Top Trends of Graph Machine Learning in 2020"}, {"link":"https://towardsdatascience.com/rage-quitting-cancer-research-5e79cb04801","id":445453895050486767,"title":"Rage Quitting Cancer Research"}, {"link":"https://towardsdatascience.com/understanding-nlp-how-ai-understands-our-languages-77601002cffc","id":445453895050486768,"title":"Understanding Natural Language Processing: how AI understands our languages"}, {"link":"https://towardsdatascience.com/svm-an-optimization-problem-242cbb8d96a8","id":445453895050486778,"title":"SVM: An optimization problem"}, {"link":"https://towardsdatascience.com/finding-optimal-nba-physiques-using-data-visualization-with-python-6ce27ac5b68f","id":445453895050486787,"title":"Finding optimal NBA physiques using data visualization with Python"}], [{"link":"https://towardsdatascience.com/top-10-in-demand-programming-languages-to-learn-in-2020-4462eb7d8d3e","id":445453895050486757,"title":"Top 10 In-Demand programming languages to learn in 2020"}, {"link":"https://towardsdatascience.com/data-cleaning-in-python-the-ultimate-guide-2020-c63b88bf0a0d","id":445453895050486761,"title":"Data Cleaning in Python: the Ultimate Guide (2020)"}, {"link":"https://towardsdatascience.com/top-trends-of-graph-machine-learning-in-2020-1194175351a3","id":445453895050486763,"title":"Top Trends of Graph Machine Learning in 2020"}, {"link":"https://towardsdatascience.com/rage-quitting-cancer-research-5e79cb04801","id":445453895050486767,"title":"Rage Quitting Cancer Research"}, {"link":"https://towardsdatascience.com/understanding-nlp-how-ai-understands-our-languages-77601002cffc","id":445453895050486768,"title":"Understanding Natural Language Processing: how AI understands our languages"}, {"link":"https://towardsdatascience.com/svm-an-optimization-problem-242cbb8d96a8","id":445453895050486778,"title":"SVM: An optimization problem"}, {"link":"https://towardsdatascience.com/finding-optimal-nba-physiques-using-data-visualization-with-python-6ce27ac5b68f","id":445453895050486787,"title":"Finding optimal NBA physiques using data visualization with Python"}]]



        // Output:
        // [[{"link":"https://towardsdatascience.com/top-10-in-demand-programming-languages-to-learn-in-2020-4462eb7d8d3e","id":445453895050486656,"title":"Top 10 In-Demand programming languages to learn in 2020"}, {"link":"https://towardsdatascience.com/data-cleaning-in-python-the-ultimate-guide-2020-c63b88bf0a0d","id":445453895050486660,"title":"Data Cleaning in Python: the Ultimate Guide (2020)"}, {"link":"https://towardsdatascience.com/top-trends-of-graph-machine-learning-in-2020-1194175351a3","id":445453895050486662,"title":"Top Trends of Graph Machine Learning in 2020"}, {"link":"https://towardsdatascience.com/rage-quitting-cancer-research-5e79cb04801","id":445453895050486666,"title":"Rage Quitting Cancer Research"}, {"link":"https://towardsdatascience.com/understanding-nlp-how-ai-understands-our-languages-77601002cffc","id":445453895050486667,"title":"Understanding Natural Language Processing: how AI understands our languages"}, {"link":"https://towardsdatascience.com/svm-an-optimization-problem-242cbb8d96a8","id":445453895050486677,"title":"SVM: An optimization problem"}, {"link":"https://towardsdatascience.com/finding-optimal-nba-physiques-using-data-visualization-with-python-6ce27ac5b68f","id":445453895050486686,"title":"Finding optimal NBA physiques using data visualization with Python"}], [{"link":"https://towardsdatascience.com/top-10-in-demand-programming-languages-to-learn-in-2020-4462eb7d8d3e","id":445453895050486656,"title":"Top 10 In-Demand programming languages to learn in 2020"}, {"link":"https://towardsdatascience.com/data-cleaning-in-python-the-ultimate-guide-2020-c63b88bf0a0d","id":445453895050486660,"title":"Data Cleaning in Python: the Ultimate Guide (2020)"}, {"link":"https://towardsdatascience.com/top-trends-of-graph-machine-learning-in-2020-1194175351a3","id":445453895050486662,"title":"Top Trends of Graph Machine Learning in 2020"}, {"link":"https://towardsdatascience.com/rage-quitting-cancer-research-5e79cb04801","id":445453895050486666,"title":"Rage Quitting Cancer Research"}, {"link":"https://towardsdatascience.com/understanding-nlp-how-ai-understands-our-languages-77601002cffc","id":445453895050486667,"title":"Understanding Natural Language Processing: how AI understands our languages"}, {"link":"https://towardsdatascience.com/svm-an-optimization-problem-242cbb8d96a8","id":445453895050486677,"title":"SVM: An optimization problem"}, {"link":"https://towardsdatascience.com/finding-optimal-nba-physiques-using-data-visualization-with-python-6ce27ac5b68f","id":445453895050486686,"title":"Finding optimal NBA physiques using data visualization with Python"}], [{"link":"https://towardsdatascience.com/top-10-in-demand-programming-languages-to-learn-in-2020-4462eb7d8d3e","id":445453895050486656,"title":"Top 10 In-Demand programming languages to learn in 2020"}, {"link":"https://towardsdatascience.com/data-cleaning-in-python-the-ultimate-guide-2020-c63b88bf0a0d","id":445453895050486660,"title":"Data Cleaning in Python: the Ultimate Guide (2020)"}, {"link":"https://towardsdatascience.com/top-trends-of-graph-machine-learning-in-2020-1194175351a3","id":445453895050486662,"title":"Top Trends of Graph Machine Learning in 2020"}, {"link":"https://towardsdatascience.com/rage-quitting-cancer-research-5e79cb04801","id":445453895050486666,"title":"Rage Quitting Cancer Research"}, {"link":"https://towardsdatascience.com/understanding-nlp-how-ai-understands-our-languages-77601002cffc","id":445453895050486667,"title":"Understanding Natural Language Processing: how AI understands our languages"}, {"link":"https://towardsdatascience.com/svm-an-optimization-problem-242cbb8d96a8","id":445453895050486677,"title":"SVM: An optimization problem"}, {"link":"https://towardsdatascience.com/finding-optimal-nba-physiques-using-data-visualization-with-python-6ce27ac5b68f","id":445453895050486686,"title":"Finding optimal NBA physiques using data visualization with Python"}], [{"link":"https://towardsdatascience.com/top-10-in-demand-programming-languages-to-learn-in-2020-4462eb7d8d3e","id":445453895050486656,"title":"Top 10 In-Demand programming languages to learn in 2020"}, {"link":"https://towardsdatascience.com/data-cleaning-in-python-the-ultimate-guide-2020-c63b88bf0a0d","id":445453895050486660,"title":"Data Cleaning in Python: the Ultimate Guide (2020)"}, {"link":"https://towardsdatascience.com/top-trends-of-graph-machine-learning-in-2020-1194175351a3","id":445453895050486662,"title":"Top Trends of Graph Machine Learning in 2020"}, {"link":"https://towardsdatascience.com/rage-quitting-cancer-research-5e79cb04801","id":445453895050486666,"title":"Rage Quitting Cancer Research"}, {"link":"https://towardsdatascience.com/understanding-nlp-how-ai-understands-our-languages-77601002cffc","id":445453895050486667,"title":"Understanding Natural Language Processing: how AI understands our languages"}, {"link":"https://towardsdatascience.com/svm-an-optimization-problem-242cbb8d96a8","id":445453895050486677,"title":"SVM: An optimization problem"}, {"link":"https://towardsdatascience.com/finding-optimal-nba-physiques-using-data-visualization-with-python-6ce27ac5b68f","id":445453895050486686,"title":"Finding optimal NBA physiques using data visualization with Python"}], [{"link":"https://towardsdatascience.com/top-10-in-demand-programming-languages-to-learn-in-2020-4462eb7d8d3e","id":445453895050486656,"title":"Top 10 In-Demand programming languages to learn in 2020"}, {"link":"https://towardsdatascience.com/data-cleaning-in-python-the-ultimate-guide-2020-c63b88bf0a0d","id":445453895050486660,"title":"Data Cleaning in Python: the Ultimate Guide (2020)"}, {"link":"https://towardsdatascience.com/top-trends-of-graph-machine-learning-in-2020-1194175351a3","id":445453895050486662,"title":"Top Trends of Graph Machine Learning in 2020"}, {"link":"https://towardsdatascience.com/rage-quitting-cancer-research-5e79cb04801","id":445453895050486666,"title":"Rage Quitting Cancer Research"}, {"link":"https://towardsdatascience.com/understanding-nlp-how-ai-understands-our-languages-77601002cffc","id":445453895050486667,"title":"Understanding Natural Language Processing: how AI understands our languages"}, {"link":"https://towardsdatascience.com/svm-an-optimization-problem-242cbb8d96a8","id":445453895050486677,"title":"SVM: An optimization problem"}, {"link":"https://towardsdatascience.com/finding-optimal-nba-physiques-using-data-visualization-with-python-6ce27ac5b68f","id":445453895050486686,"title":"Finding optimal NBA physiques using data visualization with Python"}], [{"link":"https://towardsdatascience.com/top-10-in-demand-programming-languages-to-learn-in-2020-4462eb7d8d3e","id":445453895050486656,"title":"Top 10 In-Demand programming languages to learn in 2020"}, {"link":"https://towardsdatascience.com/data-cleaning-in-python-the-ultimate-guide-2020-c63b88bf0a0d","id":445453895050486660,"title":"Data Cleaning in Python: the Ultimate Guide (2020)"}, {"link":"https://towardsdatascience.com/top-trends-of-graph-machine-learning-in-2020-1194175351a3","id":445453895050486662,"title":"Top Trends of Graph Machine Learning in 2020"}, {"link":"https://towardsdatascience.com/rage-quitting-cancer-research-5e79cb04801","id":445453895050486666,"title":"Rage Quitting Cancer Research"}, {"link":"https://towardsdatascience.com/understanding-nlp-how-ai-understands-our-languages-77601002cffc","id":445453895050486667,"title":"Understanding Natural Language Processing: how AI understands our languages"}, {"link":"https://towardsdatascience.com/svm-an-optimization-problem-242cbb8d96a8","id":445453895050486677,"title":"SVM: An optimization problem"}, {"link":"https://towardsdatascience.com/finding-optimal-nba-physiques-using-data-visualization-with-python-6ce27ac5b68f","id":445453895050486686,"title":"Finding optimal NBA physiques using data visualization with Python"}], [{"link":"https://towardsdatascience.com/top-10-in-demand-programming-languages-to-learn-in-2020-4462eb7d8d3e","id":445453895050486656,"title":"Top 10 In-Demand programming languages to learn in 2020"}, {"link":"https://towardsdatascience.com/data-cleaning-in-python-the-ultimate-guide-2020-c63b88bf0a0d","id":445453895050486660,"title":"Data Cleaning in Python: the Ultimate Guide (2020)"}, {"link":"https://towardsdatascience.com/top-trends-of-graph-machine-learning-in-2020-1194175351a3","id":445453895050486662,"title":"Top Trends of Graph Machine Learning in 2020"}, {"link":"https://towardsdatascience.com/rage-quitting-cancer-research-5e79cb04801","id":445453895050486666,"title":"Rage Quitting Cancer Research"}, {"link":"https://towardsdatascience.com/understanding-nlp-how-ai-understands-our-languages-77601002cffc","id":445453895050486667,"title":"Understanding Natural Language Processing: how AI understands our languages"}, {"link":"https://towardsdatascience.com/svm-an-optimization-problem-242cbb8d96a8","id":445453895050486677,"title":"SVM: An optimization problem"}, {"link":"https://towardsdatascience.com/finding-optimal-nba-physiques-using-data-visualization-with-python-6ce27ac5b68f","id":445453895050486686,"title":"Finding optimal NBA physiques using data visualization with Python"}]]

        List<Long> ids = new ArrayList<Long>();
        ids.add((Long) queryResultsWrapper.getFieldWrapper("id").getFieldData().get(0));
        ids.add((Long) queryResultsWrapper.getFieldWrapper("id").getFieldData().get(1));
        ids.add(1L);

        GetIdsParam getIdsParam = GetIdsParam.newBuilder()
            .withCollectionName(collectionName)
            .withPrimaryIds(ids)
            .withOutputFields(outputFields)
            .build();
        
        R<GetResponse> getResponse = client.get(getIdsParam);

        if (getResponse.getException() != null) {
            System.err.println("Failed to get: " + getResponse.getException().getMessage());
            return;
        }

        List<RowRecord> getResults = new ArrayList<>();

        for (QueryResultsWrapper.RowRecord rowRecord: getResponse.getData().getRowRecords()) {
            getResults.add(rowRecord);
        }

        System.out.println(getResults);

        // Output:
        // [[link:https://towardsdatascience.com/top-10-in-demand-programming-languages-to-learn-in-2020-4462eb7d8d3e, id:445453895050486757, title:Top 10 In-Demand programming languages to learn in 2020], [link:https://towardsdatascience.com/data-cleaning-in-python-the-ultimate-guide-2020-c63b88bf0a0d, id:445453895050486761, title:Data Cleaning in Python: the Ultimate Guide (2020)]]


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