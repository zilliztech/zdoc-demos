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

public class UseJsonFieldDemo {
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
            .dataType(DataType.JSON)
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

        List<IndexParam> indexParams = new ArrayList<>();
        indexParams.add(indexParamForIdField);
        indexParams.add(indexParamForVectorField);

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





        // 3. Insert randomly generated vectors and JSON data into the collection
        List<String> colors = Arrays.asList("green", "blue", "yellow", "red", "black", "white", "purple", "pink", "orange", "brown", "grey");
        List<JSONObject> data = new ArrayList<>();

        for (int i=0; i<1000; i++) {
            Random rand = new Random();
            String current_color = colors.get(rand.nextInt(colors.size()-1));
            Integer current_tag = rand.nextInt(8999) + 1000;
            List<Integer> current_coord = Arrays.asList(rand.nextInt(40), rand.nextInt(40), rand.nextInt(40));
            List<List<String>> current_ref = Arrays.asList(
                Arrays.asList(colors.get(rand.nextInt(colors.size()-1)), colors.get(rand.nextInt(colors.size()-1)), colors.get(rand.nextInt(colors.size()-1))),
                Arrays.asList(colors.get(rand.nextInt(colors.size()-1)), colors.get(rand.nextInt(colors.size()-1)), colors.get(rand.nextInt(colors.size()-1))),
                Arrays.asList(colors.get(rand.nextInt(colors.size()-1)), colors.get(rand.nextInt(colors.size()-1)), colors.get(rand.nextInt(colors.size()-1)))
            );
            JSONObject row = new JSONObject();
            row.put("id", Long.valueOf(i));
            row.put("vector", Arrays.asList(rand.nextFloat(), rand.nextFloat(), rand.nextFloat(), rand.nextFloat(), rand.nextFloat()));
            JSONObject color = new JSONObject();
            color.put("label", current_color);
            color.put("tag", current_tag);
            color.put("coord", current_coord);
            color.put("ref", current_ref);
            row.put("color", color);
            data.add(row);
        }
        
        System.out.println(JSONObject.toJSON(data.get(0)));   

        // Output:
        // {
        //     "color": {
        //         "coord": [
        //             38,
        //             16,
        //             3
        //         ],
        //         "ref": [
        //             [
        //                 "brown",
        //                 "red",
        //                 "white"
        //             ],
        //             [
        //                 "brown",
        //                 "green",
        //                 "white"
        //             ],
        //             [
        //                 "brown",
        //                 "orange",
        //                 "yellow"
        //             ]
        //         ],
        //         "label": "white",
        //         "tag": 2656
        //     },
        //     "vector": [
        //         0.067774534,
        //         0.0116096735,
        //         0.64342415,
        //         0.8506761,
        //         0.85658294
        //     ],
        //     "id": 0
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

        // 4. Basic search with a JSON field
        List<List<Float>> query_vectors = Arrays.asList(Arrays.asList(0.3580376395471989f, -0.6023495712049978f, 0.18414012509913835f, -0.26286205330961354f, 0.9029438446296592f));

        SearchReq searchReq = SearchReq.builder()
            .collectionName("test_collection")
            .data(query_vectors)
            .filter("color[\"label\"] in [\"red\"]")
            .outputFields(Arrays.asList("id", "color"))
            .topK(3)
            .build();

        SearchResp searchResp = client.search(searchReq);

        System.out.println(JSONObject.toJSON(searchResp));

        // Output:
        // {"searchResults": [[
        //     {
        //         "distance": 1.2636482,
        //         "id": 290,
        //         "entity": {
        //             "color": {
        //                 "coord": [
        //                     32,
        //                     37,
        //                     32
        //                 ],
        //                 "ref": [
        //                     [
        //                         "green",
        //                         "blue",
        //                         "yellow"
        //                     ],
        //                     [
        //                         "yellow",
        //                         "pink",
        //                         "pink"
        //                     ],
        //                     [
        //                         "purple",
        //                         "red",
        //                         "brown"
        //                     ]
        //                 ],
        //                 "label": "red",
        //                 "tag": 8949
        //             },
        //             "id": 290
        //         }
        //     },
        //     {
        //         "distance": 1.002122,
        //         "id": 629,
        //         "entity": {
        //             "color": {
        //                 "coord": [
        //                     23,
        //                     5,
        //                     35
        //                 ],
        //                 "ref": [
        //                     [
        //                         "black",
        //                         "yellow",
        //                         "black"
        //                     ],
        //                     [
        //                         "black",
        //                         "purple",
        //                         "white"
        //                     ],
        //                     [
        //                         "black",
        //                         "brown",
        //                         "orange"
        //                     ]
        //                 ],
        //                 "label": "red",
        //                 "tag": 5072
        //             },
        //             "id": 629
        //         }
        //     },
        //     {
        //         "distance": 0.9542817,
        //         "id": 279,
        //         "entity": {
        //             "color": {
        //                 "coord": [
        //                     20,
        //                     33,
        //                     33
        //                 ],
        //                 "ref": [
        //                     [
        //                         "yellow",
        //                         "white",
        //                         "brown"
        //                     ],
        //                     [
        //                         "black",
        //                         "white",
        //                         "purple"
        //                     ],
        //                     [
        //                         "green",
        //                         "brown",
        //                         "blue"
        //                     ]
        //                 ],
        //                 "label": "red",
        //                 "tag": 4704
        //             },
        //             "id": 279
        //         }
        //     }
        // ]]}





        // 5. Advanced search within a JSON field
        searchReq = SearchReq.builder()
            .collectionName("test_collection")
            .data(query_vectors)
            .filter("JSON_CONTAINS(color[\"ref\"], [\"purple\", \"pink\", \"orange\"])")
            .outputFields(Arrays.asList("id", "color"))
            .topK(3)
            .build();

        searchResp = client.search(searchReq);

        System.out.println(JSONObject.toJSON(searchResp));

        // Output:
        // {"searchResults": [[
        //     {
        //         "distance": 1.1811467,
        //         "id": 180,
        //         "entity": {
        //             "color": {
        //                 "coord": [
        //                     17,
        //                     26,
        //                     14
        //                 ],
        //                 "ref": [
        //                     [
        //                         "white",
        //                         "black",
        //                         "brown"
        //                     ],
        //                     [
        //                         "purple",
        //                         "pink",
        //                         "orange"
        //                     ],
        //                     [
        //                         "black",
        //                         "pink",
        //                         "red"
        //                     ]
        //                 ],
        //                 "label": "green",
        //                 "tag": 2470
        //             },
        //             "id": 180
        //         }
        //     },
        //     {
        //         "distance": 0.6487204,
        //         "id": 331,
        //         "entity": {
        //             "color": {
        //                 "coord": [
        //                     16,
        //                     32,
        //                     23
        //                 ],
        //                 "ref": [
        //                     [
        //                         "purple",
        //                         "pink",
        //                         "orange"
        //                     ],
        //                     [
        //                         "brown",
        //                         "red",
        //                         "orange"
        //                     ],
        //                     [
        //                         "red",
        //                         "yellow",
        //                         "brown"
        //                     ]
        //                 ],
        //                 "label": "white",
        //                 "tag": 1236
        //             },
        //             "id": 331
        //         }
        //     },
        //     {
        //         "distance": 0.59387654,
        //         "id": 483,
        //         "entity": {
        //             "color": {
        //                 "coord": [
        //                     8,
        //                     33,
        //                     2
        //                 ],
        //                 "ref": [
        //                     [
        //                         "red",
        //                         "orange",
        //                         "brown"
        //                     ],
        //                     [
        //                         "purple",
        //                         "pink",
        //                         "orange"
        //                     ],
        //                     [
        //                         "brown",
        //                         "blue",
        //                         "green"
        //                     ]
        //                 ],
        //                 "label": "pink",
        //                 "tag": 5686
        //             },
        //             "id": 483
        //         }
        //     }
        // ]]}





        searchReq = SearchReq.builder()
            .collectionName("test_collection")
            .data(query_vectors)
            .filter("JSON_CONTAINS_ALL(color[\"coord\"], [4, 5])")
            .outputFields(Arrays.asList("id", "color"))
            .topK(3)
            .build();

        searchResp = client.search(searchReq);

        System.out.println(JSONObject.toJSON(searchResp));     

        // Output:
        // {"searchResults": [[
        //     {
        //         "distance": 0.77485126,
        //         "id": 304,
        //         "entity": {
        //             "color": {
        //                 "coord": [
        //                     4,
        //                     5,
        //                     13
        //                 ],
        //                 "ref": [
        //                     [
        //                         "purple",
        //                         "pink",
        //                         "brown"
        //                     ],
        //                     [
        //                         "orange",
        //                         "red",
        //                         "blue"
        //                     ],
        //                     [
        //                         "yellow",
        //                         "blue",
        //                         "purple"
        //                     ]
        //                 ],
        //                 "label": "blue",
        //                 "tag": 7228
        //             },
        //             "id": 304
        //         }
        //     },
        //     {
        //         "distance": 0.68138736,
        //         "id": 253,
        //         "entity": {
        //             "color": {
        //                 "coord": [
        //                     5,
        //                     38,
        //                     4
        //                 ],
        //                 "ref": [
        //                     [
        //                         "black",
        //                         "pink",
        //                         "blue"
        //                     ],
        //                     [
        //                         "pink",
        //                         "brown",
        //                         "pink"
        //                     ],
        //                     [
        //                         "red",
        //                         "pink",
        //                         "orange"
        //                     ]
        //                 ],
        //                 "label": "blue",
        //                 "tag": 6935
        //             },
        //             "id": 253
        //         }
        //     },
        //     {
        //         "distance": 0.56997097,
        //         "id": 944,
        //         "entity": {
        //             "color": {
        //                 "coord": [
        //                     5,
        //                     6,
        //                     4
        //                 ],
        //                 "ref": [
        //                     [
        //                         "blue",
        //                         "yellow",
        //                         "orange"
        //                     ],
        //                     [
        //                         "orange",
        //                         "white",
        //                         "orange"
        //                     ],
        //                     [
        //                         "pink",
        //                         "brown",
        //                         "white"
        //                     ]
        //                 ],
        //                 "label": "pink",
        //                 "tag": 3325
        //             },
        //             "id": 944
        //         }
        //     }
        // ]]}





        
        searchReq = SearchReq.builder()
            .collectionName("test_collection")
            .data(query_vectors)
            .filter("JSON_CONTAINS_ANY(color[\"coord\"], [4, 5])")
            .outputFields(Arrays.asList("id", "color"))
            .topK(3)
            .build();

        searchResp = client.search(searchReq);

        System.out.println(JSONObject.toJSON(searchResp));   

        // Output:
        // {"searchResults": [[
        //     {
        //         "distance": 1.002122,
        //         "id": 629,
        //         "entity": {
        //             "color": {
        //                 "coord": [
        //                     23,
        //                     5,
        //                     35
        //                 ],
        //                 "ref": [
        //                     [
        //                         "black",
        //                         "yellow",
        //                         "black"
        //                     ],
        //                     [
        //                         "black",
        //                         "purple",
        //                         "white"
        //                     ],
        //                     [
        //                         "black",
        //                         "brown",
        //                         "orange"
        //                     ]
        //                 ],
        //                 "label": "red",
        //                 "tag": 5072
        //             },
        //             "id": 629
        //         }
        //     },
        //     {
        //         "distance": 0.85788506,
        //         "id": 108,
        //         "entity": {
        //             "color": {
        //                 "coord": [
        //                     25,
        //                     5,
        //                     38
        //                 ],
        //                 "ref": [
        //                     [
        //                         "green",
        //                         "brown",
        //                         "pink"
        //                     ],
        //                     [
        //                         "purple",
        //                         "green",
        //                         "green"
        //                     ],
        //                     [
        //                         "green",
        //                         "pink",
        //                         "black"
        //                     ]
        //                 ],
        //                 "label": "orange",
        //                 "tag": 8982
        //             },
        //             "id": 108
        //         }
        //     },
        //     {
        //         "distance": 0.80550396,
        //         "id": 120,
        //         "entity": {
        //             "color": {
        //                 "coord": [
        //                     25,
        //                     16,
        //                     4
        //                 ],
        //                 "ref": [
        //                     [
        //                         "red",
        //                         "green",
        //                         "orange"
        //                     ],
        //                     [
        //                         "blue",
        //                         "pink",
        //                         "blue"
        //                     ],
        //                     [
        //                         "brown",
        //                         "black",
        //                         "green"
        //                     ]
        //                 ],
        //                 "label": "purple",
        //                 "tag": 6711
        //             },
        //             "id": 120
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