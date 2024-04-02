package com.zilliz.docs;

import java.util.ArrayList;
import java.util.List;

import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.common.DataType;
import io.milvus.v2.common.IndexParam;
import io.milvus.v2.service.collection.request.AddFieldReq;
import io.milvus.v2.service.collection.request.CreateCollectionReq;

public class UsePartitionKeyDemo {
    public static void run() throws InterruptedException {
        String CLUSTER_ENDPOINT = "https://in01-0ed1e58b63f3f62.aws-us-west-2.vectordb-uat3.zillizcloud.com:19538";
        String TOKEN = "root:n8=;5cdO:5q0G%:K<SdMhwcx0Rl!G=:}";

        // 1. Connect to Milvus server
        ConnectConfig connectConfig = ConnectConfig.builder()
            .uri(CLUSTER_ENDPOINT)
            .token(TOKEN)
            .build();

        MilvusClientV2 client = new MilvusClientV2(connectConfig);

        // 2. Create a collection in customized setup mode

        // 2.1 Create schema
        CreateCollectionReq.CollectionSchema schema = client.createSchema();

        // 2.2 Add fields to schema
        schema.addField(AddFieldReq.builder().fieldName("id").dataType(DataType.Int64).isPrimaryKey(true).autoID(false).build());
        schema.addField(AddFieldReq.builder().fieldName("vector").dataType(DataType.FloatVector).dimension(5).build());
        schema.addField(AddFieldReq.builder().fieldName("color").dataType(DataType.VarChar).maxLength(512).isPartitionKey(true).build());

        // 2.3 Prepare index parameters
        IndexParam indexParamForIdField = IndexParam.builder()
            .fieldName("id")
            .indexType(IndexParam.IndexType.STL_SORT)
            .build();

        IndexParam indexParamForVectorField = IndexParam.builder()
            .fieldName("vector")
            .indexType(IndexParam.IndexType.IVF_FLAT)
            .metricType(IndexParam.MetricType.IP)
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
        CreateCollectionReq customizedSetupReq1 = CreateCollectionReq.builder()
            .collectionName("test_collection")
            .collectionSchema(schema)
            .indexParams(indexParams)
            .enableDynamicField(true)
            .build();

        client.createCollection(customizedSetupReq1);

    }

    public static void main(String[] args) {
        try {
            run();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
