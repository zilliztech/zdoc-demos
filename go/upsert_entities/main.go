package main

import (
	"context"
	"encoding/json"
	"fmt"
	"log"
	"os"
	"time"

	"github.com/milvus-io/milvus-sdk-go/v2/client"
	"github.com/milvus-io/milvus-sdk-go/v2/entity"
)

type Row struct {
	ID          int64     `json:"id" milvus:"name:id"`
	Title       string    `json:"title" milvus:"name:title"`
	TitleVector []float32 `json:"title_vector" milvus:"name:title_vector"`
	Link        string    `json:"link" milvus:"name:link"`
	ReadingTime int64     `json:"reading_time" milvus:"name:reading_time"`
	Publication string    `json:"publication" milvus:"name:publication"`
	Claps       int64     `json:"claps" milvus:"name:claps"`
	Responses   int64     `json:"responses" milvus:"name:responses"`
}

type Dataset struct {
	Rows []Row `json:"rows"`
}

func main() {
	CLUSTER_ENDPOINT := "http://localhost:19530"
	TOKEN := "root:Milvus"
	COLLNAME := "medium_articles_2020"

	// 1. Connect to cluster

	connParams := client.Config{
		Address: CLUSTER_ENDPOINT,
		APIKey:  TOKEN,
	}

	conn, err := client.NewClient(context.Background(), connParams)

	if err != nil {
		log.Fatal("Failed to connect to Zilliz Cloud:", err.Error())
	}

	// 2. Create collection

	// Define fields

	id := entity.NewField().
		WithName("id").
		WithDataType(entity.FieldTypeInt64).
		WithIsPrimaryKey(true)

	title := entity.NewField().
		WithName("title").
		WithDataType(entity.FieldTypeVarChar).
		WithMaxLength(512)

	title_vector := entity.NewField().
		WithName("title_vector").
		WithDataType(entity.FieldTypeFloatVector).
		WithDim(768)

	link := entity.NewField().
		WithName("link").
		WithDataType(entity.FieldTypeVarChar).
		WithMaxLength(512)

	reading_time := entity.NewField().
		WithName("reading_time").
		WithDataType(entity.FieldTypeInt64)

	publication := entity.NewField().
		WithName("publication").
		WithDataType(entity.FieldTypeVarChar).
		WithMaxLength(512)

	claps := entity.NewField().
		WithName("claps").
		WithDataType(entity.FieldTypeInt64)

	responses := entity.NewField().
		WithName("responses").
		WithDataType(entity.FieldTypeInt64)

	// Define schema
	schema := &entity.Schema{
		CollectionName: COLLNAME,
		AutoID:         true,
		Fields: []*entity.Field{
			id,
			title,
			title_vector,
			link,
			reading_time,
			publication,
			claps,
			responses,
		},
	}

	err = conn.CreateCollection(context.Background(), schema, 2)

	if err != nil {
		log.Fatal("Failed to create collection:", err.Error())
	}

	// 3. Create index for cluster
	index, err := entity.NewIndexAUTOINDEX(entity.MetricType("L2"))

	if err != nil {
		log.Fatal("Failed to prepare the index:", err.Error())
	}

	fmt.Println(index.Name())

	err = conn.CreateIndex(context.Background(), COLLNAME, "title_vector", index, false)

	if err != nil {
		log.Fatal("Failed to create the index:", err.Error())
	}

	// 4. Load collection
	loadCollErr := conn.LoadCollection(context.Background(), COLLNAME, false)

	if loadCollErr != nil {
		log.Fatal("Failed to load collection:", loadCollErr.Error())
	}

	// 5. Get load progress
	progress, err := conn.GetLoadingProgress(context.Background(), COLLNAME, nil)

	if err != nil {
		log.Fatal("Failed to get loading progress:", err.Error())
	}

	fmt.Println("Loading progress:", progress)

	// 6. Read the dataset
	file, err := os.ReadFile("../../medium_articles_2020_dpr.json")
	if err != nil {
		log.Fatal("Failed to read file:", err.Error())
	}

	var data Dataset

	if err := json.Unmarshal(file, &data); err != nil {
		log.Fatal(err.Error())
	}

	fmt.Println("Dataset loaded, row number: ", len(data.Rows))

	rows := make([]interface{}, 0, 1)

	for i := 0; i < len(data.Rows); i++ {
		rows = append(rows, data.Rows[i])
	}

	columns, _ := entity.AnyToColumns(rows, schema)

	// 7. Upsert

	fmt.Println("Start upserting ...")

	colUpsertRes, err := conn.Upsert(context.Background(), COLLNAME, "", columns...)

	if err != nil {
		log.Fatal("Failed to upsert columns:", err.Error())
	}

	fmt.Println("Upserted entities: ", colUpsertRes.Len())

	time.Sleep(5 * time.Second)

	// 8. Search

	fmt.Println("Start searching ...")

	vectors := []entity.Vector{}

	for _, row := range data.Rows[:1] {
		vector := make(entity.FloatVector, 0, 768)
		vector = append(vector, row.TitleVector...)
		vectors = append(vectors, vector)
	}

	sp, _ := entity.NewIndexAUTOINDEXSearchParam(1)

	limit := client.WithLimit(10)
	offset := client.WithOffset(0)
	topK := 5
	outputFields := []string{"title", "claps", "reading_time"}

	res, err := conn.Search(
		context.Background(),               // context
		COLLNAME,                           // collectionName
		[]string{},                         // partitionNames
		"claps > 30 and reading_time < 10", // expr
		outputFields,                       // outputfields
		vectors,                            // vectors
		"title_vector",                     // vectorField
		entity.MetricType("L2"),            // metricType
		topK,                               // topK
		sp,                                 // sp
		limit,                              // opts
		offset,                             // opts
	)

	if err != nil {
		log.Fatal("Failed to insert rows:", err.Error())
	}

	fmt.Println(resultsToJSON(res))

	// 9. Drop collection
	err = conn.DropCollection(context.Background(), COLLNAME)

	if err != nil {
		log.Fatal("Failed to drop collection:", err.Error())
	}

}

func resultsToJSON(results []client.SearchResult) string {
	var result []map[string]interface{}
	for _, r := range results {
		result = append(result, map[string]interface{}{
			"counts":    r.ResultCount,
			"fields":    fieldsToJSON(results, true),
			"rows":      fieldsToJSON(results, false),
			"distances": r.Scores,
		})
	}

	jsonData, _ := json.MarshalIndent(result, "", "  ")
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
	}
	// You should add more types here
	return data
}
