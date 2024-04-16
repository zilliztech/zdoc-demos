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

    // 2.1 List partitions
    res = await client.listPartitions({
        collection_name: "test_collection",
    })

    console.log(res.partition_names)

    // Output
    // 
    // [
    //   '_default_0',  '_default_1',  '_default_2',  '_default_3',
    //   '_default_4',  '_default_5',  '_default_6',  '_default_7',
    //   '_default_8',  '_default_9',  '_default_10', '_default_11',
    //   '_default_12', '_default_13', '_default_14', '_default_15',
    //   '_default_16', '_default_17', '_default_18', '_default_19',
    //   '_default_20', '_default_21', '_default_22', '_default_23',
    //   '_default_24', '_default_25', '_default_26', '_default_27',
    //   '_default_28', '_default_29', '_default_30', '_default_31',
    //   '_default_32', '_default_33', '_default_34', '_default_35',
    //   '_default_36', '_default_37', '_default_38', '_default_39',
    //   '_default_40', '_default_41', '_default_42', '_default_43',
    //   '_default_44', '_default_45', '_default_46', '_default_47',
    //   '_default_48', '_default_49', '_default_50', '_default_51',
    //   '_default_52', '_default_53', '_default_54', '_default_55',
    //   '_default_56', '_default_57', '_default_58', '_default_59',
    //   '_default_60', '_default_61', '_default_62', '_default_63'
    // ]
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
    //     0.6719498122152168,
    //     0.02840115337468796,
    //     0.25261281985653095,
    //     0.2936432421017825,
    //     0.10813415803139192
    //   ],
    //   color: 'pink',
    //   tag: 6034,
    //   color_tag: 'pink_6034'
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
    //   { score: 2.402090549468994, id: '135', color_tag: 'green_2694' },
    //   { score: 2.3938629627227783, id: '326', color_tag: 'green_7104' },
    //   { score: 2.3235254287719727, id: '801', color_tag: 'green_3162' }
    // ]
    // 

    // 5. Drop the collection
    res = await client.dropCollection({
        collection_name: "test_collection",
    })

}

main()