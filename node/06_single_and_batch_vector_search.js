const fs = require("fs")
const { MilvusClient, DataType, sleep } = require("@zilliz/milvus2-sdk-node")

const address = "YOUR_CLUSTER_ENDPOINT"
const token = "YOUR_CLUSTER_TOKEN"

async function main() {
    // 1. Set up a Milvus Client
    client = new MilvusClient({address, token});

    const version = await client.getVersion();

    console.log(version.version)

    // Output
    // 
    // v2.3.13
    // 

    // 2. Create a collection in quick setup mode
    await client.createCollection({
        collection_name: "quick_setup",
        dimension: 5,
        metric_type: "IP"
    });  

    // 3. Insert randomly generated vectors
    const colors = ["green", "blue", "yellow", "red", "black", "white", "purple", "pink", "orange", "brown", "grey"]
    data = []

    for (let i = 0; i < 1000; i++) {
        current_color = colors[Math.floor(Math.random() * colors.length)]
        data.push({
            id: i,
            vector: [Math.random(), Math.random(), Math.random(), Math.random(), Math.random()],
            color: current_color,
            color_tag: `${current_color}_${Math.floor(Math.random() * 8999) + 1000}`
        })
    }

    var res = await client.insert({
        collection_name: "quick_setup",
        data: data
    })

    console.log(res.insert_cnt)

    // Output
    // 
    // 1000
    // 

    await sleep(5000)

    // 4. Single vector search
    var query_vector = [Math.random(), Math.random(), Math.random(), Math.random(), Math.random()]

    res = await client.search({
        collection_name: "quick_setup",
        data: [query_vector],
        limit: 5,
    })

    console.log(res.results)

    // Output
    // 
    // [
    //   { score: 1.7463608980178833, id: '854' },
    //   { score: 1.744946002960205, id: '425' },
    //   { score: 1.7258622646331787, id: '718' },
    //   { score: 1.6691519021987915, id: '221' },
    //   { score: 1.6465414762496948, id: '162' }
    // ]
    // 

    // 5. Batch vector search
    var query_vectors = [
        [Math.random(), Math.random(), Math.random(), Math.random(), Math.random()],
        [Math.random(), Math.random(), Math.random(), Math.random(), Math.random()]
    ]

    res = await client.search({
        collection_name: "quick_setup",
        data: query_vectors,
        limit: 5,
    })

    console.log(res.results)

    // Output
    // 
    // [
    //   [
    //     { score: 2.3590476512908936, id: '854' },
    //     { score: 2.2896690368652344, id: '59' },
    //     { score: 2.245245933532715, id: '718' },
    //     { score: 2.212588310241699, id: '410' },
    //     { score: 2.198166847229004, id: '574' }
    //   ],
    //   [
    //     { score: 2.664059638977051, id: '59' },
    //     { score: 2.59483003616333, id: '854' },
    //     { score: 2.5465199947357178, id: '410' },
    //     { score: 2.5124058723449707, id: '640' },
    //     { score: 2.475452184677124, id: '718' }
    //   ]
    // ]
    // 

    // 6. Search within a partition
    await client.createPartition({
        collection_name: "quick_setup",
        partition_name: "red"
    })

    await client.createPartition({
        collection_name: "quick_setup",
        partition_name: "blue"
    })

    // 6.1 Insert data into partitions
    var red_data = []
    var blue_data = []

    for (let i = 1000; i < 1500; i++) {
        red_data.push({
            id: i,
            vector: [Math.random(), Math.random(), Math.random(), Math.random(), Math.random()],
            color: "red",
            color_tag: `red_${Math.floor(Math.random() * 8999) + 1000}`
        })
    }

    for (let i = 1500; i < 2000; i++) {
        blue_data.push({
            id: i,
            vector: [Math.random(), Math.random(), Math.random(), Math.random(), Math.random()],
            color: "blue",
            color_tag: `blue_${Math.floor(Math.random() * 8999) + 1000}`
        })
    }

    res = await client.insert({
        collection_name: "quick_setup",
        data: red_data,
        partition_name: "red"
    })

    console.log(res.insert_cnt)

    // Output
    // 
    // 500
    // 

    res = await client.insert({
        collection_name: "quick_setup",
        data: blue_data,
        partition_name: "blue"
    })

    console.log(res.insert_cnt)

    // Output
    // 
    // 500
    // 

    await sleep(5000)

    // 6.2 Search within partitions
    query_vector = [Math.random(), Math.random(), Math.random(), Math.random(), Math.random()]

    res = await client.search({
        collection_name: "quick_setup",
        data: [query_vector],
        partition_names: ["red"],
        limit: 5,
    })

    console.log(res.results)

    // Output
    // 
    // [
    //   { score: 3.0258803367614746, id: '1201' },
    //   { score: 3.004319190979004, id: '1458' },
    //   { score: 2.880324363708496, id: '1187' },
    //   { score: 2.8246407508850098, id: '1347' },
    //   { score: 2.797295093536377, id: '1406' }
    // ]
    // 

    res = await client.search({
        collection_name: "quick_setup",
        data: [query_vector],
        partition_names: ["blue"],
        limit: 5,
    })

    console.log(res.results)

    // Output
    // 
    // [
    //   { score: 2.8421106338500977, id: '1745' },
    //   { score: 2.838560104370117, id: '1782' },
    //   { score: 2.8134000301361084, id: '1511' },
    //   { score: 2.718268871307373, id: '1679' },
    //   { score: 2.7014894485473633, id: '1597' }
    // ]
    // 

    // 7. Search with output fields
    query_vector = [Math.random(), Math.random(), Math.random(), Math.random(), Math.random()]

    res = await client.search({
        collection_name: "quick_setup",
        data: [query_vector],
        limit: 5,
        output_fields: ["color"],
    })

    console.log(res.results)

    // Output
    // 
    // [
    //   { score: 3.036271572113037, id: '59', color: 'orange' },
    //   { score: 3.0267879962921143, id: '1745', color: 'blue' },
    //   { score: 3.0069446563720703, id: '854', color: 'black' },
    //   { score: 2.984386682510376, id: '718', color: 'black' },
    //   { score: 2.916019916534424, id: '425', color: 'purple' }
    // ]
    // 

    // 8. Filtered search
    // 8.1 Filter with "like" operator and prefix wildcard
    query_vector = [Math.random(), Math.random(), Math.random(), Math.random(), Math.random()]

    res = await client.search({
        collection_name: "quick_setup",
        data: [query_vector],
        limit: 5,
        filters: "color_tag like \"red%\"",
        output_fields: ["color_tag"]
    })

    console.log(res.results)

    // Output
    // 
    // [
    //   { score: 2.5080761909484863, id: '1201', color_tag: 'red_8904' },
    //   { score: 2.491129159927368, id: '425', color_tag: 'purple_8212' },
    //   { score: 2.4889798164367676, id: '1458', color_tag: 'red_6891' },
    //   { score: 2.42964243888855, id: '724', color_tag: 'black_9885' },
    //   { score: 2.4004223346710205, id: '854', color_tag: 'black_5990' }
    // ]
    // 

    // 8.2 Filter with "like" operator and infix wildcard (Milvus 2.4.x or later)
    if (version.version.startsWith("v2.4")) {
        query_vector = [Math.random(), Math.random(), Math.random(), Math.random(), Math.random()]

        res = await client.search({
            collection_name: "quick_setup",
            data: [query_vector],
            limit: 5,
            filters: "color_tag like \"%_4\"",
            output_fields: ["color_tag"]
        })
    } else {
        res = `This demo is not supported in {version.version}`
    }

    // 9. Range search
    query_vector = [Math.random(), Math.random(), Math.random(), Math.random(), Math.random()]

    res = await client.search({
        collection_name: "quick_setup",
        data: [query_vector],
        limit: 5,
        params: {
            radius: 0.1,
            range: 1.0
        },
        output_fields: ["color_tag"]
    })

    console.log(res.results)

    // Output
    // 
    // [
    //   { score: 2.3387961387634277, id: '718', color_tag: 'black_7154' },
    //   { score: 2.3352415561676025, id: '1745', color_tag: 'blue_8741' },
    //   { score: 2.290485382080078, id: '1408', color_tag: 'red_2324' },
    //   { score: 2.285870313644409, id: '854', color_tag: 'black_5990' },
    //   { score: 2.2593345642089844, id: '1309', color_tag: 'red_8458' }
    // ]
    // 

    // 10. Grouping search
    // TODO

    // 11. Drop collection
    await client.dropCollection({
        collection_name: "quick_setup"
    })

}

main()