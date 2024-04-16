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
    //     0.6952911213704391,
    //     0.605530519741055,
    //     0.08509114347185776,
    //     0.28398594562815127,
    //     0.9407322023329812
    //   ],
    //   color: 'blue',
    //   color_tag: 1768,
    //   color_coord: [ 0 ]
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
    //         "score": 1.100941777229309,
    //         "id": "486",
    //         "color_coord": [
    //             "8",
    //             "35"
    //         ],
    //         "color": "purple",
    //         "color_tag": "7651"
    //     },
    //     {
    //         "score": 1.0871820449829102,
    //         "id": "921",
    //         "color_coord": [
    //             "8",
    //             "15",
    //             "27",
    //             "1"
    //         ],
    //         "color": "purple",
    //         "color_tag": "6567"
    //     },
    //     {
    //         "score": 1.0368379354476929,
    //         "id": "245",
    //         "color_coord": [
    //             "1"
    //         ],
    //         "color": "blue",
    //         "color_tag": "4572"
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
    //         "score": 1.100941777229309,
    //         "id": "486",
    //         "color": "purple",
    //         "color_tag": "7651",
    //         "color_coord": [
    //             "8",
    //             "35"
    //         ]
    //     },
    //     {
    //         "score": 1.0871820449829102,
    //         "id": "921",
    //         "color": "purple",
    //         "color_tag": "6567",
    //         "color_coord": [
    //             "8",
    //             "15",
    //             "27",
    //             "1"
    //         ]
    //     },
    //     {
    //         "score": 1.036620855331421,
    //         "id": "414",
    //         "color": "brown",
    //         "color_tag": "8342",
    //         "color_coord": [
    //             "7",
    //             "20"
    //         ]
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
    //         "score": 1.0213146209716797,
    //         "id": "474",
    //         "color_tag": "5722",
    //         "color_coord": [
    //             "10",
    //             "20",
    //             "36",
    //             "11"
    //         ],
    //         "color": "yellow"
    //     },
    //     {
    //         "score": 0.9877312779426575,
    //         "id": "482",
    //         "color_tag": "2147",
    //         "color_coord": [
    //             "10",
    //             "34",
    //             "21",
    //             "14"
    //         ],
    //         "color": "orange"
    //     },
    //     {
    //         "score": 0.9429917335510254,
    //         "id": "34",
    //         "color_tag": "7772",
    //         "color_coord": [
    //             "23",
    //             "10",
    //             "36",
    //             "10",
    //             "4"
    //         ],
    //         "color": "purple"
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
    // [
    //     {
    //         "score": 0.8267516493797302,
    //         "id": "913",
    //         "color": "brown",
    //         "color_tag": "8897",
    //         "color_coord": [
    //             "39",
    //             "31",
    //             "8",
    //             "29",
    //             "7"
    //         ]
    //     },
    //     {
    //         "score": 0.6889009475708008,
    //         "id": "826",
    //         "color": "blue",
    //         "color_tag": "4903",
    //         "color_coord": [
    //             "7",
    //             "25",
    //             "5",
    //             "12",
    //             "8"
    //         ]
    //     },
    //     {
    //         "score": 0.5851659774780273,
    //         "id": "167",
    //         "color": "blue",
    //         "color_tag": "1550",
    //         "color_coord": [
    //             "8",
    //             "27",
    //             "7"
    //         ]
    //     }
    // ]
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
    //         "score": 1.1194220781326294,
    //         "id": "233",
    //         "color": "blue",
    //         "color_tag": "8281",
    //         "color_coord": [
    //             "36",
    //             "25",
    //             "19",
    //             "9",
    //             "35"
    //         ]
    //     },
    //     {
    //         "score": 1.100941777229309,
    //         "id": "486",
    //         "color": "purple",
    //         "color_tag": "7651",
    //         "color_coord": [
    //             "8",
    //             "35"
    //         ]
    //     },
    //     {
    //         "score": 1.0871820449829102,
    //         "id": "921",
    //         "color": "purple",
    //         "color_tag": "6567",
    //         "color_coord": [
    //             "8",
    //             "15",
    //             "27",
    //             "1"
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
    //         "score": 1.0871820449829102,
    //         "id": "921",
    //         "color_coord": [
    //             "8",
    //             "15",
    //             "27",
    //             "1"
    //         ],
    //         "color": "purple",
    //         "color_tag": "6567"
    //     },
    //     {
    //         "score": 1.0213146209716797,
    //         "id": "474",
    //         "color_coord": [
    //             "10",
    //             "20",
    //             "36",
    //             "11"
    //         ],
    //         "color": "yellow",
    //         "color_tag": "5722"
    //     },
    //     {
    //         "score": 1.0074255466461182,
    //         "id": "564",
    //         "color_coord": [
    //             "27",
    //             "8",
    //             "36",
    //             "22"
    //         ],
    //         "color": "white",
    //         "color_tag": "8506"
    //     }
    // ]
    // 

    await client.dropCollection({
        collection_name: "test_collection",
    })

}

main()