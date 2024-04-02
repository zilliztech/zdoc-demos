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
	"golang.org/x/exp/rand"
)

type DatasetRow struct {
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
	Rows []DatasetRow `json:"rows"`
}

type CollSchema struct {
	ID          int64      `json:"id" milvus:"name:id"`
	Title       string     `json:"title" milvus:"name:title"`
	TitleVector []float32  `json:"title_vector" milvus:"name:title_vector"`
	ArticleMeta JsonFields `json:"article_meta" milvus:"name:article_meta"`
}

type JsonFields struct {
	Link        string  `json:"link" milvus:"name:link"`
	ReadingTime int64   `json:"reading_time" milvus:"name:reading_time"`
	Publication string  `json:"publication" milvus:"name:publication"`
	Claps       int64   `json:"claps" milvus:"name:claps"`
	Responses   int64   `json:"responses" milvus:"name:responses"`
	Tags1       []int   `json:"tags1" milvus:"name:tags1"`
	Tags2       [][]int `json:"tags2" milvus:"name:tags2"`
}

func main() {
	CLUSTER_ENDPOINT := "http://localhost:19530"
	TOKEN := "root:Milvus"
	COLLNAME := "medium_articles_2020"
	DATA_FILE := "medium_articles_2020_dpr.json"

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

	article_meta := entity.NewField().
		WithName("article_meta").
		WithDataType(entity.FieldTypeJSON)

	// Define schema
	schema := &entity.Schema{
		CollectionName: COLLNAME,
		AutoID:         true,
		Fields: []*entity.Field{
			id,
			title,
			title_vector,
			article_meta,
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
	file, err := os.ReadFile(DATA_FILE)
	if err != nil {
		log.Fatal("Failed to read file:", err.Error())
	}

	var data Dataset

	if err := json.Unmarshal(file, &data); err != nil {
		log.Fatal(err.Error())
	}

	rows := make([]interface{}, 0, 1)

	for i := 0; i < len(data.Rows); i++ {
		id := data.Rows[i].ID
		title := data.Rows[i].Title
		titleVector := data.Rows[i].TitleVector
		articleMeta := JsonFields{
			Link:        data.Rows[i].Link,
			ReadingTime: data.Rows[i].ReadingTime,
			Publication: data.Rows[i].Publication,
			Claps:       data.Rows[i].Claps,
			Responses:   data.Rows[i].Responses,
			Tags1:       randList(20, 40),
			Tags2:       rand2DList(10, 4, 40),
		}

		collSchema := CollSchema{
			ID:          id,
			Title:       title,
			TitleVector: titleVector,
			ArticleMeta: articleMeta,
		}

		rows = append(rows, collSchema)
	}

	fmt.Println("Dataset loaded, row number: ", len(data.Rows))


	// 7. Insert data
	fmt.Println("Start inserting ...")


	col, err := conn.InsertRows(context.Background(), COLLNAME, "", rows)

	if err != nil {
		log.Fatal("Failed to insert rows:", err.Error())
	}

	fmt.Println("Inserted entities: ", col.Len())


	time.Sleep(5 * time.Second)

	// 8. Count entities

	fmt.Println("Start searching ...")


	vectors := []entity.Vector{}

	for _, row := range data.Rows[:1] {
		vector := make(entity.FloatVector, 0, 768)
		vector = append(vector, row.TitleVector...)
		vectors = append(vectors, vector)
	}

	sp, _ := entity.NewIndexAUTOINDEXSearchParam(1)

	// limit := client.WithLimit(10)
	// offset := client.WithOffset(0)
	topK := 5
	outputFields := []string{"count(*)"}
	expr := ""

	resq, err := conn.Query(
		context.Background(), // context
		COLLNAME,             // collectionName
		[]string{},           // partitionNames
		expr,                 // expr
		outputFields,         // outputFields
	)

	if err != nil {
		log.Fatal(err.Error())
	}

	fmt.Println(resultSetToJSON(resq, false))

	// 9. Count entities with condition

	expr = "article_meta[\"claps\"] > 30 and article_meta[\"reading_time\"] < 10"

	resq, err = conn.Query(
		context.Background(), // context
		COLLNAME,             // collectionName
		[]string{},           // partitionNames
		expr,                 // expr
		outputFields,         // outputFields
	)

	if err != nil {
		log.Fatal(err.Error())
	}

	fmt.Println(resultSetToJSON(resq, false))

	// 10. Check if a specific element exists in a JSON field

	// matches all articles with tags_1 having the member 16
	expr_1 := "JSON_CONTAINS(article_meta[\"tags_1\"], 16)"

	// matches all articles with tags_2 having the member [5, 3, 39, 8]
	// expr_2 := "JSON_CONTAINS(article_meta[\"tags_2\"], [5, 3, 39, 8])"

	// matches all articles with tags_1 having a member from [5, 3, 39, 8]
	// expr_3 := "JSON_CONTAINS_ANY(article_meta[\"tags_1\"], [5, 3, 39, 8])"

	// matches all articles with tags_1 having all members from [2, 4, 6]
	expr_4 := "JSON_CONTAINS_ALL(article_meta[\"tags_1\"], [2, 4, 6])"

	outputFields = []string{"title", "article_meta"}

	res, err := conn.Search(
		context.Background(),    // context
		COLLNAME,                // collectionName
		[]string{},              // partitionNames
		expr_1,                  // expr
		outputFields,            // outputFields
		vectors,                 // vectors
		"title_vector",          // vectorField
		entity.MetricType("L2"), // metricType
		topK,                    // topK
		sp,                      // sp
	)

	if err != nil {
		log.Fatal(err.Error())
	}

	fmt.Println(resultsToJSON(res))

	res, err = conn.Search(
		context.Background(),    // context
		COLLNAME,                // collectionName
		[]string{},              // partitionNames
		expr_4,                  // expr
		outputFields,            // outputFields
		vectors,                 // vectors
		"title_vector",          // vectorField
		entity.MetricType("L2"), // metricType
		topK,                    // topK
		sp,                      // sp
	)

	if err != nil {
		log.Fatal(err.Error())
	}

	fmt.Println(resultsToJSON(res))

	// 11. Drop collection
	err = conn.DropCollection(context.Background(), COLLNAME)

	if err != nil {
		log.Fatal("Failed to drop collection:", err.Error())
	}

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
			// "fields":    fieldsToJSON(results, true),
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
			var jsonFields JsonFields
			if err := json.Unmarshal(d, &jsonFields); err != nil {
				log.Fatal(err.Error())
			}

			data = append(data, jsonFields)
		}
	}
	// You should add more types here
	return data
}

func randList(n int, max int) []int {
	var list []int
	for i := 0; i < n; i++ {
		list = append(list, rand.Intn(max))
	}
	return list
}

func rand2DList(n int, m int, max int) [][]int {
	var list [][]int
	for i := 0; i < n; i++ {
		list = append(list, randList(m, max))
	}
	return list
}
