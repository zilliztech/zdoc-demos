import os, json
from pymilvus import connections, FieldSchema, CollectionSchema, DataType, Collection, utility

CLUSTER_ENDPOINT="YOUR_CLUSTER_ENDPOINT" # Set your cluster endpoint
TOKEN="YOUR_CLUSTER_TOKEN" # Set your token
COLLECTION_NAME="medium_articles_2020" # Set your collection name
DATASET_PATH="{}/../medium_articles_2020_dpr.json".format(os.path.dirname(__file__)) # Set your dataset path

connections.connect(
  alias='default', 
  #  Public endpoint obtained from Zilliz Cloud
  uri=CLUSTER_ENDPOINT,
  secure=True,
  token=TOKEN, # Username and password specified when you created this cluster
)

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
collection = Collection(COLLECTION_NAME, schema)

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
progress = utility.loading_progress(COLLECTION_NAME)

print(progress)

# Output
#
# {
#     "loading_progress": "100%"
# }



# 6. Prepare data
with open(DATASET_PATH) as f:
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

result = collection.insert(data_rows)
collection.flush()

print(f"Data inserted successfully! Inserted counts: {result.insert_count}")

# Output
#
# Data inserted successfully! Inserted counts: 5979



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

print([ list(map(lambda y: y.entity.to_dict(),  x)) for x in result ])

# Output
#
# [
#     [
#         {
#             "id": 443943328732940369,
#             "distance": 0.36103835701942444,
#             "entity": {
#                 "title": "The Hidden Side Effect of the Coronavirus",
#                 "article_meta": {
#                     "link": "https://medium.com/swlh/the-hidden-side-effect-of-the-coronavirus-b6a7a5ee9586",
#                     "reading_time": 8,
#                     "publication": "The Startup",
#                     "claps": 83,
#                     "responses": 0
#                 }
#             }
#         },
#         {
#             "id": 443943328732940403,
#             "distance": 0.37674015760421753,
#             "entity": {
#                 "title": "Why The Coronavirus Mortality Rate is Misleading",
#                 "article_meta": {
#                     "link": "https://towardsdatascience.com/why-the-coronavirus-mortality-rate-is-misleading-cc63f571b6a6",
#                     "reading_time": 9,
#                     "publication": "Towards Data Science",
#                     "claps": 2900,
#                     "responses": 47
#                 }
#             }
#         },
#         {
#             "id": 443943328732938203,
#             "distance": 0.4162980318069458,
#             "entity": {
#                 "title": "Coronavirus shows what ethical Amazon could look like",
#                 "article_meta": {
#                     "link": "https://medium.com/swlh/coronavirus-shows-what-ethical-amazon-could-look-like-7c80baf2c663",
#                     "reading_time": 4,
#                     "publication": "The Startup",
#                     "claps": 51,
#                     "responses": 0
#                 }
#             }
#         }
#     ]
# ]



# get collection info
print("Entity counts: ", collection.num_entities)

# Output
#
# Entity counts:  5979



# 9. Drop collection
utility.drop_collection(COLLECTION_NAME)