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
            data_type: DataType.VarChar,
            max_length: 512
        },
        {
            name: "color_tag",
            data_type: DataType.Int64,
        },
        {
            name: "color_coord",
            data_type: DataType.Array,
            element_type: DataType.Int64,
            max_capacity: 5
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
        const current_coord = Array(Math.floor(Math.random() * 5 + 1)).fill(0).map(() => Math.floor(Math.random() * 40))

        data.push({
            id: i,
            vector: Array(5).fill(0).map(() => Math.random()),
            color: current_color,
            color_tag: current_tag,
            color_coord: current_coord,
        })
    }

    console.log(data[0])

    // Output
    // 
    // {
    //   id: 0,
    //   vector: [
    //     0.0338537420906162,
    //     0.6844108238358322,
    //     0.28410588909961754,
    //     0.09752595400212116,
    //     0.22671013058761114
    //   ],
    //   color: 'orange',
    //   color_tag: 5677,
    //   color_coord: [ 3, 0, 18, 29 ]
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

    // 4. Basic search with the array field
    const query_vectors = [Array(5).fill(0).map(() => Math.random())]
    
    res = await client.search({
        collection_name: "test_collection",
        data: query_vectors,
        filter: "color_coord[0] < 10",
        output_fields: ["id", "color", "color_tag", "color_coord"],
        limit: 3
    })

    console.log(JSON.stringify(res.results, null, 4))

    // Output
    // 
    // [
    //     {
    //         "score": 2.015894889831543,
    //         "id": "260",
    //         "color": "green",
    //         "color_tag": "5320",
    //         "color_coord": [
    //             "1",
    //             "7",
    //             "33",
    //             "13",
    //             "23"
    //         ]
    //     },
    //     {
    //         "score": 2.006500720977783,
    //         "id": "720",
    //         "color": "green",
    //         "color_tag": "4939",
    //         "color_coord": [
    //             "0",
    //             "19",
    //             "5",
    //             "30",
    //             "15"
    //         ]
    //     },
    //     {
    //         "score": 1.9539016485214233,
    //         "id": "243",
    //         "color": "red",
    //         "color_tag": "2403",
    //         "color_coord": [
    //             "4"
    //         ]
    //     }
    // ]
    // 

    res = await client.search({
        collection_name: "test_collection",
        data: query_vectors,
        filter: "color_coord[0] in [7, 8, 9]",
        output_fields: ["id", "color", "color_tag", "color_coord"],
        limit: 3
    })

    console.log(JSON.stringify(res.results, null, 4))

    // Output
    // 
    // [
    //     {
    //         "score": 1.783075213432312,
    //         "id": "593",
    //         "color_tag": "4079",
    //         "color_coord": [
    //             "8",
    //             "19"
    //         ],
    //         "color": "orange"
    //     },
    //     {
    //         "score": 1.7695187330245972,
    //         "id": "248",
    //         "color_tag": "1618",
    //         "color_coord": [
    //             "7",
    //             "4",
    //             "23"
    //         ],
    //         "color": "blue"
    //     },
    //     {
    //         "score": 1.6041288375854492,
    //         "id": "667",
    //         "color_tag": "4203",
    //         "color_coord": [
    //             "8",
    //             "37",
    //             "31"
    //         ],
    //         "color": "purple"
    //     }
    // ]
    // 

    // 5. Advanced search within the array field
    res = await client.search({
        collection_name: "test_collection",
        data: query_vectors,
        filter: "ARRAY_CONTAINS(color_coord, 10)",
        output_fields: ["id", "color", "color_tag", "color_coord"],
        limit: 3
    })

    console.log(JSON.stringify(res.results, null, 4))

    // Output
    // 
    // [
    //     {
    //         "score": 1.7962548732757568,
    //         "id": "696",
    //         "color": "red",
    //         "color_tag": "1798",
    //         "color_coord": [
    //             "33",
    //             "10",
    //             "37"
    //         ]
    //     },
    //     {
    //         "score": 1.7126177549362183,
    //         "id": "770",
    //         "color": "red",
    //         "color_tag": "1962",
    //         "color_coord": [
    //             "21",
    //             "23",
    //             "10"
    //         ]
    //     },
    //     {
    //         "score": 1.6707111597061157,
    //         "id": "981",
    //         "color": "yellow",
    //         "color_tag": "3100",
    //         "color_coord": [
    //             "28",
    //             "39",
    //             "10",
    //             "6"
    //         ]
    //     }
    // ]
    // 

    res = await client.search({
        collection_name: "test_collection",
        data: query_vectors,
        filter: "ARRAY_CONTAINS_ALL(color_coord, [7, 8])",
        output_fields: ["id", "color", "color_tag", "color_coord"],
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
        filter: "ARRAY_CONTAINS_ANY(color_coord, [7, 8, 9])",
        output_fields: ["id", "color", "color_tag", "color_coord"],
        limit: 3
    })

    console.log(JSON.stringify(res.results, null, 4))

    // Output
    // 
    // [
    //     {
    //         "score": 2.015894889831543,
    //         "id": "260",
    //         "color": "green",
    //         "color_tag": "5320",
    //         "color_coord": [
    //             "1",
    //             "7",
    //             "33",
    //             "13",
    //             "23"
    //         ]
    //     },
    //     {
    //         "score": 1.783075213432312,
    //         "id": "593",
    //         "color": "orange",
    //         "color_tag": "4079",
    //         "color_coord": [
    //             "8",
    //             "19"
    //         ]
    //     },
    //     {
    //         "score": 1.7713876962661743,
    //         "id": "874",
    //         "color": "blue",
    //         "color_tag": "7029",
    //         "color_coord": [
    //             "14",
    //             "8",
    //             "15"
    //         ]
    //     }
    // ]
    // 

    res = await client.search({
        collection_name: "test_collection",
        data: query_vectors,
        filter: "ARRAY_LENGTH(color_coord) == 4",
        output_fields: ["id", "color", "color_tag", "color_coord"],
        limit: 3
    })

    console.log(JSON.stringify(res.results, null, 4))

    // Output
    // 
    // [
    //     {
    //         "score": 2.0404388904571533,
    //         "id": "439",
    //         "color": "orange",
    //         "color_tag": "7096",
    //         "color_coord": [
    //             "27",
    //             "34",
    //             "26",
    //             "39"
    //         ]
    //     },
    //     {
    //         "score": 1.9059759378433228,
    //         "id": "918",
    //         "color": "purple",
    //         "color_tag": "2903",
    //         "color_coord": [
    //             "28",
    //             "19",
    //             "36",
    //             "35"
    //         ]
    //     },
    //     {
    //         "score": 1.8385567665100098,
    //         "id": "92",
    //         "color": "yellow",
    //         "color_tag": "4693",
    //         "color_coord": [
    //             "1",
    //             "23",
    //             "2",
    //             "3"
    //         ]
    //     }
    // ]
    // 

    await client.dropCollection({
        collection_name: "test_collection",
    })

}

main()