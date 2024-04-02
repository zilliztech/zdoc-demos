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
        //     {"fields": {
        //         "vector": [
        //             0.95507205,
        //             0.61584574,
        //             0.95414686,
        //             0.71491015,
        //             0.49362534
        //         ],
        //         "id": 0
        //     }},
        //     {"fields": {
        //         "vector": [
        //             0.6985752,
        //             0.23070127,
        //             0.8181768,
        //             0.086550236,
        //             0.8761731
        //         ],
        //         "id": 1
        //     }},
        //     {"fields": {
        //         "vector": [
        //             0.09116334,
        //             0.48260283,
        //             0.7720485,
        //             0.06723672,
        //             0.6035099
        //         ],
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
        //     {"fields": {
        //         "vector": [
        //             0.26971114,
        //             0.8162539,
        //             0.24346673,
        //             0.9125845,
        //             0.29279858
        //         ],
        //         "id": 1001
        //     }},
        //     {"fields": {
        //         "vector": [
        //             0.61355865,
        //             0.80650723,
        //             0.5405893,
        //             0.2860809,
        //             0.6199198
        //         ],
        //         "id": 1002
        //     }},
        //     {"fields": {
        //         "vector": [
        //             0.0035357475,
        //             0.60450536,
        //             0.45755625,
        //             0.5474898,
        //             0.09484583
        //         ],
        //         "id": 1003
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
        //     {"fields": {
        //         "color_tag": "white_7191",
        //         "id": 2
        //     }},
        //     {"fields": {
        //         "color_tag": "orange_1697",
        //         "id": 23
        //     }},
        //     {"fields": {
        //         "color_tag": "yellow_9110",
        //         "id": 72
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
        //     {"fields": {
        //         "color_tag": "brown_6764",
        //         "id": 10
        //     }},
        //     {"fields": {
        //         "color_tag": "brown_4123",
        //         "id": 12
        //     }},
        //     {"fields": {
        //         "color_tag": "brown_1103",
        //         "id": 21
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
        //     {"fields": {
        //         "color_tag": "red_2945",
        //         "id": 1
        //     }},
        //     {"fields": {
        //         "color_tag": "white_7191",
        //         "id": 2
        //     }},
        //     {"fields": {
        //         "color_tag": "yellow_3849",
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
        //     {"fields": {
        //         "color_tag": "red_2945",
        //         "id": 1
        //     }},
        //     {"fields": {
        //         "color_tag": "red_4602",
        //         "id": 17
        //     }},
        //     {"fields": {
        //         "color_tag": "red_4854",
        //         "id": 18
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
        //     {"fields": {
        //         "color_tag": "red_1390",
        //         "id": 162
        //     }},
        //     {"fields": {
        //         "color_tag": "red_5429",
        //         "id": 912
        //     }},
        //     {"fields": {
        //         "color_tag": "red_7540",
        //         "id": 990
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
        // {"queryResults": [{"fields": {"count(*)": 2000}}]}



        
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
        // {"queryResults": [{"fields": {"count(*)": 500}}]}




        // Count the number of entities that match a specific filter
        queryReq = QueryReq.builder()
            .collectionName("quick_setup")
            .filter("(color == \"red\") and (1000 < tag < 1500)")
            .outputFields(Arrays.asList("count(*)"))
            .build();

        queryResp = client.query(queryReq);

        System.out.println(JSONObject.toJSON(queryResp));

        // Output:
        // {"queryResults": [{"fields": {"count(*)": 9}}]}




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