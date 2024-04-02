package com.zilliz.docs;

import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.collection.request.AddFieldReq;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.vector.request.InsertReq;
import io.milvus.v2.service.vector.response.InsertResp;
import io.milvus.v2.service.vector.request.SearchReq;
import io.milvus.v2.service.vector.response.SearchResp;
import io.milvus.v2.service.vector.request.GetReq;
import io.milvus.v2.service.vector.response.GetResp;
import io.milvus.v2.service.vector.request.QueryReq;
import io.milvus.v2.service.vector.response.QueryResp;
import io.milvus.v2.service.vector.request.DeleteReq;
import io.milvus.v2.service.vector.response.DeleteResp;
import io.milvus.v2.service.collection.request.DropCollectionReq;
import io.milvus.grpc.FieldSchema;
import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.common.DataType;
import io.milvus.v2.common.IndexParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import java.util.Arrays;

import com.alibaba.fastjson.JSONObject;

public final class QuickStartDemo {
    private static void run() throws InterruptedException {
        String CLUSTER_ENDPOINT = "YOUR_CLUSTER_ENDPOINT";
        String TOKEN = "YOUR_CLUSTER_TOKEN";

        // 1. Connect to Milvus server
        ConnectConfig connectConfig = ConnectConfig.builder()
            .uri(CLUSTER_ENDPOINT)
            .token(TOKEN)
            .build();

        MilvusClientV2 client = new MilvusClientV2(connectConfig);

        // 2. Create a collection in quick setup mode
        CreateCollectionReq quickSetupReq = CreateCollectionReq.builder()
            .collectionName("quick_setup")
            .dimension(5)
            .build();

        client.createCollection(quickSetupReq);

        Thread.sleep(3000);

        // 3. Create a collection in customized setup mode

        // 3.1 Craete schema
        CreateCollectionReq.CollectionSchema schema = client.createSchema();

        // 3.2 Add fields to schema

        AddFieldReq myId = AddFieldReq.builder()
            .fieldName("my_id")
            .dataType(DataType.Int64)
            .isPrimaryKey(true)
            .autoID(false)
            .build();

        schema.addField(myId);

        AddFieldReq myVector = AddFieldReq.builder()
            .fieldName("my_vector")
            .dataType(DataType.FloatVector)
            .dimension(5)
            .build();

        schema.addField(myVector);

        // 3.3 Prepare index parameters
        IndexParam indexParamForIdField = IndexParam.builder()
            .fieldName("my_id")
            .indexType(IndexParam.IndexType.STL_SORT)
            .build();

        IndexParam indexParamForVectorField = IndexParam.builder()
            .fieldName("my_vector")
            .indexType(IndexParam.IndexType.AUTOINDEX)
            .metricType(IndexParam.MetricType.IP)
            .build();

        List<IndexParam> indexParams = new ArrayList<>();
        indexParams.add(indexParamForIdField);
        indexParams.add(indexParamForVectorField);

        // 3.4 Create a collection with schema and index parameters
        CreateCollectionReq customizedSetupReq = CreateCollectionReq.builder()
            .collectionName("customized_setup")
            .collectionSchema(schema)
            .indexParams(indexParams)
            .build();

        client.createCollection(customizedSetupReq);

        // 4. Insert data into the collection

        // 4.1. Prepare data

        List<JSONObject> insertData = Arrays.asList(
            new JSONObject(Map.of("id", 0L, "vector", Arrays.asList(0.3580376395471989f, -0.6023495712049978f, 0.18414012509913835f, -0.26286205330961354f, 0.9029438446296592f), "color", "pink_8682")),
            new JSONObject(Map.of("id", 1L, "vector", Arrays.asList(0.19886812562848388f, 0.06023560599112088f, 0.6976963061752597f, 0.2614474506242501f, 0.838729485096104f), "color", "red_7025")),
            new JSONObject(Map.of("id", 2L, "vector", Arrays.asList(0.43742130801983836f, -0.5597502546264526f, 0.6457887650909682f, 0.7894058910881185f, 0.20785793220625592f), "color", "orange_6781")),
            new JSONObject(Map.of("id", 3L, "vector", Arrays.asList(0.3172005263489739f, 0.9719044792798428f, -0.36981146090600725f, -0.4860894583077995f, 0.95791889146345f), "color", "pink_9298")),
            new JSONObject(Map.of("id", 4L, "vector", Arrays.asList(0.4452349528804562f, -0.8757026943054742f, 0.8220779437047674f, 0.46406290649483184f, 0.30337481143159106f), "color", "red_4794")),
            new JSONObject(Map.of("id", 5L, "vector", Arrays.asList(0.985825131989184f, -0.8144651566660419f, 0.6299267002202009f, 0.1206906911183383f, -0.1446277761879955f), "color", "yellow_4222")),
            new JSONObject(Map.of("id", 6L, "vector", Arrays.asList(0.8371977790571115f, -0.015764369584852833f, -0.31062937026679327f, -0.562666951622192f, -0.8984947637863987f), "color", "red_9392")),
            new JSONObject(Map.of("id", 7L, "vector", Arrays.asList(-0.33445148015177995f, -0.2567135004164067f, 0.8987539745369246f, 0.9402995886420709f, 0.5378064918413052f), "color", "grey_8510")),
            new JSONObject(Map.of("id", 8L, "vector", Arrays.asList(0.39524717779832685f, 0.4000257286739164f, -0.5890507376891594f, -0.8650502298996872f, -0.6140360785406336f), "color", "white_9381")),
            new JSONObject(Map.of("id", 9L, "vector", Arrays.asList(0.5718280481994695f, 0.24070317428066512f, -0.3737913482606834f, -0.06726932177492717f, -0.6980531615588608f), "color", "purple_4976"))
        );

        // 4.2. Insert data

        InsertReq insertReq = InsertReq.builder()
            .collectionName("quick_setup")
            .data(insertData)
            .build();

        InsertResp res = client.insert(insertReq);

        System.out.println(JSONObject.toJSON(res));

        // Output:
        // {"insertCnt": 10}





        // 5. Insert more data for the sake of search

        // 5.1 Prepare data
        insertData = new ArrayList<>();
        List<String> colors = Arrays.asList("green", "blue", "yellow", "red", "black", "white", "purple", "pink", "orange", "brown", "grey");

        for (int i = 10; i < 1000; i++) {
            Random rand = new Random();
            JSONObject row = new JSONObject();
            row.put("id", Long.valueOf(i));
            row.put("vector", Arrays.asList(rand.nextFloat(), rand.nextFloat(), rand.nextFloat(), rand.nextFloat(), rand.nextFloat()));
            row.put("color", colors.get(rand.nextInt(colors.size()-1)) + '_' + rand.nextInt(1000, 9999));
            insertData.add(row);
        }

        // 5.2 Insert data

        insertReq = InsertReq.builder()
            .collectionName("quick_setup")
            .data(insertData)
            .build();

        res = client.insert(insertReq);

        System.out.println(JSONObject.toJSON(res));

        // Output:
        // {"insertCnt": 990}





        // 6. Search with a single vector

        List<List<Float>> singleVectorSearchData = new ArrayList<>();
        singleVectorSearchData.add(Arrays.asList(0.041732933f, 0.013779674f, -0.027564144f, -0.013061441f, 0.009748648f));

        SearchReq searchReq = SearchReq.builder()
            .collectionName("quick_setup")
            .data(singleVectorSearchData)
            .topK(3)
            .build();

        SearchResp singleVectorSearchRes = client.search(searchReq);

        System.out.println(JSONObject.toJSON(singleVectorSearchRes));

        // Output:
        // {"searchResults": [[
        //     {
        //         "score": 0.05297109,
        //         "fields": {
        //             "vector": [
        //                 0.99489605,
        //                 0.620082,
        //                 0.09068686,
        //                 0.27704495,
        //                 0.92576367
        //             ],
        //             "id": 66
        //         }
        //     },
        //     {
        //         "score": 0.05258018,
        //         "fields": {
        //             "vector": [
        //                 0.9346232,
        //                 0.7711106,
        //                 0.12809038,
        //                 0.16101098,
        //                 0.8805015
        //             ],
        //             "id": 785
        //         }
        //     },
        //     {
        //         "score": 0.05251121,
        //         "fields": {
        //             "vector": [
        //                 0.3172005,
        //                 0.97190446,
        //                 -0.36981148,
        //                 -0.48608947,
        //                 0.9579189
        //             ],
        //             "id": 3
        //         }
        //     }
        // ]]}





        // 7. Search with multiple vectors
        List<List<Float>> multiVectorSearchData = new ArrayList<>();
        multiVectorSearchData.add(Arrays.asList(0.041732933f, 0.013779674f, -0.027564144f, -0.013061441f, 0.009748648f));
        multiVectorSearchData.add(Arrays.asList(0.0039737443f, 0.003020432f, -0.0006188639f, 0.03913546f, -0.00089768134f));

        searchReq = SearchReq.builder()
            .collectionName("quick_setup")
            .data(multiVectorSearchData)
            .topK(3)
            .build();

        SearchResp multiVectorSearchRes = client.search(searchReq);

        System.out.println(JSONObject.toJSON(multiVectorSearchRes));

        // Output:
        // {"searchResults": [
        //     [
        //         {
        //             "score": 0.05297109,
        //             "fields": {
        //                 "vector": [
        //                     0.99489605,
        //                     0.620082,
        //                     0.09068686,
        //                     0.27704495,
        //                     0.92576367
        //                 ],
        //                 "id": 66
        //             }
        //         },
        //         {
        //             "score": 0.05258018,
        //             "fields": {
        //                 "vector": [
        //                     0.9346232,
        //                     0.7711106,
        //                     0.12809038,
        //                     0.16101098,
        //                     0.8805015
        //                 ],
        //                 "id": 785
        //             }
        //         },
        //         {
        //             "score": 0.05251121,
        //             "fields": {
        //                 "vector": [
        //                     0.3172005,
        //                     0.97190446,
        //                     -0.36981148,
        //                     -0.48608947,
        //                     0.9579189
        //                 ],
        //                 "id": 3
        //             }
        //         }
        //     ],
        //     [
        //         {
        //             "score": 0.044951554,
        //             "fields": {
        //                 "vector": [
        //                     0.92594373,
        //                     0.880975,
        //                     0.21442568,
        //                     0.9900633,
        //                     0.0030285716
        //                 ],
        //                 "id": 867
        //             }
        //         },
        //         {
        //             "score": 0.044517424,
        //             "fields": {
        //                 "vector": [
        //                     0.9028428,
        //                     0.7563419,
        //                     0.43204427,
        //                     0.9965389,
        //                     0.09731263
        //                 ],
        //                 "id": 529
        //             }
        //         },
        //         {
        //             "score": 0.04314029,
        //             "fields": {
        //                 "vector": [
        //                     0.8447083,
        //                     0.781666,
        //                     0.58065236,
        //                     0.98519135,
        //                     0.8621161
        //                 ],
        //                 "id": 127
        //             }
        //         }
        //     ]
        // ]}





        // 8. Search with a filter expression using schema-defined fields
        List<List<Float>> filteredVectorSearchData = new ArrayList<>();
        filteredVectorSearchData.add(Arrays.asList(0.041732933f, 0.013779674f, -0.027564144f, -0.013061441f, 0.009748648f));

        searchReq = SearchReq.builder()
            .collectionName("quick_setup")
            .data(filteredVectorSearchData)
            .filter("500 < id < 800")
            .outputFields(Arrays.asList("id"))
            .topK(3)
            .build();

        SearchResp filteredVectorSearchRes = client.search(searchReq);

        System.out.println(JSONObject.toJSON(filteredVectorSearchRes));

        // Output:
        // {"searchResults": [[
        //     {
        //         "score": 0.05258018,
        //         "fields": {"id": 785}
        //     },
        //     {
        //         "score": 0.044485703,
        //         "fields": {"id": 708}
        //     },
        //     {
        //         "score": 0.04229039,
        //         "fields": {"id": 576}
        //     }
        // ]]}





        // 9. Search with a filter expression using custom fields
        List<List<Float>> customFilteredVectorSearchData = new ArrayList<>();
        customFilteredVectorSearchData.add(Arrays.asList(0.041732933f, 0.013779674f, -0.027564144f, -0.013061441f, 0.009748648f));

        searchReq = SearchReq.builder()
            .collectionName("quick_setup")
            .data(customFilteredVectorSearchData)
            .filter("$meta[\"color\"] like \"red%\"")
            .topK(3)
            .outputFields(Arrays.asList("color"))
            .build();

        SearchResp customFilteredVectorSearchRes = client.search(searchReq);

        System.out.println(JSONObject.toJSON(customFilteredVectorSearchRes));

        // Output:
        // {"searchResults": [[
        //     {
        //         "score": 0.04506649,
        //         "fields": {"color": "red_6124"}
        //     },
        //     {
        //         "score": 0.043304324,
        //         "fields": {"color": "red_2464"}
        //     },
        //     {
        //         "score": 0.041873857,
        //         "fields": {"color": "red_9392"}
        //     }
        // ]]}





        // 10. Query with filter using schema-defined fields
        QueryReq queryReq = QueryReq.builder()
            .collectionName("quick_setup")
            .filter("10 < id < 15")
            .outputFields(Arrays.asList("id"))
            .limit(5)
            .build();

        QueryResp queryRes = client.query(queryReq);

        System.out.println(JSONObject.toJSON(queryRes));

        // Output:
        // {"queryResults": [
        //     {"fields": {
        //         "color": "yellow_5750",
        //         "id": 11
        //     }},
        //     {"fields": {
        //         "color": "white_9744",
        //         "id": 12
        //     }},
        //     {"fields": {
        //         "color": "red_3180",
        //         "id": 13
        //     }},
        //     {"fields": {
        //         "color": "white_5496",
        //         "id": 14
        //     }}
        // ]}





        // 11. Query with filter using custom fields
        QueryReq customQueryReq = QueryReq.builder()
            .collectionName("quick_setup")
            .filter("$meta[\"color\"] like \"brown_8%\"")
            .outputFields(Arrays.asList("color"))
            .limit(5)
            .build();

        QueryResp customQueryRes = client.query(customQueryReq);

        System.out.println(JSONObject.toJSON(customQueryRes));

        // Output:
        // {"queryResults": [
        //     {"fields": {
        //         "color": "brown_8488",
        //         "id": 22
        //     }},
        //     {"fields": {
        //         "color": "brown_8356",
        //         "id": 93
        //     }},
        //     {"fields": {
        //         "color": "brown_8612",
        //         "id": 353
        //     }},
        //     {"fields": {
        //         "color": "brown_8217",
        //         "id": 727
        //     }},
        //     {"fields": {
        //         "color": "brown_8142",
        //         "id": 739
        //     }}
        // ]}





        // 12. Get entities by IDs
        GetReq getReq = GetReq.builder()
            .collectionName("quick_setup")
            .ids(Arrays.asList(0L, 1L, 2L))
            .build();

        GetResp getRes = client.get(getReq);

        System.out.println(JSONObject.toJSON(getRes));

        // Output:
        // {"getResults": [
        //     {"fields": {
        //         "vector": [
        //             0.35803765,
        //             -0.6023496,
        //             0.18414013,
        //             -0.26286206,
        //             0.90294385
        //         ],
        //         "id": 0
        //     }},
        //     {"fields": {
        //         "vector": [
        //             0.19886813,
        //             0.060235605,
        //             0.6976963,
        //             0.26144746,
        //             0.8387295
        //         ],
        //         "id": 1
        //     }},
        //     {"fields": {
        //         "vector": [
        //             0.43742132,
        //             -0.55975026,
        //             0.6457888,
        //             0.7894059,
        //             0.20785794
        //         ],
        //         "id": 2
        //     }}
        // ]}





        // 13. Delete entities by IDs
        DeleteReq deleteReq = DeleteReq.builder()
            .collectionName("quick_setup")
            .ids(Arrays.asList(0L, 1L, 2L, 3L, 4L))
            .build();

        DeleteResp deleteRes = client.delete(deleteReq);

        System.out.println(JSONObject.toJSON(deleteRes));

        // Output:
        // {"deleteCnt": 5}





        // 14. Delete entities by filter
        DeleteReq filterDeleteReq = DeleteReq.builder()
            .collectionName("quick_setup")
            .filter("id in [5, 6, 7, 8, 9]")
            .build();

        DeleteResp filterDeleteRes = client.delete(filterDeleteReq);

        System.out.println(JSONObject.toJSON(filterDeleteRes));

        // Output:
        // {"deleteCnt": 5}





        // 15. Drop collections
        DropCollectionReq dropQuickSetupParam = DropCollectionReq.builder()
            .collectionName("quick_setup")
            .build();

        client.dropCollection(dropQuickSetupParam);

        DropCollectionReq dropCustomizedSetupParam = DropCollectionReq.builder()
            .collectionName("customized_setup")
            .build();

        client.dropCollection(dropCustomizedSetupParam);
    }
    public static void main(String[] args) {
        try {
            run();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }        
    }
}