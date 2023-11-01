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
            name: "article_meta",
            // This field is a JSON field.
            data_type: DataType.JSON
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
            nlist: 16384
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
    
    const data = JSON.parse(fs.readFileSync(data_file))

    // read rows
    const rows = data["rows"]
    rows.forEach(row => {
        // add a new field in the row object
        row.article_meta = {}
        // move the following fields into the new field
        row.article_meta.link = row.link
        delete row.link
        row.article_meta.reading_time = row.reading_time
        delete row.reading_time
        row.article_meta.publication = row.publication
        delete row.publication
        row.article_meta.claps = row.claps
        delete row.claps
        row.article_meta.responses = row.responses
        delete row.responses
        row.article_meta.tags_1 = Array.from({length: 40}, () => Math.floor(Math.random() * 40));   
        row.article_meta.tags_2 = Array.from({length: 10}, () => Array.from({length: 4}, () => Math.floor(Math.random() * 40)))      
    });

    console.log(Object.keys(rows[0]))

    // Output
    // 
    // [ 'id', 'title', 'title_vector', 'article_meta' ]
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
    //   timestamp: '445318026051190787'
    // }
    // 


    await sleep(5000)

    // 6. Count entities

    counts = await client.query({
        collection_name: collectionName,
        filter: "",
        output_fields: ["count(*)"]
    })

    console.log(counts)

    // Output
    // 
    // {
    //   status: { error_code: 'Success', reason: '', code: 0 },
    //   data: [ { 'count(*)': '5979' } ]
    // }
    // 

    // 7. Count entities with condition

    // matches all articles with tags_1 having the member 16
    const expr_1 = 'JSON_CONTAINS(article_meta["tags_1"], 16)'

    // matches all articles with tags_2 having the member [5, 3, 39, 8]
    const expr_2 = 'JSON_CONTAINS(article_meta["tags_2"], [5, 3, 39, 8])'

    // matches all articles with tags_1 having a member from [5, 3, 39, 8]
    const expr_3 = 'JSON_CONTAINS_ANY(article_meta["tags_1"], [5, 3, 39, 8])'

    // matches all articles with tags_1 having all members from [2, 4, 6]
    const expr_4 = 'JSON_CONTAINS_ALL(article_meta["tags_1"], [2, 4, 6])'

    counts = await client.query({
        collection_name: collectionName,
        filter: expr_1,
        output_fields: ["count(*)"]
    })

    console.log(counts)

    // Output
    // 
    // {
    //   status: { error_code: 'Success', reason: '', code: 0 },
    //   data: [ { 'count(*)': '3825' } ]
    // }
    // 

    // 8. Check if a specific element exists in JSON field

    // Search

    res = await client.search({
        collection_name: collectionName,
        vector: rows[0].title_vector,
        limit: 5,
        filter: expr_3,
        output_fields: ["title", "article_meta"]
    })

    console.log(res)

    // Output
    // 
    // {
    //   status: { error_code: 'Success', reason: '', code: 0 },
    //   results: [
    //     {
    //       score: 0,
    //       id: '0',
    //       title: 'The Reported Mortality Rate of Coronavirus Is Not Important',
    //       article_meta: [Object]
    //     },
    //     {
    //       score: 0.29999834299087524,
    //       id: '3177',
    //       title: 'Following the Spread of Coronavirus',
    //       article_meta: [Object]
    //     },
    //     {
    //       score: 0.36103832721710205,
    //       id: '5607',
    //       title: 'The Hidden Side Effect of the Coronavirus',
    //       article_meta: [Object]
    //     },
    //     {
    //       score: 0.37674015760421753,
    //       id: '5641',
    //       title: 'Why The Coronavirus Mortality Rate is Misleading',
    //       article_meta: [Object]
    //     },
    //     {
    //       score: 0.416297972202301,
    //       id: '3441',
    //       title: 'Coronavirus shows what ethical Amazon could look like',
    //       article_meta: [Object]
    //     }
    //   ]
    // }
    // 

    tags_1 = res.results.map((item) => item.article_meta["tags_1"])

    console.log(tags_1)

    // Output
    // 
    // [
    //   [
    //     24, 20, 37, 11,  3, 16, 26, 39, 18, 34,
    //     13, 25,  9, 37, 29, 30, 18, 34, 27, 22,
    //      6, 23,  3,  5, 14,  0, 23,  2, 36, 37,
    //     31, 28,  7, 18, 16,  1,  9, 12, 17,  3
    //   ],
    //   [
    //      9,  6, 34, 16,  2,  5, 15, 36,  6, 37,
    //     17,  7,  7, 13, 21,  6, 33, 37, 16, 24,
    //     35, 32, 17, 11, 33, 16, 22, 22, 39, 36,
    //      7, 37, 15, 21, 31, 16,  8, 37, 18, 17
    //   ],
    //   [
    //     23, 10, 35, 32, 23,  1,  1, 31, 10, 20,
    //     20,  2, 15,  3,  3, 22,  8, 27, 24, 18,
    //     36, 24, 22,  1, 24, 31,  9, 20, 25, 22,
    //     10, 36, 29, 25,  3, 17, 37,  2,  6,  0
    //   ],
    //   [
    //     37, 22, 38, 32, 20, 27,  3, 28, 23, 26,
    //     23, 22, 24,  7, 38, 23,  2,  5, 19,  6,
    //     14, 38, 24, 27, 11,  9, 30, 18, 38,  2,
    //     19, 16,  9,  1, 12, 16,  4,  0,  5,  2
    //   ],
    //   [
    //     18, 38, 28, 36,  0, 31, 31, 39,  1, 21,
    //     18,  0, 18, 38, 20, 31, 17,  1, 37,  7,
    //     29, 33, 13, 32, 29, 27, 27, 39, 19, 36,
    //     30,  4, 21,  6, 31, 26, 35,  7, 11, 24
    //   ]
    // ]
    // 

    // Query

    res = await client.query({
        collection_name: collectionName,
        filter: expr_4,
        limit: 5,
        output_fields: ["title", "article_meta"]
    })

    console.log(res)

    // Output
    // 
    // {
    //   status: { error_code: 'Success', reason: '', code: 0 },
    //   data: [
    //     {
    //       title: 'Maternity leave shouldn’t set women back',
    //       article_meta: [Object],
    //       id: '3'
    //     },
    //     {
    //       title: 'Python NLP Tutorial: Information Extraction and Knowledge Graphs',
    //       article_meta: [Object],
    //       id: '4'
    //     },
    //     {
    //       title: 'Building Comprehensible Customer Churn Prediction Models',
    //       article_meta: [Object],
    //       id: '7'
    //     },
    //     {
    //       title: '5 Underrated Traits That Separate Exceptional Teams From the Rest',
    //       article_meta: [Object],
    //       id: '8'
    //     },
    //     {
    //       title: 'Building high performance startup teams',
    //       article_meta: [Object],
    //       id: '10'
    //     }
    //   ]
    // }
    // 

    tags_1 = res.data.map((item) => item.article_meta["tags_1"])

    console.log(tags_1)

    // Output
    // 
    // [
    //   [
    //      2, 34, 25, 19, 19,  6,  3, 37, 16,  1,
    //     27,  9,  9, 26, 27, 34, 30, 12, 11, 36,
    //     30,  3,  1,  2, 24,  4, 14, 33, 32, 14,
    //     26, 12, 22, 28, 10,  6,  2, 39, 10, 34
    //   ],
    //   [
    //     37,  3, 11, 27, 20,  6,  4, 33,  7,  4,
    //     37, 39, 29, 19, 35, 24, 29, 25, 38, 25,
    //      7, 27,  8, 19, 27,  7, 15, 36,  8, 28,
    //     29, 13, 13, 30, 29, 30, 22,  2, 35, 30
    //   ],
    //   [
    //      4, 26,  1, 31, 10,  5, 33, 8,  0,  2,
    //     39, 37, 38, 25, 12, 28, 19, 9, 13, 29,
    //     22, 19, 24, 25,  3, 34, 22, 3,  9,  3,
    //      8, 27, 36, 26,  5, 20, 18, 6, 26, 30
    //   ],
    //   [
    //     27,  1,  5, 27, 14,  4,  2, 35,  6, 35,
    //     34,  0, 22, 37, 33,  4,  0, 26,  7,  1,
    //     22, 21,  8, 28, 31, 24, 33, 22, 27, 22,
    //     19, 20,  3, 34,  8, 13, 24, 28, 25, 34
    //   ],
    //   [
    //     38, 12,  4, 18, 17,  2, 25, 31,  7, 23,
    //      0, 10, 23, 35, 37, 32, 26,  9, 32, 27,
    //      8, 38,  5, 30, 10, 10,  7, 25, 29, 10,
    //     27, 23, 34, 27, 38,  0,  6, 38, 34, 23
    //   ]
    // ]
    // 

    // 7. Get collection info

    res = await client.describeCollection({
        collection_name: collectionName
    })

    console.log(res);

    // Output
    // 
    // {
    //   virtual_channel_names: [ 'by-dev-rootcoord-dml_11_445311585783000491v0' ],
    //   physical_channel_names: [ 'by-dev-rootcoord-dml_11' ],
    //   aliases: [],
    //   start_positions: [],
    //   properties: [],
    //   status: { error_code: 'Success', reason: '', code: 0 },
    //   schema: {
    //     fields: [ [Object], [Object], [Object], [Object] ],
    //     name: 'medium_articles_2020',
    //     description: '',
    //     autoID: false,
    //     enable_dynamic_field: false
    //   },
    //   collectionID: '445311585783000491',
    //   created_timestamp: '445318018907504647',
    //   created_utc_timestamp: '1698753429060',
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
    