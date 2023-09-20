import json
from pymilvus import connections, FieldSchema, CollectionSchema, DataType, Collection, utility

CLUSTER_ENDPOINT="YOUR_CLUSTER_ENDPOINT" # Set your cluster endpoint
TOKEN="YOUR_CLUSTER_TOKEN" # Set your token

connections.connect(
  alias='default', 
  #  Public endpoint obtained from Zilliz Cloud
  uri=CLUSTER_ENDPOINT,
  secure=True,
  token=TOKEN, # Username and password specified when you created this cluster
)

print("Connected successfully")

# 2. Define fields
fields = [
    FieldSchema(name="id", dtype=DataType.INT64, is_primary=True, auto_id=True, max_length=100),
    FieldSchema(name="title", dtype=DataType.VARCHAR, max_length=512),
    FieldSchema(name="title_vector", dtype=DataType.FLOAT_VECTOR, dim=768),
    # The following field is a JSON field
    FieldSchema(name="article_meta", dtype=DataType.JSON)
]

# 3. Create schema with dynamic field enabled
schema = CollectionSchema(
		fields, 
		"The schema for a medium news collection", 
)

# 4. Create collection
collection = Collection("medium_articles_with_json_field", schema)

print("Collection created successfully")

# 5. Index collection
index_params = {
    "index_type": "AUTOINDEX",
    "metric_type": "L2",
    "params": {}
}

collection.create_index(
  field_name="title_vector", 
  index_params=index_params
)

collection.load()

# Get loading progress
progress = utility.loading_progress("medium_articles_with_json_field")

print(f"Collection loaded successfully: {progress}")

# 6. Prepare data
with open("../medium_articles_2020_dpr.json") as f:
    data = json.load(f)
    list_of_rows = data['rows']

    data_rows = []
    for row in list_of_rows:
        # Remove the id field because auto-id is enabled for the primary key
        del row['id']
        # Create the article_meta field and 
        row['article_meta'] = {}
        # Move the following keys into the article_meta field
        row['article_meta']['link'] = row.pop('link')
        row['article_meta']['reading_time'] = row.pop('reading_time')
        row['article_meta']['publication'] = row.pop('publication')
        row['article_meta']['claps'] = row.pop('claps')
        row['article_meta']['responses'] = row.pop('responses')
        # Append this row to the data_rows list
        data_rows.append(row)

# 7. Insert data

print("Inserting data...")

result = collection.insert(data_rows)
collection.flush()

print(f"Data inserted successfully! Inserted counts: {result.insert_count}")

# 8. Search data
result = collection.search(
    data=[data_rows[0]['title_vector']],
    anns_field="title_vector",
    param={"metric_type": "L2", "params": {"nprobe": 10}},
    limit=3,
    # Access the keys in the JSON field
    expr='article_meta["claps"] > 30 and article_meta["reading_time"] < 10',
    # Include the JSON field in the output to return
    output_fields=["title", "article_meta"],
)

print("Search completed successfully!")
print("===========================================")

for hits in result:
    print("Matched IDs: ", hits.ids)
    print("Distance to the query vector: ", hits.distances)
    print("Matched articles: ")
    for hit in hits:
        print(
            "Title: ", 
            hit.entity.get("title"), 
            ", Reading time: ", 
            hit.entity.get("article_meta")['reading_time'], 
            ", Claps: ", hit.entity.get("article_meta")["claps"]
        )
    

# get collection info
print("Entity counts: ", collection.num_entities)

# 9. Drop collection
utility.drop_collection("medium_articles_with_json_field")