import json, os, time
from pymilvus import connections, FieldSchema, CollectionSchema, DataType, Collection, utility

CLUSTER_ENDPOINT="YOUR_CLUSTER_ENDPOINT" # Set your cluster endpoint
TOKEN="YOUR_CLUSTER_TOKEN" # Set your token
COLLECTION_NAME="medium_articles_2020" # Set your collection name
DATASET_PATH="{}/../medium_articles_2020_dpr.json".format(os.path.dirname(__file__)) # Set your dataset path

connections.connect(
  alias='default', 
  #  Public endpoint obtained from Zilliz Cloud
  uri=CLUSTER_ENDPOINT,
  # API key or a colon-separated cluster username and password
  token=TOKEN, 
)

# 1. Define fields
fields = [
    FieldSchema(name="id", dtype=DataType.INT64, is_primary=True),
    FieldSchema(name="title", dtype=DataType.VARCHAR, max_length=512),   
    FieldSchema(name="title_vector", dtype=DataType.FLOAT_VECTOR, dim=768),
    FieldSchema(name="link", dtype=DataType.VARCHAR, max_length=512),
    FieldSchema(name="reading_time", dtype=DataType.INT64),
    FieldSchema(name="publication", dtype=DataType.VARCHAR, max_length=512),
    FieldSchema(name="claps", dtype=DataType.INT64),
    FieldSchema(name="responses", dtype=DataType.INT64)
]

# 2. Build the schema
schema = CollectionSchema(
    fields,
    description="Schema of Medium articles",
    enable_dynamic_field=False
)

# 3. Create collection
collection = Collection(
    name=COLLECTION_NAME, 
    description="Medium articles published between Jan and August in 2020 in prominent publications",
    schema=schema
)

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
progress = utility.loading_progress(COLLECTION_NAME)

print(progress)

# Output
#
# {
#     "loading_progress": "100%"
# }



# 6. Prepare data

# Prepare a list of rows
with open(DATASET_PATH) as f:
    data = json.load(f)
    rows = data['rows']

print(rows[:3])

# Output
#
# [
#     {
#         "id": 0,
#         "title": "The Reported Mortality Rate of Coronavirus Is Not Important",
#         "title_vector": [
#             0.041732933,
#             0.013779674,
#             -0.027564144,
#             -0.013061441,
#             0.009748648,
#             0.00082446384,
#             -0.00071647146,
#             0.048612226,
#             -0.04836573,
#             -0.04567751,
#             0.018008126,
#             0.0063936645,
#             -0.011913628,
#             0.030776596,
#             -0.018274948,
#             0.019929802,
#             0.020547243,
#             0.032735646,
#             -0.031652678,
#             -0.033816382,
#             "(748 more items hidden)"
#         ],
#         "link": "https://medium.com/swlh/the-reported-mortality-rate-of-coronavirus-is-not-important-369989c8d912",
#         "reading_time": 13,
#         "publication": "The Startup",
#         "claps": 1100,
#         "responses": 18
#     },
#     {
#         "id": 1,
#         "title": "Dashboards in Python: 3 Advanced Examples for Dash Beginners and Everyone Else",
#         "title_vector": [
#             0.0039737443,
#             0.003020432,
#             -0.0006188639,
#             0.03913546,
#             -0.00089768134,
#             0.021238148,
#             0.014454661,
#             0.025742851,
#             0.0022063442,
#             -0.051130578,
#             -0.0010897011,
#             0.038453076,
#             0.011593861,
#             -0.046852026,
#             0.0064208573,
#             0.010120634,
#             -0.023668954,
#             0.041229635,
#             0.008146385,
#             -0.023367394,
#             "(748 more items hidden)"
#         ],
#         "link": "https://medium.com/swlh/dashboards-in-python-3-advanced-examples-for-dash-beginners-and-everyone-else-b1daf4e2ec0a",
#         "reading_time": 14,
#         "publication": "The Startup",
#         "claps": 726,
#         "responses": 3
#     },
#     {
#         "id": 2,
#         "title": "How Can We Best Switch in Python?",
#         "title_vector": [
#             0.031961977,
#             0.00047043373,
#             -0.018263113,
#             0.027324716,
#             -0.0054595284,
#             -0.014779159,
#             0.017511465,
#             0.030381083,
#             -0.018930407,
#             -0.03372473,
#             -0.009049301,
#             0.05401713,
#             -0.030117748,
#             -0.05029242,
#             -0.004565209,
#             -0.013697411,
#             0.0091306195,
#             0.020263411,
#             0.022377398,
#             -0.013710004,
#             "(748 more items hidden)"
#         ],
#         "link": "https://medium.com/swlh/how-can-we-best-switch-in-python-458fb33f7835",
#         "reading_time": 6,
#         "publication": "The Startup",
#         "claps": 500,
#         "responses": 7
#     }
# ]



# Prepare a list of columns
with open(DATASET_PATH) as f:
    keys = list(rows[0].keys())
    columns = [ [] for x in keys ]
    for row in rows:
        for x in keys:
            columns[keys.index(x)].append(row[x])

    columns_demo = [ [] for x in keys ]
    for row in rows[:3]:
        for x in keys:
            columns_demo[keys.index(x)].append(row[x])

print(columns_demo)

# Output
#
# [
#     [
#         0,
#         1,
#         2
#     ],
#     [
#         "The Reported Mortality Rate of Coronavirus Is Not Important",
#         "Dashboards in Python: 3 Advanced Examples for Dash Beginners and Everyone Else",
#         "How Can We Best Switch in Python?"
#     ],
#     [
#         [
#             0.041732933,
#             0.013779674,
#             -0.027564144,
#             -0.013061441,
#             0.009748648,
#             0.00082446384,
#             -0.00071647146,
#             0.048612226,
#             -0.04836573,
#             -0.04567751,
#             0.018008126,
#             0.0063936645,
#             -0.011913628,
#             0.030776596,
#             -0.018274948,
#             0.019929802,
#             0.020547243,
#             0.032735646,
#             -0.031652678,
#             -0.033816382,
#             "(748 more items hidden)"
#         ],
#         [
#             0.0039737443,
#             0.003020432,
#             -0.0006188639,
#             0.03913546,
#             -0.00089768134,
#             0.021238148,
#             0.014454661,
#             0.025742851,
#             0.0022063442,
#             -0.051130578,
#             -0.0010897011,
#             0.038453076,
#             0.011593861,
#             -0.046852026,
#             0.0064208573,
#             0.010120634,
#             -0.023668954,
#             0.041229635,
#             0.008146385,
#             -0.023367394,
#             "(748 more items hidden)"
#         ],
#         [
#             0.031961977,
#             0.00047043373,
#             -0.018263113,
#             0.027324716,
#             -0.0054595284,
#             -0.014779159,
#             0.017511465,
#             0.030381083,
#             -0.018930407,
#             -0.03372473,
#             -0.009049301,
#             0.05401713,
#             -0.030117748,
#             -0.05029242,
#             -0.004565209,
#             -0.013697411,
#             0.0091306195,
#             0.020263411,
#             0.022377398,
#             -0.013710004,
#             "(748 more items hidden)"
#         ]
#     ],
#     [
#         "https://medium.com/swlh/the-reported-mortality-rate-of-coronavirus-is-not-important-369989c8d912",
#         "https://medium.com/swlh/dashboards-in-python-3-advanced-examples-for-dash-beginners-and-everyone-else-b1daf4e2ec0a",
#         "https://medium.com/swlh/how-can-we-best-switch-in-python-458fb33f7835"
#     ],
#     [
#         13,
#         14,
#         6
#     ],
#     [
#         "The Startup",
#         "The Startup",
#         "The Startup"
#     ],
#     [
#         1100,
#         726,
#         500
#     ],
#     [
#         18,
#         3,
#         7
#     ]
# ]



# 7. Insert data
results = collection.insert(rows)
# results = collection.insert(columns) # also works

print(f"Data inserted successfully! Inserted rows: {results.insert_count}")

# Output
#
# Data inserted successfully! Inserted rows: 5979

# If you have prepared your data in columns, you can do as follows:
# results = collection.insert(columns)

time.sleep(5)

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
    output_fields=["title", "link"],
    limit=5
)

# Get all returned IDs
# results[0] indicates the result 
# of the first query vector in the 'data' list
ids = results[0].ids

print(ids)

# Output
#
# [0, 3177, 5607, 5641, 3441]



# Get the distance from 
# all returned vectors to the query vector.
distances = results[0].distances

print(distances)

# Output
#
# [0.0, 0.29999837279319763, 0.36103835701942444, 0.37674015760421753, 0.4162980318069458]



# Get the values of the output fields
# specified in the search request
entities = [ x.entity.to_dict()["entity"] for x in results[0] ]

print(entities)

# Output
#
# [
#     {
#         "title": "The Reported Mortality Rate of Coronavirus Is Not Important",
#         "link": "https://medium.com/swlh/the-reported-mortality-rate-of-coronavirus-is-not-important-369989c8d912"
#     },
#     {
#         "title": "Following the Spread of Coronavirus",
#         "link": "https://towardsdatascience.com/following-the-spread-of-coronavirus-23626940c125"
#     },
#     {
#         "title": "The Hidden Side Effect of the Coronavirus",
#         "link": "https://medium.com/swlh/the-hidden-side-effect-of-the-coronavirus-b6a7a5ee9586"
#     },
#     {
#         "title": "Why The Coronavirus Mortality Rate is Misleading",
#         "link": "https://towardsdatascience.com/why-the-coronavirus-mortality-rate-is-misleading-cc63f571b6a6"
#     },
#     {
#         "title": "Coronavirus shows what ethical Amazon could look like",
#         "link": "https://medium.com/swlh/coronavirus-shows-what-ethical-amazon-could-look-like-7c80baf2c663"
#     }
# ]




# 9. Drop collection
utility.drop_collection(COLLECTION_NAME)