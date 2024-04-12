const fs = require("fs")
const { MilvusClient, DataType, sleep } = require("@zilliz/milvus2-sdk-node")

const address = "YOUR_CLUSTER_ENDPOINT"
const token = "YOUR_CLUSTER_TOKEN"

async function main() {
    // 1. Connect to the cluster
    const client = new MilvusClient({address, token})
        
    // 2. Create a collection
    await client.createCollection({
        collection_name: "quick_setup",
        dimension: 5,
    });  

    // 3. Create a collection in customized setup mode
    // 3.1 Define fields
    const fields = [
        {
            name: "my_id",
            data_type: DataType.Int64,
            is_primary_key: true,
            auto_id: false
        },
        {
            name: "my_vector",
            data_type: DataType.FloatVector,
            dim: 5
        },
    ]

    // 3.2 Prepare index parameters
    const index_params = [{
        field_name: "my_vector",
        index_type: "IVF_FLAT",
        metric_type: "IP",
        params: { nlist: 1024}
    }]
    
    // 3.3 Create a collection with fields and index parameters
    await client.createCollection({
        collection_name: "customized_setup_1",
        fields: fields,
        index_params: index_params,
    })

    
    res = await client.getLoadState({
        collection_name: "customized_setup_1"
    })

    console.log(res.state)

    // Output
    // 
    // LoadStateLoaded
    // 

    // 4. Insert data into the collection
    var data = [
        {id: 0, vector: [0.3580376395471989, -0.6023495712049978, 0.18414012509913835, -0.26286205330961354, 0.9029438446296592], color: "pink_8682"},
        {id: 1, vector: [0.19886812562848388, 0.06023560599112088, 0.6976963061752597, 0.2614474506242501, 0.838729485096104], color: "red_7025"},
        {id: 2, vector: [0.43742130801983836, -0.5597502546264526, 0.6457887650909682, 0.7894058910881185, 0.20785793220625592], color: "orange_6781"},
        {id: 3, vector: [0.3172005263489739, 0.9719044792798428, -0.36981146090600725, -0.4860894583077995, 0.95791889146345], color: "pink_9298"},
        {id: 4, vector: [0.4452349528804562, -0.8757026943054742, 0.8220779437047674, 0.46406290649483184, 0.30337481143159106], color: "red_4794"},
        {id: 5, vector: [0.985825131989184, -0.8144651566660419, 0.6299267002202009, 0.1206906911183383, -0.1446277761879955], color: "yellow_4222"},
        {id: 6, vector: [0.8371977790571115, -0.015764369584852833, -0.31062937026679327, -0.562666951622192, -0.8984947637863987], color: "red_9392"},
        {id: 7, vector: [-0.33445148015177995, -0.2567135004164067, 0.8987539745369246, 0.9402995886420709, 0.5378064918413052], color: "grey_8510"},
        {id: 8, vector: [0.39524717779832685, 0.4000257286739164, -0.5890507376891594, -0.8650502298996872, -0.6140360785406336], color: "white_9381"},
        {id: 9, vector: [0.5718280481994695, 0.24070317428066512, -0.3737913482606834, -0.06726932177492717, -0.6980531615588608], color: "purple_4976"}
    ]

    res = await client.insert({
        collection_name: "quick_setup",
        data: data
    })

    console.log(res.insert_cnt)

    // Output
    // 
    // 10
    // 

    // 5. Insert more records
    data = []
    colors = ["green", "blue", "yellow", "red", "black", "white", "purple", "pink", "orange", "brown", "grey"]

    for (i =5; i < 1000; i++) {
        vector = [(Math.random() * (0.99 - 0.01) + 0.01), (Math.random() * (0.99 - 0.01) + 0.01), (Math.random() * (0.99 - 0.01) + 0.01), (Math.random() * (0.99 - 0.01) + 0.01), (Math.random() * (0.99 - 0.01) + 0.01)]
        color = colors[Math.floor(Math.random() * colors.length)] + "_" + Math.floor(Math.random() * (9999 - 1000) + 1000)

        data.push({id: i, vector: vector, color: color})
    }

    res = await client.insert({
        collection_name: "quick_setup",
        data: data
    })

    console.log(res.insert_cnt)

    // Output
    // 
    // 995
    // 

    await sleep(5000)

    // 6. Search with a single vector
    const query_vector = [0.3580376395471989, -0.6023495712049978, 0.18414012509913835, -0.26286205330961354, 0.9029438446296592]

    res = await client.search({
        collection_name: "quick_setup",
        vectors: query_vector,
        limit: 5,
    })

    console.log(res.results)

    // Output
    // 
    // [
    //   { score: 1, id: '0' },
    //   { score: 0.749187171459198, id: '160' },
    //   { score: 0.7374353408813477, id: '109' },
    //   { score: 0.7352343797683716, id: '120' },
    //   { score: 0.7103434205055237, id: '721' }
    // ]
    // 

// 7. Search with multiple vectors
const query_vectors = [
    [0.3580376395471989, -0.6023495712049978, 0.18414012509913835, -0.26286205330961354, 0.9029438446296592], 
    [0.19886812562848388, 0.06023560599112088, 0.6976963061752597, 0.2614474506242501, 0.838729485096104]
]

res = await client.search({
    collection_name: "quick_setup",
    vectors: query_vectors,
    limit: 5,
})

console.log(res.results)

// Output
// 
// [
//   [
//     { score: 1, id: '0' },
//     { score: 0.749187171459198, id: '160' },
//     { score: 0.7374353408813477, id: '109' },
//     { score: 0.7352343797683716, id: '120' },
//     { score: 0.7103434205055237, id: '721' }
//   ],
//   [
//     { score: 0.9999998807907104, id: '1' },
//     { score: 0.983799934387207, id: '247' },
//     { score: 0.9833251237869263, id: '851' },
//     { score: 0.982724666595459, id: '871' },
//     { score: 0.9819263219833374, id: '80' }
//   ]
// ]
// 

// 8. Search with a filter expression using schema-defined fields
res = await client.search({
    collection_name: "quick_setup",
    vectors: query_vector,
    limit: 5,
    filter: "500 < id < 800",
    output_fields: ["id"]
})

console.log(res.results)

// Output
// 
// [
//   { score: 0.7103434205055237, id: '721' },
//   { score: 0.6970766186714172, id: '736' },
//   { score: 0.69532310962677, id: '797' },
//   { score: 0.6908581852912903, id: '642' },
//   { score: 0.634956955909729, id: '715' }
// ]
// 

// 9. Search with a filter expression using non-schema-defined fields
res = await client.search({
    collection_name: "quick_setup",
    vectors: query_vector,
    limit: 5,
    filter: '$meta["color"] like "red%"',
    output_fields: ["color"]
})

console.log(res.results)

// Output
// 
// [
//   { score: 0.6625675559043884, id: '844', color: 'red_6894' },
//   { score: 0.634956955909729, id: '715', color: 'red_2506' },
//   { score: 0.6290165185928345, id: '1', color: 'red_7025' },
//   { score: 0.6236231327056885, id: '539', color: 'red_9562' },
//   { score: 0.6213124990463257, id: '224', color: 'red_3419' }
// ]
// 

// 10. query with schema-defined fields
res = await client.query({
    collection_name: "quick_setup",
    expr: "id in [0, 1, 2, 3, 4]",
    output_fields: ["id", "color"]  
})

console.log(res.data)

// Output
// 
// [
//   { id: '0', '$meta': { color: 'pink_8682' } },
//   { id: '1', '$meta': { color: 'red_7025' } },
//   { id: '2', '$meta': { color: 'orange_6781' } },
//   { id: '3', '$meta': { color: 'pink_9298' } },
//   { id: '4', '$meta': { color: 'red_4794' } }
// ]
// 

// 11. query with non-schema-defined fields
res = await client.query({
    collection_name: "quick_setup",
    expr: '$meta["color"] like "brown_8%"',
    output_fields: ["color"],
    limit: 5
})

console.log(res.data)

// Output
// 
// [
//   { '$meta': { color: 'brown_8242' }, id: '97' },
//   { '$meta': { color: 'brown_8442' }, id: '137' },
//   { '$meta': { color: 'brown_8243' }, id: '146' },
//   { '$meta': { color: 'brown_8105' }, id: '278' },
//   { '$meta': { color: 'brown_8447' }, id: '294' }
// ]
// 

    // 12. Get entities by IDs
    res = await client.get({
        collection_name: "quick_setup",
        ids: [0, 1, 2, 3, 4],
        output_fields: ["vector"]
    })
    
    console.log(res.data)

    // Output
    // 
    // [
    //   {
    //     id: '0',
    //     vector: [
    //       0.35803765058517456,
    //       -0.602349579334259,
    //       0.1841401308774948,
    //       -0.26286205649375916,
    //       0.9029438495635986
    //     ]
    //   },
    //   {
    //     id: '1',
    //     vector: [
    //       0.19886812567710876,
    //       0.060235604643821716,
    //       0.697696328163147,
    //       0.2614474594593048,
    //       0.8387295007705688
    //     ]
    //   },
    //   {
    //     id: '2',
    //     vector: [
    //       0.4374213218688965,
    //       -0.5597502589225769,
    //       0.6457887887954712,
    //       0.789405882358551,
    //       0.20785793662071228
    //     ]
    //   },
    //   {
    //     id: '3',
    //     vector: [
    //       0.31720051169395447,
    //       0.971904456615448,
    //       -0.369811475276947,
    //       -0.48608946800231934,
    //       0.9579188823699951
    //     ]
    //   },
    //   {
    //     id: '4',
    //     vector: [
    //       0.4452349543571472,
    //       -0.8757026791572571,
    //       0.8220779299736023,
    //       0.46406289935112,
    //       0.3033747971057892
    //     ]
    //   }
    // ]
    // 

    // 13. Delete entities by IDs
    res = await client.deleteEntities({
        collection_name: "quick_setup",
        expr: "id in [5, 6, 7, 8, 9]",
        output_fields: ["vector"]
    })

    console.log(res.delete_cnt)

    // Output
    // 
    // 5
    // 

    // 14. Delete entities by filter
    res = await client.delete({
        collection_name: "quick_setup",
        ids: [0, 1, 2, 3, 4]
    })

    console.log(res.delete_cnt)

    // Output
    // 
    // 5
    // 

    // 15. Drop the collection
    res = await client.dropCollection({
        collection_name: "quick_setup"
    })

    console.log(res.error_code)

    // Output
    // 
    // Success
    // 

    res = await client.dropCollection({
        collection_name: "customized_setup"
    })

    console.log(res.error_code)

    // Output
    // 
    // Success
    // 

}

main()