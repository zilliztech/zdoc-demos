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
            List<Long> current_coord = Arrays.asList(rand.nextLong(40L), rand.nextLong(40L), rand.nextLong(40L), rand.nextLong(40L), rand.nextLong(40L));
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
        //     "color": "green",
        //     "color_tag": 2301,
        //     "vector": [
        //         0.7508706,
        //         0.2748196,
        //         0.9617409,
        //         0.1634661,
        //         0.96298915
        //     ],
        //     "id": 0,
        //     "color_coord": [
        //         1,
        //         5,
        //         11,
        //         35,
        //         2
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
        List<List<Float>> query_vectors = Arrays.asList(Arrays.asList(0.3580376395471989f, -0.6023495712049978f, 0.18414012509913835f, -0.26286205330961354f, 0.9029438446296592f));

        SearchReq searchReq = SearchReq.builder()
            .collectionName("test_collection")
            .data(query_vectors)
            .filter("color_coord[0] in [7, 8, 9]")
            .outputFields(Arrays.asList("id", "color", "color_tag", "color_coord"))
            .topK(3)
            .build();

        SearchResp searchResp = client.search(searchReq);

        System.out.println(JSONObject.toJSON(searchResp));

        // Output:
        // {"searchResults": [[
        //     {
        //         "distance": 1.0286106,
        //         "id": 899,
        //         "entity": {
        //             "color": "blue",
        //             "color_tag": 3973,
        //             "id": 899,
        //             "color_coord": [
        //                 7,
        //                 23,
        //                 24,
        //                 2,
        //                 23
        //             ]
        //         }
        //     },
        //     {
        //         "distance": 0.9756197,
        //         "id": 822,
        //         "entity": {
        //             "color": "brown",
        //             "color_tag": 9303,
        //             "id": 822,
        //             "color_coord": [
        //                 9,
        //                 18,
        //                 25,
        //                 17,
        //                 29
        //             ]
        //         }
        //     },
        //     {
        //         "distance": 0.9494407,
        //         "id": 631,
        //         "entity": {
        //             "color": "orange",
        //             "color_tag": 9954,
        //             "id": 631,
        //             "color_coord": [
        //                 7,
        //                 9,
        //                 15,
        //                 30,
        //                 5
        //             ]
        //         }
        //     }
        // ]]}





        // 5. Advanced search within an Array field
        searchReq = SearchReq.builder()
            .collectionName("test_collection")
            .data(query_vectors)
            .filter("ARRAY_CONTAINS(color_coord, 10)")
            .outputFields(Arrays.asList("id", "color", "color_tag", "color_coord"))
            .topK(3)
            .build();

        searchResp = client.search(searchReq);

        System.out.println(JSONObject.toJSON(searchResp));

        // Output:
        // {"searchResults": [[
        //     {
        //         "distance": 0.8984557,
        //         "id": 780,
        //         "entity": {
        //             "color": "blue",
        //             "color_tag": 3080,
        //             "id": 780,
        //             "color_coord": [
        //                 9,
        //                 7,
        //                 10,
        //                 31,
        //                 1
        //             ]
        //         }
        //     },
        //     {
        //         "distance": 0.8786885,
        //         "id": 590,
        //         "entity": {
        //             "color": "yellow",
        //             "color_tag": 6692,
        //             "id": 590,
        //             "color_coord": [
        //                 10,
        //                 11,
        //                 8,
        //                 29,
        //                 35
        //             ]
        //         }
        //     },
        //     {
        //         "distance": 0.8543533,
        //         "id": 246,
        //         "entity": {
        //             "color": "orange",
        //             "color_tag": 1649,
        //             "id": 246,
        //             "color_coord": [
        //                 12,
        //                 19,
        //                 9,
        //                 20,
        //                 10
        //             ]
        //         }
        //     }
        // ]]}





        searchReq = SearchReq.builder()
            .collectionName("test_collection")
            .data(query_vectors)
            .filter("ARRAY_CONTAINS_ALL(color_coord, [7, 8, 9])")
            .outputFields(Arrays.asList("id", "color"))
            .topK(3)
            .build();

        searchResp = client.search(searchReq);

        System.out.println(JSONObject.toJSON(searchResp));     

        // Output:
        // {"searchResults": [[]]}





        
        searchReq = SearchReq.builder()
            .collectionName("test_collection")
            .data(query_vectors)
            .filter("ARRAY_CONTAINS_ANY(color_coord, [7, 8, 9])")
            .outputFields(Arrays.asList("id", "color"))
            .topK(3)
            .build();

        searchResp = client.search(searchReq);

        System.out.println(JSONObject.toJSON(searchResp));   

        // Output:
        // {"searchResults": [[
        //     {
        //         "distance": 1.1452067,
        //         "id": 67,
        //         "entity": {
        //             "color": "purple",
        //             "id": 67
        //         }
        //     },
        //     {
        //         "distance": 1.13125,
        //         "id": 24,
        //         "entity": {
        //             "color": "red",
        //             "id": 24
        //         }
        //     },
        //     {
        //         "distance": 1.050978,
        //         "id": 170,
        //         "entity": {
        //             "color": "white",
        //             "id": 170
        //         }
        //     }
        // ]]}





        
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