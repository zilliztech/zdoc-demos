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
    //     0.521639270918109,
    //     0.9418960358339306,
    //     0.7576785932128338,
    //     0.7256261748654758,
    //     0.14608423286768568
    //   ],
    //   color: 'yellow',
    //   tag: 9286,
    //   color_tag: 'yellow_9286'
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

    // 4. Search with partition key
    const query_vectors = [Math.random(), Math.random(), Math.random(), Math.random(), Math.random()]

    res = await client.search({
        collection_name: "test_collection",
        data: query_vectors,
        filter: "color == 'green'",
        output_fields: ["color_tag"],
        limit: 3
    })

    console.log(res.results)

    // Output
    // 
    // [
    //   { score: 3.416375160217285, id: '426', color_tag: 'green_5277' },
    //   { score: 3.246188163757324, id: '852', color_tag: 'green_9450' },
    //   { score: 3.2429375648498535, id: '54', color_tag: 'green_8308' }
    // ]
    // 

    // 5. Drop the collection
    res = await client.dropCollection({
        collection_name: "test_collection",
    })

}

main()