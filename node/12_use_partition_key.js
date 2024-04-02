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
            max_length: 512,
            is_partition_key: true
        }
    ]

    // 2.2 Prepare index parameters
    const index_params = [{
        field_name: "color",
        index_type: "Trie"
    },{
        field_name: "id",
        index_type: "STL_SORT"
    },{
        field_name: "vector",
        index_type: "IVF_FLAT",
        metric_type: "IP",
        params: { nlist: 1024}
    }]
    
    // 2.3 Create a collection with fields and index parameters
    res = await client.createCollection({
        collection_name: "test_collection",
        fields: fields, 
        index_params: index_params,
        enable_dynamic_field: true
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
        data.push({
            id: i,
            vector: [Math.random(), Math.random(), Math.random(), Math.random(), Math.random()],
            color: current_color,
            tag: current_tag,
            color_tag: `${current_color}_${current_tag}`
        })
    }

    console.log(data[0])

    // Output
    // 
    // {
    //   id: 0,
    //   vector: [
    //     0.9044868976050435,
    //     0.6177625444645114,
    //     0.7895390632163257,
    //     0.7680228430101279,
    //     0.9926867413499765
    //   ],
    //   color: 'red',
    //   tag: 7019,
    //   color_tag: 'red_7019'
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
    //   timestamp: '448169526627926018'
    // }
    // 

    // 4. Search with partition key
    const query_vectors = [Math.random(), Math.random(), Math.random(), Math.random(), Math.random()]

    res = await client.search({
        collection_name: "test_collection",
        data: query_vectors,
        filter: "color == 'green'",
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
    //   results: [
    //     { score: 1.4449520111083984, id: '297', color_tag: 'green_8601' },
    //     { score: 1.3547699451446533, id: '714', color_tag: 'green_4808' },
    //     { score: 1.3535854816436768, id: '283', color_tag: 'green_1994' }
    //   ]
    // }
    // 

    // 5. Drop the collection
    res = await client.dropCollection({
        collection_name: "test_collection",
    })

}

main()