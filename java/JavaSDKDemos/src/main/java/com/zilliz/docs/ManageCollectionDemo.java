package com.zilliz.docs;

import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.common.DataType;
import io.milvus.v2.common.IndexParam;
import io.milvus.v2.service.collection.request.*;
import io.milvus.v2.service.collection.response.DescribeCollectionResp;
import io.milvus.v2.service.collection.response.ListCollectionsResp;
import io.milvus.v2.service.index.request.CreateIndexReq;
import io.milvus.v2.service.utility.request.AlterAliasReq;
import io.milvus.v2.service.utility.request.CreateAliasReq;
import io.milvus.v2.service.utility.request.DescribeAliasReq;
import io.milvus.v2.service.utility.request.DropAliasReq;
import io.milvus.v2.service.utility.response.DescribeAliasResp;
import io.milvus.v2.service.utility.request.ListAliasesReq;
import io.milvus.v2.service.utility.response.ListAliasResp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

public class ManageCollectionDemo {
    private static void run() throws InterruptedException {
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
            .build();

        client.createCollection(quickSetupReq);

        // Thread.sleep(5000);

        GetLoadStateReq quickSetupLoadStateReq = GetLoadStateReq.builder()
            .collectionName("quick_setup")
            .build();

        Boolean res = client.getLoadState(quickSetupLoadStateReq);

        System.out.println(res);

        // Output:
        // true





        // 3. Create a collection in customized setup mode

        // 3.1 Create schema
        CreateCollectionReq.CollectionSchema schema = client.createSchema();

        // 3.2 Add fields to schema
        schema.addField(AddFieldReq.builder().fieldName("my_id").dataType(DataType.Int64).isPrimaryKey(true).autoID(false).build());
        schema.addField(AddFieldReq.builder().fieldName("my_vector").dataType(DataType.FloatVector).dimension(5).build());

        // 3.3 Prepare index parameters
        IndexParam indexParamForIdField = IndexParam.builder()
            .fieldName("my_id")
            .indexType(IndexParam.IndexType.STL_SORT)
            .build();

        IndexParam indexParamForVectorField = IndexParam.builder()
            .fieldName("my_vector")
            .indexType(IndexParam.IndexType.IVF_FLAT)
            .metricType(IndexParam.MetricType.L2)
            .extraParams(Map.of("nlist", 1024))
            .build();

        List<IndexParam> indexParams = new ArrayList<>();
        indexParams.add(indexParamForIdField);
        indexParams.add(indexParamForVectorField);

        // 3.4 Create a collection with schema and index parameters
        CreateCollectionReq customizedSetupReq1 = CreateCollectionReq.builder()
            .collectionName("customized_setup_1")
            .collectionSchema(schema)
            .indexParams(indexParams)
            .build();

        client.createCollection(customizedSetupReq1);

        // Thread.sleep(5000);

        // 3.5 Get load state of the collection
        GetLoadStateReq customSetupLoadStateReq1 = GetLoadStateReq.builder()
            .collectionName("customized_setup_1")
            .build();

        res = client.getLoadState(customSetupLoadStateReq1);

        System.out.println(res);

        // Output:
        // true





        // 3.6 Create a collection and index it separately
        CreateCollectionReq customizedSetupReq2 = CreateCollectionReq.builder()
            .collectionName("customized_setup_2")
            .collectionSchema(schema)
            .build();

        client.createCollection(customizedSetupReq2);

        CreateIndexReq  createIndexReq = CreateIndexReq.builder()
            .collectionName("customized_setup_2")
            .indexParams(indexParams)
            .build();

        client.createIndex(createIndexReq);

        // Thread.sleep(1000);

        // 3.7 Get load state of the collection
        GetLoadStateReq customSetupLoadStateReq2 = GetLoadStateReq.builder()
            .collectionName("customized_setup_2")
            .build();

        res = client.getLoadState(customSetupLoadStateReq2);

        System.out.println(res);

        // Output:
        // false





        // 4. View collections
        DescribeCollectionReq describeCollectionReq = DescribeCollectionReq.builder()
            .collectionName("customized_setup_2")
            .build();

        DescribeCollectionResp describeCollectionRes = client.describeCollection(describeCollectionReq);

        System.out.println(JSONObject.toJSON(describeCollectionRes));

        // Output:
        // {
        //     "createTime": 449005822816026627,
        //     "collectionSchema": {"fieldSchemaList": [
        //         {
        //             "autoID": false,
        //             "dataType": "Int64",
        //             "name": "my_id",
        //             "description": "",
        //             "isPrimaryKey": true,
        //             "maxLength": 65535,
        //             "isPartitionKey": false
        //         },
        //         {
        //             "autoID": false,
        //             "dataType": "FloatVector",
        //             "name": "my_vector",
        //             "description": "",
        //             "isPrimaryKey": false,
        //             "dimension": 5,
        //             "maxLength": 65535,
        //             "isPartitionKey": false
        //         }
        //     ]},
        //     "vectorFieldName": ["my_vector"],
        //     "autoID": false,
        //     "fieldNames": [
        //         "my_id",
        //         "my_vector"
        //     ],
        //     "description": "",
        //     "numOfPartitions": 1,
        //     "primaryFieldName": "my_id",
        //     "enableDynamicField": true,
        //     "collectionName": "customized_setup_2"
        // }





        // 5. List all collection names
        ListCollectionsResp listCollectionsRes = client.listCollections();

        System.out.println(listCollectionsRes.getCollectionNames());

        // Output:
        // [
        //     "customized_setup_1",
        //     "test_collection",
        //     "quick_setup",
        //     "customized_setup_2"
        // ]





        // 6. Load the collection
        LoadCollectionReq loadCollectionReq = LoadCollectionReq.builder()
            .collectionName("customized_setup_2")
            .build();

        client.loadCollection(loadCollectionReq);

        // Thread.sleep(5000);

        // 7. Get load state of the collection
        GetLoadStateReq loadStateReq = GetLoadStateReq.builder()
            .collectionName("customized_setup_2")
            .build();

        res = client.getLoadState(loadStateReq);

        System.out.println(res);

        // Output:
        // true





        // 8. Release the collection
        ReleaseCollectionReq releaseCollectionReq = ReleaseCollectionReq.builder()
            .collectionName("customized_setup_2")
            .build();

        client.releaseCollection(releaseCollectionReq);


        // Thread.sleep(1000);

        res = client.getLoadState(loadStateReq);

        System.out.println(res);

        // Output:
        // false





        // 9. Manage aliases

        // 9.1 Create alias
        CreateAliasReq createAliasReq = CreateAliasReq.builder()
            .collectionName("customized_setup_2")
            .alias("bob")
            .build();

        client.createAlias(createAliasReq);

        createAliasReq = CreateAliasReq.builder()
            .collectionName("customized_setup_2")
            .alias("alice")
            .build();

        client.createAlias(createAliasReq);

        // Thread.sleep(1000);

        // 9.2 List alises
        ListAliasesReq listAliasesReq = ListAliasesReq.builder()
            .collectionName("customized_setup_2")
            .build();

        ListAliasResp listAliasRes = client.listAliases(listAliasesReq);

        System.out.println(listAliasRes.getAlias());

        // Output:
        // [
        //     "bob",
        //     "alice"
        // ]





        // 9.3 Describe alias
        DescribeAliasReq describeAliasReq = DescribeAliasReq.builder()
            .alias("bob")
            .build();

        DescribeAliasResp describeAliasRes = client.describeAlias(describeAliasReq);

        System.out.println(JSONObject.toJSON(describeAliasRes));

        // Output:
        // {
        //     "alias": "bob",
        //     "collectionName": "customized_setup_2"
        // }





        // 9.4 Reassign alias to other collections
        AlterAliasReq alterAliasReq = AlterAliasReq.builder()
            .collectionName("customized_setup_1")
            .alias("alice")
            .build();

        client.alterAlias(alterAliasReq);

        listAliasesReq = ListAliasesReq.builder()
            .collectionName("customized_setup_1")
            .build();

        listAliasRes = client.listAliases(listAliasesReq);

        System.out.println(listAliasRes.getAlias());

        // Output:
        // ["alice"]





        listAliasesReq = ListAliasesReq.builder()
            .collectionName("customized_setup_2")
            .build();

        listAliasRes = client.listAliases(listAliasesReq);

        System.out.println(listAliasRes.getAlias());

        // Output:
        // ["bob"]





        // 9.5 Drop alias
        DropAliasReq dropAliasReq = DropAliasReq.builder()
            .alias("bob")
            .build();

        client.dropAlias(dropAliasReq);

        dropAliasReq = DropAliasReq.builder()
            .alias("alice")
            .build();

        client.dropAlias(dropAliasReq);

        // 10. Drop collections

        DropCollectionReq dropQuickSetupParam = DropCollectionReq.builder()
            .collectionName("quick_setup")
            .build();

        client.dropCollection(dropQuickSetupParam);

        DropCollectionReq dropCustomizedSetupParam = DropCollectionReq.builder()
            .collectionName("customized_setup_1")
            .build();

        client.dropCollection(dropCustomizedSetupParam);

        dropCustomizedSetupParam = DropCollectionReq.builder()
            .collectionName("customized_setup_2")
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