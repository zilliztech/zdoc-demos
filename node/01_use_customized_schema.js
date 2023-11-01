const fs = require("fs")
const { MilvusClient, DataType, sleep } = require("@zilliz/milvus2-sdk-node")

const address = "YOUR_CLUSTER_ENDPOINT"
const token = "YOUR_CLUSTER_TOKEN"
const collectionName = "medium_articles_2020"
const data_file = `./medium_articles_2020_dpr.json`

async function main() {
    // 1. Connect to the cluster
    const client = new MilvusClient({address, token})
    
    // 2. Define fields
    fields = [
        {
            name: "id",
            data_type: DataType.Int64,
            is_primary_key: true,
            auto_id: true
        },
        {
            name: "title",
            data_type: DataType.VarChar,
            max_length: 512
        },
        {
            name: "title_vector",
            data_type: DataType.FloatVector,
            dim: 768
        },
        {
            name: "link",
            data_type: DataType.VarChar,
            max_length: 512
        },
        {
            name: "reading_time",
            data_type: DataType.Int64
        },
        {
            name: "publication",
            data_type: DataType.VarChar,
            max_length: 512
        },
        {
            name: "claps",
            data_type: DataType.Int64
        },
        {
            name: "responses",
            data_type: DataType.Int64
        }
    ]
    
    // 3. Create collection
    res = await client.createCollection({
        collection_name: collectionName,
        fields: fields
    })

    console.log(res)

    // Output
    // 
    // { error_code: 'Success', reason: '', code: 0 }
    // 

    // 4. Create index
    res = await client.createIndex({
        collection_name: collectionName,
        field_name: "title_vector",
        index_type: "IVF_FLAT",
        metric_type: "L2",
        params: {
            nlist: 1024
        }
    })

    console.log(res)

    // Output
    // 
    // { error_code: 'Success', reason: '', code: 0 }
    // 

    res = await client.loadCollection({
        collection_name: collectionName
    })

    console.log(res)

    // Output
    // 
    // { error_code: 'Success', reason: '', code: 0 }
    // 

    // 5. Insert vectors
    const data = JSON.parse(fs.readFileSync(data_file, "utf8"))

    // read rows
    const rows = data["rows"]
    const row = rows[0]

    console.log(Object.keys(row))

    // Output
    // 
    // [
    //   'id',
    //   'title',
    //   'title_vector',
    //   'link',
    //   'reading_time',
    //   'publication',
    //   'claps',
    //   'responses'
    // ]
    // 

    //insert vectors
    res = await client.insert({
        collection_name: collectionName,
        data: rows
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
    //     ... 5879 more items
    //   ],
    //   err_index: [],
    //   status: { error_code: 'Success', reason: '', code: 0 },
    //   IDs: { int_id: { data: [Array] }, id_field: 'int_id' },
    //   acknowledged: false,
    //   insert_cnt: '5979',
    //   delete_cnt: '0',
    //   upsert_cnt: '0',
    //   timestamp: '445316631681040387'
    // }
    // 

    await sleep(5000)

    // 6. Coduct an ANN search

    res = await client.search({
        collection_name: collectionName,
        vector: rows[0].title_vector,
        limit: 5,
        filter: "claps > 30 and reading_time < 10",
        output_fields: ["title", "link"]
    });
    
    console.log(res);

    // Output
    // 
    // {
    //   status: { error_code: 'Success', reason: '', code: 0 },
    //   results: [
    //     {
    //       score: 0.36103832721710205,
    //       id: '5607',
    //       title: 'The Hidden Side Effect of the Coronavirus',
    //       link: 'https://medium.com/swlh/the-hidden-side-effect-of-the-coronavirus-b6a7a5ee9586'
    //     },
    //     {
    //       score: 0.37674015760421753,
    //       id: '5641',
    //       title: 'Why The Coronavirus Mortality Rate is Misleading',
    //       link: 'https://towardsdatascience.com/why-the-coronavirus-mortality-rate-is-misleading-cc63f571b6a6'
    //     },
    //     {
    //       score: 0.416297972202301,
    //       id: '3441',
    //       title: 'Coronavirus shows what ethical Amazon could look like',
    //       link: 'https://medium.com/swlh/coronavirus-shows-what-ethical-amazon-could-look-like-7c80baf2c663'
    //     },
    //     {
    //       score: 0.436093807220459,
    //       id: '938',
    //       title: 'Mortality Rate As an Indicator of an Epidemic Outbreak',
    //       link: 'https://towardsdatascience.com/mortality-rate-as-an-indicator-of-an-epidemic-outbreak-704592f3bb39'
    //     },
    //     {
    //       score: 0.48886317014694214,
    //       id: '4275',
    //       title: 'How Can AI Help Fight Coronavirus?',
    //       link: 'https://medium.com/swlh/how-can-ai-help-fight-coronavirus-60f2182de93a'
    //     }
    //   ]
    // }
    // 

    // 7. Get collection info

    res = await client.describeCollection({
        collection_name: collectionName
    })

    console.log(res);

    // Output
    // 
    // {
    //   virtual_channel_names: [ 'by-dev-rootcoord-dml_11_445311585782771678v0' ],
    //   physical_channel_names: [ 'by-dev-rootcoord-dml_11' ],
    //   aliases: [],
    //   start_positions: [],
    //   properties: [],
    //   status: { error_code: 'Success', reason: '', code: 0 },
    //   schema: {
    //     fields: [
    //       [Object], [Object],
    //       [Object], [Object],
    //       [Object], [Object],
    //       [Object], [Object]
    //     ],
    //     name: 'medium_articles_2020',
    //     description: '',
    //     autoID: false,
    //     enable_dynamic_field: false
    //   },
    //   collectionID: '445311585782771678',
    //   created_timestamp: '445316626739625989',
    //   created_utc_timestamp: '1698748118361',
    //   shards_num: 1,
    //   consistency_level: 'Bounded',
    //   collection_name: 'medium_articles_2020',
    //   db_name: 'default',
    //   num_partitions: '1'
    // }
    // 

    // 8. Drop collection

    res = await client.dropCollection({
        collection_name: collectionName
    })     
    
    console.log(res);

    // Output
    // 
    // { error_code: 'Success', reason: '', code: 0 }
    // 
}

main()