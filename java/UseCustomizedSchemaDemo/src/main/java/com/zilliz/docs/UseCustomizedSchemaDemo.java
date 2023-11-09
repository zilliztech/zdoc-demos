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

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Hello world!
 */
public final class UseCustomizedSchemaDemo  {
    private UseCustomizedSchemaDemo () {
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
        List<JSONObject> rows = getRows(dataset.getJSONArray("rows"), 5979);
        
        // Also, you can get fields from dataset and insert them
        // List<Field> fields = getFields(dataset.getJSONArray("rows"), 5979);

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
        // Successfully insert entities: 5979





        
        // wait for a while
        try {
            // pause execution for 5 seconds
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            // handle the exception
            Thread.currentThread().interrupt();
        } 

        List<String> collectionNames = new ArrayList<>();
        collectionNames.add(collectionName);

        FlushParam flushParam = FlushParam.newBuilder()
            .withCollectionNames(collectionNames)
            .build();

        R<FlushResponse> flushResponse = client.flush(flushParam);

        if (flushResponse.getStatus() != R.Status.Success.getCode()) {
            System.err.println(flushResponse.getMessage());
        }

        // 7. Search vectors

        // single vector search in a collection

        List<List<Float>> queryVectors = new ArrayList<>();
        List<Float> queryVector1 = rows.get(0).getJSONArray("title_vector").toJavaList(Float.class);
        queryVectors.add(queryVector1);

        List<String> outputFields = new ArrayList<>();
        outputFields.add("title");
        outputFields.add("link");

        SearchParam searchParam1 = SearchParam.newBuilder()
            .withCollectionName(collectionName)
            .withVectorFieldName("title_vector")
            .withVectors(queryVectors)
            .withTopK(5)   
            .withMetricType(MetricType.L2)  
            .withParams("{\"nprobe\":1024}")
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
        // [[
        //     {
        //         "distance": 0,
        //         "link": "https://medium.com/swlh/the-reported-mortality-rate-of-coronavirus-is-not-important-369989c8d912",
        //         "id": 445297206350829205,
        //         "title": "The Reported Mortality Rate of Coronavirus Is Not Important"
        //     },
        //     {
        //         "distance": 0.29999837,
        //         "link": "https://towardsdatascience.com/following-the-spread-of-coronavirus-23626940c125",
        //         "id": 445297206350832382,
        //         "title": "Following the Spread of Coronavirus"
        //     },
        //     {
        //         "distance": 0.36103836,
        //         "link": "https://medium.com/swlh/the-hidden-side-effect-of-the-coronavirus-b6a7a5ee9586",
        //         "id": 445297206350834812,
        //         "title": "The Hidden Side Effect of the Coronavirus"
        //     },
        //     {
        //         "distance": 0.37674016,
        //         "link": "https://towardsdatascience.com/why-the-coronavirus-mortality-rate-is-misleading-cc63f571b6a6",
        //         "id": 445297206350834846,
        //         "title": "Why The Coronavirus Mortality Rate is Misleading"
        //     },
        //     {
        //         "distance": 0.41629803,
        //         "link": "https://medium.com/swlh/coronavirus-shows-what-ethical-amazon-could-look-like-7c80baf2c663",
        //         "id": 445297206350832646,
        //         "title": "Coronavirus shows what ethical Amazon could look like"
        //     }
        // ]]





        // Search multiple vectors in a collection

        List<Float> queryVector2 = rows.get(1).getJSONArray("title_vector").toJavaList(Float.class);
        queryVectors.add(queryVector2);


        SearchParam searchParam2 = SearchParam.newBuilder()
            .withCollectionName(collectionName)
            .withVectorFieldName("title_vector")
            .withVectors(queryVectors)
            .withTopK(5)   
            .withMetricType(MetricType.L2)  
            .withParams("{\"nprobe\":1024}")
            .withConsistencyLevel(ConsistencyLevelEnum.BOUNDED)
            .withOutFields(outputFields)
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
        // [
        //     [
        //         {
        //             "distance": 0,
        //             "link": "https://medium.com/swlh/the-reported-mortality-rate-of-coronavirus-is-not-important-369989c8d912",
        //             "id": 445297206350829205,
        //             "title": "The Reported Mortality Rate of Coronavirus Is Not Important"
        //         },
        //         {
        //             "distance": 0.29999837,
        //             "link": "https://towardsdatascience.com/following-the-spread-of-coronavirus-23626940c125",
        //             "id": 445297206350832382,
        //             "title": "Following the Spread of Coronavirus"
        //         },
        //         {
        //             "distance": 0.36103836,
        //             "link": "https://medium.com/swlh/the-hidden-side-effect-of-the-coronavirus-b6a7a5ee9586",
        //             "id": 445297206350834812,
        //             "title": "The Hidden Side Effect of the Coronavirus"
        //         },
        //         {
        //             "distance": 0.37674016,
        //             "link": "https://towardsdatascience.com/why-the-coronavirus-mortality-rate-is-misleading-cc63f571b6a6",
        //             "id": 445297206350834846,
        //             "title": "Why The Coronavirus Mortality Rate is Misleading"
        //         },
        //         {
        //             "distance": 0.41629803,
        //             "link": "https://medium.com/swlh/coronavirus-shows-what-ethical-amazon-could-look-like-7c80baf2c663",
        //             "id": 445297206350832646,
        //             "title": "Coronavirus shows what ethical Amazon could look like"
        //         }
        //     ],
        //     [
        //         {
        //             "distance": 0,
        //             "link": "https://medium.com/swlh/dashboards-in-python-3-advanced-examples-for-dash-beginners-and-everyone-else-b1daf4e2ec0a",
        //             "id": 445297206350829206,
        //             "title": "Dashboards in Python: 3 Advanced Examples for Dash Beginners and Everyone Else"
        //         },
        //         {
        //             "distance": 0.1953007,
        //             "link": "https://medium.com/swlh/dashboards-in-python-using-dash-creating-a-data-table-using-data-from-reddit-1d6c0cecb4bd",
        //             "id": 445297206350834776,
        //             "title": "Dashboards in Python Using Dash \u2014 Creating a Data Table using Data from Reddit"
        //         },
        //         {
        //             "distance": 0.40734136,
        //             "link": "https://medium.com/swlh/ocr-engine-comparison-tesseract-vs-easyocr-729be893d3ae",
        //             "id": 445297206350832449,
        //             "title": "OCR Engine Comparison \u2014 Tesseract vs. EasyOCR"
        //         },
        //         {
        //             "distance": 0.41571742,
        //             "link": "https://medium.com/swlh/how-to-import-data-to-salesforce-marketing-cloud-exacttarget-using-python-rest-api-1302a26f89c0",
        //             "id": 445297206350832481,
        //             "title": "How to Import Data to Salesforce Marketing Cloud (ExactTarget) Using Python REST API"
        //         },
        //         {
        //             "distance": 0.41766006,
        //             "link": "https://towardsdatascience.com/how-to-automate-multiple-excel-workbooks-and-perform-analysis-13e8aa5a2042",
        //             "id": 445297206350830401,
        //             "title": "How to Automate Multiple Excel Workbooks and Perform Analysis"
        //         }
        //     ]
        // ]





        // Search vector with filters in a collection

        queryVectors.clear();
        queryVectors.add(queryVector1);

        outputFields.clear();
        outputFields.add("title");
        outputFields.add("reading_time");

        SearchParam searchParam3 = SearchParam.newBuilder()
            .withCollectionName(collectionName)
            .withVectorFieldName("title_vector")
            .withVectors(queryVectors)
            .withTopK(5)   
            .withMetricType(MetricType.L2)  
            .withParams("{\"nprobe\":1024}")
            .withConsistencyLevel(ConsistencyLevelEnum.BOUNDED)
            .withOutFields(outputFields)
            .withExpr("10 < reading_time < 15")
            .build();

        R<SearchResults> response3 = client.search(searchParam3);

        SearchResultsWrapper wrapper3 = new SearchResultsWrapper(response3.getData().getResults());

        List<List<JSONObject>> results3 = new ArrayList<>();

        for (int i = 0; i < queryVectors.size(); ++i) {
            List<SearchResultsWrapper.IDScore> scores = wrapper3.getIDScore(i);
            List<String> titles = (List<String>) wrapper3.getFieldData("title", i);
            List<Long> readingTimes = (List<Long>) wrapper3.getFieldData("reading_time", i);
            List<JSONObject> entities = new ArrayList<>();
            for (int j = 0; j < scores.size(); ++j) {
                SearchResultsWrapper.IDScore score = scores.get(j);
                JSONObject entity = new JSONObject();
                entity.put("id", score.getLongID());
                entity.put("distance", score.getScore());
                entity.put("title", titles.get(j));
                entity.put("reading_time", readingTimes.get(j));
                entities.add(entity);
            }

            results3.add(entities);
        } 

        System.out.println(results3);

        // Output:
        // [[
        //     {
        //         "reading_time": 13,
        //         "distance": 0,
        //         "id": 445297206350829205,
        //         "title": "The Reported Mortality Rate of Coronavirus Is Not Important"
        //     },
        //     {
        //         "reading_time": 12,
        //         "distance": 0.45862228,
        //         "id": 445297206350834985,
        //         "title": "Heart Disease Risk Assessment Using Machine Learning"
        //     },
        //     {
        //         "reading_time": 13,
        //         "distance": 0.5037479,
        //         "id": 445297206350834708,
        //         "title": "New Data Shows a Lower Covid-19 Fatality Rate"
        //     },
        //     {
        //         "reading_time": 11,
        //         "distance": 0.52556163,
        //         "id": 445297206350833536,
        //         "title": "Common Pipenv Errors"
        //     },
        //     {
        //         "reading_time": 12,
        //         "distance": 0.567989,
        //         "id": 445297206350832008,
        //         "title": "How Does US Healthcare Compare With Healthcare Around the World?"
        //     }
        // ]]





        // -------

        queryVectors.clear();
        queryVectors.add(queryVector1);

        outputFields.clear();
        outputFields.add("title");
        outputFields.add("claps");
        outputFields.add("responses");

        SearchParam searchParam4 = SearchParam.newBuilder()
            .withCollectionName(collectionName)
            .withVectorFieldName("title_vector")
            .withVectors(queryVectors)
            .withTopK(5)   
            .withMetricType(MetricType.L2)  
            .withParams("{\"nprobe\":1024}")
            .withConsistencyLevel(ConsistencyLevelEnum.BOUNDED)
            .withOutFields(outputFields)
            .withExpr("claps > 1500 and responses > 15")
            .build();

        R<SearchResults> response4 = client.search(searchParam4);

        SearchResultsWrapper wrapper4 = new SearchResultsWrapper(response4.getData().getResults());

        List<List<JSONObject>> results4 = new ArrayList<>();

        for (int i = 0; i < queryVectors.size(); ++i) {
            List<SearchResultsWrapper.IDScore> scores = wrapper4.getIDScore(i);
            List<String> titles = (List<String>) wrapper4.getFieldData("title", i);
            List<Long> claps_ = (List<Long>) wrapper4.getFieldData("claps", i);
            List<Long> responses_ = (List<Long>) wrapper4.getFieldData("responses", i); 
            List<JSONObject> entities = new ArrayList<>();
            for (int j = 0; j < scores.size(); ++j) {
                SearchResultsWrapper.IDScore score = scores.get(j);
                JSONObject entity = new JSONObject();
                entity.put("id", score.getLongID());
                entity.put("distance", score.getScore());
                entity.put("title", titles.get(j));
                entity.put("claps", claps_.get(j));
                entity.put("responses", responses_.get(j));
                entities.add(entity);
            }

            results4.add(entities);
        } 

        System.out.println(results4);

        // Output:
        // [[
        //     {
        //         "distance": 0.37674016,
        //         "responses": 47,
        //         "id": 445297206350834846,
        //         "title": "Why The Coronavirus Mortality Rate is Misleading",
        //         "claps": 2900
        //     },
        //     {
        //         "distance": 0.6518569,
        //         "responses": 95,
        //         "id": 445297206350833906,
        //         "title": "The Discovery of Aliens Would Be Terrible",
        //         "claps": 4300
        //     },
        //     {
        //         "distance": 0.6772847,
        //         "responses": 212,
        //         "id": 445297206350830599,
        //         "title": "Remote Work Is Not Here to Stay",
        //         "claps": 2600
        //     },
        //     {
        //         "distance": 0.683691,
        //         "responses": 40,
        //         "id": 445297206350833778,
        //         "title": "Apple May Lose the Developer Crowd",
        //         "claps": 1800
        //     },
        //     {
        //         "distance": 0.71434104,
        //         "responses": 45,
        //         "id": 445297206350834004,
        //         "title": "Sorry, Online Courses Won\u2019t Make you a Data Scientist",
        //         "claps": 5000
        //     }
        // ]]





        // -------

        queryVectors.clear();
        queryVectors.add(queryVector1);

        outputFields.clear();
        outputFields.add("title");
        outputFields.add("publication");

        SearchParam searchParam5 = SearchParam.newBuilder()
            .withCollectionName(collectionName)
            .withVectorFieldName("title_vector")
            .withVectors(queryVectors)
            .withTopK(5)   
            .withMetricType(MetricType.L2)  
            .withParams("{\"nprobe\":1024}")
            .withConsistencyLevel(ConsistencyLevelEnum.BOUNDED)
            .withOutFields(outputFields)
            .withExpr("publication == \"Towards Data Science\"")
            .build();

        R<SearchResults> response5 = client.search(searchParam5);

        SearchResultsWrapper wrapper5 = new SearchResultsWrapper(response5.getData().getResults());

        List<List<JSONObject>> results5 = new ArrayList<>();

        for (int i = 0; i < queryVectors.size(); ++i) {
            List<SearchResultsWrapper.IDScore> scores = wrapper5.getIDScore(i);
            List<String> titles = (List<String>) wrapper5.getFieldData("title", i);
            List<String> publications = (List<String>) wrapper5.getFieldData("publication", i); 
            List<JSONObject> entities = new ArrayList<>();
            for (int j = 0; j < scores.size(); ++j) {
                SearchResultsWrapper.IDScore score = scores.get(j);
                JSONObject entity = new JSONObject();
                entity.put("id", score.getLongID());
                entity.put("distance", score.getScore());
                entity.put("title", titles.get(j));
                entity.put("publication", publications.get(j));
                entities.add(entity);
            }

            results5.add(entities);
        } 

        System.out.println(results5);

        // Output:
        // [[
        //     {
        //         "distance": 0.29999837,
        //         "publication": "Towards Data Science",
        //         "id": 445297206350832382,
        //         "title": "Following the Spread of Coronavirus"
        //     },
        //     {
        //         "distance": 0.37674016,
        //         "publication": "Towards Data Science",
        //         "id": 445297206350834846,
        //         "title": "Why The Coronavirus Mortality Rate is Misleading"
        //     },
        //     {
        //         "distance": 0.4360938,
        //         "publication": "Towards Data Science",
        //         "id": 445297206350830143,
        //         "title": "Mortality Rate As an Indicator of an Epidemic Outbreak"
        //     },
        //     {
        //         "distance": 0.45862228,
        //         "publication": "Towards Data Science",
        //         "id": 445297206350834985,
        //         "title": "Heart Disease Risk Assessment Using Machine Learning"
        //     },
        //     {
        //         "distance": 0.46275583,
        //         "publication": "Towards Data Science",
        //         "id": 445297206350832277,
        //         "title": "Can we learn anything from the progression of influenza to analyze the COVID-19 pandemic better?"
        //     }
        // ]]





        // -------

        queryVectors.clear();
        queryVectors.add(queryVector1);

        outputFields.clear();
        outputFields.add("title");
        outputFields.add("publication");

        SearchParam searchParam6 = SearchParam.newBuilder()
            .withCollectionName(collectionName)
            .withVectorFieldName("title_vector")
            .withVectors(queryVectors)
            .withTopK(5)   
            .withMetricType(MetricType.L2)  
            .withParams("{\"nprobe\":1024}")
            .withConsistencyLevel(ConsistencyLevelEnum.BOUNDED)
            .withOutFields(outputFields)
            .withExpr("publication not in [\"Towards Data Science\", \"Personal Growth\"]")
            .build();

        R<SearchResults> response6 = client.search(searchParam6);

        SearchResultsWrapper wrapper6 = new SearchResultsWrapper(response6.getData().getResults());

        List<List<JSONObject>> results6 = new ArrayList<>();

        for (int i = 0; i < queryVectors.size(); ++i) {
            List<SearchResultsWrapper.IDScore> scores = wrapper6.getIDScore(i);
            List<String> titles = (List<String>) wrapper6.getFieldData("title", i);
            List<String> publications = (List<String>) wrapper6.getFieldData("publication", i);
            List<JSONObject> entities = new ArrayList<>();
            for (int j = 0; j < scores.size(); ++j) {
                SearchResultsWrapper.IDScore score = scores.get(j);
                JSONObject entity = new JSONObject();
                entity.put("id", score.getLongID());
                entity.put("distance", score.getScore());
                entity.put("title", titles.get(j));
                entity.put("publication", publications.get(j));
                entities.add(entity);
            }

            results6.add(entities);
        } 

        System.out.println(results6);

        // Output:
        // [[
        //     {
        //         "distance": 0,
        //         "publication": "The Startup",
        //         "id": 445297206350829205,
        //         "title": "The Reported Mortality Rate of Coronavirus Is Not Important"
        //     },
        //     {
        //         "distance": 0.36103836,
        //         "publication": "The Startup",
        //         "id": 445297206350834812,
        //         "title": "The Hidden Side Effect of the Coronavirus"
        //     },
        //     {
        //         "distance": 0.41629803,
        //         "publication": "The Startup",
        //         "id": 445297206350832646,
        //         "title": "Coronavirus shows what ethical Amazon could look like"
        //     },
        //     {
        //         "distance": 0.48886314,
        //         "publication": "The Startup",
        //         "id": 445297206350833480,
        //         "title": "How Can AI Help Fight Coronavirus?"
        //     },
        //     {
        //         "distance": 0.49283177,
        //         "publication": "The Startup",
        //         "id": 445297206350833350,
        //         "title": "Will Coronavirus Impact Freelancers\u2019 Ability to Rent?"
        //     }
        // ]]





        // -------

        queryVectors.clear();
        queryVectors.add(queryVector1);

        outputFields.clear();
        outputFields.add("title");
        outputFields.add("link");

        SearchParam searchParam7 = SearchParam.newBuilder()
            .withCollectionName(collectionName)
            .withVectorFieldName("title_vector")
            .withVectors(queryVectors)
            .withTopK(5)   
            .withMetricType(MetricType.L2)  
            .withParams("{\"nprobe\":1024}")
            .withConsistencyLevel(ConsistencyLevelEnum.BOUNDED)
            .withOutFields(outputFields)
            .withExpr("title like \"Top%\"")
            .build();

        R<SearchResults> response7 = client.search(searchParam7);

        SearchResultsWrapper wrapper7 = new SearchResultsWrapper(response7.getData().getResults());

        List<List<JSONObject>> results7 = new ArrayList<>();

        for (int i = 0; i < queryVectors.size(); ++i) {
            List<SearchResultsWrapper.IDScore> scores = wrapper7.getIDScore(i);
            List<String> titles = (List<String>) wrapper7.getFieldData("title", i);
            List<String> links = (List<String>) wrapper7.getFieldData("link", i);
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

            results7.add(entities);
        } 

        System.out.println(results7);

        // Output:
        // [[
        //     {
        //         "distance": 0.7496654,
        //         "link": "https://towardsdatascience.com/topic-modeling-in-power-bi-using-pycaret-54422b4e36d6",
        //         "id": 445297206350832835,
        //         "title": "Topic Modeling in Power BI using PyCaret"
        //     },
        //     {
        //         "distance": 0.7691308,
        //         "link": "https://towardsdatascience.com/topic-modeling-the-comment-section-from-a-new-york-times-article-e4775261530e",
        //         "id": 445297206350832871,
        //         "title": "Topic Modeling the comment section from a New York Times article"
        //     },
        //     {
        //         "distance": 0.81268203,
        //         "link": "https://medium.com/swlh/top-4-myths-about-app-store-conversion-rate-optimization-cro-c62476901c90",
        //         "id": 445297206350832493,
        //         "title": "Top 4 Myths About App Store Conversion Rate Optimization (CRO)"
        //     },
        //     {
        //         "distance": 0.8307196,
        //         "link": "https://medium.com/swlh/top-vs-code-extensions-for-web-developers-1e038201a8fc",
        //         "id": 445297206350830831,
        //         "title": "Top VS Code extensions for Web Developers"
        //     },
        //     {
        //         "distance": 0.84897053,
        //         "link": "https://medium.com/swlh/top-ten-mistakes-found-while-doing-code-reviews-b935ef44e797",
        //         "id": 445297206350832603,
        //         "title": "Top ten mistakes found while performing code reviews"
        //     }
        // ]]





        // -------

        queryVectors.clear();
        queryVectors.add(queryVector1);

        outputFields.clear();
        outputFields.add("title");
        outputFields.add("publication");
        outputFields.add("claps");
        outputFields.add("responses");
        outputFields.add("reading_time");


        SearchParam searchParam8 = SearchParam.newBuilder()
            .withCollectionName(collectionName)
            .withVectorFieldName("title_vector")
            .withVectors(queryVectors)
            .withTopK(5)   
            .withMetricType(MetricType.L2)  
            .withParams("{\"nprobe\":1024}")
            .withConsistencyLevel(ConsistencyLevelEnum.BOUNDED)
            .withOutFields(outputFields)
            .withExpr("(publication == \"Towards Data Science\") and ((claps > 1500 and responses > 15) or (10 < reading_time < 15))")
            .build();

        R<SearchResults> response8 = client.search(searchParam8);

        SearchResultsWrapper wrapper8 = new SearchResultsWrapper(response8.getData().getResults());

        List<List<JSONObject>> results8 = new ArrayList<>();

        for (int i = 0; i < queryVectors.size(); ++i) {
            List<SearchResultsWrapper.IDScore> scores = wrapper8.getIDScore(i);
            List<String> titles = (List<String>) wrapper8.getFieldData("title", i);
            List<String> publications = (List<String>) wrapper8.getFieldData("publication", i);
            List<Long> readingTimes = (List<Long>) wrapper8.getFieldData("reading_time", i);
            List<Long> claps_ = (List<Long>) wrapper8.getFieldData("claps", i);
            List<Long> responses_ = (List<Long>) wrapper8.getFieldData("responses", i);
            List<JSONObject> entities = new ArrayList<>();
            for (int j = 0; j < scores.size(); ++j) {
                SearchResultsWrapper.IDScore score = scores.get(j);
                JSONObject entity = new JSONObject();
                entity.put("id", score.getLongID());
                entity.put("distance", score.getScore());
                entity.put("title", titles.get(j));
                entity.put("publication", publications.get(j));
                entity.put("reading_time", readingTimes.get(j));
                entity.put("claps", claps_.get(j));
                entity.put("responses", responses_.get(j));
                entities.add(entity);
            }

            results8.add(entities);
        } 

        System.out.println(results8);

        // Output:
        // [[
        //     {
        //         "reading_time": 9,
        //         "distance": 0.37674016,
        //         "publication": "Towards Data Science",
        //         "responses": 47,
        //         "id": 445297206350834846,
        //         "title": "Why The Coronavirus Mortality Rate is Misleading",
        //         "claps": 2900
        //     },
        //     {
        //         "reading_time": 12,
        //         "distance": 0.45862228,
        //         "publication": "Towards Data Science",
        //         "responses": 0,
        //         "id": 445297206350834985,
        //         "title": "Heart Disease Risk Assessment Using Machine Learning",
        //         "claps": 15
        //     },
        //     {
        //         "reading_time": 13,
        //         "distance": 0.5037479,
        //         "publication": "Towards Data Science",
        //         "responses": 3,
        //         "id": 445297206350834708,
        //         "title": "New Data Shows a Lower Covid-19 Fatality Rate",
        //         "claps": 161
        //     },
        //     {
        //         "reading_time": 11,
        //         "distance": 0.52556163,
        //         "publication": "Towards Data Science",
        //         "responses": 1,
        //         "id": 445297206350833536,
        //         "title": "Common Pipenv Errors",
        //         "claps": 20
        //     },
        //     {
        //         "reading_time": 12,
        //         "distance": 0.58774835,
        //         "publication": "Towards Data Science",
        //         "responses": 0,
        //         "id": 445297206350831792,
        //         "title": "Data quality impact on the dataset",
        //         "claps": 61
        //     }
        // ]]





        // -------

        outputFields.clear();
        outputFields.add("title");
        outputFields.add("publication");
        outputFields.add("claps");
        outputFields.add("responses");
        outputFields.add("reading_time");

        QueryParam queryParam = QueryParam.newBuilder()
            .withCollectionName(collectionName)
            .withExpr("(publication == \"Towards Data Science\") and ((claps > 1500 and responses > 15) or (10 < reading_time < 15))")
            .withOutFields(outputFields)
            .withLimit(5L)
            .build();
        
        R<QueryResults> queryResponse = client.query(queryParam);

        QueryResultsWrapper queryResultsWrapper = new QueryResultsWrapper(queryResponse.getData());

        List<List<JSONObject>> queryResults = new ArrayList<>();

        List<Long> ids = (List<Long>) queryResultsWrapper.getFieldWrapper("id").getFieldData();
        List<String> titles = (List<String>) queryResultsWrapper.getFieldWrapper("title").getFieldData();
        List<String> publications = (List<String>) queryResultsWrapper.getFieldWrapper("publication").getFieldData();
        List<Long> readingTimes = (List<Long>) queryResultsWrapper.getFieldWrapper("reading_time").getFieldData();
        List<Long> claps_ = (List<Long>) queryResultsWrapper.getFieldWrapper("claps").getFieldData();
        List<Long> responses_ = (List<Long>) queryResultsWrapper.getFieldWrapper("responses").getFieldData();
        List<JSONObject> entities = new ArrayList<>();
        for (int j = 0; j < ids.size(); ++j) {
            JSONObject entity = new JSONObject();
            entity.put("id", ids.get(j));
            entity.put("title", titles.get(j));
            entity.put("publication", publications.get(j));
            entity.put("reading_time", readingTimes.get(j));
            entity.put("claps", claps_.get(j));
            entity.put("responses", responses_.get(j));
            entities.add(entity);
        }

        queryResults.add(entities);

        System.out.println(queryResults);

        // Output:
        // [[
        //     {
        //         "reading_time": 21,
        //         "publication": "Towards Data Science",
        //         "responses": 18,
        //         "id": 445297206350829274,
        //         "title": "Top 10 In-Demand programming languages to learn in 2020",
        //         "claps": 3000
        //     },
        //     {
        //         "reading_time": 12,
        //         "publication": "Towards Data Science",
        //         "responses": 7,
        //         "id": 445297206350829278,
        //         "title": "Data Cleaning in Python: the Ultimate Guide (2020)",
        //         "claps": 1500
        //     },
        //     {
        //         "reading_time": 11,
        //         "publication": "Towards Data Science",
        //         "responses": 0,
        //         "id": 445297206350829280,
        //         "title": "Top Trends of Graph Machine Learning in 2020",
        //         "claps": 1100
        //     },
        //     {
        //         "reading_time": 12,
        //         "publication": "Towards Data Science",
        //         "responses": 3,
        //         "id": 445297206350829284,
        //         "title": "Rage Quitting Cancer Research",
        //         "claps": 331
        //     },
        //     {
        //         "reading_time": 13,
        //         "publication": "Towards Data Science",
        //         "responses": 0,
        //         "id": 445297206350829285,
        //         "title": "Understanding Natural Language Processing: how AI understands our languages",
        //         "claps": 109
        //     }
        // ]]





        List<Long> gids = new ArrayList<Long>();
        gids.add((Long) queryResultsWrapper.getFieldWrapper("id").getFieldData().get(0));
        gids.add((Long) queryResultsWrapper.getFieldWrapper("id").getFieldData().get(1));

        GetIdsParam getIdsParam = GetIdsParam.newBuilder()
            .withCollectionName(collectionName)
            .withPrimaryIds(gids)
            .withOutputFields(outputFields)
            .build();
        
        R<GetResponse> getResponse = client.get(getIdsParam);

        if (getResponse.getException() != null) {
            System.err.println("Failed to get: " + getResponse.getException().getMessage());
            return;
        }

        List<JSONObject> getResults = new ArrayList<>();

        for (QueryResultsWrapper.RowRecord rowRecord: getResponse.getData().getRowRecords()) {
            JSONObject object = new JSONObject();
            object.put("id", rowRecord.getFieldValues().get("id"));
            object.put("title", rowRecord.getFieldValues().get("title"));
            object.put("publication", rowRecord.getFieldValues().get("publication"));
            object.put("reading_time", rowRecord.getFieldValues().get("reading_time"));
            object.put("claps", rowRecord.getFieldValues().get("claps"));
            object.put("responses", rowRecord.getFieldValues().get("responses"));
            getResults.add(object);
        }

        System.out.println(getResults);

        // Output:
        // [
        //     {
        //         "reading_time": 21,
        //         "publication": "Towards Data Science",
        //         "responses": 18,
        //         "id": 445297206350829274,
        //         "title": "Top 10 In-Demand programming languages to learn in 2020",
        //         "claps": 3000
        //     },
        //     {
        //         "reading_time": 12,
        //         "publication": "Towards Data Science",
        //         "responses": 7,
        //         "id": 445297206350829278,
        //         "title": "Data Cleaning in Python: the Ultimate Guide (2020)",
        //         "claps": 1500
        //     }
        // ]





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