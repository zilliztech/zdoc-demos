package com.zilliz.docs;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONObject;

import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.common.DataType;
import io.milvus.v2.common.IndexParam;
import io.milvus.v2.service.collection.request.AddFieldReq;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.collection.request.DropCollectionReq;
import io.milvus.v2.service.index.request.CreateIndexReq;
import io.milvus.v2.service.index.request.DescribeIndexReq;
import io.milvus.v2.service.index.request.DropIndexReq;
import io.milvus.v2.service.index.request.ListIndexesReq;
import io.milvus.v2.service.index.response.DescribeIndexResp;

public class ManageIndexesDemo {

    public static void run() throws InterruptedException {
        String CLUSTER_ENDPOINT = "YOUR_CLUSTER_ENDPOINT";
        String TOKEN = "YOUR_CLUSTER_TOKEN";

        // 1. Connect to Milvus server
        ConnectConfig connectConfig = ConnectConfig.builder()
            .uri(CLUSTER_ENDPOINT)
            .token(TOKEN)
            .build();

        MilvusClientV2 client = new MilvusClientV2(connectConfig);

        // 2. Create a collection

        // 2.1 Create schema
        CreateCollectionReq.CollectionSchema schema = client.createSchema();

        // 2.2 Add fields to schema
        schema.addField(AddFieldReq.builder().fieldName("id").dataType(DataType.Int64).isPrimaryKey(true).autoID(false).build());
        schema.addField(AddFieldReq.builder().fieldName("vector").dataType(DataType.FloatVector).dimension(5).build());

        // 2.3 Create a collection without schema and index parameters
        CreateCollectionReq customizedSetupReq = CreateCollectionReq.builder()
        .collectionName("customized_setup")
        .collectionSchema(schema)
        .build();

        client.createCollection(customizedSetupReq);

        // 4 Prepare index parameters
        // 4.1 Add an index for the primary field "id"
        IndexParam indexParamForIdField = IndexParam.builder()
            .fieldName("id")
            .indexName("primary_field_index")
            .indexType(IndexParam.IndexType.STL_SORT)
            .build();


        // 4.2 Add an index for the vector field "vector"
        IndexParam indexParamForVectorField = IndexParam.builder()
            .fieldName("vector")
            .indexName("vector_index")
            .indexType(IndexParam.IndexType.AUTOINDEX)
            .metricType(IndexParam.MetricType.COSINE)
            .build();

        List<IndexParam> indexParams = new ArrayList<>();
        indexParams.add(indexParamForIdField);
        indexParams.add(indexParamForVectorField);

        // 4.3 Crate an index file
        CreateIndexReq createIndexReq = CreateIndexReq.builder()
            .collectionName("customized_setup")
            .indexParams(indexParams)
            .build();

        client.createIndex(createIndexReq);

        // 5. Describe index
        // 5.1 List the index names
        ListIndexesReq listIndexesReq = ListIndexesReq.builder()
            .collectionName("customized_setup")
            .build();

        List<String> indexNames = client.listIndexes(listIndexesReq);

        System.out.println(indexNames);

        // Output:
        // [
        //     "primary_field_index",
        //     "vector_index"
        // ]




        // 5.2 Describe an index
        DescribeIndexReq describeIndexReq = DescribeIndexReq.builder()
            .collectionName("customized_setup")
            .indexName("vector_index")
            .build();

        DescribeIndexResp describeIndexResp = client.describeIndex(describeIndexReq);

        System.out.println(JSONObject.toJSON(describeIndexResp));

        // Output:
        // {
        //     "metricType": "COSINE",
        //     "indexType": "AUTOINDEX",
        //     "fieldName": "vector",
        //     "indexName": "vector_index"
        // }




        describeIndexReq = DescribeIndexReq.builder()
            .collectionName("customized_setup")
            .indexName("primary_field_index")
            .build();

        describeIndexResp = client.describeIndex(describeIndexReq);

        System.out.println(JSONObject.toJSON(describeIndexResp));

        // Output:
        // {
        //     "indexType": "STL_SORT",
        //     "fieldName": "id",
        //     "indexName": "primary_field_index"
        // }




        // 6. Drop index

        DropIndexReq dropIndexReq = DropIndexReq.builder()
            .collectionName("customized_setup")
            .indexName("vector_index")
            .build();

        client.dropIndex(dropIndexReq);

        dropIndexReq = DropIndexReq.builder()
            .collectionName("customized_setup")
            .indexName("primary_field_index")
            .build();

        client.dropIndex(dropIndexReq);

        // 7. Drop collection
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
