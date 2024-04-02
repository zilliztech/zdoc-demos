const fs = require("fs")
const { MilvusClient, DataType, sleep } = require("@zilliz/milvus2-sdk-node")

const address = "YOUR_CLUSTER_ENDPOINT"
const token = "YOUR_CLUSTER_TOKEN"

async function main() {

    // 1. Set up a Milvus Client
    client = new MilvusClient({address, token});

    // 2. Create a collection in quick setup mode
    let res = await client.createCollection({
        collection_name: "quick_setup",
        dimension: 5,
    });  

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
        collection_name: "quick_setup"
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






    // 3. Create a collection in customized setup mode
    // 3.1 Define fields
    const fields = [
        {
            name: "my_id",
            data_type: DataType.Int64,
            is_primary_key: true,
            auto_id: false
        },
        {
            name: "my_vector",
            data_type: DataType.FloatVector,
            dim: 5
        },
    ]

    // 3.2 Prepare index parameters
    const index_params = [{
        field_name: "my_id",
        index_type: "STL_SORT"
    },{
        field_name: "my_vector",
        index_type: "IVF_FLAT",
        metric_type: "IP",
        params: { nlist: 1024}
    }]
    
    // 3.3 Create a collection with fields and index parameters
    res = await client.createCollection({
        collection_name: "customized_setup_1",
        fields: fields,
        index_params: index_params,
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
        collection_name: "customized_setup_1"
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






    // 3.4 Create a collection and index it seperately
    res = await client.createCollection({
        collection_name: "customized_setup_2",
        fields: fields,
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
        collection_name: "customized_setup_2"
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
    //   state: 'LoadStateNotLoad'
    // }
    // 






    // 3.5 Create index
    res = await client.createIndex({
        collection_name: "customized_setup_2",
        field_name: "my_vector",
        index_type: "IVF_FLAT",
        metric_type: "IP",
        params: { nlist: 1024}
    })

    res = await client.getLoadState({
        collection_name: "customized_setup_2"
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
    //   state: 'LoadStateNotLoad'
    // }
    // 





    
    // 5. View Collections
    res = await client.describeCollection({
        collection_name: "customized_setup_2"
    })

    console.log(res)

    // Output
    // 
    // {
    //   virtual_channel_names: [ 'in01-0ed1e58b63f3f62-rootcoord-dml_1_448162378879935162v0' ],
    //   physical_channel_names: [ 'in01-0ed1e58b63f3f62-rootcoord-dml_1' ],
    //   aliases: [],
    //   start_positions: [],
    //   properties: [],
    //   status: {
    //     error_code: 'Success',
    //     reason: '',
    //     code: 0,
    //     retriable: false,
    //     detail: ''
    //   },
    //   schema: {
    //     fields: [ [Object], [Object] ],
    //     properties: [],
    //     name: 'customized_setup_2',
    //     description: '',
    //     autoID: false,
    //     enable_dynamic_field: false
    //   },
    //   collectionID: '448162378879935162',
    //   created_timestamp: '448212685858537475',
    //   created_utc_timestamp: '1709795707163',
    //   shards_num: 1,
    //   consistency_level: 'Bounded',
    //   collection_name: 'customized_setup_2',
    //   db_name: 'default',
    //   num_partitions: '1'
    // }
    // 






    // 6. List all collection names
    res = await client.listCollections()

    console.log(res.collection_names)

    // Output
    // 
    // [ 'customized_setup_1', 'quick_setup', 'customized_setup_2' ]
    // 






    // 7. Load the collection
    res = await client.loadCollection({
        collection_name: "customized_setup_2"
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






    await sleep(3000)

    res = await client.getLoadState({
        collection_name: "customized_setup_2"
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






    // 8. Release the collection
    res = await client.releaseCollection({
        collection_name: "customized_setup_2"
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
        collection_name: "customized_setup_2"
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
    //   state: 'LoadStateNotLoad'
    // }
    // 






    // 9. Manage aliases
    // 9.1 Create aliases
    res = await client.createAlias({
        collection_name: "customized_setup_2",
        alias: "bob"
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






    res = await client.createAlias({
        collection_name: "customized_setup_2",
        alias: "alice"
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




    await sleep(3000)

    // 9.2 List aliases
    res = await client.listAliases({
        collection_name: "customized_setup_2"
    })

    console.log(res)

    // Output
    // 
    // {
    //   aliases: [ 'bob', 'alice' ],
    //   status: {
    //     error_code: 'Success',
    //     reason: '',
    //     code: 0,
    //     retriable: false,
    //     detail: ''
    //   },
    //   db_name: 'default',
    //   collection_name: 'customized_setup_2'
    // }
    // 






    // 9.3 Describe aliases
    res = await client.describeAlias({
        collection_name: "customized_setup_2",
        alias: "bob"
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
    //   db_name: 'default',
    //   alias: 'bob',
    //   collection: 'customized_setup_2'
    // }
    // 






    // 9.4 Reassign aliases to other collections
    res = await client.alterAlias({
        collection_name: "customized_setup_1",
        alias: "alice"
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






    res = await client.listAliases({
        collection_name: "customized_setup_1"
    })

    console.log(res)

    // Output
    // 
    // {
    //   aliases: [ 'alice' ],
    //   status: {
    //     error_code: 'Success',
    //     reason: '',
    //     code: 0,
    //     retriable: false,
    //     detail: ''
    //   },
    //   db_name: 'default',
    //   collection_name: 'customized_setup_1'
    // }
    // 






    res = await client.listAliases({
        collection_name: "customized_setup_2"
    })

    console.log(res)

    // Output
    // 
    // {
    //   aliases: [ 'bob' ],
    //   status: {
    //     error_code: 'Success',
    //     reason: '',
    //     code: 0,
    //     retriable: false,
    //     detail: ''
    //   },
    //   db_name: 'default',
    //   collection_name: 'customized_setup_2'
    // }
    // 






    // 9.5 Drop aliases
    res = await client.dropAlias({
        alias: "bob"
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


    res = await client.dropAlias({
        alias: "alice"
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










    // 10. Drop the collection
    res = await client.dropCollection({
        collection_name: "customized_setup_2"
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






    res = await client.dropCollection({
        collection_name: "customized_setup_1"
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






    res = await client.dropCollection({
        collection_name: "quick_setup"
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






}

main()