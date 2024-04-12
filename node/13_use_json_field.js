const fs = require("fs")
const { MilvusClient, DataType, sleep } = require("@zilliz/milvus2-sdk-node")

const address = "YOUR_CLUSTER_ENDPOINT"
const token = "YOUR_CLUSTER_TOKEN"

async function main() {
    // 1. Set up a Milvus Client
    client = new MilvusClient({address, token}); 

    // 2. Create a collection
    // 2.1 Define fields
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
        {
            name: "color",
            data_type: DataType.JSON,
        }
    ]

    // 2.2 Prepare index parameters
    const index_params = [{
        field_name: "vector",
        index_type: "IVF_FLAT",
        metric_type: "IP",
        params: { nlist: 1024}
    }]
    
    // 2.3 Create a collection with fields and index parameters
    res = await client.createCollection({
        collection_name: "test_collection",
        fields: fields, 
        index_params: index_params
    })

    console.log(res.error_code)

    // Output
    // 
    // Success
    // 

    res = await client.getLoadState({
        collection_name: "test_collection",
    })  

    console.log(res.state)

    // Output
    // 
    // LoadStateLoaded
    // 

    // 3. Insert randomly generated vectors 
    const colors = ["green", "blue", "yellow", "red", "black", "white", "purple", "pink", "orange", "brown", "grey"]
    var data = []

    for (let i = 0; i < 1000; i++) {
        const current_color = colors[Math.floor(Math.random() * colors.length)]
        const current_tag = Math.floor(Math.random() * 8999 + 1000)
        const current_coord = Array(3).fill(0).map(() => Math.floor(Math.random() * 40))
        const current_ref = [ Array(3).fill(0).map(() => colors[Math.floor(Math.random() * colors.length)]) ]

        data.push({
            id: i,
            vector: [Math.random(), Math.random(), Math.random(), Math.random(), Math.random()],
            color: {
                label: current_color,
                tag: current_tag,
                coord: current_coord,
                ref: current_ref
            }
        })
    }

    console.log(data[0])

    // Output
    // 
    // {
    //   id: 0,
    //   vector: [
    //     0.11455530974226114,
    //     0.21704086958595314,
    //     0.9430119822312437,
    //     0.7802712923612023,
    //     0.9106927960926137
    //   ],
    //   color: { label: 'grey', tag: 7393, coord: [ 22, 1, 22 ], ref: [ [Array] ] }
    // }
    // 

    res = await client.insert({
        collection_name: "test_collection",
        data: data,
    })

    console.log(res.insert_cnt)

    // Output
    // 
    // 1000
    // 

    await sleep(5000)

    // 4. Basic search with a JSON field
    query_vectors = [[0.6765405125697714, 0.759217474274025, 0.4122471841491111, 0.3346805565394215, 0.09679748345514638]]

    res = await client.search({
        collection_name: "test_collection",
        data: query_vectors,
        filter: 'color["label"] in ["red"]',
        output_fields: ["color", "id"],
        limit: 3
    })

    console.log(JSON.stringify(res.results, null, 4))

    // Output
    // 
    // [
    //     {
    //         "score": 1.777988076210022,
    //         "id": "595",
    //         "color": {
    //             "label": "red",
    //             "tag": 7393,
    //             "coord": [
    //                 31,
    //                 34,
    //                 18
    //             ],
    //             "ref": [
    //                 [
    //                     "grey",
    //                     "white",
    //                     "orange"
    //                 ]
    //             ]
    //         }
    //     },
    //     {
    //         "score": 1.7542595863342285,
    //         "id": "82",
    //         "color": {
    //             "label": "red",
    //             "tag": 8636,
    //             "coord": [
    //                 4,
    //                 37,
    //                 29
    //             ],
    //             "ref": [
    //                 [
    //                     "brown",
    //                     "brown",
    //                     "pink"
    //                 ]
    //             ]
    //         }
    //     },
    //     {
    //         "score": 1.7537562847137451,
    //         "id": "748",
    //         "color": {
    //             "label": "red",
    //             "tag": 1626,
    //             "coord": [
    //                 31,
    //                 4,
    //                 25
    //             ],
    //             "ref": [
    //                 [
    //                     "grey",
    //                     "green",
    //                     "blue"
    //                 ]
    //             ]
    //         }
    //     }
    // ]
    // 

    // 5. Advanced search within a JSON field
    res = await client.search({
        collection_name: "test_collection",
        data: query_vectors,
        filter: 'JSON_CONTAINS(color["ref"], ["blue", "brown", "grey"])',
        output_fields: ["color", "id"],
        limit: 3
    })

    console.log(JSON.stringify(res.results, null, 4))

    // Output
    // 
    // []
    // 

    res = await client.search({
        collection_name: "test_collection",
        data: query_vectors,
        filter: 'JSON_CONTAINS_ALL(color["coord"], [4, 5])',
        output_fields: ["color", "id"],
        limit: 3
    })

    console.log(JSON.stringify(res.results, null, 4))

    // Output
    // 
    // [
    //     {
    //         "score": 1.8944344520568848,
    //         "id": "792",
    //         "color": {
    //             "label": "purple",
    //             "tag": 8161,
    //             "coord": [
    //                 4,
    //                 38,
    //                 5
    //             ],
    //             "ref": [
    //                 [
    //                     "red",
    //                     "white",
    //                     "grey"
    //                 ]
    //             ]
    //         }
    //     },
    //     {
    //         "score": 1.2801706790924072,
    //         "id": "489",
    //         "color": {
    //             "label": "red",
    //             "tag": 4358,
    //             "coord": [
    //                 5,
    //                 4,
    //                 1
    //             ],
    //             "ref": [
    //                 [
    //                     "blue",
    //                     "orange",
    //                     "orange"
    //                 ]
    //             ]
    //         }
    //     },
    //     {
    //         "score": 1.2097992897033691,
    //         "id": "656",
    //         "color": {
    //             "label": "red",
    //             "tag": 7856,
    //             "coord": [
    //                 5,
    //                 20,
    //                 4
    //             ],
    //             "ref": [
    //                 [
    //                     "black",
    //                     "orange",
    //                     "white"
    //                 ]
    //             ]
    //         }
    //     }
    // ]
    // 

    res = await client.search({
        collection_name: "test_collection",
        data: query_vectors,
        filter: 'JSON_CONTAINS_ANY(color["coord"], [4, 5])',
        output_fields: ["color", "id"],
        limit: 3
    })

    console.log(JSON.stringify(res.results, null, 4))

    // Output
    // 
    // [
    //     {
    //         "score": 1.9083369970321655,
    //         "id": "453",
    //         "color": {
    //             "label": "brown",
    //             "tag": 8788,
    //             "coord": [
    //                 21,
    //                 18,
    //                 5
    //             ],
    //             "ref": [
    //                 [
    //                     "pink",
    //                     "black",
    //                     "brown"
    //                 ]
    //             ]
    //         }
    //     },
    //     {
    //         "score": 1.8944344520568848,
    //         "id": "792",
    //         "color": {
    //             "label": "purple",
    //             "tag": 8161,
    //             "coord": [
    //                 4,
    //                 38,
    //                 5
    //             ],
    //             "ref": [
    //                 [
    //                     "red",
    //                     "white",
    //                     "grey"
    //                 ]
    //             ]
    //         }
    //     },
    //     {
    //         "score": 1.8615753650665283,
    //         "id": "272",
    //         "color": {
    //             "label": "grey",
    //             "tag": 3400,
    //             "coord": [
    //                 5,
    //                 1,
    //                 32
    //             ],
    //             "ref": [
    //                 [
    //                     "purple",
    //                     "green",
    //                     "white"
    //                 ]
    //             ]
    //         }
    //     }
    // ]
    // 

    await client.dropCollection({
        collection_name: "test_collection",
    })

}

main()