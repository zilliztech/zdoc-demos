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
    //   timestamp: '445332879628304386'
    // }
    // 


    await sleep(5000)

    // 6. Coduct an range search

    res = await client.search({
        collection_name: collectionName,
        vector: rows[0].title_vector,
        limit: 100,
        filter: "claps > 30 and reading_time < 10",
        output_fields: ["title", "link"],
        params: {
            nprobe: 10,
            radius: 1.0,
            range_filter: 0.8
        }
    });
    
    // Count the results

    console.log(res.results.length);

    // Output
    // 
    // 100
    // 

    // List first few results

    console.log(res.results.slice(0, 5));

    // Output
    // 
    // [
    //   {
    //     score: 0.8003642559051514,
    //     id: '4411',
    //     title: 'Why Passion Is Not Enough in the Working World — Learn Professionalism Instead',
    //     link: 'https://medium.com/swlh/why-passion-is-not-enough-in-the-working-world-learn-professionalism-instead-d1bdb0acd750'
    //   },
    //   {
    //     score: 0.8004330992698669,
    //     id: '3503',
    //     title: 'Figma to video prototyping — easy way in 3 steps',
    //     link: 'https://uxdesign.cc/figma-to-video-prototyping-easy-way-in-3-steps-d7ac3770d253'
    //   },
    //   {
    //     score: 0.8004655838012695,
    //     id: '4397',
    //     title: 'An Introduction to Survey Research',
    //     link: 'https://medium.com/swlh/an-introduction-to-survey-research-ba9e9fb9ca57'
    //   },
    //   {
    //     score: 0.8005216121673584,
    //     id: '2705',
    //     title: 'Exploratory Data Analysis: DataPrep.eda vs Pandas-Profiling',
    //     link: 'https://towardsdatascience.com/exploratory-data-analysis-dataprep-eda-vs-pandas-profiling-7137683fe47f'
    //   },
    //   {
    //     score: 0.8005879521369934,
    //     id: '3185',
    //     title: 'Modelling Volatile Time Series with LSTM Networks',
    //     link: 'https://towardsdatascience.com/modelling-volatile-time-series-with-lstm-networks-51250fb7cfa3'
    //   }
    // ]
    // 


    // 7. Drop the collection

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