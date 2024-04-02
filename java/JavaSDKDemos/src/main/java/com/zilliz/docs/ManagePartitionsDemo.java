package com.zilliz.docs;

import java.util.List;

import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.collection.request.DropCollectionReq;
import io.milvus.v2.service.collection.request.GetLoadStateReq;
import io.milvus.v2.service.collection.request.ReleaseCollectionReq;
import io.milvus.v2.service.partition.request.CreatePartitionReq;
import io.milvus.v2.service.partition.request.DropPartitionReq;
import io.milvus.v2.service.partition.request.HasPartitionReq;
import io.milvus.v2.service.partition.request.ListPartitionsReq;
import io.milvus.v2.service.partition.request.LoadPartitionsReq;
import io.milvus.v2.service.partition.request.ReleasePartitionsReq;

public class ManagePartitionsDemo {
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
            .build();

        client.createCollection(quickSetupReq);

        // 3. List all partitions in the collection
        ListPartitionsReq listPartitionsReq = ListPartitionsReq.builder()
            .collectionName("quick_setup")
            .build();

        List<String> partitionNames = client.listPartitions(listPartitionsReq);

        System.out.println(partitionNames);

        // Output:
        // ["_default"]




        // 4. Create more partitions
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

        listPartitionsReq = ListPartitionsReq.builder()
            .collectionName("quick_setup")
            .build();

        partitionNames = client.listPartitions(listPartitionsReq);

        System.out.println(partitionNames);

        // Output:
        // [
        //     "_default",
        //     "partitionA",
        //     "partitionB"
        // ]




        // 5. Check whether a partition exists
        HasPartitionReq hasPartitionReq = HasPartitionReq.builder()
            .collectionName("quick_setup")
            .partitionName("partitionA")
            .build();

        boolean exists = client.hasPartition(hasPartitionReq);

        System.out.println(exists);

        // Output:
        // true




        hasPartitionReq = HasPartitionReq.builder()
            .collectionName("quick_setup")
            .partitionName("partitionC")
            .build();

        exists = client.hasPartition(hasPartitionReq);

        System.out.println(exists);

        // Output:
        // false




        // 6. Load a partition independantly
        // 6.1 Release the collection
        ReleaseCollectionReq releaseCollectionReq = ReleaseCollectionReq.builder()
            .collectionName("quick_setup")
            .build();

        client.releaseCollection(releaseCollectionReq);

        // 6.2 Load partitionA
        LoadPartitionsReq loadPartitionsReq = LoadPartitionsReq.builder()
            .collectionName("quick_setup")
            .partitionNames(List.of("partitionA"))
            .build();

        client.loadPartitions(loadPartitionsReq);

        Thread.sleep(3000);

        // 6.3 Check the load status of the collection and its partitions
        GetLoadStateReq getLoadStateReq = GetLoadStateReq.builder()
            .collectionName("quick_setup")
            .build();

        boolean state = client.getLoadState(getLoadStateReq);

        System.out.println(state);

        // Output:
        // true




        getLoadStateReq = GetLoadStateReq.builder()
            .collectionName("quick_setup")
            .partitionName("partitionA")
            .build();

        state = client.getLoadState(getLoadStateReq);

        System.out.println(state);

        // Output:
        // true




        getLoadStateReq = GetLoadStateReq.builder()
            .collectionName("quick_setup")
            .partitionName("partitionB")
            .build();

        state = client.getLoadState(getLoadStateReq);

        System.out.println(state);

        // Output:
        // false




        // 7. Release a partition
        ReleasePartitionsReq releasePartitionsReq = ReleasePartitionsReq.builder()
            .collectionName("quick_setup")
            .partitionNames(List.of("partitionA"))
            .build();

        client.releasePartitions(releasePartitionsReq);

        getLoadStateReq = GetLoadStateReq.builder()
            .collectionName("quick_setup")
            .partitionName("partitionA")
            .build();

        state = client.getLoadState(getLoadStateReq);

        System.out.println(state);

        // Output:
        // false




        // Drop a partition
        DropPartitionReq dropPartitionReq = DropPartitionReq.builder()
            .collectionName("quick_setup")
            .partitionName("partitionB")
            .build();

        client.dropPartition(dropPartitionReq);

        listPartitionsReq = ListPartitionsReq.builder()
            .collectionName("quick_setup")
            .build();


        partitionNames = client.listPartitions(listPartitionsReq);

        System.out.println(partitionNames);

        // Output:
        // [
        //     "_default",
        //     "partitionA"
        // ]




        // 9. Drop a collection
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