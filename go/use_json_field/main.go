package main

import (
	"context"
	"encoding/json"
	"fmt"
	"log"
	"os"

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

type JsonRow struct {
	ID          int64     `json:"id" milvus:"name:id"`
	Title       string    `json:"title" milvus:"name:title"`
	TitleVector []float32 `json:"title_vector" milvus:"name:title_vector"`
	ArticleMeta Dynamic   `json:"article_meta" milvus:"name:article_meta"`
}

type Dynamic struct {
	Link        string `json:"link" milvus:"name:link"`
	ReadingTime int64  `json:"reading_time" milvus:"name:reading_time"`
	Publication string `json:"publication" milvus:"name:publication"`
	Claps       int64  `json:"claps" milvus:"name:claps"`
	Responses   int64  `json:"responses" milvus:"name:responses"`
}

type SearchParameters struct {
	nprobe float64
}

func (s SearchParameters) Params() map[string]interface{} {
	parameters := make(map[string]interface{})
	parameters["nprobe"] = s.nprobe

	return parameters
}

func (s SearchParameters) AddRadius(radius float64) {
}

func (s SearchParameters) AddRangeFilter(rangeFilter float64) {
}

func main() {
	CLUSTER_ENDPOINT := "YOUR_CLUSTER_ENDPOINT"
	TOKEN := "YOUR_CLUSTER_TOKEN"
	COLLNAME := "medium_articles_2020"

	// 1. Connect to cluster

	connParams := client.Config{
		Address:       CLUSTER_ENDPOINT,
		APIKey:        TOKEN,
		EnableTLSAuth: true,
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

	// Create index
	index, err := entity.NewIndexAUTOINDEX(entity.MetricType("L2"))

	if err != nil {
		log.Fatal("Failed to prepare the index:", err.Error())
	}

	fmt.Println(index.Name())

	err = conn.CreateIndex(context.Background(), COLLNAME, "title_vector", index, false)

	if err != nil {
		log.Fatal("Failed to create the index:", err.Error())
	}

	// Load collection
	loadCollErr := conn.LoadCollection(context.Background(), COLLNAME, false)

	if loadCollErr != nil {
		log.Fatal("Failed to load collection:", loadCollErr.Error())
	}

	// Get load progress
	progress, err := conn.GetLoadingProgress(context.Background(), COLLNAME, nil)

	if err != nil {
		log.Fatal("Failed to get loading progress:", err.Error())
	}

	fmt.Println("Loading progress:", progress)

	// Read the dataset
	file, err := os.ReadFile("../../medium_articles_2020_dpr.json")
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
		articleMeta := Dynamic{
			Link:        data.Rows[i].Link,
			ReadingTime: data.Rows[i].ReadingTime,
			Publication: data.Rows[i].Publication,
			Claps:       data.Rows[i].Claps,
			Responses:   data.Rows[i].Responses,
		}

		jsonRow := JsonRow{
			ID:          id,
			Title:       title,
			TitleVector: titleVector,
			ArticleMeta: articleMeta,
		}

		rows = append(rows, jsonRow)
	}

	fmt.Println("Dataset loaded, row number: ", len(data.Rows))
	fmt.Println("Start inserting ...")

	col, err := conn.InsertRows(context.Background(), COLLNAME, "", rows)

	if err != nil {
		log.Fatal("Failed to insert rows:", err.Error())
	}

	fmt.Println("Inserted entities: ", col.Len())
	fmt.Println("Start searching ...")

	vectors := []entity.Vector{}

	for _, row := range data.Rows[:1] {
		vector := make(entity.FloatVector, 0, 768)
		vector = append(vector, row.TitleVector...)
		vectors = append(vectors, vector)
	}

	var sp entity.SearchParam = SearchParameters{1024}

	limit := client.WithLimit(10)
	offset := client.WithOffset(0)
	expr := "article_meta[\"claps\"] > 30 and article_meta['reading_time'] < 10"

	res, err := conn.Search(
		context.Background(),              // context
		COLLNAME,                          // collectionName
		[]string{},                        // partitionNames
		expr,                              // expr
		[]string{"title", "article_meta"}, // outputFields
		vectors,                           // vectors
		"title_vector",                    // vectorField
		entity.MetricType("L2"),           // metricType
		5,                                 // topK
		sp,                                // sp
		limit,                             // opts
		offset,                            // opts
	)

	if err != nil {
		log.Fatal("Failed to insert rows:", err.Error())
	}

	for i, result := range res {
		log.Println("Result counts", i, ":", result.ResultCount)

		ids := result.IDs.FieldData().GetScalars().GetLongData().GetData()
		scores := result.Scores
		titles := result.Fields.GetColumn("title").FieldData().GetScalars().GetStringData().GetData()
		article_metas := result.Fields.GetColumn("article_meta").FieldData().GetScalars().GetJsonData().GetData()

		for i, record := range ids {
			var article_meta Dynamic
			json.Unmarshal(article_metas[i], &article_meta)

			log.Println("ID:", record, "Score:", scores[i], "Title:", titles[i], "Claps:", article_meta.Link, "Reading time:", article_meta.ReadingTime)
		}
	}

	// Drop collection
	err = conn.DropCollection(context.Background(), COLLNAME)

	if err != nil {
		log.Fatal("Failed to drop collection:", err.Error())
	}

}
