package com.zilliz.docs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.alibaba.fastjson.JSONObject;

import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.common.DataType;
import io.milvus.v2.common.IndexParam;
import io.milvus.v2.service.collection.request.AddFieldReq;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.collection.request.DropCollectionReq;
import io.milvus.v2.service.collection.request.GetLoadStateReq;
import io.milvus.v2.service.vector.request.InsertReq;
import io.milvus.v2.service.vector.request.SearchReq;
import io.milvus.v2.service.vector.response.InsertResp;
import io.milvus.v2.service.vector.response.SearchResp;

public class EnableDynamicFieldDemo {
    public static void run() throws InterruptedException {
        String CLUSTER_ENDPOINT = "YOUR_CLUSTER_ENDPOINT";

        // 1. Connect to Milvus server
        ConnectConfig connectConfig = ConnectConfig.builder()
            .uri(CLUSTER_ENDPOINT)
            .build();

        MilvusClientV2 client = new MilvusClientV2(connectConfig);

        // 2. Create a collection in customized setup mode

        // 2.1 Create schema
        CreateCollectionReq.CollectionSchema schema = client.createSchema();

        // 2.2 Add fields to schema
        schema.addField(AddFieldReq.builder().fieldName("id").dataType(DataType.Int64).isPrimaryKey(true).autoID(false).build());
        schema.addField(AddFieldReq.builder().fieldName("vector").dataType(DataType.FloatVector).dimension(5).build());

        // 2.3 Prepare index parameters
        IndexParam indexParamForIdField = IndexParam.builder()
            .fieldName("id")
            .indexType(IndexParam.IndexType.STL_SORT)
            .build();

        IndexParam indexParamForVectorField = IndexParam.builder()
            .fieldName("vector")
            .indexType(IndexParam.IndexType.IVF_FLAT)
            .metricType(IndexParam.MetricType.IP)
            .extraParams(Map.of("nlist", 1024))
            .build();

        List<IndexParam> indexParams = new ArrayList<>();
        indexParams.add(indexParamForIdField);
        indexParams.add(indexParamForVectorField);

        // 2.4 Create a collection with schema and index parameters
        CreateCollectionReq customizedSetupReq = CreateCollectionReq.builder()
            .collectionName("customized_setup")
            .collectionSchema(schema)
            .indexParams(indexParams)
            .enableDynamicField(true)
            .build();

        client.createCollection(customizedSetupReq);

        Thread.sleep(5000);

        // 2.5 Get load state of the collection
        GetLoadStateReq customSetupLoadStateReq1 = GetLoadStateReq.builder()
            .collectionName("customized_setup")
            .build();

        boolean res = client.getLoadState(customSetupLoadStateReq1);

        System.out.println(res);

        // Output:
        // true





        // 3. Insert randomly generated vectors

        List<String> colors = Arrays.asList("green", "blue", "yellow", "red", "black", "white", "purple", "pink", "orange", "brown", "grey");
        List<JSONObject> data = new ArrayList<>();

        for (int i=0; i<1000; i++) {
            Random rand = new Random();
            String current_color = colors.get(rand.nextInt(colors.size()-1));
            int current_tag = rand.nextInt(8999) + 1000;
            JSONObject row = new JSONObject();
            row.put("id", Long.valueOf(i));
            row.put("vector", Arrays.asList(rand.nextFloat(), rand.nextFloat(), rand.nextFloat(), rand.nextFloat(), rand.nextFloat()));
            row.put("color", current_color);
            row.put("tag", current_tag);
            row.put("color_tag", current_color + "_" + String.valueOf(rand.nextInt(8999) + 1000));
            data.add(row);
        }

        System.out.println(JSONObject.toJSON(data.get(0)));

        // Output:
        // {
        //     "color": "orange",
        //     "color_tag": "orange_3484",
        //     "vector": [
        //         0.7102922,
        //         0.108297944,
        //         0.51227134,
        //         0.5478929,
        //         0.47800457
        //     ],
        //     "id": 0,
        //     "tag": 8745
        // }




        // 3.1 Insert data into the collection
        InsertReq insertReq = InsertReq.builder()
            .collectionName("customized_setup")
            .data(data)
            .build();

        InsertResp insertResp = client.insert(insertReq);

        System.out.println(JSONObject.toJSON(insertResp));

        // Output:
        // {"insertCnt": 1000}




        Thread.sleep(5000);

        // 4. Search with non-schema-defined fields
        List<List<Float>> queryVectors = Arrays.asList(Arrays.asList(0.3580376395471989f, -0.6023495712049978f, 0.18414012509913835f, -0.26286205330961354f, 0.9029438446296592f));

        SearchReq searchReq = SearchReq.builder()
            .collectionName("customized_setup")
            .data(queryVectors)
            .filter("$meta[\"color\"] in [\"red\", \"green\"]")
            .topK(3)
            .build();

        SearchResp searchResp = client.search(searchReq);

        System.out.println(JSONObject.toJSON(searchResp));

        // Output:
        // {"searchResults": [[
        //     {
        //         "distance": 1.1244214,
        //         "id": 525,
        //         "entity": {}
        //     },
        //     {
        //         "distance": 0.9610535,
        //         "id": 722,
        //         "entity": {}
        //     },
        //     {
        //         "distance": 0.9521862,
        //         "id": 420,
        //         "entity": {}
        //     }
        // ]]}




        // 5. Drop the collection
        DropCollectionReq dropCollectionReq = DropCollectionReq.builder()
            .collectionName("customized_setup")
            .build();

        client.dropCollection(dropCollectionReq);
    }

    public static void main(String[] args) {
        try {
            run();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}