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

    // 3. Insert randomly generated vectors
    const colors = ["green", "blue", "yellow", "red", "black", "white", "purple", "pink", "orange", "brown", "grey"]
    var data = []

    for (let i = 0; i < 1000; i++) {
        current_color = colors[Math.floor(Math.random() * colors.length)]
        current_tag = Math.floor(Math.random() * 8999 + 1000)
        data.push({
            "id": i,
            "vector": [Math.random(), Math.random(), Math.random(), Math.random(), Math.random()],
            "color": current_color,
            "tag": current_tag,
            "color_tag": `${current_color}_${current_tag}`
        })
    }

    console.log(data[0])

    // Output
    // 
    // {
    //   id: 0,
    //   vector: [
    //     0.4079589522056084,
    //     0.8722964388388377,
    //     0.00173434210748602,
    //     0.9321480028417519,
    //     0.6234270861758995
    //   ],
    //   color: 'white',
    //   tag: 4013,
    //   color_tag: 'white_4013'
    // }
    // 

    res = await client.insert({
        collection_name: "quick_setup",
        data: data
    })

    console.log(res)

    // Output
    // 
    // {
    //   succ_index: [
    //      0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11,
    //     12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23,
    //     24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35,
    //     36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47,
    //     48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59,
    //     60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71,
    //     72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83,
    //     84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95,
    //     96, 97, 98, 99,
    //     ... 900 more items
    //   ],
    //   err_index: [],
    //   status: {
    //     error_code: 'Success',
    //     reason: '',
    //     code: 0,
    //     retriable: false,
    //     detail: ''
    //   },
    //   IDs: { int_id: { data: [Array] }, id_field: 'int_id' },
    //   acknowledged: false,
    //   insert_cnt: '1000',
    //   delete_cnt: '0',
    //   upsert_cnt: '0',
    //   timestamp: '448167895130701826'
    // }
    // 

    await sleep(5000)

    // 4. Create partitions and insert more entities
    await client.createPartition({
        collection_name: "quick_setup",
        partition_name: "partitionA"
    })

    await client.createPartition({
        collection_name: "quick_setup",
        partition_name: "partitionB"
    })

    data = []

    for (let i = 1000; i < 1500; i++) {
        current_color = colors[Math.floor(Math.random() * colors.length)]
        current_tag = Math.floor(Math.random() * 8999 + 1000)
        data.push({
            "id": i,
            "vector": [Math.random(), Math.random(), Math.random(), Math.random(), Math.random()],
            "color": current_color,
            "tag": current_tag,
            "color_tag": `${current_color}_${current_tag}`
        })
    }

    res = await client.insert({
        collection_name: "quick_setup",
        data: data,
        partition_name: "partitionA"
    })


    console.log(res)

    // Output
    // 
    // {
    //   succ_index: [
    //      0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11,
    //     12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23,
    //     24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35,
    //     36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47,
    //     48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59,
    //     60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71,
    //     72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83,
    //     84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95,
    //     96, 97, 98, 99,
    //     ... 400 more items
    //   ],
    //   err_index: [],
    //   status: {
    //     error_code: 'Success',
    //     reason: '',
    //     code: 0,
    //     retriable: false,
    //     detail: ''
    //   },
    //   IDs: { int_id: { data: [Array] }, id_field: 'int_id' },
    //   acknowledged: false,
    //   insert_cnt: '500',
    //   delete_cnt: '0',
    //   upsert_cnt: '0',
    //   timestamp: '448167896520327170'
    // }
    // 

    await sleep(5000)

    data = []

    for (let i = 1500; i < 2000; i++) {
        current_color = colors[Math.floor(Math.random() * colors.length)]
        current_tag = Math.floor(Math.random() * 8999 + 1000)
        data.push({
            "id": i,
            "vector": [Math.random(), Math.random(), Math.random(), Math.random(), Math.random()],
            "color": current_color,
            "tag": current_tag,
            "color_tag": `${current_color}_${current_tag}`
        })
    }
    
    res = await client.insert({
        collection_name: "quick_setup",
        data: data,
        partition_name: "partitionB"
    })

    console.log(res)

    // Output
    // 
    // {
    //   succ_index: [
    //      0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11,
    //     12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23,
    //     24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35,
    //     36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47,
    //     48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59,
    //     60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71,
    //     72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83,
    //     84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95,
    //     96, 97, 98, 99,
    //     ... 400 more items
    //   ],
    //   err_index: [],
    //   status: {
    //     error_code: 'Success',
    //     reason: '',
    //     code: 0,
    //     retriable: false,
    //     detail: ''
    //   },
    //   IDs: { int_id: { data: [Array] }, id_field: 'int_id' },
    //   acknowledged: false,
    //   insert_cnt: '500',
    //   delete_cnt: '0',
    //   upsert_cnt: '0',
    //   timestamp: '448167897988333569'
    // }
    // 

    await sleep(5000)

    // 5. Get entities by id
    res = await client.get({
        collection_name: "quick_setup",
        ids: [0, 1, 2]
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
    //   data: [ { id: '0' }, { id: '1' }, { id: '2' } ]
    // }
    // 

    // 5.1 Get entities by id in a partition
    res = await client.get({
        collection_name: "quick_setup",
        ids: [1000, 1001, 1002],
        partition_names: ["partitionA"]
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
    //   data: [ { id: '1000' }, { id: '1001' }, { id: '1002' } ]
    // }
    // 

    // 6. Use basic operators
    res = await client.query({
        collection_name: "quick_setup",
        filter: "1000 < tag < 1500",
        output_fields: ["color_tag"],
        limit: 3
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
    //   data: [
    //     { '$meta': [Object], id: '46' },
    //     { '$meta': [Object], id: '51' },
    //     { '$meta': [Object], id: '114' }
    //   ]
    // }
    // 

    res = await client.query({
        collection_name: "quick_setup",
        filter: 'color == "brown"',
        output_fields: ["color_tag"],
        limit: 3
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
    //   data: [
    //     { id: '3', '$meta': [Object] },
    //     { id: '10', '$meta': [Object] },
    //     { id: '24', '$meta': [Object] }
    //   ]
    // }
    // 

    res = await client.query({
        collection_name: "quick_setup",
        filter: 'color not in ["green", "purple"]',
        output_fields: ["color_tag"],
        limit: 3
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
    //   data: [
    //     { '$meta': [Object], id: '0' },
    //     { '$meta': [Object], id: '1' },
    //     { '$meta': [Object], id: '2' }
    //   ]
    // }
    // 

    res = await client.query({
        collection_name: "quick_setup",
        filter: 'color_tag like "red%"',
        output_fields: ["color_tag"],
        limit: 3
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
    //   data: [
    //     { '$meta': [Object], id: '11' },
    //     { '$meta': [Object], id: '27' },
    //     { '$meta': [Object], id: '29' }
    //   ]
    // }
    // 

    res = await client.query({
        collection_name: "quick_setup",
        filter: '(color == "red") and (1000 < tag < 1500)',
        output_fields: ["color_tag"],
        limit: 3
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
    //   data: [
    //     { '$meta': [Object], id: '259' },
    //     { '$meta': [Object], id: '336' },
    //     { '$meta': [Object], id: '367' }
    //   ]
    // }
    // 

    // 7. Use advanced operators
    // Count the total number of entities in a collection
    res = await client.query({
        collection_name: "quick_setup",
        output_fields: ["count(*)"]
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
    //   data: [ { 'count(*)': '2000' } ]
    // }
    // 
    
    // Count the number of entities in a partition
    res = await client.query({
        collection_name: "quick_setup",
        output_fields: ["count(*)"],
        partition_names: ["partitionA"]
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
    //   data: [ { 'count(*)': '500' } ]
    // }
    // 
    
    // Count the number of entities that match a specific filter
    res = await client.query({
        collection_name: "quick_setup",
        filter: '(color == "red") and (1000 < tag < 1500)',
        output_fields: ["count(*)"]
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
    //   data: [ { 'count(*)': '11' } ]
    // }
    // 

    // 8. Drop the collection
    await client.dropCollection({
        collection_name: "quick_setup"
    })
}

main()
