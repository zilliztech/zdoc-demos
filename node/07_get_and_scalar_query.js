const fs = require("fs")
const { MilvusClient, DataType, sleep } = require("@zilliz/milvus2-sdk-node")

const address = "YOUR_CLUSTER_ENDPOINT"
const token = "YOUR_CLUSTER_TOKEN"

async function main() {
    // 1. Set up a Milvus Client
    client = new MilvusClient({address, token}); 

    // 2. Create a collection in quick setup mode
    await client.createCollection({
        collection_name: "quick_setup",
        dimension: 5,
    }); 

    // 3. Insert randomly generated vectors
    const colors = ["green", "blue", "yellow", "red", "black", "white", "purple", "pink", "orange", "brown", "grey"]
    var data = []

    for (let i = 0; i < 1000; i++) {
        current_color = colors[Math.floor(Math.random() * colors.length)]
        current_tag = Math.floor(Math.random() * 8999 + 1000)
        data.push({
            "id": i,
            "vector": [Math.random(), Math.random(), Math.random(), Math.random(), Math.random()],
            "color": current_color,
            "tag": current_tag,
            "color_tag": `${current_color}_${current_tag}`
        })
    }

    console.log(data[0])

    // Output
    // 
    // {
    //   id: 0,
    //   vector: [
    //     0.16022394821966035,
    //     0.6514875214491056,
    //     0.18294484964044666,
    //     0.30227694168725394,
    //     0.47553087493572255
    //   ],
    //   color: 'blue',
    //   tag: 8907,
    //   color_tag: 'blue_8907'
    // }
    // 

    res = await client.insert({
        collection_name: "quick_setup",
        data: data
    })

    console.log(res.insert_cnt)

    // Output
    // 
    // 1000
    // 

    await sleep(5000)

    // 4. Create partitions and insert more entities
    await client.createPartition({
        collection_name: "quick_setup",
        partition_name: "partitionA"
    })

    await client.createPartition({
        collection_name: "quick_setup",
        partition_name: "partitionB"
    })

    data = []

    for (let i = 1000; i < 1500; i++) {
        current_color = colors[Math.floor(Math.random() * colors.length)]
        current_tag = Math.floor(Math.random() * 8999 + 1000)
        data.push({
            "id": i,
            "vector": [Math.random(), Math.random(), Math.random(), Math.random(), Math.random()],
            "color": current_color,
            "tag": current_tag,
            "color_tag": `${current_color}_${current_tag}`
        })
    }

    res = await client.insert({
        collection_name: "quick_setup",
        data: data,
        partition_name: "partitionA"
    })

    console.log(res.insert_cnt)

    // Output
    // 
    // 500
    // 

    await sleep(5000)

    data = []

    for (let i = 1500; i < 2000; i++) {
        current_color = colors[Math.floor(Math.random() * colors.length)]
        current_tag = Math.floor(Math.random() * 8999 + 1000)
        data.push({
            "id": i,
            "vector": [Math.random(), Math.random(), Math.random(), Math.random(), Math.random()],
            "color": current_color,
            "tag": current_tag,
            "color_tag": `${current_color}_${current_tag}`
        })
    }
    
    res = await client.insert({
        collection_name: "quick_setup",
        data: data,
        partition_name: "partitionB"
    })

    console.log(res.insert_cnt)

    // Output
    // 
    // 500
    // 

    await sleep(5000)

    // 5. Get entities by id
    res = await client.get({
        collection_name: "quick_setup",
        ids: [0, 1, 2],
        output_fields: ["vector", "color_tag"]
    })

    console.log(res.data)

    // Output
    // 
    // [
    //   {
    //     vector: [
    //       0.16022394597530365,
    //       0.6514875292778015,
    //       0.18294484913349152,
    //       0.30227693915367126,
    //       0.47553086280822754
    //     ],
    //     '$meta': { color: 'blue', tag: 8907, color_tag: 'blue_8907' },
    //     id: '0'
    //   },
    //   {
    //     vector: [
    //       0.2459285855293274,
    //       0.4974019527435303,
    //       0.2154673933982849,
    //       0.03719571232795715,
    //       0.8348019123077393
    //     ],
    //     '$meta': { color: 'grey', tag: 3710, color_tag: 'grey_3710' },
    //     id: '1'
    //   },
    //   {
    //     vector: [
    //       0.9404329061508179,
    //       0.49662265181541443,
    //       0.8088793158531189,
    //       0.9337621331214905,
    //       0.8269071578979492
    //     ],
    //     '$meta': { color: 'blue', tag: 2993, color_tag: 'blue_2993' },
    //     id: '2'
    //   }
    // ]
    // 

    // 5.1 Get entities by id in a partition
    res = await client.get({
        collection_name: "quick_setup",
        ids: [1000, 1001, 1002],
        partition_names: ["partitionA"],
        output_fields: ["vector", "color_tag"]
    })

    console.log(res.data)

    // Output
    // 
    // [
    //   {
    //     id: '1000',
    //     vector: [
    //       0.014254206791520119,
    //       0.5817716121673584,
    //       0.19793470203876495,
    //       0.8064294457435608,
    //       0.7745839357376099
    //     ],
    //     '$meta': { color: 'white', tag: 5996, color_tag: 'white_5996' }
    //   },
    //   {
    //     id: '1001',
    //     vector: [
    //       0.6073881983757019,
    //       0.05214758217334747,
    //       0.730999231338501,
    //       0.20900958776474,
    //       0.03665429726243019
    //     ],
    //     '$meta': { color: 'grey', tag: 2834, color_tag: 'grey_2834' }
    //   },
    //   {
    //     id: '1002',
    //     vector: [
    //       0.48877206444740295,
    //       0.34028753638267517,
    //       0.6527213454246521,
    //       0.9763909578323364,
    //       0.8031482100486755
    //     ],
    //     '$meta': { color: 'pink', tag: 9107, color_tag: 'pink_9107' }
    //   }
    // ]
    // 

    // 6. Use basic operators
    res = await client.query({
        collection_name: "quick_setup",
        filter: "1000 < tag < 1500",
        output_fields: ["color_tag"],
        limit: 3
    })

    console.log(res.data)

    // Output
    // 
    // [
    //   {
    //     '$meta': { color: 'pink', tag: 1050, color_tag: 'pink_1050' },
    //     id: '6'
    //   },
    //   {
    //     '$meta': { color: 'purple', tag: 1174, color_tag: 'purple_1174' },
    //     id: '24'
    //   },
    //   {
    //     '$meta': { color: 'orange', tag: 1023, color_tag: 'orange_1023' },
    //     id: '40'
    //   }
    // ]
    // 

    res = await client.query({
        collection_name: "quick_setup",
        filter: 'color == "brown"',
        output_fields: ["color_tag"],
        limit: 3
    })

    console.log(res.data)

    // Output
    // 
    // [
    //   {
    //     '$meta': { color: 'brown', tag: 6839, color_tag: 'brown_6839' },
    //     id: '22'
    //   },
    //   {
    //     '$meta': { color: 'brown', tag: 7849, color_tag: 'brown_7849' },
    //     id: '32'
    //   },
    //   {
    //     '$meta': { color: 'brown', tag: 7855, color_tag: 'brown_7855' },
    //     id: '33'
    //   }
    // ]
    // 

    res = await client.query({
        collection_name: "quick_setup",
        filter: 'color not in ["green", "purple"]',
        output_fields: ["color_tag"],
        limit: 3
    })

    console.log(res.data)

    // Output
    // 
    // [
    //   {
    //     '$meta': { color: 'blue', tag: 8907, color_tag: 'blue_8907' },
    //     id: '0'
    //   },
    //   {
    //     '$meta': { color: 'grey', tag: 3710, color_tag: 'grey_3710' },
    //     id: '1'
    //   },
    //   {
    //     '$meta': { color: 'blue', tag: 2993, color_tag: 'blue_2993' },
    //     id: '2'
    //   }
    // ]
    // 

    res = await client.query({
        collection_name: "quick_setup",
        filter: 'color_tag like "red%"',
        output_fields: ["color_tag"],
        limit: 3
    })

    console.log(res.data)

    // Output
    // 
    // [
    //   {
    //     '$meta': { color: 'red', tag: 8773, color_tag: 'red_8773' },
    //     id: '17'
    //   },
    //   {
    //     '$meta': { color: 'red', tag: 9197, color_tag: 'red_9197' },
    //     id: '34'
    //   },
    //   {
    //     '$meta': { color: 'red', tag: 7914, color_tag: 'red_7914' },
    //     id: '46'
    //   }
    // ]
    // 

    res = await client.query({
        collection_name: "quick_setup",
        filter: '(color == "red") and (1000 < tag < 1500)',
        output_fields: ["color_tag"],
        limit: 3
    })

    console.log(res.data)

    // Output
    // 
    // [
    //   {
    //     '$meta': { color: 'red', tag: 1436, color_tag: 'red_1436' },
    //     id: '67'
    //   },
    //   {
    //     '$meta': { color: 'red', tag: 1463, color_tag: 'red_1463' },
    //     id: '160'
    //   },
    //   {
    //     '$meta': { color: 'red', tag: 1073, color_tag: 'red_1073' },
    //     id: '291'
    //   }
    // ]
    // 

    // 7. Use advanced operators
    // Count the total number of entities in a collection
    res = await client.query({
        collection_name: "quick_setup",
        output_fields: ["count(*)"]
    })

    console.log(res.data)   

    // Output
    // 
    // [ { 'count(*)': '2000' } ]
    // 

    
    // Count the number of entities in a partition
    res = await client.query({
        collection_name: "quick_setup",
        output_fields: ["count(*)"],
        partition_names: ["partitionA"]
    })

    console.log(res.data)     

    // Output
    // 
    // [ { 'count(*)': '500' } ]
    // 

    
    // Count the number of entities that match a specific filter
    res = await client.query({
        collection_name: "quick_setup",
        filter: '(color == "red") and (1000 < tag < 1500)',
        output_fields: ["count(*)"]
    })

    console.log(res.data)   

    // Output
    // 
    // [ { 'count(*)': '10' } ]
    // 

    // 8. Drop the collection
    await client.dropCollection({
        collection_name: "quick_setup"
    })
}

main()
