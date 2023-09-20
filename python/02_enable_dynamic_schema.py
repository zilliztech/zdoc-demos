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
    FieldSchema(name="title_vector", dtype=DataType.FLOAT_VECTOR, dim=768)
]

# 3. Create schema with dynamic field enabled
schema = CollectionSchema(
		fields, 
		"The schema for a medium news collection", 
		enable_dynamic_field=True
)

# 4. Create collection
collection = Collection("medium_articles_with_dynamic", schema)

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
progress = utility.loading_progress("medium_articles_with_dynamic")

print(f"Collection loaded successfully: {progress}")

# 6. Prepare data
with open("../medium_articles_2020_dpr.json") as f:
    data = json.load(f)
    list_of_rows = data['rows']

    data_rows = []
    for row in list_of_rows:
        # Remove the id field because the primary key has auto_id enabled.
        del row['id']
        # Other keys except the title and title_vector fields in the row 
        # will be treated as dynamic fields.
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
    # Access dynamic fields in the boolean expression
    expr='claps > 30 and reading_time < 10',
    # Include dynamic fields in the output to return
    output_fields=["title", "reading_time", "claps"],
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
            hit.entity.get("reading_time"), 
            ", Claps: ", hit.entity.get("claps")
        )
    

# get collection info
print("Entity counts: ", collection.num_entities)

# 9. Drop collection
utility.drop_collection("medium_articles_with_dynamic")