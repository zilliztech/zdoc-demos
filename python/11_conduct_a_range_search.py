import json, os, time
from pymilvus import connections, FieldSchema, CollectionSchema, DataType, Collection, utility

CLUSTER_ENDPOINT="YOUR_CLUSTER_ENDPOINT" # Set your cluster endpoint
TOKEN="YOUR_CLUSTER_TOKEN" # Set your token
COLLECTION_NAME="medium_articles_2020" # Set your collection name
DATASET_PATH="{}/../medium_articles_2020_dpr.json".format(os.path.dirname(__file__)) # Set your dataset path

# 0. Connect to cluster
connections.connect(
    uri=CLUSTER_ENDPOINT, # Public endpoint obtained from Zilliz Cloud
    token=TOKEN, # API key or a colon-separated cluster username and password
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
#             "(758 more items hidden)"
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
#             "(758 more items hidden)"
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
#             "(758 more items hidden)"
#         ],
#         "link": "https://medium.com/swlh/how-can-we-best-switch-in-python-458fb33f7835",
#         "reading_time": 6,
#         "publication": "The Startup",
#         "claps": 500,
#         "responses": 7
#     }
# ]



# 7. Insert data
results = collection.insert(rows)

print(f"Data inserted successfully! inserted rows: {results.insert_count}")

# Output
#
# Data inserted successfully! inserted rows: 5979



time.sleep(10)

# 8. conduct a range search

# Search

query_vector = rows[0]['title_vector']

# Define search parameters
search_params = {
    "metric_type": "L2",
    "params": {
        "nprobe": 10,
        "radius": 1.0,
        "range_filter": 0.8
    }
}

res = collection.search(
    data=[query_vector],
    anns_field="title_vector",
    param={"metric_type": "L2", "params": {"nprobe": 10}},
    output_fields=["title", "link"],
    limit=100,
)

ids = [ hits.ids for hits in res ]

print(ids)

# Output
#
# [
#     [
#         0,
#         3177,
#         5607,
#         5641,
#         3441,
#         938,
#         5780,
#         3072,
#         4328,
#         4275,
#         "(40 more items hidden)"
#     ]
# ]



distances = [ hits.distances for hits in res ]

print(distances)

# Output
#
# [
#     [
#         0.0,
#         0.29999834299087524,
#         0.36103832721710205,
#         0.37674015760421753,
#         0.416297972202301,
#         0.436093807220459,
#         0.4586222767829895,
#         0.46275579929351807,
#         0.48078253865242004,
#         0.48886317014694214,
#         "(40 more items hidden)"
#     ]
# ]



results = [ {
    "id": hit.id,
    "distance": hit.distance,
    "entity": {
        "title": hit.entity.get("title"),
        "link": hit.entity.get("link"),
    }
} for hits in res for hit in hits ]

print(results)

# Output
#
# [
#     {
#         "id": 0,
#         "distance": 0.0,
#         "entity": {
#             "title": "The Reported Mortality Rate of Coronavirus Is Not Important",
#             "link": "https://medium.com/swlh/the-reported-mortality-rate-of-coronavirus-is-not-important-369989c8d912"
#         }
#     },
#     {
#         "id": 3177,
#         "distance": 0.29999834299087524,
#         "entity": {
#             "title": "Following the Spread of Coronavirus",
#             "link": "https://towardsdatascience.com/following-the-spread-of-coronavirus-23626940c125"
#         }
#     },
#     {
#         "id": 5607,
#         "distance": 0.36103832721710205,
#         "entity": {
#             "title": "The Hidden Side Effect of the Coronavirus",
#             "link": "https://medium.com/swlh/the-hidden-side-effect-of-the-coronavirus-b6a7a5ee9586"
#         }
#     },
#     {
#         "id": 5641,
#         "distance": 0.37674015760421753,
#         "entity": {
#             "title": "Why The Coronavirus Mortality Rate is Misleading",
#             "link": "https://towardsdatascience.com/why-the-coronavirus-mortality-rate-is-misleading-cc63f571b6a6"
#         }
#     },
#     {
#         "id": 3441,
#         "distance": 0.416297972202301,
#         "entity": {
#             "title": "Coronavirus shows what ethical Amazon could look like",
#             "link": "https://medium.com/swlh/coronavirus-shows-what-ethical-amazon-could-look-like-7c80baf2c663"
#         }
#     },
#     {
#         "id": 938,
#         "distance": 0.436093807220459,
#         "entity": {
#             "title": "Mortality Rate As an Indicator of an Epidemic Outbreak",
#             "link": "https://towardsdatascience.com/mortality-rate-as-an-indicator-of-an-epidemic-outbreak-704592f3bb39"
#         }
#     },
#     {
#         "id": 5780,
#         "distance": 0.4586222767829895,
#         "entity": {
#             "title": "Heart Disease Risk Assessment Using Machine Learning",
#             "link": "https://towardsdatascience.com/heart-disease-risk-assessment-using-machine-learning-83335d077dad"
#         }
#     },
#     {
#         "id": 3072,
#         "distance": 0.46275579929351807,
#         "entity": {
#             "title": "Can we learn anything from the progression of influenza to analyze the COVID-19 pandemic better?",
#             "link": "https://towardsdatascience.com/can-we-learn-anything-from-the-progression-of-influenza-to-analyze-the-covid-19-pandemic-better-b20a5b3f4933"
#         }
#     },
#     {
#         "id": 4328,
#         "distance": 0.48078253865242004,
#         "entity": {
#             "title": "Ever Wondered How Epidemiologists Simulate COVID-19 Deaths?",
#             "link": "https://towardsdatascience.com/ever-wondered-how-epidemiologists-simulate-covid-19-deaths-32e3aca7531d"
#         }
#     },
#     {
#         "id": 4275,
#         "distance": 0.48886317014694214,
#         "entity": {
#             "title": "How Can AI Help Fight Coronavirus?",
#             "link": "https://medium.com/swlh/how-can-ai-help-fight-coronavirus-60f2182de93a"
#         }
#     },
#     "(40 more items hidden)"
# ]

# 9. drop collection

utility.drop_collection(COLLECTION_NAME)