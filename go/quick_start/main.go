package main

import (
	"context"
	"encoding/json"
	"fmt"
	"log"
	"math/rand"
	"time"

	"github.com/milvus-io/milvus-sdk-go/v2/client"
	"github.com/milvus-io/milvus-sdk-go/v2/entity"
)

type Row struct {
	ID     int64     `json:"id" milvus:"name:id"`
	Vector []float32 `json:"vector" milvus:"name:vector"`
	Color  string    `json:"color" milvus:"name:color"`
}

func main() {
	CLUSTER_ENDPOINT := "YOUR_CLUSTER_ENDPOINT"
	TOKEN := "YOUR_CLUSTER_TOKEN"

	// 1. Connect to cluster

	connParams := client.Config{
		Address: CLUSTER_ENDPOINT,
		APIKey:  TOKEN,
	}

	conn, err := client.NewClient(context.Background(), connParams)

	if err != nil {
		log.Fatal("Failed to connect to Zilliz Cloud:", err.Error())
	}

	fmt.Println("Connected to Milvus")

	// Output:
	//
	// Connected to Milvus

	// 2. Create a collection
	collectionName := "quick_setup"

	id := entity.NewField().
		WithName("id").
		WithDataType(entity.FieldTypeInt64).
		WithIsPrimaryKey(true)

	vector := entity.NewField().
		WithName("vector").
		WithDataType(entity.FieldTypeFloatVector).
		WithDim(5)

	schema := &entity.Schema{
		CollectionName: collectionName,
		AutoID:         false,
		Fields: []*entity.Field{
			id,
			vector,
		},
		EnableDynamicField: true,
	}

	err = conn.CreateCollection(
		context.Background(), // ctx
		schema,               // schema
		1,                    // shards
	)

	if err != nil {
		log.Fatal("Failed to create collection:", err.Error())
	}

	fmt.Println("Collection created")

	// Output:
	//
	// Collection created

	// 3. Create the index
	index, err := entity.NewIndexAUTOINDEX(entity.MetricType("IP"))

	if err != nil {
		log.Fatal("Failed to prepare the index:", err.Error())
	}

	err = conn.CreateIndex(
		context.Background(), // ctx
		collectionName,       // collection name
		"vector",             // target field name
		index,                // index type
		false,                // async
	)

	if err != nil {
		log.Fatal("Failed to create the index:", err.Error())
	}

	fmt.Println("Index created")

	// Output:
	//
	// Index created

	// 4. Load the collection
	err = conn.LoadCollection(
		context.Background(), // ctx
		collectionName,       // collection name
		false,                // async
	)

	if err != nil {
		log.Fatal("Failed to load the collection:", err.Error())
	}

	fmt.Println("Collection loaded")

	// Output:
	//
	// Collection loaded

	// 5. Prepare the data
	rows := make([]interface{}, 0, 1)

	rows = append(rows, Row{
		ID:     0,
		Vector: []float32{0.3580376395471989, -0.6023495712049978, 0.18414012509913835, -0.26286205330961354, 0.9029438446296592},
		Color:  "pink_8682",
	})

	rows = append(rows, Row{
		ID:     1,
		Vector: []float32{0.19886812562848388, 0.06023560599112088, 0.6976963061752597, 0.2614474506242501, 0.838729485096104},
		Color:  "red_7025",
	})

	rows = append(rows, Row{
		ID:     2,
		Vector: []float32{0.43742130801983836, -0.5597502546264526, 0.6457887650909682, 0.7894058910881185, 0.20785793220625592},
		Color:  "orange_6781",
	})

	rows = append(rows, Row{
		ID:     3,
		Vector: []float32{0.2923324203491211, -0.14941246032714844, 0.5195220947265625, -0.8015365600585938, 0.12919048309326172},
		Color:  "blue_5452",
	})

	rows = append(rows, Row{
		ID:     4,
		Vector: []float32{0.6222862243652344, -0.7742881774902344, 0.13988418579101562, -0.1562347412109375, 0.1722564697265625},
		Color:  "green_4620",
	})

	// 6. Insert the data
	col, err := conn.InsertRows(
		context.Background(), // ctx
		collectionName,       // collection name
		"",                   // partition name
		rows,                 // rows
	)

	if err != nil {
		log.Fatal("Failed to insert the data:", err.Error())
	}

	fmt.Println("Insert Counts:", col.Len())

	// Output:
	//
	// Insert Counts: 5

	// 7. Insert more data
	rows = make([]interface{}, 0, 1)
	colors := []string{"green", "blue", "yellow", "red", "black", "white", "purple", "pink", "orange", "brown", "grey"}

	for i := 5; i < 1000; i++ {
		rows = append(rows, Row{
			ID:     int64(i),
			Vector: []float32{rand.Float32(), rand.Float32(), rand.Float32(), rand.Float32(), rand.Float32()},
			Color:  "color_" + colors[rand.Intn(len(colors))],
		})
	}

	col, err = conn.InsertRows(
		context.Background(), // ctx
		collectionName,       // collection name
		"",                   // partition name
		rows,                 // rows
	)

	if err != nil {
		log.Fatal("Failed to insert the data:", err.Error())
	}

	fmt.Println("Insert Counts:", col.Len())

	// Output:
	//
	// Insert Counts: 995

	time.Sleep(10 * time.Second)

	// 8. Search with a single vector
	queryVector := make(entity.FloatVector, 0, 5)
	queryVector = append(queryVector, 0.3580376395471989, -0.6023495712049978, 0.18414012509913835, -0.26286205330961354, 0.9029438446296592)
	queryVectors := []entity.Vector{}
	queryVectors = append(queryVectors, queryVector)
	expr := ""
	outputFields := []string{"id", "color"}
	topK := 3

	sp, _ := entity.NewIndexAUTOINDEXSearchParam(1)

	search, err := conn.Search(
		context.Background(),    // ctx
		collectionName,          // collection name
		[]string{},              // partition names
		expr,                    // expression
		outputFields,            // output fields
		queryVectors,            // query vectors
		"vector",                // target field name
		entity.MetricType("IP"), // metric type
		topK,                    // topK
		sp,                      // search param
	)

	if err != nil {
		log.Fatal("Failed to search the data:", err.Error())
	}

	fmt.Println(resultsToJSON(search))

	// Output:
	// [
	// 	{
	// 		"counts": 3,
	// 		"distances": [
	// 			1.4093276,
	// 			1.2094578,
	// 			1.1929908
	// 		],
	// 		"rows": [
	// 			{
	// 				"color": "pink_8682",
	// 				"id": 0
	// 			},
	// 			{
	// 				"color": "color_red",
	// 				"id": 960
	// 			},
	// 			{
	// 				"color": "color_yellow",
	// 				"id": 253
	// 			}
	// 		]
	// 	}
	// ]

	// 9. Search with multiple vectors
	queryVector1 := make(entity.FloatVector, 0, 5)
	queryVector2 := make(entity.FloatVector, 0, 5)
	queryVector1 = append(queryVector1, 0.3580376395471989, -0.6023495712049978, 0.18414012509913835, -0.26286205330961354, 0.9029438446296592)
	queryVector2 = append(queryVector2, 0.19886812562848388, 0.06023560599112088, 0.6976963061752597, 0.2614474506242501, 0.838729485096104)
	queryVectors = []entity.Vector{}
	queryVectors = append(queryVectors, queryVector1)
	queryVectors = append(queryVectors, queryVector2)
	expr = ""
	outputFields = []string{"id", "color"}
	topK = 3

	sp, _ = entity.NewIndexAUTOINDEXSearchParam(1)

	search, err = conn.Search(
		context.Background(),    // ctx
		collectionName,          // collection name
		[]string{},              // partition names
		expr,                    // expression
		outputFields,            // output fields
		queryVectors,            // query vectors
		"vector",                // target field name
		entity.MetricType("IP"), // metric type
		topK,                    // topK
		sp,                      // search param
	)

	if err != nil {
		log.Fatal("Failed to search the data:", err.Error())
	}

	fmt.Println(resultsToJSON(search))

	// Output:
	// [
	// 	{
	// 		"counts": 3,
	// 		"distances": [
	// 			1.4093276,
	// 			1.2094578,
	// 			1.1929908
	// 		],
	// 		"rows": [
	// 			{
	// 				"color": "color_black",
	// 				"id": 488
	// 			},
	// 			{
	// 				"color": "color_black",
	// 				"id": 338
	// 			},
	// 			{
	// 				"color": "color_green",
	// 				"id": 160
	// 			}
	// 		]
	// 	},
	// 	{
	// 		"counts": 3,
	// 		"distances": [
	// 			1.8348937,
	// 			1.807337,
	// 			1.7894672
	// 		],
	// 		"rows": [
	// 			{
	// 				"color": "color_black",
	// 				"id": 488
	// 			},
	// 			{
	// 				"color": "color_black",
	// 				"id": 338
	// 			},
	// 			{
	// 				"color": "color_green",
	// 				"id": 160
	// 			}
	// 		]
	// 	}
	// ]

	// 10. Search with filter expressions using schema-defined fields
	queryVector = make(entity.FloatVector, 0, 5)
	queryVector = append(queryVector, 0.3580376395471989, -0.6023495712049978, 0.18414012509913835, -0.26286205330961354, 0.9029438446296592)
	queryVectors = []entity.Vector{}
	queryVectors = append(queryVectors, queryVector)
	expr = "500 < id < 800"
	outputFields = []string{"id", "color"}
	topK = 3

	sp, _ = entity.NewIndexAUTOINDEXSearchParam(1)

	search, err = conn.Search(
		context.Background(),    // ctx
		collectionName,          // collection name
		[]string{},              // partition names
		expr,                    // expression
		outputFields,            // output fields
		queryVectors,            // query vectors
		"vector",                // target field name
		entity.MetricType("IP"), // metric type
		topK,                    // topK
		sp,                      // search param
	)

	if err != nil {
		log.Fatal("Failed to search the data:", err.Error())
	}

	fmt.Println(resultsToJSON(search))

	// Output:
	// [
	// 	{
	// 		"counts": 3,
	// 		"distances": [
	// 			1.1309906,
	// 			1.0854248,
	// 			1.0046971
	// 		],
	// 		"rows": [
	// 			{
	// 				"color": "color_brown",
	// 				"id": 750
	// 			},
	// 			{
	// 				"color": "color_black",
	// 				"id": 771
	// 			},
	// 			{
	// 				"color": "color_black",
	// 				"id": 683
	// 			}
	// 		]
	// 	}
	// ]

	// 10. Search with filter expressions using non-schema-defined fields
	queryVector = make(entity.FloatVector, 0, 5)
	queryVector = append(queryVector, 0.3580376395471989, -0.6023495712049978, 0.18414012509913835, -0.26286205330961354, 0.9029438446296592)
	queryVectors = []entity.Vector{}
	queryVectors = append(queryVectors, queryVector)
	expr = "$meta[\"color\"] like \"red%\""
	outputFields = []string{"id", "color"}
	topK = 3

	sp, _ = entity.NewIndexAUTOINDEXSearchParam(1)

	search, err = conn.Search(
		context.Background(),    // ctx
		collectionName,          // collection name
		[]string{},              // partition names
		expr,                    // expression
		outputFields,            // output fields
		queryVectors,            // query vectors
		"vector",                // target field name
		entity.MetricType("IP"), // metric type
		topK,                    // topK
		sp,                      // search param
	)

	if err != nil {
		log.Fatal("Failed to search the data:", err.Error())
	}

	fmt.Println(resultsToJSON(search))

	// Output:
	// [
	// 	{
	// 		"counts": 1,
	// 		"distances": [
	// 			0.8519943
	// 		],
	// 		"rows": [
	// 			{
	// 				"color": "red_7025",
	// 				"id": 1
	// 			}
	// 		]
	// 	}
	// ]

	// 10. Query using schema-defined fields
	expr = "10 < id < 15"
	outputFields = []string{"id", "color"}
	topK = 3

	sp, _ = entity.NewIndexAUTOINDEXSearchParam(1)

	query, err := conn.Query(
		context.Background(), // ctx
		collectionName,       // collection name
		[]string{},           // partition names
		expr,                 // expression
		outputFields,         // output fields
	)

	if err != nil {
		log.Fatal("Failed to query the data:", err.Error())
	}

	fmt.Println(resultSetToJSON(query, true))

	// Output:
	// [
	// 	{
	// 		"id": [
	// 			11,
	// 			12,
	// 			13,
	// 			14
	// 		]
	// 	},
	// 	{
	// 		"$meta": [
	// 			"color_purple",
	// 			"color_green",
	// 			"color_white",
	// 			"color_blue"
	// 		]
	// 	}
	// ]

	// 11. Query using schema-defined fields
	expr = "$meta[\"color\"] like \"brown_8%\""
	outputFields = []string{"id", "color"}
	topK = 3

	sp, _ = entity.NewIndexAUTOINDEXSearchParam(1)

	query, err = conn.Query(
		context.Background(), // ctx
		collectionName,       // collection name
		[]string{},           // partition names
		expr,                 // expression
		outputFields,         // output fields
	)

	if err != nil {
		log.Fatal("Failed to query the data:", err.Error())
	}

	fmt.Println(resultSetToJSON(query, true))

	// Output:
	// [
	// 	{
	// 		"id": null
	// 	},
	// 	{
	// 		"$meta": null
	// 	}
	// ]

	// 12. Get entity by ID
	ids := entity.NewColumnInt64("id", []int64{0, 1, 2, 3, 4})

	get, err := conn.Get(
		context.Background(), // ctx
		collectionName,       // collection name
		ids,                  // ids
	)

	if err != nil {
		log.Fatal("Failed to get the data:", err.Error())
	}

	fmt.Println(resultSetToJSON(get, true))

	// Output:
	// [
	// 	{
	// 		"id": [
	// 			0,
	// 			1,
	// 			2,
	// 			3,
	// 			4
	// 		]
	// 	},
	// 	{
	// 		"vector": null
	// 	}
	// ]

	// 13. Delete entities by ID
	ids = entity.NewColumnInt64("id", []int64{0, 1, 2, 3, 4})

	err = conn.DeleteByPks(
		context.Background(), // ctx
		collectionName,       // collection name
		"",                   // partition names
		ids,                  // ids
	)

	if err != nil {
		log.Fatal("Failed to delete the data:", err.Error())
	}

	// 13. Delete entities by filter expressions
	expr = "id in [5,6,7,8,9]"

	err = conn.Delete(
		context.Background(), // ctx
		collectionName,       // collection name
		"",                   // partition names
		expr,                 // expression
	)

	if err != nil {
		log.Fatal("Failed to delete the data:", err.Error())
	}

	// 14. Drop the collection
	err = conn.DropCollection(
		context.Background(), // ctx
		collectionName,       // collection name
	)

	if err != nil {
		log.Fatal("Failed to drop the collection:", err.Error())
	}

	fmt.Println("Collection dropped")

	// Output:
	//
	// Collection dropped

}

func resultSetToJSON(results client.ResultSet, inFields bool) string {
	var fields []map[string]interface{}
	var rows []map[string]interface{}
	var ret []map[string]interface{}
	for _, r := range results {
		field := make(map[string]interface{})
		fname := r.FieldData().FieldName
		values := typeSwitch(r)

		for i, v := range values {
			if len(rows) < i+1 {
				row := make(map[string]interface{})
				row[fname] = v
				rows = append(rows, row)
			} else {
				rows[i][fname] = v
			}
		}

		field[fname] = values
		fields = append(fields, field)
	}

	if inFields {
		ret = fields
	} else {
		ret = rows
	}

	jsonData, _ := json.Marshal(ret)

	return string(jsonData)
}

func resultsToJSON(results []client.SearchResult) string {
	var result []map[string]interface{}
	for _, r := range results {
		result = append(result, map[string]interface{}{
			"counts": r.ResultCount,
			// "fields": fieldsToJSON(results, true),
			"rows":      fieldsToJSON(results, false),
			"distances": r.Scores,
		})
	}

	jsonData, _ := json.Marshal(result)
	return string(jsonData)
}

func fieldsToJSON(results []client.SearchResult, inFields bool) []map[string]interface{} {
	var fields []map[string]interface{}
	var rows []map[string]interface{}
	var ret []map[string]interface{}
	for _, r := range results {
		for _, f := range r.Fields {
			field := make(map[string]interface{})
			name := f.Name()
			data := typeSwitch(f)

			for i, v := range data {
				if len(rows) < i+1 {
					row := make(map[string]interface{})
					row[name] = v
					rows = append(rows, row)
				} else {
					rows[i][name] = v
				}
			}

			field[name] = data
			fields = append(fields, field)
		}
	}

	if inFields {
		ret = fields
	} else {
		ret = rows
	}

	return ret
}

func typeSwitch(c entity.Column) []interface{} {
	ctype := c.FieldData().GetType().String()

	var data []interface{}
	switch ctype {
	case "Int64":
		longData := c.FieldData().GetScalars().GetLongData().Data
		for _, d := range longData {
			data = append(data, d)
		}
	case "VarChar":
		stringData := c.FieldData().GetScalars().GetStringData().Data
		for _, d := range stringData {
			data = append(data, d)
		}
	case "JSON":
		jsonData := c.FieldData().GetScalars().GetJsonData().Data
		for _, d := range jsonData {
			var jsonValue interface{}
			err := json.Unmarshal(d, &jsonValue)
			if err != nil {
				log.Fatal("Failed to unmarshal")
				continue
			}
			value, _ := jsonValue.(map[string]interface{})
			data = append(data, value[c.Name()])
		}
	}
	// You should add more types here
	return data
}
