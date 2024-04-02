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

    console.log(res)

    // Output
    // 
    // {
    //   partition_names: [ '_default' ],
    //   partitionIDs: [ '448162378879033217' ],
    //   created_timestamps: [ '448166680294588419' ],
    //   created_utc_timestamps: [ '1709620209864' ],
    //   inMemory_percentages: [],
    //   status: {
    //     error_code: 'Success',
    //     reason: '',
    //     code: 0,
    //     retriable: false,
    //     detail: ''
    //   }
    // }
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

    console.log(res)

    // Output
    // 
    // {
    //   partition_names: [ '_default', 'partitionA', 'partitionB' ],
    //   partitionIDs: [ '448162378879033217', '448162378879033248', '448162378879033252' ],
    //   created_timestamps: [ '448166680294588419', '448166681408700418', '448166681461129219' ],
    //   created_utc_timestamps: [ '1709620209864', '1709620214114', '1709620214314' ],
    //   inMemory_percentages: [],
    //   status: {
    //     error_code: 'Success',
    //     reason: '',
    //     code: 0,
    //     retriable: false,
    //     detail: ''
    //   }
    // }
    // 

    // 5. Check whether a partition exists
    res = await client.hasPartition({
        collection_name: "quick_setup",
        partition_name: "partitionA"
    })

    console.log(res)

    // Output
    // 
    // {
    //   status: {
    //     error_code: 'Success',
    //     reason: '',
    //     code: 0,
    //     retriable: false,
    //     detail: ''
    //   },
    //   value: true
    // }
    // 

    res = await client.hasPartition({
        collection_name: "quick_setup",
        partition_name: "partitionC"
    })
    
    console.log(res)

    // Output
    // 
    // {
    //   status: {
    //     error_code: 'Success',
    //     reason: '',
    //     code: 0,
    //     retriable: false,
    //     detail: ''
    //   },
    //   value: false
    // }
    // 

    // 6. Load a partition indenpendantly
    await client.releaseCollection({
        collection_name: "quick_setup"
    })

    res = await client.getLoadState({
        collection_name: "quick_setup"
    })

    console.log(res)

    // Output
    // 
    // {
    //   status: {
    //     error_code: 'Success',
    //     reason: '',
    //     code: 0,
    //     retriable: false,
    //     detail: ''
    //   },
    //   state: 'LoadStateNotLoad'
    // }
    // 

    await client.loadPartitions({
        collection_name: "quick_setup",
        partition_names: ["partitionA"]
    })

    await sleep(3000)
    
    res = await client.getLoadState({
        collection_name: "quick_setup"
    })

    console.log(res)

    // Output
    // 
    // {
    //   status: {
    //     error_code: 'Success',
    //     reason: '',
    //     code: 0,
    //     retriable: false,
    //     detail: ''
    //   },
    //   state: 'LoadStateLoaded'
    // }
    // 

    res = await client.getLoadState({
        collection_name: "quick_setup",
        partition_name: "partitionA"
    })

    console.log(res)

    // Output
    // 
    // {
    //   status: {
    //     error_code: 'Success',
    //     reason: '',
    //     code: 0,
    //     retriable: false,
    //     detail: ''
    //   },
    //   state: 'LoadStateLoaded'
    // }
    // 

    res = await client.getLoadState({
        collection_name: "quick_setup",
        partition_name: "partitionB"
    })
    
    console.log(res)

    // Output
    // 
    // {
    //   status: {
    //     error_code: 'Success',
    //     reason: '',
    //     code: 0,
    //     retriable: false,
    //     detail: ''
    //   },
    //   state: 'LoadStateLoaded'
    // }
    // 

    // 7. Release a partition
    await client.releasePartitions({
        collection_name: "quick_setup",
        partition_names: ["partitionA"]
    })

    res = await client.getLoadState({
        collection_name: "quick_setup"
    })

    console.log(res)

    // Output
    // 
    // {
    //   status: {
    //     error_code: 'Success',
    //     reason: '',
    //     code: 0,
    //     retriable: false,
    //     detail: ''
    //   },
    //   state: 'LoadStateNotLoad'
    // }
    // 

// 8 Drop a partition
await client.dropPartition({
    collection_name: "quick_setup",
    partition_name: "partitionB"
})

res = await client.listPartitions({
    collection_name: "quick_setup"
})

console.log(res)

// Output
// 
// {
//   partition_names: [ '_default', 'partitionA' ],
//   partitionIDs: [ '448162378879033217', '448162378879033248' ],
//   created_timestamps: [ '448166680294588419', '448166681408700418' ],
//   created_utc_timestamps: [ '1709620209864', '1709620214114' ],
//   inMemory_percentages: [],
//   status: {
//     error_code: 'Success',
//     reason: '',
//     code: 0,
//     retriable: false,
//     detail: ''
//   }
// }
// 

    // 9. Drop a collection
    await client.dropCollection({
        collection_name: "quick_setup"
    })
}

main()