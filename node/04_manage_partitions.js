const fs = require("fs")
const { MilvusClient, DataType, sleep } = require("@zilliz/milvus2-sdk-node")

const address = "YOUR_CLUSTER_ENDPOINT"
const token = "YOUR_CLUSTER_TOKEN"

async function main() {
    // 1. Set up a Milvus Client
    client = new MilvusClient({address, token});

    // 2. Create a collection in quick setup mode
    await client.createCollection({
        collection_name: "quick_setup",
        dimension: 5,
    });  

    // 3. List partitions
    res = await client.listPartitions({
        collection_name: "quick_setup"
    })

    console.log(res.partition_names)

    // Output
    // 
    // [ '_default' ]
    // 

    // 4. Create more partitions
    await client.createPartition({
        collection_name: "quick_setup",
        partition_name: "partitionA"
    })

    await client.createPartition({
        collection_name: "quick_setup",
        partition_name: "partitionB"
    })

    res = await client.listPartitions({
        collection_name: "quick_setup"
    })

    console.log(res.partition_names)

    // Output
    // 
    // [ '_default', 'partitionA', 'partitionB' ]
    // 

    // 5. Check whether a partition exists
    res = await client.hasPartition({
        collection_name: "quick_setup",
        partition_name: "partitionA"
    })

    console.log(res.value)

    // Output
    // 
    // true
    // 

    res = await client.hasPartition({
        collection_name: "quick_setup",
        partition_name: "partitionC"
    })
    
    console.log(res.value)

    // Output
    // 
    // false
    // 

    // 6. Load a partition indenpendantly
    await client.releaseCollection({
        collection_name: "quick_setup"
    })

    res = await client.getLoadState({
        collection_name: "quick_setup"
    })

    console.log(res.state)

    // Output
    // 
    // LoadStateNotLoad
    // 

    await client.loadPartitions({
        collection_name: "quick_setup",
        partition_names: ["partitionA"]
    })

    await sleep(3000)
    
    res = await client.getLoadState({
        collection_name: "quick_setup"
    })

    console.log(res.state)

    // Output
    // 
    // LoadStateLoaded
    // 

    res = await client.getLoadState({
        collection_name: "quick_setup",
        partition_name: "partitionA"
    })

    console.log(res.state)

    // Output
    // 
    // LoadStateLoaded
    // 

    res = await client.getLoadState({
        collection_name: "quick_setup",
        partition_name: "partitionB"
    })
    
    console.log(res.state)

    // Output
    // 
    // LoadStateLoaded
    // 

    // 7. Release a partition
    await client.releasePartitions({
        collection_name: "quick_setup",
        partition_names: ["partitionA"]
    })

    res = await client.getLoadState({
        collection_name: "quick_setup"
    })

    console.log(res.state)

    // Output
    // 
    // LoadStateNotLoad
    // 

    // 8 Drop a partition
    await client.dropPartition({
        collection_name: "quick_setup",
        partition_name: "partitionB"
    })

    res = await client.listPartitions({
        collection_name: "quick_setup"
    })

    console.log(res.partition_names)

    // Output
    // 
    // [ '_default', 'partitionA' ]
    // 

    // 9. Drop a collection
    await client.dropCollection({
        collection_name: "quick_setup"
    })
}

main()