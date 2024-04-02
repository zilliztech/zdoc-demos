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

    console.log(res)

    // Output
    // 
    // {
    //   error_code: 'Success',
    //   reason: '',
    //   code: 0,
    //   retriable: false,
    //   detail: ''
    // }
    // 





    res = await client.getLoadState({
        collection_name: "test_collection",
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
    //     0.26360857467104326,
    //     0.4749221816507778,
    //     0.516524403744753,
    //     0.8042319045856339,
    //     0.6586510992013501
    //   ],
    //   color: { label: 'blue', tag: 2425, coord: [ 3, 17, 27 ], ref: [ [Array] ] }
    // }
    // 





    res = await client.insert({
        collection_name: "test_collection",
        data: data,
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
    //   timestamp: '448186258492751874'
    // }
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
    //         "score": 1.8816416263580322,
    //         "id": "625",
    //         "color": {
    //             "label": "red",
    //             "tag": 9032,
    //             "coord": [
    //                 4,
    //                 15,
    //                 18
    //             ],
    //             "ref": [
    //                 [
    //                     "blue",
    //                     "yellow",
    //                     "blue"
    //                 ]
    //             ]
    //         }
    //     },
    //     {
    //         "score": 1.7273869514465332,
    //         "id": "552",
    //         "color": {
    //             "label": "red",
    //             "tag": 5747,
    //             "coord": [
    //                 7,
    //                 32,
    //                 6
    //             ],
    //             "ref": [
    //                 [
    //                     "purple",
    //                     "red",
    //                     "purple"
    //                 ]
    //             ]
    //         }
    //     },
    //     {
    //         "score": 1.7183022499084473,
    //         "id": "517",
    //         "color": {
    //             "label": "red",
    //             "tag": 1564,
    //             "coord": [
    //                 32,
    //                 33,
    //                 25
    //             ],
    //             "ref": [
    //                 [
    //                     "brown",
    //                     "grey",
    //                     "brown"
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
    // [
    //     {
    //         "score": 1.6892653703689575,
    //         "id": "34",
    //         "color": {
    //             "label": "grey",
    //             "tag": 6638,
    //             "coord": [
    //                 16,
    //                 36,
    //                 11
    //             ],
    //             "ref": [
    //                 [
    //                     "blue",
    //                     "brown",
    //                     "grey"
    //                 ]
    //             ]
    //         }
    //     }
    // ]
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
    //         "score": 1.6978987455368042,
    //         "id": "127",
    //         "color": {
    //             "label": "white",
    //             "tag": 9263,
    //             "coord": [
    //                 5,
    //                 4,
    //                 5
    //             ],
    //             "ref": [
    //                 [
    //                     "grey",
    //                     "brown",
    //                     "yellow"
    //                 ]
    //             ]
    //         }
    //     },
    //     {
    //         "score": 1.5648834705352783,
    //         "id": "633",
    //         "color": {
    //             "label": "blue",
    //             "tag": 3265,
    //             "coord": [
    //                 4,
    //                 24,
    //                 5
    //             ],
    //             "ref": [
    //                 [
    //                     "white",
    //                     "blue",
    //                     "black"
    //                 ]
    //             ]
    //         }
    //     },
    //     {
    //         "score": 0.8837333917617798,
    //         "id": "528",
    //         "color": {
    //             "label": "white",
    //             "tag": 7735,
    //             "coord": [
    //                 4,
    //                 5,
    //                 11
    //             ],
    //             "ref": [
    //                 [
    //                     "pink",
    //                     "blue",
    //                     "grey"
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
    //         "score": 1.9774878025054932,
    //         "id": "856",
    //         "color": {
    //             "label": "purple",
    //             "tag": 1854,
    //             "coord": [
    //                 4,
    //                 0,
    //                 34
    //             ],
    //             "ref": [
    //                 [
    //                     "pink",
    //                     "white",
    //                     "white"
    //                 ]
    //             ]
    //         }
    //     },
    //     {
    //         "score": 1.9525892734527588,
    //         "id": "22",
    //         "color": {
    //             "label": "pink",
    //             "tag": 1264,
    //             "coord": [
    //                 4,
    //                 29,
    //                 35
    //             ],
    //             "ref": [
    //                 [
    //                     "orange",
    //                     "orange",
    //                     "grey"
    //                 ]
    //             ]
    //         }
    //     },
    //     {
    //         "score": 1.8816416263580322,
    //         "id": "625",
    //         "color": {
    //             "label": "red",
    //             "tag": 9032,
    //             "coord": [
    //                 4,
    //                 15,
    //                 18
    //             ],
    //             "ref": [
    //                 [
    //                     "blue",
    //                     "yellow",
    //                     "blue"
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