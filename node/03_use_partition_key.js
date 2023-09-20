const fs = require("fs")
const { MilvusClient, DataType } = require("@zilliz/milvus2-sdk-node")

const address = "YOUR_CLUSTER_ENDPOINT"
// - For a serverless cluster, use an API key as the token. 
// - For a dedicated cluster, use the cluster credentials as the token in the format of 'user:password'.
const token = "YOUR_CLUSTER_TOKEN"
const collectionName = "medium_articles_2020"

async function main() {
    // Connect to the cluster
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
            max_length: 512,
            // The field "publication" acts as the primary key
            is_partition_key: true
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
        fields: fields,
        // As an alternative, you can set the partition key by its name when creating a collection.
        // partition_key_field: "publication"
    })

    if (res.error_code == 'Success') {
        res = await client.describeCollection({
            collection_name: collectionName
        })

        console.log("Collection details: ", res)
    } else {
        console.log("Create collection failed: ", res.reason)
    }

    // 4. Create index
    res = await client.createIndex({
        collection_name: collectionName,
        field_name: "title_vector",
        index_name: "title_vector_index",
        index_type: "IVF_FLAT",
        params: {
            nlist: 16384
        }
    })

    if (res.error_code == 'Success') {
        res = await client.loadCollection({
            collection_name: collectionName
        })

        console.log("Load collection: ", res)
    }

    // 5. Insert vectors
    fs.readFile('../medium_articles_2020_dpr.json', async (err, jsonString) => {
        if (err) {
            console.log("File read failed: ", err)
            return
        }

        const data = JSON.parse(jsonString)

        // read rows
        const rows = data["rows"]
        const row = rows[0]
        console.log("Data: ", Object.keys(row))

        //insert vectors
        res = await client.insert({
            collection_name: collectionName,
            data: rows.slice(1, 1000)
        })

        console.log("Insert vectors: ", res)

        res = await client.flush({
            collection_names: [collectionName]
        })

        // 6. Coduct an ANN search

        res = await client.search({
            collection_name: collectionName,
            vector: rows[0].title_vector,
            limit: 3,
            filter: "claps > 30 and reading_time < 10 and publication in ['Towards Data Science', 'Personal Growth']",
            output_fields: ["title", "link"]
        });
        
        console.log(res);

        // 7. Get collection info

        res = await client.describeCollection({
            collection_name: collectionName
        })

        console.log(res);

        // 8. Drop collection

        res = await client.dropCollection({
            collection_name: collectionName
        })     
        
        console.log(res);
    })

    return 0    
}

main()