const fs = require("fs")
const { MilvusClient, DataType, sleep } = require("@zilliz/milvus2-sdk-node")

const address = "YOUR_CLUSTER_ENDPOINT"
const token = "YOUR_CLUSTER_TOKEN"

async function main() {

    // 1. Set up a Milvus Client
    client = new MilvusClient({address, token});

    // 2. Define fields for the collection
    const fields = [
        {
            name: "id",
            data_type: DataType.Int64,
            is_primary_key: true,
            auto_id: false
        },
        {
            name: "vector",
            data_type: DataType.FloatVector,
            dim: 5
        },
    ]
    
    // 3. Create a collection
    res = await client.createCollection({
        collection_name: "customized_setup",
        fields: fields,
    })

    console.log(res.error_code)  

    // Output
    // 
    // Success
    // 

    // 4. Set up index for the collection
    // 4.1. Set up the index parameters
    res = await client.createIndex({
        collection_name: "customized_setup",
        field_name: "vector",
        index_type: "AUTOINDEX",
        metric_type: "COSINE",   
        index_name: "vector_index"
    })

    console.log(res.error_code)

    // Output
    // 
    // Success
    // 

    // 4.2 Add an index on a scalar field.
    res = await client.createIndex({
        collection_name: "customized_setup",
        field_name: "id",
        index_name: "primary_field_index",
        index_type: "STL_SORT"
    })

    console.log(res.error_code)

    // Output
    // 
    // Success
    // 

    await sleep(5000)

    // 5. Describe the index
    res = await client.describeIndex({
        collection_name: "customized_setup",
        index_name: "primary_field_index"
    })

    console.log(JSON.stringify(res.index_descriptions, null, 2))

    // Output
    // 
    // [
    //   {
    //     "params": [
    //       {
    //         "key": "index_type",
    //         "value": "STL_SORT"
    //       }
    //     ],
    //     "index_name": "primary_field_index",
    //     "indexID": "449007919953063142",
    //     "field_name": "id",
    //     "indexed_rows": "0",
    //     "total_rows": "0",
    //     "state": "Finished",
    //     "index_state_fail_reason": "",
    //     "pending_index_rows": "0"
    //   }
    // ]
    // 

    res = await client.describeIndex({
        collection_name: "customized_setup",
        index_name: "vector_index"
    })

    console.log(JSON.stringify(res.index_descriptions, null, 2))

    // Output
    // 
    // [
    //   {
    //     "params": [
    //       {
    //         "key": "index_type",
    //         "value": "AUTOINDEX"
    //       },
    //       {
    //         "key": "metric_type",
    //         "value": "COSINE"
    //       }
    //     ],
    //     "index_name": "vector_index",
    //     "indexID": "449007919953063141",
    //     "field_name": "vector",
    //     "indexed_rows": "0",
    //     "total_rows": "0",
    //     "state": "Finished",
    //     "index_state_fail_reason": "",
    //     "pending_index_rows": "0"
    //   }
    // ]
    // 

    // 6. Drop the index
    res = await client.dropIndex({
        collection_name: "customized_setup",
        index_name: "vector_index"
    })

    console.log(res.error_code)

    // Output
    // 
    // Success
    // 

    res = await client.dropIndex({
        collection_name: "customized_setup",
        index_name: "primary_field_index"
    })

    console.log(res.error_code)

    // Output
    // 
    // Success
    // 

    // 7. Drop the collection
    res = await client.dropCollection({
        collection_name: "customized_setup",
    })

    console.log(res.error_code)

    // Output
    // 
    // Success
    // 

}

main()