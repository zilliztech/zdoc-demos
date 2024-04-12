package com.zilliz.docs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.alibaba.fastjson.JSONObject;

import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.collection.request.DropCollectionReq;
import io.milvus.v2.service.collection.request.GetLoadStateReq;
import io.milvus.v2.service.partition.request.CreatePartitionReq;
import io.milvus.v2.service.vector.request.GetReq;
import io.milvus.v2.service.vector.request.InsertReq;
import io.milvus.v2.service.vector.request.QueryReq;
import io.milvus.v2.service.vector.response.GetResp;
import io.milvus.v2.service.vector.response.InsertResp;
import io.milvus.v2.service.vector.response.QueryResp;

public class ScalarGetQueryDemo {
    public static void run() throws InterruptedException {
        String CLUSTER_ENDPOINT = "YOUR_CLUSTER_ENDPOINT";

        // 1. Connect to Milvus server
        ConnectConfig connectConfig = ConnectConfig.builder()
            .uri(CLUSTER_ENDPOINT)
            .build();

        MilvusClientV2 client = new MilvusClientV2(connectConfig);  
        
        // 2. Create a collection in quick setup mode
        CreateCollectionReq quickSetupReq = CreateCollectionReq.builder()
            .collectionName("quick_setup")
            .dimension(5)
            .metricType("IP")
            .build();

        client.createCollection(quickSetupReq);

        GetLoadStateReq loadStateReq = GetLoadStateReq.builder()
            .collectionName("quick_setup")
            .build();

        boolean state = client.getLoadState(loadStateReq);

        System.out.println(state);

        // Output:
        // true





        // 3. Insert randomly generated vectors into the collection
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
            row.put("color_tag", current_color + '_' + String.valueOf(rand.nextInt(8999) + 1000));
            data.add(row);
        }

        InsertReq insertReq = InsertReq.builder()
            .collectionName("quick_setup")
            .data(data)
            .build();

        InsertResp insertResp = client.insert(insertReq);

        System.out.println(JSONObject.toJSON(insertResp));

        // Output:
        // {"insertCnt": 1000}





        // 4. Create partitions and insert some more data
        CreatePartitionReq createPartitionReq = CreatePartitionReq.builder()
            .collectionName("quick_setup")
            .partitionName("partitionA")
            .build();

        client.createPartition(createPartitionReq);

        createPartitionReq = CreatePartitionReq.builder()
            .collectionName("quick_setup")
            .partitionName("partitionB")
            .build();

        client.createPartition(createPartitionReq);

        data.clear();

        for (int i=1000; i<1500; i++) {
            Random rand = new Random();
            String current_color = colors.get(rand.nextInt(colors.size()-1));
            int current_tag = rand.nextInt(8999) + 1000;
            JSONObject row = new JSONObject();
            row.put("id", Long.valueOf(i));
            row.put("vector", Arrays.asList(rand.nextFloat(), rand.nextFloat(), rand.nextFloat(), rand.nextFloat(), rand.nextFloat()));
            row.put("color", current_color);
            row.put("tag", current_tag);
            data.add(row);
        }

        insertReq = InsertReq.builder()
            .collectionName("quick_setup")
            .data(data)
            .partitionName("partitionA")
            .build();
        
        insertResp = client.insert(insertReq);

        System.out.println(JSONObject.toJSON(insertResp));

        // Output:
        // {"insertCnt": 500}





        data.clear();

        for (int i=1500; i<2000; i++) {
            Random rand = new Random();
            String current_color = colors.get(rand.nextInt(colors.size()-1));
            int current_tag = rand.nextInt(8999) + 1000;
            JSONObject row = new JSONObject();
            row.put("id", Long.valueOf(i));
            row.put("vector", Arrays.asList(rand.nextFloat(), rand.nextFloat(), rand.nextFloat(), rand.nextFloat(), rand.nextFloat()));
            row.put("color", current_color);
            row.put("tag", current_tag);
            data.add(row);
        }

        insertReq = InsertReq.builder()
            .collectionName("quick_setup")
            .data(data)
            .partitionName("partitionB")
            .build();
        
        insertResp = client.insert(insertReq);

        System.out.println(JSONObject.toJSON(insertResp));

        // Output:
        // {"insertCnt": 500}





        // 5. Get entities by ID
        GetReq getReq = GetReq.builder()
            .collectionName("quick_setup")
            .ids(Arrays.asList(0L, 1L, 2L))
            .build();

        GetResp entities = client.get(getReq);

        System.out.println(JSONObject.toJSON(entities));

        // Output:
        // {"getResults": [
        //     {"entity": {
        //         "color": "white",
        //         "color_tag": "white_4597",
        //         "vector": [
        //             0.09665024,
        //             0.1163497,
        //             0.0701347,
        //             0.32577968,
        //             0.40943468
        //         ],
        //         "tag": 8946,
        //         "id": 0
        //     }},
        //     {"entity": {
        //         "color": "green",
        //         "color_tag": "green_3039",
        //         "vector": [
        //             0.90689456,
        //             0.4377399,
        //             0.75387514,
        //             0.36454988,
        //             0.8702918
        //         ],
        //         "tag": 2341,
        //         "id": 1
        //     }},
        //     {"entity": {
        //         "color": "white",
        //         "color_tag": "white_8708",
        //         "vector": [
        //             0.9757728,
        //             0.13974023,
        //             0.8023141,
        //             0.61947155,
        //             0.8290197
        //         ],
        //         "tag": 9913,
        //         "id": 2
        //     }}
        // ]}





        // 5. Get entities by ID in a partition
        getReq = GetReq.builder()
            .collectionName("quick_setup")
            .ids(Arrays.asList(1001L, 1002L, 1003L))
            .partitionName("partitionA")
            .build();

        entities = client.get(getReq);

        System.out.println(JSONObject.toJSON(entities));

        // Output:
        // {"getResults": [
        //     {"entity": {
        //         "color": "yellow",
        //         "vector": [
        //             0.4300114,
        //             0.599917,
        //             0.799163,
        //             0.75395125,
        //             0.89947814
        //         ],
        //         "id": 1001,
        //         "tag": 5803
        //     }},
        //     {"entity": {
        //         "color": "blue",
        //         "vector": [
        //             0.009218454,
        //             0.64637834,
        //             0.19815737,
        //             0.30519038,
        //             0.8218663
        //         ],
        //         "id": 1002,
        //         "tag": 7212
        //     }},
        //     {"entity": {
        //         "color": "black",
        //         "vector": [
        //             0.76521933,
        //             0.7818409,
        //             0.16976339,
        //             0.8719652,
        //             0.1434964
        //         ],
        //         "id": 1003,
        //         "tag": 1710
        //     }}
        // ]}





        // 6. Use basic operators

        QueryReq queryReq = QueryReq.builder()
            .collectionName("quick_setup")
            .filter("1000 < tag < 1500")
            .outputFields(Arrays.asList("color_tag"))
            .limit(3)
            .build();

        QueryResp queryResp = client.query(queryReq);

        System.out.println(JSONObject.toJSON(queryResp));

        // Output:
        // {"queryResults": [
        //     {"entity": {
        //         "color_tag": "white_7588",
        //         "id": 34
        //     }},
        //     {"entity": {
        //         "color_tag": "orange_4989",
        //         "id": 64
        //     }},
        //     {"entity": {
        //         "color_tag": "white_3415",
        //         "id": 73
        //     }}
        // ]}





        queryReq = QueryReq.builder()
            .collectionName("quick_setup")
            .filter("color == \"brown\"")
            .outputFields(Arrays.asList("color_tag"))
            .limit(3)
            .build();

        queryResp = client.query(queryReq);

        System.out.println(JSONObject.toJSON(queryResp));

        // Output:
        // {"queryResults": [
        //     {"entity": {
        //         "color_tag": "brown_7792",
        //         "id": 3
        //     }},
        //     {"entity": {
        //         "color_tag": "brown_9695",
        //         "id": 7
        //     }},
        //     {"entity": {
        //         "color_tag": "brown_2551",
        //         "id": 15
        //     }}
        // ]}





        queryReq = QueryReq.builder()
            .collectionName("quick_setup")
            .filter("color not in [\"green\", \"purple\"]")
            .outputFields(Arrays.asList("color_tag"))
            .limit(3)
            .build();

        queryResp = client.query(queryReq);

        System.out.println(JSONObject.toJSON(queryResp));   

        // Output:
        // {"queryResults": [
        //     {"entity": {
        //         "color_tag": "white_4597",
        //         "id": 0
        //     }},
        //     {"entity": {
        //         "color_tag": "white_8708",
        //         "id": 2
        //     }},
        //     {"entity": {
        //         "color_tag": "brown_7792",
        //         "id": 3
        //     }}
        // ]}





        queryReq = QueryReq.builder()
            .collectionName("quick_setup")
            .filter("color_tag like \"red%\"")
            .outputFields(Arrays.asList("color_tag"))
            .limit(3)
            .build();

        queryResp = client.query(queryReq);

        System.out.println(JSONObject.toJSON(queryResp));  

        // Output:
        // {"queryResults": [
        //     {"entity": {
        //         "color_tag": "red_4929",
        //         "id": 9
        //     }},
        //     {"entity": {
        //         "color_tag": "red_8284",
        //         "id": 13
        //     }},
        //     {"entity": {
        //         "color_tag": "red_3021",
        //         "id": 44
        //     }}
        // ]}





        queryReq = QueryReq.builder()
            .collectionName("quick_setup")
            .filter("(color == \"red\") and (1000 < tag < 1500)")
            .outputFields(Arrays.asList("color_tag"))
            .limit(3)
            .build();

        queryResp = client.query(queryReq);

        System.out.println(JSONObject.toJSON(queryResp));  

        // Output:
        // {"queryResults": [
        //     {"entity": {
        //         "color_tag": "red_8124",
        //         "id": 83
        //     }},
        //     {"entity": {
        //         "color_tag": "red_5358",
        //         "id": 501
        //     }},
        //     {"entity": {
        //         "color_tag": "red_3564",
        //         "id": 638
        //     }}
        // ]}





        // 7. Use advanced operators
        // Count the total number of entities in the collection
        queryReq = QueryReq.builder()
            .collectionName("quick_setup")
            .filter("")
            .outputFields(Arrays.asList("count(*)"))
            .build();

        queryResp = client.query(queryReq);

        System.out.println(JSONObject.toJSON(queryResp));

        // Output:
        // {"queryResults": [{"entity": {"count(*)": 2000}}]}





        
        // Count the number of entities in a partition
        queryReq = QueryReq.builder()
            .collectionName("quick_setup")
            .partitionNames(Arrays.asList("partitionA"))
            .filter("")
            .outputFields(Arrays.asList("count(*)"))
            .build();

        queryResp = client.query(queryReq);

        System.out.println(JSONObject.toJSON(queryResp));

        // Output:
        // {"queryResults": [{"entity": {"count(*)": 500}}]}





        // Count the number of entities that match a specific filter
        queryReq = QueryReq.builder()
            .collectionName("quick_setup")
            .filter("(color == \"red\") and (1000 < tag < 1500)")
            .outputFields(Arrays.asList("count(*)"))
            .build();

        queryResp = client.query(queryReq);

        System.out.println(JSONObject.toJSON(queryResp));

        // Output:
        // {"queryResults": [{"entity": {"count(*)": 7}}]}





        // 8. Drop the collection
        DropCollectionReq dropCollectionReq = DropCollectionReq.builder()
            .collectionName("quick_setup")
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