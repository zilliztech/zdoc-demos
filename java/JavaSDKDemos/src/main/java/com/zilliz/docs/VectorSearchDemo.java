package com.zilliz.docs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.alibaba.fastjson.JSONObject;

import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.collection.request.DropCollectionReq;
import io.milvus.v2.service.collection.request.GetLoadStateReq;
import io.milvus.v2.service.partition.request.CreatePartitionReq;
import io.milvus.v2.service.vector.request.InsertReq;
import io.milvus.v2.service.vector.request.SearchReq;
import io.milvus.v2.service.vector.response.InsertResp;
import io.milvus.v2.service.vector.response.SearchResp;

public class VectorSearchDemo {
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
            JSONObject row = new JSONObject();
            row.put("id", Long.valueOf(i));
            row.put("vector", Arrays.asList(rand.nextFloat(), rand.nextFloat(), rand.nextFloat(), rand.nextFloat(), rand.nextFloat()));
            row.put("color_tag", current_color + "_" + String.valueOf(rand.nextInt(8999) + 1000));
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





        // 4. Single vector search
        List<List<Float>> query_vectors = Arrays.asList(Arrays.asList(0.3580376395471989f, -0.6023495712049978f, 0.18414012509913835f, -0.26286205330961354f, 0.9029438446296592f));

        SearchReq searchReq = SearchReq.builder()
            .collectionName("quick_setup")
            .data(query_vectors)
            .topK(3)
            .build();

        SearchResp searchResp = client.search(searchReq);

        System.out.println(JSONObject.toJSON(searchResp));

        // Output:
        // {"searchResults": [[
        //     {
        //         "score": 1.263043,
        //         "fields": {
        //             "vector": [
        //                 0.9533119,
        //                 0.02538395,
        //                 0.76714665,
        //                 0.35481733,
        //                 0.9845762
        //             ],
        //             "id": 740
        //         }
        //     },
        //     {
        //         "score": 1.2377806,
        //         "fields": {
        //             "vector": [
        //                 0.7411156,
        //                 0.08687937,
        //                 0.8254139,
        //                 0.08370924,
        //                 0.99095553
        //             ],
        //             "id": 640
        //         }
        //     },
        //     {
        //         "score": 1.1869997,
        //         "fields": {
        //             "vector": [
        //                 0.87928146,
        //                 0.05324632,
        //                 0.6312755,
        //                 0.28005534,
        //                 0.9542448
        //             ],
        //             "id": 455
        //         }
        //     }
        // ]]}





        // 5. Batch vector search
        query_vectors = Arrays.asList(
            Arrays.asList(0.3580376395471989f, -0.6023495712049978f, 0.18414012509913835f, -0.26286205330961354f, 0.9029438446296592f),
            Arrays.asList(0.19886812562848388f, 0.06023560599112088f, 0.6976963061752597f, 0.2614474506242501f, 0.838729485096104f)
        );

        searchReq = SearchReq.builder()
            .collectionName("quick_setup")
            .data(query_vectors)
            .topK(3)
            .build();

        searchResp = client.search(searchReq);

        System.out.println(JSONObject.toJSON(searchResp));

        // Output:
        // {"searchResults": [
        //     [
        //         {
        //             "score": 1.263043,
        //             "fields": {
        //                 "vector": [
        //                     0.9533119,
        //                     0.02538395,
        //                     0.76714665,
        //                     0.35481733,
        //                     0.9845762
        //                 ],
        //                 "id": 740
        //             }
        //         },
        //         {
        //             "score": 1.2377806,
        //             "fields": {
        //                 "vector": [
        //                     0.7411156,
        //                     0.08687937,
        //                     0.8254139,
        //                     0.08370924,
        //                     0.99095553
        //                 ],
        //                 "id": 640
        //             }
        //         },
        //         {
        //             "score": 1.1869997,
        //             "fields": {
        //                 "vector": [
        //                     0.87928146,
        //                     0.05324632,
        //                     0.6312755,
        //                     0.28005534,
        //                     0.9542448
        //                 ],
        //                 "id": 455
        //             }
        //         }
        //     ],
        //     [
        //         {
        //             "score": 1.8654699,
        //             "fields": {
        //                 "vector": [
        //                     0.4671427,
        //                     0.8378432,
        //                     0.98844475,
        //                     0.82763994,
        //                     0.9729997
        //                 ],
        //                 "id": 638
        //             }
        //         },
        //         {
        //             "score": 1.8581753,
        //             "fields": {
        //                 "vector": [
        //                     0.735541,
        //                     0.60140246,
        //                     0.86730254,
        //                     0.93152493,
        //                     0.98603314
        //                 ],
        //                 "id": 855
        //             }
        //         },
        //         {
        //             "score": 1.8132881,
        //             "fields": {
        //                 "vector": [
        //                     0.9283006,
        //                     0.9465367,
        //                     0.9434311,
        //                     0.49135542,
        //                     0.93590534
        //                 ],
        //                 "id": 804
        //             }
        //         }
        //     ]
        // ]}





        // 6. Search within a partition
        // 6.1. Create a partition
        CreatePartitionReq partitionReq = CreatePartitionReq.builder()
            .collectionName("quick_setup")
            .partitionName("red")
            .build();

        client.createPartition(partitionReq);

        partitionReq = CreatePartitionReq.builder()
            .collectionName("quick_setup")
            .partitionName("blue")
            .build();

        client.createPartition(partitionReq);

        // 6.2 Insert data into the partition
        data = new ArrayList<>();

        for (int i=1000; i<1500; i++) {
            Random rand = new Random();
            String current_color = "red";
            JSONObject row = new JSONObject();
            row.put("id", Long.valueOf(i));
            row.put("vector", Arrays.asList(rand.nextFloat(), rand.nextFloat(), rand.nextFloat(), rand.nextFloat(), rand.nextFloat()));
            row.put("color", current_color);
            row.put("color_tag", current_color + "_" + String.valueOf(rand.nextInt(8999) + 1000));
            data.add(row);
        }     
        
        insertReq = InsertReq.builder()
            .collectionName("quick_setup")
            .data(data)
            .partitionName("red")
            .build();
        
        insertResp = client.insert(insertReq);

        System.out.println(JSONObject.toJSON(insertResp));

        // Output:
        // {"insertCnt": 500}





        data = new ArrayList<>();

        for (int i=1500; i<2000; i++) {
            Random rand = new Random();
            String current_color = "blue";
            JSONObject row = new JSONObject();
            row.put("id", Long.valueOf(i));
            row.put("vector", Arrays.asList(rand.nextFloat(), rand.nextFloat(), rand.nextFloat(), rand.nextFloat(), rand.nextFloat()));
            row.put("color", current_color);
            row.put("color_tag", current_color + "_" + String.valueOf(rand.nextInt(8999) + 1000));
            data.add(row);
        }
        
        insertReq = InsertReq.builder()
            .collectionName("quick_setup")
            .data(data)
            .partitionName("blue")
            .build();


        insertResp = client.insert(insertReq);

        System.out.println(JSONObject.toJSON(insertResp));

        // Output:
        // {"insertCnt": 500}





        // 6.3 Search within partitions

        searchReq = SearchReq.builder()
            .collectionName("quick_setup")
            .data(query_vectors)
            .partitionNames(Arrays.asList("red"))
            .topK(5)
            .build();

        searchResp = client.search(searchReq);

        System.out.println(JSONObject.toJSON(searchResp));

        // Output:
        // {"searchResults": [
        //     [
        //         {
        //             "score": 1.1677284,
        //             "fields": {
        //                 "vector": [
        //                     0.9986977,
        //                     0.17964739,
        //                     0.49086612,
        //                     0.23155272,
        //                     0.98438674
        //                 ],
        //                 "id": 1435
        //             }
        //         },
        //         {
        //             "score": 1.1476475,
        //             "fields": {
        //                 "vector": [
        //                     0.6952647,
        //                     0.13417172,
        //                     0.91045254,
        //                     0.119336545,
        //                     0.9338931
        //                 ],
        //                 "id": 1291
        //             }
        //         },
        //         {
        //             "score": 1.0969629,
        //             "fields": {
        //                 "vector": [
        //                     0.3363194,
        //                     0.028906643,
        //                     0.6675426,
        //                     0.030419827,
        //                     0.9735209
        //                 ],
        //                 "id": 1168
        //             }
        //         },
        //         {
        //             "score": 1.0741848,
        //             "fields": {
        //                 "vector": [
        //                     0.9980543,
        //                     0.36063594,
        //                     0.66427994,
        //                     0.17359233,
        //                     0.94954175
        //                 ],
        //                 "id": 1164
        //             }
        //         },
        //         {
        //             "score": 1.0584627,
        //             "fields": {
        //                 "vector": [
        //                     0.7187005,
        //                     0.12674773,
        //                     0.987718,
        //                     0.3110777,
        //                     0.86093885
        //                 ],
        //                 "id": 1085
        //             }
        //         }
        //     ],
        //     [
        //         {
        //             "score": 1.8030131,
        //             "fields": {
        //                 "vector": [
        //                     0.59726167,
        //                     0.7054632,
        //                     0.9573117,
        //                     0.94529945,
        //                     0.8664103
        //                 ],
        //                 "id": 1203
        //             }
        //         },
        //         {
        //             "score": 1.7728865,
        //             "fields": {
        //                 "vector": [
        //                     0.6672442,
        //                     0.60448086,
        //                     0.9325822,
        //                     0.80272985,
        //                     0.8861626
        //                 ],
        //                 "id": 1448
        //             }
        //         },
        //         {
        //             "score": 1.7536311,
        //             "fields": {
        //                 "vector": [
        //                     0.59663296,
        //                     0.77831805,
        //                     0.8578314,
        //                     0.88818026,
        //                     0.9030075
        //                 ],
        //                 "id": 1010
        //             }
        //         },
        //         {
        //             "score": 1.7520742,
        //             "fields": {
        //                 "vector": [
        //                     0.854198,
        //                     0.72294194,
        //                     0.9245805,
        //                     0.86126596,
        //                     0.7969224
        //                 ],
        //                 "id": 1219
        //             }
        //         },
        //         {
        //             "score": 1.7452049,
        //             "fields": {
        //                 "vector": [
        //                     0.96419,
        //                     0.943535,
        //                     0.87611496,
        //                     0.8268136,
        //                     0.79786557
        //                 ],
        //                 "id": 1149
        //             }
        //         }
        //     ]
        // ]}





        searchReq = SearchReq.builder()
            .collectionName("quick_setup")
            .data(query_vectors)
            .partitionNames(Arrays.asList("blue"))
            .topK(5)
            .build();

        searchResp = client.search(searchReq);

        System.out.println(JSONObject.toJSON(searchResp));

        // Output:
        // {"searchResults": [
        //     [
        //         {
        //             "score": 1.1628494,
        //             "fields": {
        //                 "vector": [
        //                     0.7442872,
        //                     0.046407282,
        //                     0.71031404,
        //                     0.3544345,
        //                     0.9819991
        //                 ],
        //                 "id": 1992
        //             }
        //         },
        //         {
        //             "score": 1.1470042,
        //             "fields": {
        //                 "vector": [
        //                     0.5505825,
        //                     0.04367262,
        //                     0.9985836,
        //                     0.18922359,
        //                     0.93255126
        //                 ],
        //                 "id": 1977
        //             }
        //         },
        //         {
        //             "score": 1.1450152,
        //             "fields": {
        //                 "vector": [
        //                     0.89994013,
        //                     0.052991092,
        //                     0.8645576,
        //                     0.6406729,
        //                     0.95679337
        //                 ],
        //                 "id": 1573
        //             }
        //         },
        //         {
        //             "score": 1.1439825,
        //             "fields": {
        //                 "vector": [
        //                     0.9253267,
        //                     0.15890503,
        //                     0.7999555,
        //                     0.19126713,
        //                     0.898583
        //                 ],
        //                 "id": 1552
        //             }
        //         },
        //         {
        //             "score": 1.1029172,
        //             "fields": {
        //                 "vector": [
        //                     0.95661926,
        //                     0.18777144,
        //                     0.38115507,
        //                     0.14323527,
        //                     0.93137646
        //                 ],
        //                 "id": 1823
        //             }
        //         }
        //     ],
        //     [
        //         {
        //             "score": 1.8005109,
        //             "fields": {
        //                 "vector": [
        //                     0.5953582,
        //                     0.7794224,
        //                     0.9388869,
        //                     0.79825854,
        //                     0.9197286
        //                 ],
        //                 "id": 1888
        //             }
        //         },
        //         {
        //             "score": 1.7714822,
        //             "fields": {
        //                 "vector": [
        //                     0.56805456,
        //                     0.89422905,
        //                     0.88187534,
        //                     0.914824,
        //                     0.8944365
        //                 ],
        //                 "id": 1648
        //             }
        //         },
        //         {
        //             "score": 1.7561421,
        //             "fields": {
        //                 "vector": [
        //                     0.83421993,
        //                     0.39865613,
        //                     0.92319834,
        //                     0.42695504,
        //                     0.96633124
        //                 ],
        //                 "id": 1688
        //             }
        //         },
        //         {
        //             "score": 1.7553532,
        //             "fields": {
        //                 "vector": [
        //                     0.89994013,
        //                     0.052991092,
        //                     0.8645576,
        //                     0.6406729,
        //                     0.95679337
        //                 ],
        //                 "id": 1573
        //             }
        //         },
        //         {
        //             "score": 1.7543385,
        //             "fields": {
        //                 "vector": [
        //                     0.16542226,
        //                     0.38248396,
        //                     0.9888778,
        //                     0.80913955,
        //                     0.9501492
        //                 ],
        //                 "id": 1544
        //             }
        //         }
        //     ]
        // ]}





        // 7. Search with output fields
        searchReq = SearchReq.builder()
            .collectionName("quick_setup")
            .data(query_vectors)
            .outputFields(Arrays.asList("color"))
            .topK(5)
            .build();

        searchResp = client.search(searchReq);

        System.out.println(JSONObject.toJSON(searchResp));

        // Output:
        // {"searchResults": [
        //     [
        //         {
        //             "score": 1.263043,
        //             "fields": {}
        //         },
        //         {
        //             "score": 1.2377806,
        //             "fields": {}
        //         },
        //         {
        //             "score": 1.1869997,
        //             "fields": {}
        //         },
        //         {
        //             "score": 1.1748955,
        //             "fields": {}
        //         },
        //         {
        //             "score": 1.1720343,
        //             "fields": {}
        //         }
        //     ],
        //     [
        //         {
        //             "score": 1.8654699,
        //             "fields": {}
        //         },
        //         {
        //             "score": 1.8581753,
        //             "fields": {}
        //         },
        //         {
        //             "score": 1.8132881,
        //             "fields": {}
        //         },
        //         {
        //             "score": 1.8030131,
        //             "fields": {"color": "red"}
        //         },
        //         {
        //             "score": 1.8026174,
        //             "fields": {}
        //         }
        //     ]
        // ]}





        // 8. Filtered search
        searchReq = SearchReq.builder()
            .collectionName("quick_setup")
            .data(query_vectors)
            .outputFields(Arrays.asList("color_tag"))
            .filter("color_tag like \"red%\"")
            .topK(5)
            .build();

        searchResp = client.search(searchReq);

        System.out.println(JSONObject.toJSON(searchResp));

        // Output:
        // {"searchResults": [
        //     [
        //         {
        //             "score": 1.1869997,
        //             "fields": {"color_tag": "red_3026"}
        //         },
        //         {
        //             "score": 1.1677284,
        //             "fields": {"color_tag": "red_9030"}
        //         },
        //         {
        //             "score": 1.1476475,
        //             "fields": {"color_tag": "red_3744"}
        //         },
        //         {
        //             "score": 1.0969629,
        //             "fields": {"color_tag": "red_4168"}
        //         },
        //         {
        //             "score": 1.0741848,
        //             "fields": {"color_tag": "red_9678"}
        //         }
        //     ],
        //     [
        //         {
        //             "score": 1.8030131,
        //             "fields": {"color_tag": "red_4010"}
        //         },
        //         {
        //             "score": 1.7870991,
        //             "fields": {"color_tag": "red_1842"}
        //         },
        //         {
        //             "score": 1.7728865,
        //             "fields": {"color_tag": "red_2312"}
        //         },
        //         {
        //             "score": 1.7536311,
        //             "fields": {"color_tag": "red_3094"}
        //         },
        //         {
        //             "score": 1.7520742,
        //             "fields": {"color_tag": "red_9085"}
        //         }
        //     ]
        // ]}





        // 8.2 Filtered search with infix wildcard
        // searchReq = SearchReq.builder()
        //     .collectionName("quick_setup")
        //     .data(query_vectors)
        //     .outputFields(Arrays.asList("color_tag"))
        //     .filter("color_tag like \"%_4%\"")
        //     .topK(5)
        //     .build();

        // searchResp = client.search(searchReq);         


        // 9. Range search
        searchReq = SearchReq.builder()
            .collectionName("quick_setup")
            .data(query_vectors)
            .outputFields(Arrays.asList("color_tag"))
            .searchParams(Map.of("radius", 0.1, "range", 1.0))
            .topK(5)
            .build();

        searchResp = client.search(searchReq);

        System.out.println(JSONObject.toJSON(searchResp));

        // Output:
        // {"searchResults": [
        //     [
        //         {
        //             "score": 1.263043,
        //             "fields": {"color_tag": "green_2052"}
        //         },
        //         {
        //             "score": 1.2377806,
        //             "fields": {"color_tag": "purple_3709"}
        //         },
        //         {
        //             "score": 1.1869997,
        //             "fields": {"color_tag": "red_3026"}
        //         },
        //         {
        //             "score": 1.1748955,
        //             "fields": {"color_tag": "black_1646"}
        //         },
        //         {
        //             "score": 1.1720343,
        //             "fields": {"color_tag": "green_4853"}
        //         }
        //     ],
        //     [
        //         {
        //             "score": 1.8654699,
        //             "fields": {"color_tag": "orange_2405"}
        //         },
        //         {
        //             "score": 1.8581753,
        //             "fields": {"color_tag": "yellow_2427"}
        //         },
        //         {
        //             "score": 1.8132881,
        //             "fields": {"color_tag": "pink_8064"}
        //         },
        //         {
        //             "score": 1.8030131,
        //             "fields": {"color_tag": "red_4010"}
        //         },
        //         {
        //             "score": 1.8026174,
        //             "fields": {"color_tag": "purple_3184"}
        //         }
        //     ]
        // ]}





        
        // 10. Grouping search 
        // TODO: Not supported yet.
            
        // 11. Drop collection
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