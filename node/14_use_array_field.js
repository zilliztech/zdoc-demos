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
    //     0.30695661540580166,
    //     0.0683065292996865,
    //     0.1460991476466451,
    //     0.22134006614929214,
    //     0.05159331158626035
    //   ],
    //   color: 'pink',
    //   color_tag: 7260,
    //   color_coord: [ 23, 31, 4, 37 ]
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
    //   timestamp: '448186242960719874'
    // }
    // 


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
    //         "score": 1.6622756719589233,
    //         "id": "564",
    //         "color": "red",
    //         "color_tag": "4379",
    //         "color_coord": [
    //             "6",
    //             "0",
    //             "30"
    //         ]
    //     },
    //     {
    //         "score": 1.6098737716674805,
    //         "id": "308",
    //         "color": "grey",
    //         "color_tag": "4518",
    //         "color_coord": [
    //             "8",
    //             "10",
    //             "25",
    //             "14",
    //             "34"
    //         ]
    //     },
    //     {
    //         "score": 1.4963085651397705,
    //         "id": "610",
    //         "color": "purple",
    //         "color_tag": "3431",
    //         "color_coord": [
    //             "1",
    //             "36"
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
    //         "score": 1.6098737716674805,
    //         "id": "308",
    //         "color": "grey",
    //         "color_tag": "4518",
    //         "color_coord": [
    //             "8",
    //             "10",
    //             "25",
    //             "14",
    //             "34"
    //         ]
    //     },
    //     {
    //         "score": 1.4961185455322266,
    //         "id": "203",
    //         "color": "yellow",
    //         "color_tag": "4338",
    //         "color_coord": [
    //             "8",
    //             "9"
    //         ]
    //     },
    //     {
    //         "score": 1.4894033670425415,
    //         "id": "870",
    //         "color": "brown",
    //         "color_tag": "7322",
    //         "color_coord": [
    //             "8",
    //             "9",
    //             "33"
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
    //         "score": 1.6098737716674805,
    //         "id": "308",
    //         "color_coord": [
    //             "8",
    //             "10",
    //             "25",
    //             "14",
    //             "34"
    //         ],
    //         "color": "grey",
    //         "color_tag": "4518"
    //     },
    //     {
    //         "score": 1.4775745868682861,
    //         "id": "35",
    //         "color_coord": [
    //             "10",
    //             "16"
    //         ],
    //         "color": "brown",
    //         "color_tag": "5497"
    //     },
    //     {
    //         "score": 1.4648866653442383,
    //         "id": "355",
    //         "color_coord": [
    //             "10",
    //             "9",
    //             "34",
    //             "14"
    //         ],
    //         "color": "white",
    //         "color_tag": "4509"
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
    //         "score": 1.2061448097229004,
    //         "id": "967",
    //         "color": "grey",
    //         "color_tag": "9709",
    //         "color_coord": [
    //             "8",
    //             "10",
    //             "9",
    //             "21",
    //             "7"
    //         ]
    //     },
    //     {
    //         "score": 1.1449353694915771,
    //         "id": "221",
    //         "color": "blue",
    //         "color_tag": "3574",
    //         "color_coord": [
    //             "15",
    //             "8",
    //             "0",
    //             "7"
    //         ]
    //     },
    //     {
    //         "score": 1.1130155324935913,
    //         "id": "576",
    //         "color": "grey",
    //         "color_tag": "8260",
    //         "color_coord": [
    //             "7",
    //             "8",
    //             "31",
    //             "11",
    //             "19"
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
    //         "score": 1.6098737716674805,
    //         "id": "308",
    //         "color": "grey",
    //         "color_tag": "4518",
    //         "color_coord": [
    //             "8",
    //             "10",
    //             "25",
    //             "14",
    //             "34"
    //         ]
    //     },
    //     {
    //         "score": 1.549060583114624,
    //         "id": "471",
    //         "color": "green",
    //         "color_tag": "9836",
    //         "color_coord": [
    //             "37",
    //             "22",
    //             "9"
    //         ]
    //     },
    //     {
    //         "score": 1.5089154243469238,
    //         "id": "844",
    //         "color": "white",
    //         "color_tag": "6711",
    //         "color_coord": [
    //             "26",
    //             "9",
    //             "18"
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
    //         "score": 1.5182652473449707,
    //         "id": "531",
    //         "color_coord": [
    //             "34",
    //             "6",
    //             "15",
    //             "21"
    //         ],
    //         "color": "blue",
    //         "color_tag": "6693"
    //     },
    //     {
    //         "score": 1.4957042932510376,
    //         "id": "459",
    //         "color_coord": [
    //             "39",
    //             "9",
    //             "36",
    //             "15"
    //         ],
    //         "color": "pink",
    //         "color_tag": "1805"
    //     },
    //     {
    //         "score": 1.4648866653442383,
    //         "id": "355",
    //         "color_coord": [
    //             "10",
    //             "9",
    //             "34",
    //             "14"
    //         ],
    //         "color": "white",
    //         "color_tag": "4509"
    //     }
    // ]
    // 


    await client.dropCollection({
        collection_name: "test_collection",
    })

}

main()