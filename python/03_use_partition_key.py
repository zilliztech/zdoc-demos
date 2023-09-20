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

# 1. Define fields
fields = [
    FieldSchema(name="id", dtype=DataType.INT64, is_primary=True),
    FieldSchema(name="title", dtype=DataType.VARCHAR, max_length=512),   
    FieldSchema(name="title_vector", dtype=DataType.FLOAT_VECTOR, dim=768),
    FieldSchema(name="link", dtype=DataType.VARCHAR, max_length=512),
    FieldSchema(name="reading_time", dtype=DataType.INT64),
    # The field "publication" acts as the partition key.
    FieldSchema(name="publication", dtype=DataType.VARCHAR, max_length=512, is_partition_key=True),
    FieldSchema(name="claps", dtype=DataType.INT64),
    FieldSchema(name="responses", dtype=DataType.INT64)
]

# 2. Build the schema
schema = CollectionSchema(
    fields,
    description="Schema of Medium articles",
    # As an alternative, you can set the partition key by its name in the collection schema
    # partition_key_field="publication"
)

# 3. Create collection
collection = Collection(
    name="medium_articles", 
    description="Medium articles published between Jan and August in 2020 in prominent publications",
    schema=schema
)

print("Collection created successfully")

# 4. Index collection
# 'index_type' defines the index algorithm to be used.
#    AUTOINDEX is the only option.
#
# 'metric_type' defines the way to measure the distance 
#    between vectors. Possible values are L2, IP, and Cosine,
#    and defaults to Cosine.
index_params = {
    "index_type": "AUTOINDEX",
    "metric_type": "L2",
    "params": {}
}

# To name the index, do as follows:
collection.create_index(
  field_name="title_vector", 
  index_params=index_params,
  index_name='title_vector_index'
)

# 5. Load collection
collection.load()

# Get loading progress
progress = utility.loading_progress("medium_articles")

print(f"Collection loaded successfully: {progress}")

# 6. Prepare data

# Prepare a list of rows
with open('../medium_articles_2020_dpr.json') as f:
    data = json.load(f)
    rows = data['rows']

    print("A list of rows is as follows")
    print(json.dumps(rows[:3], indent=2))

# Prepare a list of columns
    keys = list(rows[0].keys())
    columns = [ [] for x in keys ]
    for row in rows:
        for x in keys:
            columns[keys.index(x)].append(row[x])

    print("A list of columns is as follows")
    columns_demo = [ [] for x in keys ]
    for row in rows[:3]:
        for x in keys:
            columns_demo[keys.index(x)].append(row[x])

    print(json.dumps(columns_demo, indent=2))

# 7. Insert data

print("Inserting data...")

results = collection.insert(rows)
# results = collection.insert(columns) # also works

print(f"Data inserted successfully! Inserted rows: {results.insert_count}")

collection.flush()

# 8. Search data
# Metric type should be the same as
# that defined in the index parameters 
# used to create the index.
search_params = {
    "metric_type": "L2"
}

results = collection.search(
    data=[rows[0]['title_vector']],
    anns_field="title_vector",
    param=search_params,
    # When conducting searches and queries, include the partition key in the bolean expression
    expr="claps > 30 and reading_time < 10 and publication in ['Towards Data Science', 'Personal Growth']",
    output_fields=["title", "link"],
    limit=5
)

# Get all returned IDs
# results[0] indicates the result 
# of the first query vector in the 'data' list
ids = results[0].ids

print(f"Matched IDs: {ids}")

# Get the distance from 
# all returned vectors to the query vector.
distances = results[0].distances

print(f"Distances from matched entities: {distances}")

# Get the values of the output fields
# specified in the search request
print("Search completed successfully!")
print("===========================================")
hits = results[0]
for hit in hits:
  print(hit.entity.get("title"))
  print(hit.entity.get("link"))


# 9. Drop collection
utility.drop_collection("medium_articles")