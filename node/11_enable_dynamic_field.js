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
    ]

    // 2.2 Prepare index parameters
    const index_params = [{
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
    //     0.1275656405044483,
    //     0.47417858592773277,
    //     0.13858264437643286,
    //     0.2390904907020377,
    //     0.8447862593689635
    //   ],
    //   color: 'blue',
    //   tag: 2064,
    //   color_tag: 'blue_2064'
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

    // 4. Search with non-schema-defined fields
    const query_vectors = [[0.1, 0.2, 0.3, 0.4, 0.5]]
    
    res = await client.search({
        collection_name: "test_collection",
        data: query_vectors,
        filter: "color in [\"red\", \"green\"]",
        output_fields: ["color_tag"],
        limit: 3
    })

    console.log(res.results)

    // Output
    // 
    // [
    //   { score: 1.2284551858901978, id: '301', color_tag: 'red_1270' },
    //   { score: 1.2195171117782593, id: '205', color_tag: 'red_2780' },
    //   { score: 1.2055039405822754, id: '487', color_tag: 'red_6653' }
    // ]
    // 

    // 5. Drop the collection
    await client.dropCollection({
        collection_name: "test_collection",
    })

}

main()