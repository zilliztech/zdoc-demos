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

print(data["rows"][0])

# Output
#
# {
#     "id": 0,
#     "title": "The Reported Mortality Rate of Coronavirus Is Not Important",
#     "title_vector": [
#         0.041732933,
#         0.013779674,
#         -0.027564144,
#         -0.013061441,
#         0.009748648,
#         0.00082446384,
#         -0.00071647146,
#         0.048612226,
#         -0.04836573,
#         -0.04567751,
#         "(758 more items hidden)"
#     ],
#     "link": "https://medium.com/swlh/the-reported-mortality-rate-of-coronavirus-is-not-important-369989c8d912",
#     "reading_time": 13,
#     "publication": "The Startup",
#     "claps": 1100,
#     "responses": 18
# }



# Prepare a list of columns
with open(DATASET_PATH) as f:
    keys = list(data["rows"][0].keys())
    columns = [ [] for x in keys ]
    for row in data["rows"]:
        for x in keys:
            columns[keys.index(x)].append(row[x])

    columns_demo = [ [] for x in keys ]
    for row in data["rows"][:2]:
        for x in keys:
            columns_demo[keys.index(x)].append(row[x])

print(columns_demo)

# Output
#
# [
#     [
#         0,
#         1
#     ],
#     [
#         "The Reported Mortality Rate of Coronavirus Is Not Important",
#         "Dashboards in Python: 3 Advanced Examples for Dash Beginners and Everyone Else"
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
#             "(758 more items hidden)"
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
#             "(758 more items hidden)"
#         ]
#     ],
#     [
#         "https://medium.com/swlh/the-reported-mortality-rate-of-coronavirus-is-not-important-369989c8d912",
#         "https://medium.com/swlh/dashboards-in-python-3-advanced-examples-for-dash-beginners-and-everyone-else-b1daf4e2ec0a"
#     ],
#     [
#         13,
#         14
#     ],
#     [
#         "The Startup",
#         "The Startup"
#     ],
#     [
#         1100,
#         726
#     ],
#     [
#         18,
#         3
#     ]
# ]



# 7. Insert data
results = collection.insert(data["rows"])
# results = collection.insert(columns) # also works

print(f"Data inserted successfully! Inserted rows: {results.insert_count}")

# Output
#
# Data inserted successfully! Inserted rows: 5979



# If you have prepared your data in columns, you can do as follows:
# results = collection.insert(columns)

time.sleep(10)

# 8. Search data
# Metric type should be the same as
# that defined in the index parameters 
# used to create the index.
search_params = {
    "metric_type": "L2"
}

# single vector search

results = collection.search(
    # highlight-next-line
    data=[data["rows"][0]['title_vector']],
    anns_field="title_vector",
    param=search_params,
    output_fields=["title", "link"],
    limit=5
)

entities = [ x.entity.to_dict() for x in results[0] ]

print(entities)

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
#     }
# ]



# bulk vector search

results = collection.search(
    # highlight-next-line
    data=[data["rows"][0]['title_vector'], data["rows"][1]['title_vector']],
    anns_field="title_vector",
    param=search_params,
    output_fields=["title", "link"],
    limit=5
)

entities = [ list(map(lambda y: y.entity.to_dict(), x)) for x in results ]

print(entities)

# Output
#
# [
#     [
#         {
#             "id": 0,
#             "distance": 0.0,
#             "entity": {
#                 "title": "The Reported Mortality Rate of Coronavirus Is Not Important",
#                 "link": "https://medium.com/swlh/the-reported-mortality-rate-of-coronavirus-is-not-important-369989c8d912"
#             }
#         },
#         {
#             "id": 3177,
#             "distance": 0.29999834299087524,
#             "entity": {
#                 "title": "Following the Spread of Coronavirus",
#                 "link": "https://towardsdatascience.com/following-the-spread-of-coronavirus-23626940c125"
#             }
#         },
#         {
#             "id": 5607,
#             "distance": 0.36103832721710205,
#             "entity": {
#                 "title": "The Hidden Side Effect of the Coronavirus",
#                 "link": "https://medium.com/swlh/the-hidden-side-effect-of-the-coronavirus-b6a7a5ee9586"
#             }
#         }
#     ],
#     [
#         {
#             "id": 1,
#             "distance": 0.0,
#             "entity": {
#                 "title": "Dashboards in Python: 3 Advanced Examples for Dash Beginners and Everyone Else",
#                 "link": "https://medium.com/swlh/dashboards-in-python-3-advanced-examples-for-dash-beginners-and-everyone-else-b1daf4e2ec0a"
#             }
#         },
#         {
#             "id": 5571,
#             "distance": 0.19530069828033447,
#             "entity": {
#                 "title": "Dashboards in Python Using Dash \u2014 Creating a Data Table using Data from Reddit",
#                 "link": "https://medium.com/swlh/dashboards-in-python-using-dash-creating-a-data-table-using-data-from-reddit-1d6c0cecb4bd"
#             }
#         },
#         {
#             "id": 3244,
#             "distance": 0.4073413610458374,
#             "entity": {
#                 "title": "OCR Engine Comparison \u2014 Tesseract vs. EasyOCR",
#                 "link": "https://medium.com/swlh/ocr-engine-comparison-tesseract-vs-easyocr-729be893d3ae"
#             }
#         }
#     ]
# ]



# search with filters

results = collection.search(
    data=[data["rows"][0]['title_vector']],
    anns_field="title_vector",
    param=search_params,
    # highlight-start
    expr="10 < reading_time < 15",
    output_fields=["title", "reading_time"],
    # highlight-end
    limit=5
)

entities = [ x.entity.to_dict() for x in results[0] ]

print(entities)

# Output
#
# [
#     {
#         "id": 0,
#         "distance": 0.0,
#         "entity": {
#             "title": "The Reported Mortality Rate of Coronavirus Is Not Important",
#             "reading_time": 13
#         }
#     },
#     {
#         "id": 5780,
#         "distance": 0.4586222767829895,
#         "entity": {
#             "title": "Heart Disease Risk Assessment Using Machine Learning",
#             "reading_time": 12
#         }
#     },
#     {
#         "id": 5503,
#         "distance": 0.5037479400634766,
#         "entity": {
#             "title": "New Data Shows a Lower Covid-19 Fatality Rate",
#             "reading_time": 13
#         }
#     }
# ]



results = collection.search(
    data=[data["rows"][0]['title_vector']],
    anns_field="title_vector",
    param=search_params,
    # highlight-start
    expr='claps > 1500 and responses > 15',
    output_fields=['title', 'claps', 'responses'],
    # highlight-end
    limit=5
)

entities = [ x.entity.to_dict() for x in results[0] ]

print(entities)

# Output
#
# [
#     {
#         "id": 5641,
#         "distance": 0.37674015760421753,
#         "entity": {
#             "claps": 2900,
#             "responses": 47,
#             "title": "Why The Coronavirus Mortality Rate is Misleading"
#         }
#     },
#     {
#         "id": 1394,
#         "distance": 0.6772846579551697,
#         "entity": {
#             "claps": 2600,
#             "responses": 212,
#             "title": "Remote Work Is Not Here to Stay"
#         }
#     },
#     {
#         "id": 4573,
#         "distance": 0.6836910247802734,
#         "entity": {
#             "claps": 1800,
#             "responses": 40,
#             "title": "Apple May Lose the Developer Crowd"
#         }
#     }
# ]



results = collection.search(
    data=[data["rows"][0]['title_vector']],
    anns_field="title_vector",
    param=search_params,
    # highlight-start
    expr='publication == "Towards Data Science"',
    output_fields=["title", "publication"],
    # highlight-end
    limit=5
)

entities = [ x.entity.to_dict() for x in results[0] ]

print(entities)

# Output
#
# [
#     {
#         "id": 3177,
#         "distance": 0.29999834299087524,
#         "entity": {
#             "title": "Following the Spread of Coronavirus",
#             "publication": "Towards Data Science"
#         }
#     },
#     {
#         "id": 5641,
#         "distance": 0.37674015760421753,
#         "entity": {
#             "title": "Why The Coronavirus Mortality Rate is Misleading",
#             "publication": "Towards Data Science"
#         }
#     },
#     {
#         "id": 938,
#         "distance": 0.436093807220459,
#         "entity": {
#             "title": "Mortality Rate As an Indicator of an Epidemic Outbreak",
#             "publication": "Towards Data Science"
#         }
#     }
# ]



results = collection.search(
    data=[data["rows"][0]['title_vector']],
    anns_field="title_vector",
    param=search_params,
    # highlight-start
    expr='publication not in ["Towards Data Science", "Personal Growth"]',
    output_fields=["title", "publication"],
    # highlight-end
    limit=5
)

entities = [ x.entity.to_dict() for x in results[0] ]

print(entities)

# Output
#
# [
#     {
#         "id": 0,
#         "distance": 0.0,
#         "entity": {
#             "publication": "The Startup",
#             "title": "The Reported Mortality Rate of Coronavirus Is Not Important"
#         }
#     },
#     {
#         "id": 5607,
#         "distance": 0.36103832721710205,
#         "entity": {
#             "publication": "The Startup",
#             "title": "The Hidden Side Effect of the Coronavirus"
#         }
#     },
#     {
#         "id": 3441,
#         "distance": 0.416297972202301,
#         "entity": {
#             "publication": "The Startup",
#             "title": "Coronavirus shows what ethical Amazon could look like"
#         }
#     }
# ]



results = collection.search(
    data=[data["rows"][0]['title_vector']],
    anns_field="title_vector",
    param=search_params,
    # highlight-start
    expr='title like "Top%"',
    output_fields=["title", "link"],
    # highlight-end
    limit=5
)

entities = [ x.entity.to_dict() for x in results[0] ]

print(entities)

# Output
#
# []



results = collection.search(
    data=[data["rows"][0]['title_vector']],
    anns_field="title_vector",
    param=search_params,
    # highlight-start
    expr='(publication == "Towards Data Science") and ((claps > 1500 and responses > 15) or (10 < reading_time < 15))',
    output_fields=["title", "publication", "claps", "responses", "reading_time"],
    # highlight-end
    limit=5
)

entities = [ x.entity.to_dict() for x in results[0] ]

print(entities)

# Output
#
# [
#     {
#         "id": 5641,
#         "distance": 0.37674015760421753,
#         "entity": {
#             "title": "Why The Coronavirus Mortality Rate is Misleading",
#             "publication": "Towards Data Science",
#             "claps": 2900,
#             "responses": 47,
#             "reading_time": 9
#         }
#     },
#     {
#         "id": 5780,
#         "distance": 0.4586222767829895,
#         "entity": {
#             "title": "Heart Disease Risk Assessment Using Machine Learning",
#             "publication": "Towards Data Science",
#             "claps": 15,
#             "responses": 0,
#             "reading_time": 12
#         }
#     },
#     {
#         "id": 5503,
#         "distance": 0.5037479400634766,
#         "entity": {
#             "title": "New Data Shows a Lower Covid-19 Fatality Rate",
#             "publication": "Towards Data Science",
#             "claps": 161,
#             "responses": 3,
#             "reading_time": 13
#         }
#     }
# ]



# query

results = collection.query(
    expr='(publication == "Towards Data Science") and ((claps > 1500 and responses > 15) or (10 < reading_time < 15))',
    output_fields=["title", "publication", "claps", "responses", "reading_time"],
    limit=5
)

print(results)

# Output
#
# [
#     {
#         "responses": 18,
#         "reading_time": 21,
#         "id": 69,
#         "title": "Top 10 In-Demand programming languages to learn in 2020",
#         "publication": "Towards Data Science",
#         "claps": 3000
#     },
#     {
#         "responses": 7,
#         "reading_time": 12,
#         "id": 73,
#         "title": "Data Cleaning in Python: the Ultimate Guide (2020)",
#         "publication": "Towards Data Science",
#         "claps": 1500
#     },
#     {
#         "responses": 0,
#         "reading_time": 11,
#         "id": 75,
#         "title": "Top Trends of Graph Machine Learning in 2020",
#         "publication": "Towards Data Science",
#         "claps": 1100
#     }
# ]



# delete

results = collection.delete(expr='id in [1, 2, 3]')

print(results)

# Output
#
# (insert count: 0, delete count: 3, upsert count: 0, timestamp: 445474758328320001, success count: 0, err count: 0)



# 9. Drop collection
utility.drop_collection(COLLECTION_NAME)