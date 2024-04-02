const fs = require("fs")
const { MilvusClient, DataType, sleep } = require("@zilliz/milvus2-sdk-node")

const address = "YOUR_CLUSTER_ENDPOINT"
const token = "YOUR_CLUSTER_TOKEN"

async function main() {
    // 1. Set up a Milvus Client
    client = new MilvusClient({address, token});

    const version = await client.getVersion();

    console.log(version)

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
    //   version: 'v2.3.10'
    // }
    // 



    // 2. Create a collection in quick setup mode
    await client.createCollection({
        collection_name: "quick_setup",
        dimension: 5,
        metric_type: "IP"
    });  

    // 3. Insert randomly generated vectors
    const colors = ["green", "blue", "yellow", "red", "black", "white", "purple", "pink", "orange", "brown", "grey"]
    data = []

    for (let i = 0; i < 1000; i++) {
        current_color = colors[Math.floor(Math.random() * colors.length)]
        data.push({
            id: i,
            vector: [Math.random(), Math.random(), Math.random(), Math.random(), Math.random()],
            color: current_color,
            color_tag: `${current_color}_${Math.floor(Math.random() * 8999) + 1000}`
        })
    }

    var res = await client.insert({
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
    //   timestamp: '448167613902880770'
    // }
    // 



    // 4. Single vector search
    var query_vector = [Math.random(), Math.random(), Math.random(), Math.random(), Math.random()]

    res = await client.search({
        collection_name: "quick_setup",
        data: [query_vector],
        limit: 5,
    })

    console.log(res)

    // Output
    // 
    // {
    //   status: {
    //     error_code: 'Success',
    //     reason: 'search result is empty',
    //     code: 0,
    //     retriable: false,
    //     detail: ''
    //   },
    //   results: []
    // }
    // 



    // 5. Batch vector search
    var query_vectors = [
        [Math.random(), Math.random(), Math.random(), Math.random(), Math.random()],
        [Math.random(), Math.random(), Math.random(), Math.random(), Math.random()]
    ]

    res = await client.search({
        collection_name: "quick_setup",
        data: query_vectors,
        limit: 5,
    })

    console.log(res)

    // Output
    // 
    // {
    //   status: {
    //     error_code: 'Success',
    //     reason: 'search result is empty',
    //     code: 0,
    //     retriable: false,
    //     detail: ''
    //   },
    //   results: []
    // }
    // 



    // 6. Search within a partition
    await client.createPartition({
        collection_name: "quick_setup",
        partition_name: "red"
    })

    await client.createPartition({
        collection_name: "quick_setup",
        partition_name: "blue"
    })

    // 6.1 Insert data into partitions
    var red_data = []
    var blue_data = []

    for (let i = 1000; i < 1500; i++) {
        red_data.push({
            id: i,
            vector: [Math.random(), Math.random(), Math.random(), Math.random(), Math.random()],
            color: "red",
            color_tag: `red_${Math.floor(Math.random() * 8999) + 1000}`
        })
    }

    for (let i = 1500; i < 2000; i++) {
        blue_data.push({
            id: i,
            vector: [Math.random(), Math.random(), Math.random(), Math.random(), Math.random()],
            color: "blue",
            color_tag: `blue_${Math.floor(Math.random() * 8999) + 1000}`
        })
    }

    res = await client.insert({
        collection_name: "quick_setup",
        data: red_data,
        partition_name: "red"
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
    //   timestamp: '448167613981523970'
    // }
    // 



    res = await client.insert({
        collection_name: "quick_setup",
        data: blue_data,
        partition_name: "blue"
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
    //   timestamp: '448167614007738369'
    // }
    // 



    // 6.2 Search within partitions
    query_vector = [Math.random(), Math.random(), Math.random(), Math.random(), Math.random()]

    res = await client.search({
        collection_name: "quick_setup",
        data: [query_vector],
        partition_names: ["red"],
        limit: 5,
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
    //   results: [
    //     { score: 2.310852527618408, id: '1401' },
    //     { score: 2.2832722663879395, id: '1400' },
    //     { score: 2.2603259086608887, id: '1052' },
    //     { score: 2.2497174739837646, id: '1315' },
    //     { score: 2.2475643157958984, id: '1269' }
    //   ]
    // }
    // 



    res = await client.search({
        collection_name: "quick_setup",
        data: [query_vector],
        partition_names: ["blue"],
        limit: 5,
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
    //   results: [
    //     { score: 2.3310344219207764, id: '1700' },
    //     { score: 2.2636027336120605, id: '1773' },
    //     { score: 2.213263511657715, id: '1565' },
    //     { score: 2.167454719543457, id: '1502' },
    //     { score: 2.159524917602539, id: '1979' }
    //   ]
    // }
    // 



    // 7. Search with output fields
    query_vector = [Math.random(), Math.random(), Math.random(), Math.random(), Math.random()]

    res = await client.search({
        collection_name: "quick_setup",
        data: [query_vector],
        limit: 5,
        output_fields: ["color"],
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
    //   results: [
    //     { score: 2.99387788772583, id: '283', color: 'green' },
    //     { score: 2.884753704071045, id: '1863', color: 'blue' },
    //     { score: 2.8638439178466797, id: '1401', color: 'red' },
    //     { score: 2.862755060195923, id: '1308', color: 'red' },
    //     { score: 2.8455042839050293, id: '1961', color: 'blue' }
    //   ]
    // }
    // 



    // 8. Filtered search
    // 8.1 Filter with "like" operator and prefix wildcard
    query_vector = [Math.random(), Math.random(), Math.random(), Math.random(), Math.random()]

    res = await client.search({
        collection_name: "quick_setup",
        data: [query_vector],
        limit: 5,
        filters: "color_tag like \"red%\"",
        output_fields: ["color_tag"]
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
    //   results: [
    //     { score: 2.6909549236297607, id: '1401', color_tag: 'red_2576' },
    //     { score: 2.639719009399414, id: '283', color_tag: 'green_7649' },
    //     { score: 2.604219913482666, id: '1308', color_tag: 'red_2742' },
    //     { score: 2.455399513244629, id: '407', color_tag: 'orange_2702' },
    //     { score: 2.4457592964172363, id: '1594', color_tag: 'blue_5944' }
    //   ]
    // }
    // 



    // 8.2 Filter with "like" operator and infix wildcard (Milvus 2.4.x or later)
    if (version.version.startsWith("v2.4")) {
        query_vector = [Math.random(), Math.random(), Math.random(), Math.random(), Math.random()]

        res = await client.search({
            collection_name: "quick_setup",
            data: [query_vector],
            limit: 5,
            filters: "color_tag like \"%_4\"",
            output_fields: ["color_tag"]
        })
    } else {
        res = `This demo is not supported in {version.version}`
    }

    // 9. Range search
    query_vector = [Math.random(), Math.random(), Math.random(), Math.random(), Math.random()]

    res = await client.search({
        collection_name: "quick_setup",
        data: [query_vector],
        limit: 5,
        params: {
            radius: 0.1,
            range: 1.0
        },
        output_fields: ["color_tag"]
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
    //   results: [
    //     { score: 2.64945125579834, id: '1401', color_tag: 'red_2576' },
    //     { score: 2.6447155475616455, id: '283', color_tag: 'green_7649' },
    //     { score: 2.5453732013702393, id: '1308', color_tag: 'red_2742' },
    //     { score: 2.5166821479797363, id: '1863', color_tag: 'blue_7237' },
    //     { score: 2.4510371685028076, id: '28', color_tag: 'white_7012' }
    //   ]
    // }
    // 



    // 10. Grouping search
    // TODO

    // 11. Drop collection
    await client.dropCollection({
        collection_name: "quick_setup"
    })


}

main()