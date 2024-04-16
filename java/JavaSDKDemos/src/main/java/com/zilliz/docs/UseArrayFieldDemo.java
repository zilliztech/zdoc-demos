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
import io.milvus.v2.service.vector.request.QueryReq;
import io.milvus.v2.service.vector.response.InsertResp;
import io.milvus.v2.service.vector.response.QueryResp;

public class UseArrayFieldDemo {
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
        schema.addField(AddFieldReq.builder()
            .fieldName("id")
            .dataType(DataType.Int64)
            .isPrimaryKey(true)
            .autoID(false)
            .build());

        schema.addField(AddFieldReq.builder()
            .fieldName("vector")
            .dataType(DataType.FloatVector)
            .dimension(5)
            .build());
            
        schema.addField(AddFieldReq.builder()
            .fieldName("color")
            .dataType(DataType.VarChar)
            .maxLength(512)
            .build());

        schema.addField(AddFieldReq.builder()
            .fieldName("color_tag")
            .dataType(DataType.Int64)
            .build());

        schema.addField(AddFieldReq.builder()
            .fieldName("color_coord")
            .dataType(DataType.Array)
            .elementType(DataType.Int64)
            .maxCapacity(5)
            .build());

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

        IndexParam indexParamForColorField = IndexParam.builder()
            .fieldName("color")
            .indexType(IndexParam.IndexType.TRIE)
            .build();

        List<IndexParam> indexParams = new ArrayList<>();
        indexParams.add(indexParamForIdField);
        indexParams.add(indexParamForVectorField);
        indexParams.add(indexParamForColorField);

        // 2.4 Create a collection with schema and index parameters
        CreateCollectionReq customizedSetupReq = CreateCollectionReq.builder()
            .collectionName("test_collection")
            .collectionSchema(schema)
            .indexParams(indexParams)         
            .build();

        client.createCollection(customizedSetupReq);

        // 2.5 Check if the collection is loaded
        GetLoadStateReq getLoadStateReq = GetLoadStateReq.builder()
            .collectionName("test_collection")
            .build();

        Boolean isLoaded = client.getLoadState(getLoadStateReq);

        System.out.println(isLoaded);

        // Output:
        // true





        // 3. Insert randomly generated vectors and array data into the collection
        List<String> colors = Arrays.asList("green", "blue", "yellow", "red", "black", "white", "purple", "pink", "orange", "brown", "grey");
        List<JSONObject> data = new ArrayList<>();

        for (int i=0; i<1000; i++) {
            Random rand = new Random();
            String current_color = colors.get(rand.nextInt(colors.size()-1));
            Long current_tag = rand.nextLong(8999L) + 1000L;

            // Generate an random-sized array
            Long capacity = rand.nextLong(5L) + 1L;
            List<Long> current_coord = new ArrayList<>();
            current_coord.add(rand.nextLong(40L) + 1L);
            current_coord.add(rand.nextLong(40L) + 1L);
            for (int j=3; j<capacity; j++) {
                current_coord.add(rand.nextLong(40L) + 1L);
            }

            JSONObject row = new JSONObject();
            row.put("id", Long.valueOf(i));
            row.put("vector", Arrays.asList(rand.nextFloat(), rand.nextFloat(), rand.nextFloat(), rand.nextFloat(), rand.nextFloat()));
            row.put("color", current_color);
            row.put("color_tag", current_tag);
            row.put("color_coord", current_coord);
            data.add(row);
        }

        System.out.println(JSONObject.toJSON(data.get(0)));   

        // Output:
        // {
        //     "color": "white",
        //     "color_tag": 2273,
        //     "vector": [
        //         0.9996968,
        //         0.7590675,
        //         0.76990354,
        //         0.1731013,
        //         0.8714095
        //     ],
        //     "id": 0,
        //     "color_coord": [
        //         36,
        //         29
        //     ]
        // }





        // 3.1 Insert data into the collection
        InsertReq insertReq = InsertReq.builder()
            .collectionName("test_collection")
            .data(data)
            .build();

        InsertResp insertResp = client.insert(insertReq);

        System.out.println(JSONObject.toJSON(insertResp));

        // Output:
        // {"insertCnt": 1000}





        Thread.sleep(5000);   // wait for a while to ensure the data is indexed

        // 4. Basic search with an Array field

        QueryReq queryReq = QueryReq.builder()
            .collectionName("test_collection")
            .filter("color_coord[0] in [7, 8, 9]")
            .outputFields(Arrays.asList("id", "color", "color_tag", "color_coord"))
            .limit(3L)
            .build();

        QueryResp queryResp = client.query(queryReq);

        System.out.println(JSONObject.toJSON(queryResp));

        // Output:
        // {"queryResults": [
        //     {"entity": {
        //         "color": "orange",
        //         "color_tag": 2464,
        //         "id": 18,
        //         "color_coord": [
        //             9,
        //             30
        //         ]
        //     }},
        //     {"entity": {
        //         "color": "pink",
        //         "color_tag": 2602,
        //         "id": 22,
        //         "color_coord": [
        //             8,
        //             34,
        //             16
        //         ]
        //     }},
        //     {"entity": {
        //         "color": "pink",
        //         "color_tag": 1243,
        //         "id": 42,
        //         "color_coord": [
        //             9,
        //             20
        //         ]
        //     }}
        // ]}





        // 5. Advanced search within an Array field
        queryReq = QueryReq.builder()
            .collectionName("test_collection")
            .filter("ARRAY_CONTAINS(color_coord, 10)")
            .outputFields(Arrays.asList("id", "color", "color_tag", "color_coord"))
            .limit(3)
            .build();

        queryResp = client.query(queryReq);

        System.out.println(JSONObject.toJSON(queryResp));

        // Output:
        // {"queryResults": [
        //     {"entity": {
        //         "color": "blue",
        //         "color_tag": 4337,
        //         "id": 17,
        //         "color_coord": [
        //             11,
        //             33,
        //             10,
        //             20
        //         ]
        //     }},
        //     {"entity": {
        //         "color": "white",
        //         "color_tag": 5219,
        //         "id": 25,
        //         "color_coord": [
        //             10,
        //             15
        //         ]
        //     }},
        //     {"entity": {
        //         "color": "red",
        //         "color_tag": 7120,
        //         "id": 35,
        //         "color_coord": [
        //             19,
        //             10,
        //             10,
        //             14
        //         ]
        //     }}
        // ]}





        queryReq = QueryReq.builder()
            .collectionName("test_collection")
            .filter("ARRAY_CONTAINS_ALL(color_coord, [7, 8, 9])")
            .outputFields(Arrays.asList("id", "color", "color_tag", "color_coord"))
            .limit(3)
            .build();

        queryResp = client.query(queryReq);

        System.out.println(JSONObject.toJSON(queryResp));     

        // Output:
        // {"queryResults": [{"entity": {
        //     "color": "red",
        //     "color_tag": 6986,
        //     "id": 423,
        //     "color_coord": [
        //         26,
        //         7,
        //         8,
        //         9
        //     ]
        // }}]}





        
        queryReq = QueryReq.builder()
            .collectionName("test_collection")
            .filter("ARRAY_CONTAINS_ANY(color_coord, [7, 8, 9])")
            .outputFields(Arrays.asList("id", "color", "color_tag", "color_coord"))
            .limit(3)
            .build();

        queryResp = client.query(queryReq);

        System.out.println(JSONObject.toJSON(queryResp));   

        // Output:
        // {"queryResults": [
        //     {"entity": {
        //         "color": "orange",
        //         "color_tag": 2464,
        //         "id": 18,
        //         "color_coord": [
        //             9,
        //             30
        //         ]
        //     }},
        //     {"entity": {
        //         "color": "pink",
        //         "color_tag": 2602,
        //         "id": 22,
        //         "color_coord": [
        //             8,
        //             34,
        //             16
        //         ]
        //     }},
        //     {"entity": {
        //         "color": "pink",
        //         "color_tag": 1243,
        //         "id": 42,
        //         "color_coord": [
        //             9,
        //             20
        //         ]
        //     }}
        // ]}





        queryReq = QueryReq.builder()
            .collectionName("test_collection")
            .filter("ARRAY_LENGTH(color_coord) == 4")
            .outputFields(Arrays.asList("id", "color", "color_tag", "color_coord"))
            .limit(3)
            .build();

        queryResp = client.query(queryReq);

        System.out.println(JSONObject.toJSON(queryResp));   

        // Output:
        // {"queryResults": [
        //     {"entity": {
        //         "color": "green",
        //         "color_tag": 2984,
        //         "id": 2,
        //         "color_coord": [
        //             27,
        //             31,
        //             23,
        //             29
        //         ]
        //     }},
        //     {"entity": {
        //         "color": "black",
        //         "color_tag": 6867,
        //         "id": 4,
        //         "color_coord": [
        //             37,
        //             3,
        //             30,
        //             33
        //         ]
        //     }},
        //     {"entity": {
        //         "color": "brown",
        //         "color_tag": 3464,
        //         "id": 10,
        //         "color_coord": [
        //             31,
        //             38,
        //             21,
        //             28
        //         ]
        //     }}
        // ]}





        
        // 6. Drop the collection
        DropCollectionReq dropCollectionReq = DropCollectionReq.builder()
            .collectionName("test_collection")
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