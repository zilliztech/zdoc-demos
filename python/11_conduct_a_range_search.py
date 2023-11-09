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
    param=search_params,
    output_fields=["title", "link"],
    limit=100,
)

ids = [ hits.ids for hits in res ]

print(ids)

# Output
#
# [
#     [
#         1846,
#         2906,
#         4411,
#         3503,
#         4397,
#         4969,
#         2705,
#         3185,
#         5532,
#         1969,
#         "(90 more items hidden)"
#     ]
# ]



distances = [ hits.distances for hits in res ]

print(distances)

# Output
#
# [
#     [
#         0.8001112341880798,
#         0.8001610040664673,
#         0.8003642559051514,
#         0.8004330992698669,
#         0.8004655838012695,
#         0.8004793524742126,
#         0.8005216121673584,
#         0.8005879521369934,
#         0.8005922436714172,
#         0.8007100224494934,
#         "(90 more items hidden)"
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
#         "id": 1846,
#         "distance": 0.8001112341880798,
#         "entity": {
#             "title": "Simple VSCode Setup To Develop C++",
#             "link": "https://medium.com/swlh/simple-vscode-setup-to-develop-c-7830182ee4d8"
#         }
#     },
#     {
#         "id": 2906,
#         "distance": 0.8001610040664673,
#         "entity": {
#             "title": "Binary cross-entropy and logistic regression",
#             "link": "https://towardsdatascience.com/binary-cross-entropy-and-logistic-regression-bf7098e75559"
#         }
#     },
#     {
#         "id": 4411,
#         "distance": 0.8003642559051514,
#         "entity": {
#             "title": "Why Passion Is Not Enough in the Working World \u2014 Learn Professionalism Instead",
#             "link": "https://medium.com/swlh/why-passion-is-not-enough-in-the-working-world-learn-professionalism-instead-d1bdb0acd750"
#         }
#     },
#     {
#         "id": 3503,
#         "distance": 0.8004330992698669,
#         "entity": {
#             "title": "Figma to video prototyping \u2014 easy way in 3 steps",
#             "link": "https://uxdesign.cc/figma-to-video-prototyping-easy-way-in-3-steps-d7ac3770d253"
#         }
#     },
#     {
#         "id": 4397,
#         "distance": 0.8004655838012695,
#         "entity": {
#             "title": "An Introduction to Survey Research",
#             "link": "https://medium.com/swlh/an-introduction-to-survey-research-ba9e9fb9ca57"
#         }
#     },
#     {
#         "id": 4969,
#         "distance": 0.8004793524742126,
#         "entity": {
#             "title": "Warning: Your campaign (process) is broken",
#             "link": "https://medium.com/swlh/warning-your-campaign-process-is-broken-97f3c603f8aa"
#         }
#     },
#     {
#         "id": 2705,
#         "distance": 0.8005216121673584,
#         "entity": {
#             "title": "Exploratory Data Analysis: DataPrep.eda vs Pandas-Profiling",
#             "link": "https://towardsdatascience.com/exploratory-data-analysis-dataprep-eda-vs-pandas-profiling-7137683fe47f"
#         }
#     },
#     {
#         "id": 3185,
#         "distance": 0.8005879521369934,
#         "entity": {
#             "title": "Modelling Volatile Time Series with LSTM Networks",
#             "link": "https://towardsdatascience.com/modelling-volatile-time-series-with-lstm-networks-51250fb7cfa3"
#         }
#     },
#     {
#         "id": 5532,
#         "distance": 0.8005922436714172,
#         "entity": {
#             "title": "Removing \u2018The Wall\u2019 in ML Ops",
#             "link": "https://towardsdatascience.com/removing-the-wall-in-ml-ops-44dac377b4c6"
#         }
#     },
#     {
#         "id": 1969,
#         "distance": 0.8007100224494934,
#         "entity": {
#             "title": "Base Plotting in R",
#             "link": "https://towardsdatascience.com/base-plotting-in-r-eb365da06b22"
#         }
#     },
#     "(90 more items hidden)"
# ]



# 9. drop collection

utility.drop_collection(COLLECTION_NAME)