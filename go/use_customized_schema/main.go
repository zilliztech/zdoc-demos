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
	CLUSTER_ENDPOINT := "YOUR_CLUSTER_ENDPOINT"
	TOKEN := "YOUR_CLUSTER_TOKEN"
	COLLNAME := "medium_articles_2020"
	DATA_FILE := "../../medium_articles_2020_dpr.json"

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

	// Output:
	//
	// AUTOINDEX

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

	// Output:
	//
	// Loading progress: 100

	// 6. Read the dataset
	file, err := os.ReadFile(DATA_FILE)
	if err != nil {
		log.Fatal("Failed to read file:", err.Error())
	}

	var data Dataset

	if err := json.Unmarshal(file, &data); err != nil {
		log.Fatal(err.Error())
	}

	fmt.Println("Dataset loaded, row number: ", len(data.Rows))

	// Output:
	//
	// Dataset loaded, row number:  5979

	// 7. Insert data
	fmt.Println("Start inserting ...")

	// Output:
	//
	// Start inserting ...

	rows := make([]interface{}, 0, 1)

	for i := 0; i < len(data.Rows); i++ {
		rows = append(rows, data.Rows[i])
	}

	col, err := conn.InsertRows(context.Background(), COLLNAME, "", rows)

	if err != nil {
		log.Fatal("Failed to insert rows:", err.Error())
	}

	fmt.Println("Inserted entities: ", col.Len())

	// Output:
	//
	// Inserted entities:  5979

	time.Sleep(5 * time.Second)

	// 8. Search

	fmt.Println("Start searching ...")

	// Output:
	//
	// Start searching ...

	// single vector search
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
		context.Background(),    // context
		COLLNAME,                // collectionName
		[]string{},              // partitionNames
		"",                      // expr
		outputFields,            // outputfields
		vectors,                 // vectors
		"title_vector",          // vectorField
		entity.MetricType("L2"), // metricType
		topK,                    // topK
		sp,                      // sp
		limit,                   // opts
		offset,                  // opts
	)

	if err != nil {
		log.Fatal("Failed to insert rows:", err.Error())
	}

	fmt.Println(resultsToJSON(res))

	// Output:
	// [
	// 	{
	// 		"counts": 5,
	// 		"distances": [
	// 			0,
	// 			0.29999837,
	// 			0.36103836,
	// 			0.37674016,
	// 			0.41629803
	// 		],
	// 		"rows": [
	// 			{
	// 				"claps": 1100,
	// 				"reading_time": 13,
	// 				"title": "The Reported Mortality Rate of Coronavirus Is Not Important"
	// 			},
	// 			{
	// 				"claps": 215,
	// 				"reading_time": 10,
	// 				"title": "Following the Spread of Coronavirus"
	// 			},
	// 			{
	// 				"claps": 83,
	// 				"reading_time": 8,
	// 				"title": "The Hidden Side Effect of the Coronavirus"
	// 			},
	// 			{
	// 				"claps": 2900,
	// 				"reading_time": 9,
	// 				"title": "Why The Coronavirus Mortality Rate is Misleading"
	// 			},
	// 			{
	// 				"claps": 51,
	// 				"reading_time": 4,
	// 				"title": "Coronavirus shows what ethical Amazon could look like"
	// 			}
	// 		]
	// 	}
	// ]

	// multiple vector search
	vectors = []entity.Vector{}

	for _, row := range data.Rows[:2] {
		vector := make(entity.FloatVector, 0, 768)
		vector = append(vector, row.TitleVector...)
		vectors = append(vectors, vector)
	}

	sp, _ = entity.NewIndexAUTOINDEXSearchParam(1)

	limit = client.WithLimit(10)
	offset = client.WithOffset(0)
	topK = 5
	outputFields = []string{"title", "claps", "reading_time"}

	res, err = conn.Search(
		context.Background(),    // context
		COLLNAME,                // collectionName
		[]string{},              // partitionNames
		"",                      // expr
		outputFields,            // outputfields
		vectors,                 // vectors
		"title_vector",          // vectorField
		entity.MetricType("L2"), // metricType
		topK,                    // topK
		sp,                      // sp
		limit,                   // opts
		offset,                  // opts
	)

	if err != nil {
		log.Fatal("Failed to insert rows:", err.Error())
	}

	fmt.Println(resultsToJSON(res))

	// Output:
	// [
	// 	{
	// 		"counts": 5,
	// 		"distances": [
	// 			0,
	// 			0.29999837,
	// 			0.36103836,
	// 			0.37674016,
	// 			0.41629803
	// 		],
	// 		"rows": [
	// 			{
	// 				"claps": 726,
	// 				"reading_time": 14,
	// 				"title": "Dashboards in Python: 3 Advanced Examples for Dash Beginners and Everyone Else"
	// 			},
	// 			{
	// 				"claps": 546,
	// 				"reading_time": 13,
	// 				"title": "Dashboards in Python Using Dash — Creating a Data Table using Data from Reddit"
	// 			},
	// 			{
	// 				"claps": 110,
	// 				"reading_time": 4,
	// 				"title": "OCR Engine Comparison — Tesseract vs. EasyOCR"
	// 			},
	// 			{
	// 				"claps": 51,
	// 				"reading_time": 4,
	// 				"title": "How to Import Data to Salesforce Marketing Cloud (ExactTarget) Using Python REST API"
	// 			},
	// 			{
	// 				"claps": 92,
	// 				"reading_time": 12,
	// 				"title": "How to Automate Multiple Excel Workbooks and Perform Analysis"
	// 			}
	// 		]
	// 	},
	// 	{
	// 		"counts": 5,
	// 		"distances": [
	// 			0,
	// 			0.1953007,
	// 			0.40734136,
	// 			0.41571742,
	// 			0.41766006
	// 		],
	// 		"rows": [
	// 			{
	// 				"claps": 726,
	// 				"reading_time": 14,
	// 				"title": "Dashboards in Python: 3 Advanced Examples for Dash Beginners and Everyone Else"
	// 			},
	// 			{
	// 				"claps": 546,
	// 				"reading_time": 13,
	// 				"title": "Dashboards in Python Using Dash — Creating a Data Table using Data from Reddit"
	// 			},
	// 			{
	// 				"claps": 110,
	// 				"reading_time": 4,
	// 				"title": "OCR Engine Comparison — Tesseract vs. EasyOCR"
	// 			},
	// 			{
	// 				"claps": 51,
	// 				"reading_time": 4,
	// 				"title": "How to Import Data to Salesforce Marketing Cloud (ExactTarget) Using Python REST API"
	// 			},
	// 			{
	// 				"claps": 92,
	// 				"reading_time": 12,
	// 				"title": "How to Automate Multiple Excel Workbooks and Perform Analysis"
	// 			}
	// 		]
	// 	}
	// ]

	err = conn.Delete(context.Background(), COLLNAME, "", "id in [253]")

	if err != nil {
		log.Fatal("Failed to delete rows:", err.Error())
	}

	// search with filters
	vectors = []entity.Vector{}

	for _, row := range data.Rows[:1] {
		vector := make(entity.FloatVector, 0, 768)
		vector = append(vector, row.TitleVector...)
		vectors = append(vectors, vector)
	}

	sp, _ = entity.NewIndexAUTOINDEXSearchParam(1)

	limit = client.WithLimit(10)
	offset = client.WithOffset(0)
	topK = 5
	outputFields = []string{"title", "reading_time"}
	expr := "10 < reading_time < 15"

	res, err = conn.Search(
		context.Background(),    // context
		COLLNAME,                // collectionName
		[]string{},              // partitionNames
		expr,                    // expr
		outputFields,            // outputfields
		vectors,                 // vectors
		"title_vector",          // vectorField
		entity.MetricType("L2"), // metricType
		topK,                    // topK
		sp,                      // sp
		limit,                   // opts
		offset,                  // opts
	)

	if err != nil {
		log.Fatal("Failed to insert rows:", err.Error())
	}

	fmt.Println(resultsToJSON(res))

	// Output:
	// [
	// 	{
	// 		"counts": 5,
	// 		"distances": [
	// 			0,
	// 			0.45862228,
	// 			0.5037479,
	// 			0.52556163,
	// 			0.567989
	// 		],
	// 		"rows": [
	// 			{
	// 				"reading_time": 13,
	// 				"title": "The Reported Mortality Rate of Coronavirus Is Not Important"
	// 			},
	// 			{
	// 				"reading_time": 12,
	// 				"title": "Heart Disease Risk Assessment Using Machine Learning"
	// 			},
	// 			{
	// 				"reading_time": 13,
	// 				"title": "New Data Shows a Lower Covid-19 Fatality Rate"
	// 			},
	// 			{
	// 				"reading_time": 11,
	// 				"title": "Common Pipenv Errors"
	// 			},
	// 			{
	// 				"reading_time": 12,
	// 				"title": "How Does US Healthcare Compare With Healthcare Around the World?"
	// 			}
	// 		]
	// 	}
	// ]

	// ------

	vectors = []entity.Vector{}

	for _, row := range data.Rows[:1] {
		vector := make(entity.FloatVector, 0, 768)
		vector = append(vector, row.TitleVector...)
		vectors = append(vectors, vector)
	}

	sp, _ = entity.NewIndexAUTOINDEXSearchParam(1)

	limit = client.WithLimit(10)
	offset = client.WithOffset(0)
	topK = 5
	outputFields = []string{"title", "claps", "responses"}
	expr = "claps > 1500 and responses > 15"

	res, err = conn.Search(
		context.Background(),    // context
		COLLNAME,                // collectionName
		[]string{},              // partitionNames
		expr,                    // expr
		outputFields,            // outputfields
		vectors,                 // vectors
		"title_vector",          // vectorField
		entity.MetricType("L2"), // metricType
		topK,                    // topK
		sp,                      // sp
		limit,                   // opts
		offset,                  // opts
	)

	if err != nil {
		log.Fatal("Failed to insert rows:", err.Error())
	}

	fmt.Println(resultsToJSON(res))

	// Output:
	// [
	// 	{
	// 		"counts": 5,
	// 		"distances": [
	// 			0.37674016,
	// 			0.6518569,
	// 			0.6772847,
	// 			0.683691,
	// 			0.71434104
	// 		],
	// 		"rows": [
	// 			{
	// 				"claps": 2900,
	// 				"responses": 47,
	// 				"title": "Why The Coronavirus Mortality Rate is Misleading"
	// 			},
	// 			{
	// 				"claps": 4300,
	// 				"responses": 95,
	// 				"title": "The Discovery of Aliens Would Be Terrible"
	// 			},
	// 			{
	// 				"claps": 2600,
	// 				"responses": 212,
	// 				"title": "Remote Work Is Not Here to Stay"
	// 			},
	// 			{
	// 				"claps": 1800,
	// 				"responses": 40,
	// 				"title": "Apple May Lose the Developer Crowd"
	// 			},
	// 			{
	// 				"claps": 5000,
	// 				"responses": 45,
	// 				"title": "Sorry, Online Courses Won’t Make you a Data Scientist"
	// 			}
	// 		]
	// 	}
	// ]

	// ------

	vectors = []entity.Vector{}

	for _, row := range data.Rows[:1] {
		vector := make(entity.FloatVector, 0, 768)
		vector = append(vector, row.TitleVector...)
		vectors = append(vectors, vector)
	}

	sp, _ = entity.NewIndexAUTOINDEXSearchParam(1)

	limit = client.WithLimit(10)
	offset = client.WithOffset(0)
	topK = 5
	outputFields = []string{"title", "publication"}
	expr = "publication == \"Towards Data Science\""

	res, err = conn.Search(
		context.Background(),    // context
		COLLNAME,                // collectionName
		[]string{},              // partitionNames
		expr,                    // expr
		outputFields,            // outputfields
		vectors,                 // vectors
		"title_vector",          // vectorField
		entity.MetricType("L2"), // metricType
		topK,                    // topK
		sp,                      // sp
		limit,                   // opts
		offset,                  // opts
	)

	if err != nil {
		log.Fatal("Failed to insert rows:", err.Error())
	}

	fmt.Println(resultsToJSON(res))

	// Output:
	// [
	// 	{
	// 		"counts": 5,
	// 		"distances": [
	// 			0.29999837,
	// 			0.37674016,
	// 			0.4360938,
	// 			0.45862228,
	// 			0.46275583
	// 		],
	// 		"rows": [
	// 			{
	// 				"publication": "Towards Data Science",
	// 				"title": "Following the Spread of Coronavirus"
	// 			},
	// 			{
	// 				"publication": "Towards Data Science",
	// 				"title": "Why The Coronavirus Mortality Rate is Misleading"
	// 			},
	// 			{
	// 				"publication": "Towards Data Science",
	// 				"title": "Mortality Rate As an Indicator of an Epidemic Outbreak"
	// 			},
	// 			{
	// 				"publication": "Towards Data Science",
	// 				"title": "Heart Disease Risk Assessment Using Machine Learning"
	// 			},
	// 			{
	// 				"publication": "Towards Data Science",
	// 				"title": "Can we learn anything from the progression of influenza to analyze the COVID-19 pandemic better?"
	// 			}
	// 		]
	// 	}
	// ]

	// ------

	vectors = []entity.Vector{}

	for _, row := range data.Rows[:1] {
		vector := make(entity.FloatVector, 0, 768)
		vector = append(vector, row.TitleVector...)
		vectors = append(vectors, vector)
	}

	sp, _ = entity.NewIndexAUTOINDEXSearchParam(1)

	limit = client.WithLimit(10)
	offset = client.WithOffset(0)
	topK = 5
	outputFields = []string{"title", "publication"}
	expr = "publication not in [\"Towards Data Science\", \"Personal Growth\"]"

	res, err = conn.Search(
		context.Background(),    // context
		COLLNAME,                // collectionName
		[]string{},              // partitionNames
		expr,                    // expr
		outputFields,            // outputfields
		vectors,                 // vectors
		"title_vector",          // vectorField
		entity.MetricType("L2"), // metricType
		topK,                    // topK
		sp,                      // sp
		limit,                   // opts
		offset,                  // opts
	)

	if err != nil {
		log.Fatal("Failed to insert rows:", err.Error())
	}

	fmt.Println(resultsToJSON(res))

	// Output:
	// [
	// 	{
	// 		"counts": 5,
	// 		"distances": [
	// 			0,
	// 			0.36103836,
	// 			0.41629803,
	// 			0.48886314,
	// 			0.49283177
	// 		],
	// 		"rows": [
	// 			{
	// 				"publication": "The Startup",
	// 				"title": "The Reported Mortality Rate of Coronavirus Is Not Important"
	// 			},
	// 			{
	// 				"publication": "The Startup",
	// 				"title": "The Hidden Side Effect of the Coronavirus"
	// 			},
	// 			{
	// 				"publication": "The Startup",
	// 				"title": "Coronavirus shows what ethical Amazon could look like"
	// 			},
	// 			{
	// 				"publication": "The Startup",
	// 				"title": "How Can AI Help Fight Coronavirus?"
	// 			},
	// 			{
	// 				"publication": "The Startup",
	// 				"title": "Will Coronavirus Impact Freelancers’ Ability to Rent?"
	// 			}
	// 		]
	// 	}
	// ]

	// ------

	vectors = []entity.Vector{}

	for _, row := range data.Rows[:1] {
		vector := make(entity.FloatVector, 0, 768)
		vector = append(vector, row.TitleVector...)
		vectors = append(vectors, vector)
	}

	sp, _ = entity.NewIndexAUTOINDEXSearchParam(1)

	limit = client.WithLimit(10)
	offset = client.WithOffset(0)
	topK = 5
	outputFields = []string{"title", "link"}
	expr = "title like \"Top%\""

	res, err = conn.Search(
		context.Background(),    // context
		COLLNAME,                // collectionName
		[]string{},              // partitionNames
		expr,                    // expr
		outputFields,            // outputfields
		vectors,                 // vectors
		"title_vector",          // vectorField
		entity.MetricType("L2"), // metricType
		topK,                    // topK
		sp,                      // sp
		limit,                   // opts
		offset,                  // opts
	)

	if err != nil {
		log.Fatal("Failed to insert rows:", err.Error())
	}

	fmt.Println(resultsToJSON(res))

	// Output:
	// [
	// 	{
	// 		"counts": 5,
	// 		"distances": [
	// 			0.7496654,
	// 			0.7691308,
	// 			0.81268203,
	// 			0.8307196,
	// 			0.84897053
	// 		],
	// 		"rows": [
	// 			{
	// 				"link": "https://towardsdatascience.com/topic-modeling-in-power-bi-using-pycaret-54422b4e36d6",
	// 				"title": "Topic Modeling in Power BI using PyCaret"
	// 			},
	// 			{
	// 				"link": "https://towardsdatascience.com/topic-modeling-the-comment-section-from-a-new-york-times-article-e4775261530e",
	// 				"title": "Topic Modeling the comment section from a New York Times article"
	// 			},
	// 			{
	// 				"link": "https://medium.com/swlh/top-4-myths-about-app-store-conversion-rate-optimization-cro-c62476901c90",
	// 				"title": "Top 4 Myths About App Store Conversion Rate Optimization (CRO)"
	// 			},
	// 			{
	// 				"link": "https://medium.com/swlh/top-vs-code-extensions-for-web-developers-1e038201a8fc",
	// 				"title": "Top VS Code extensions for Web Developers"
	// 			},
	// 			{
	// 				"link": "https://medium.com/swlh/top-ten-mistakes-found-while-doing-code-reviews-b935ef44e797",
	// 				"title": "Top ten mistakes found while performing code reviews"
	// 			}
	// 		]
	// 	}
	// ]

	// ------

	vectors = []entity.Vector{}

	for _, row := range data.Rows[:1] {
		vector := make(entity.FloatVector, 0, 768)
		vector = append(vector, row.TitleVector...)
		vectors = append(vectors, vector)
	}

	sp, _ = entity.NewIndexAUTOINDEXSearchParam(1)

	limit = client.WithLimit(10)
	offset = client.WithOffset(0)
	topK = 5
	outputFields = []string{"title", "publication", "claps", "responses", "reading_time"}
	expr = "(publication == \"Towards Data Science\") and ((claps > 1500 and responses > 15) or (10 < reading_time < 15))"

	res, err = conn.Search(
		context.Background(),    // context
		COLLNAME,                // collectionName
		[]string{},              // partitionNames
		expr,                    // expr
		outputFields,            // outputfields
		vectors,                 // vectors
		"title_vector",          // vectorField
		entity.MetricType("L2"), // metricType
		topK,                    // topK
		sp,                      // sp
		limit,                   // opts
		offset,                  // opts
	)

	if err != nil {
		log.Fatal("Failed to insert rows:", err.Error())
	}

	fmt.Println(resultsToJSON(res))

	// Output:
	// [
	// 	{
	// 		"counts": 5,
	// 		"distances": [
	// 			0.37674016,
	// 			0.45862228,
	// 			0.5037479,
	// 			0.52556163,
	// 			0.58774835
	// 		],
	// 		"rows": [
	// 			{
	// 				"claps": 2900,
	// 				"publication": "Towards Data Science",
	// 				"reading_time": 9,
	// 				"responses": 47,
	// 				"title": "Why The Coronavirus Mortality Rate is Misleading"
	// 			},
	// 			{
	// 				"claps": 15,
	// 				"publication": "Towards Data Science",
	// 				"reading_time": 12,
	// 				"responses": 0,
	// 				"title": "Heart Disease Risk Assessment Using Machine Learning"
	// 			},
	// 			{
	// 				"claps": 161,
	// 				"publication": "Towards Data Science",
	// 				"reading_time": 13,
	// 				"responses": 3,
	// 				"title": "New Data Shows a Lower Covid-19 Fatality Rate"
	// 			},
	// 			{
	// 				"claps": 20,
	// 				"publication": "Towards Data Science",
	// 				"reading_time": 11,
	// 				"responses": 1,
	// 				"title": "Common Pipenv Errors"
	// 			},
	// 			{
	// 				"claps": 61,
	// 				"publication": "Towards Data Science",
	// 				"reading_time": 12,
	// 				"responses": 0,
	// 				"title": "Data quality impact on the dataset"
	// 			}
	// 		]
	// 	}
	// ]

	// ------

	// query

	resq, err := conn.Query(
		context.Background(),
		COLLNAME,
		[]string{},
		expr,
		outputFields,
		limit,
		offset,
	)

	if err != nil {
		log.Fatal("Failed to query rows:", err.Error())
	}

	fmt.Println(resultSetToJSON(resq, false))

	// Output:
	// [
	// 	{
	// 		"claps": 3000,
	// 		"id": 69,
	// 		"publication": "Towards Data Science",
	// 		"reading_time": 21,
	// 		"responses": 18,
	// 		"title": "Top 10 In-Demand programming languages to learn in 2020"
	// 	},
	// 	{
	// 		"claps": 1500,
	// 		"id": 73,
	// 		"publication": "Towards Data Science",
	// 		"reading_time": 12,
	// 		"responses": 7,
	// 		"title": "Data Cleaning in Python: the Ultimate Guide (2020)"
	// 	},
	// 	{
	// 		"claps": 1100,
	// 		"id": 75,
	// 		"publication": "Towards Data Science",
	// 		"reading_time": 11,
	// 		"responses": 0,
	// 		"title": "Top Trends of Graph Machine Learning in 2020"
	// 	},
	// 	{
	// 		"claps": 331,
	// 		"id": 79,
	// 		"publication": "Towards Data Science",
	// 		"reading_time": 12,
	// 		"responses": 3,
	// 		"title": "Rage Quitting Cancer Research"
	// 	},
	// 	{
	// 		"claps": 109,
	// 		"id": 80,
	// 		"publication": "Towards Data Science",
	// 		"reading_time": 13,
	// 		"responses": 0,
	// 		"title": "Understanding Natural Language Processing: how AI understands our languages"
	// 	},
	// 	{
	// 		"claps": 44,
	// 		"id": 90,
	// 		"publication": "Towards Data Science",
	// 		"reading_time": 11,
	// 		"responses": 0,
	// 		"title": "SVM: An optimization problem"
	// 	},
	// 	{
	// 		"claps": 89,
	// 		"id": 99,
	// 		"publication": "Towards Data Science",
	// 		"reading_time": 13,
	// 		"responses": 0,
	// 		"title": "Finding optimal NBA physiques using data visualization with Python"
	// 	},
	// 	{
	// 		"claps": 74,
	// 		"id": 103,
	// 		"publication": "Towards Data Science",
	// 		"reading_time": 12,
	// 		"responses": 0,
	// 		"title": "A Primer on Domain Adaptation"
	// 	},
	// 	{
	// 		"claps": 103,
	// 		"id": 212,
	// 		"publication": "Towards Data Science",
	// 		"reading_time": 11,
	// 		"responses": 0,
	// 		"title": "How does the Bellman equation work in Deep RL?"
	// 	},
	// 	{
	// 		"claps": 14,
	// 		"id": 222,
	// 		"publication": "Towards Data Science",
	// 		"reading_time": 13,
	// 		"responses": 0,
	// 		"title": "Machine learning techniques for investigative reporting"
	// 	}
	// ]

	// ------

	// get

	ids := resq.GetColumn("id")

	resg, err := conn.Get(
		context.Background(),
		COLLNAME,
		ids,
	)

	if err != nil {
		log.Fatal("Failed to get rows:", err.Error())
	}

	fmt.Println(resultSetToJSON(resg, false))

	// Output:
	// [
	// 	{
	// 		"claps": 3000,
	// 		"id": 69,
	// 		"link": "https://towardsdatascience.com/top-10-in-demand-programming-languages-to-learn-in-2020-4462eb7d8d3e",
	// 		"publication": "Towards Data Science",
	// 		"reading_time": 21,
	// 		"responses": 18,
	// 		"title": "Top 10 In-Demand programming languages to learn in 2020"
	// 	},
	// 	{
	// 		"claps": 1500,
	// 		"id": 73,
	// 		"link": "https://towardsdatascience.com/data-cleaning-in-python-the-ultimate-guide-2020-c63b88bf0a0d",
	// 		"publication": "Towards Data Science",
	// 		"reading_time": 12,
	// 		"responses": 7,
	// 		"title": "Data Cleaning in Python: the Ultimate Guide (2020)"
	// 	},
	// 	{
	// 		"claps": 1100,
	// 		"id": 75,
	// 		"link": "https://towardsdatascience.com/top-trends-of-graph-machine-learning-in-2020-1194175351a3",
	// 		"publication": "Towards Data Science",
	// 		"reading_time": 11,
	// 		"responses": 0,
	// 		"title": "Top Trends of Graph Machine Learning in 2020"
	// 	},
	// 	{
	// 		"claps": 331,
	// 		"id": 79,
	// 		"link": "https://towardsdatascience.com/rage-quitting-cancer-research-5e79cb04801",
	// 		"publication": "Towards Data Science",
	// 		"reading_time": 12,
	// 		"responses": 3,
	// 		"title": "Rage Quitting Cancer Research"
	// 	},
	// 	{
	// 		"claps": 109,
	// 		"id": 80,
	// 		"link": "https://towardsdatascience.com/understanding-nlp-how-ai-understands-our-languages-77601002cffc",
	// 		"publication": "Towards Data Science",
	// 		"reading_time": 13,
	// 		"responses": 0,
	// 		"title": "Understanding Natural Language Processing: how AI understands our languages"
	// 	},
	// 	{
	// 		"claps": 44,
	// 		"id": 90,
	// 		"link": "https://towardsdatascience.com/svm-an-optimization-problem-242cbb8d96a8",
	// 		"publication": "Towards Data Science",
	// 		"reading_time": 11,
	// 		"responses": 0,
	// 		"title": "SVM: An optimization problem"
	// 	},
	// 	{
	// 		"claps": 89,
	// 		"id": 99,
	// 		"link": "https://towardsdatascience.com/finding-optimal-nba-physiques-using-data-visualization-with-python-6ce27ac5b68f",
	// 		"publication": "Towards Data Science",
	// 		"reading_time": 13,
	// 		"responses": 0,
	// 		"title": "Finding optimal NBA physiques using data visualization with Python"
	// 	},
	// 	{
	// 		"claps": 74,
	// 		"id": 103,
	// 		"link": "https://towardsdatascience.com/a-primer-on-domain-adaptation-cf6abf7087a3",
	// 		"publication": "Towards Data Science",
	// 		"reading_time": 12,
	// 		"responses": 0,
	// 		"title": "A Primer on Domain Adaptation"
	// 	},
	// 	{
	// 		"claps": 103,
	// 		"id": 212,
	// 		"link": "https://towardsdatascience.com/how-the-bellman-equation-works-in-deep-reinforcement-learning-5301fe41b25a",
	// 		"publication": "Towards Data Science",
	// 		"reading_time": 11,
	// 		"responses": 0,
	// 		"title": "How does the Bellman equation work in Deep RL?"
	// 	},
	// 	{
	// 		"claps": 14,
	// 		"id": 222,
	// 		"link": "https://towardsdatascience.com/machine-learning-techniques-for-investigative-reporting-344d74f69f84",
	// 		"publication": "Towards Data Science",
	// 		"reading_time": 13,
	// 		"responses": 0,
	// 		"title": "Machine learning techniques for investigative reporting"
	// 	}
	// ]

	// delete

	err = conn.Delete(context.Background(), COLLNAME, "", "id in [253]")

	if err != nil {
		log.Fatal("Failed to delete rows:", err.Error())
	}

	// 9. Drop collection
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
	}
	// You should add more types here
	return data
}
