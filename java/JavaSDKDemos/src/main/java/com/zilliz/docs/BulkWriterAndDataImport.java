package com.zilliz.docs;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.apache.commons.lang3.StringUtils;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.milvus.bulkwriter.CloudImport;
import io.milvus.bulkwriter.RemoteBulkWriter;
import io.milvus.bulkwriter.RemoteBulkWriterParam;
import io.milvus.bulkwriter.common.clientenum.BulkFileType;
import io.milvus.bulkwriter.connect.S3ConnectParam;
import io.milvus.bulkwriter.connect.StorageConnectParam;
import io.milvus.bulkwriter.response.BulkImportResponse;
import io.milvus.bulkwriter.response.GetImportProgressResponse;
import io.milvus.bulkwriter.response.ListImportJobsResponse;
import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.DataType;
import io.milvus.param.ConnectParam;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import io.milvus.param.collection.CollectionSchemaParam;
import io.milvus.param.collection.CreateCollectionParam;
import io.milvus.param.collection.FieldType;
import io.milvus.param.collection.LoadCollectionParam;
import io.milvus.param.index.CreateIndexParam;

public class BulkWriterAndDataImport {
    public static void run() throws InterruptedException, IOException {
        // Configs for Zilliz Cloud cluster
        String CLUSTER_ENDPOINT = "https://in01-0ed1e58b63f3f62.aws-us-west-2.vectordb-uat3.zillizcloud.com:19538";
        String TOKEN = "root:n8=;5cdO:5q0G%:K<SdMhwcx0Rl!G=:}";
        String API_KEY = "";
        String CLUSTER_ID = "";
        String CLOUD_REGION = "";
        String CLOUD_API_ENDPOINT = String.format("controller.api.%s.zillizcloud.com", CLOUD_REGION);
        String COLLECTION_NAME = "";

        // Configs for remote bucket
        String ACCESS_KEY = "";
        String SECRET_KEY = "";
        String BUCKET_NAME = "";

        // Define schema for the target collection
        FieldType id = FieldType.newBuilder()
                .withName("id")
                .withDataType(DataType.Int64)
                .withPrimaryKey(true)
                .withAutoID(false)
                .build();

        FieldType titleVector = FieldType.newBuilder()
                .withName("title_vector")
                .withDataType(DataType.FloatVector)
                .withDimension(768)
                .build();

        FieldType title = FieldType.newBuilder()
                .withName("title")
                .withDataType(DataType.VarChar)
                .withMaxLength(512)
                .build();
        
        FieldType link = FieldType.newBuilder()
                .withName("link")
                .withDataType(DataType.VarChar)
                .withMaxLength(512)
                .build();

        CollectionSchemaParam schema = CollectionSchemaParam.newBuilder()
                .withEnableDynamicField(true)
                .addFieldType(id)
                .addFieldType(titleVector)
                .addFieldType(title)
                .addFieldType(link)
                .build();

        // Create a collection with the given schema
        ConnectParam connectParam = ConnectParam.newBuilder()
                .withUri(CLUSTER_ENDPOINT)
                .withToken(TOKEN)
                .build();

        MilvusServiceClient milvusClient = new MilvusServiceClient(connectParam);

        CreateCollectionParam collectionParam = CreateCollectionParam.newBuilder()
                .withCollectionName(COLLECTION_NAME)
                .withSchema(schema)
                .build();

        milvusClient.createCollection(collectionParam);

        CreateIndexParam indexParam = CreateIndexParam.newBuilder()
                .withCollectionName(COLLECTION_NAME)
                .withFieldName("title_vector")
                .withIndexType(IndexType.AUTOINDEX)
                .withMetricType(MetricType.IP)
                .build();

        milvusClient.createIndex(indexParam);

        LoadCollectionParam loadCollectionParam = LoadCollectionParam.newBuilder()
                .withCollectionName(COLLECTION_NAME)
                .build();

        milvusClient.loadCollection(loadCollectionParam);

        // Create a remote bucket writer.
        StorageConnectParam storageConnectParam = S3ConnectParam.newBuilder()
                .withEndpoint("storage.googleapis.com")
                .withBucketName(BUCKET_NAME)
                .withAccessKey(ACCESS_KEY)
                .withSecretKey(SECRET_KEY)
                .build();

        RemoteBulkWriterParam remoteBulkWriterParam = RemoteBulkWriterParam.newBuilder()
                .withCollectionSchema(schema)
                .withRemotePath("/")
                .withChunkSize(512 * 1024 * 1024)
                .withConnectParam(storageConnectParam)
                .withFileType(BulkFileType.PARQUET)
                .build();

        @SuppressWarnings("resource")
        RemoteBulkWriter remoteBulkWriter = new RemoteBulkWriter(remoteBulkWriterParam);

        CsvMapper csvMapper = new CsvMapper();
        File csvFile = new File("medium_articles_partial.csv");

        CsvSchema csvSchema = CsvSchema.builder().setUseHeader(true).build();
        Iterator<CsvDataObject> iterator = csvMapper
                .readerFor(CsvDataObject.class)
                .with(csvSchema)
                .readValues(csvFile);

        while (iterator.hasNext()) {
            CsvDataObject data = iterator.next();
            JSONObject row = new JSONObject();

            row.put("id", data.getId());
            row.put("title_vector", data.toFloatArray());
            row.put("title", data.getTitle());
            row.put("link", data.getLink());

            remoteBulkWriter.appendRow(row);
        }
        
        remoteBulkWriter.commit(false);

        List<List<String>> batchFiles = remoteBulkWriter.getBatchFiles();

        System.out.println(batchFiles);

        // Insert the data into the collection
        String prefix = batchFiles.get(0).get(0).split("/")[0];
        String OBJECT_URL = String.format("https://storage.googleapis.com/%s/%s", BUCKET_NAME, prefix);

        BulkImportResponse bulkImportResponse = CloudImport.bulkImport(
            CLUSTER_ENDPOINT,
            API_KEY,
            CLUSTER_ID,
            COLLECTION_NAME,
            OBJECT_URL,
            ACCESS_KEY,
            SECRET_KEY
        );

        // Get import job ID
        String jobId = bulkImportResponse.getJobId();

        System.out.println(jobId);

        while (true) {
            System.out.println("Wait 5 second to check bulkInsert job state...");
            TimeUnit.SECONDS.sleep(5);

            GetImportProgressResponse getImportProgressResponse = CloudImport.getImportProgress(
                CLUSTER_ENDPOINT,
                API_KEY,
                CLUSTER_ID,
                jobId
            );

            if (getImportProgressResponse.getReadyPercentage().intValue() == 1) {
                System.err.printf("The job %s completed%n", jobId);
                break;
            } else if (StringUtils.isNotEmpty(getImportProgressResponse.getErrorMessage())) {
                System.err.printf("The job %s failed, reason: %s%n", jobId, getImportProgressResponse.getErrorMessage());
                break;
            } else {
                System.err.printf("The job %s is running, progress:%s%n", jobId, getImportProgressResponse.getReadyPercentage());
            }
        }

        ListImportJobsResponse listImportJobsResponse = CloudImport.listImportJobs(
            CLUSTER_ENDPOINT,
            API_KEY,
            CLUSTER_ID,
            10,
            1
        );

        System.out.println(listImportJobsResponse);
    }  
    
    private static class CsvDataObject {
        @JsonProperty
        private long id;
        @JsonProperty
        private String title_vector;
        @JsonProperty
        private String title;
        @JsonProperty
        private String link;

        public long getId() {
        return id;
        }

        @SuppressWarnings("unused")
        public String getTitleVector() {
        return title_vector;
        }

        public String getTitle() {
        return title;
        }

        public String getLink() {
        return link;
        }

        public List<Float> toFloatArray() {
        return new Gson().fromJson(title_vector, new TypeToken<List<Float>>(){}.getType());
        }
    }     

    public static void main(String[] args) throws IOException {
        try {
            run();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }        
    }      
}
