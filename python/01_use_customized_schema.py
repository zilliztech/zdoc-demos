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
#             "link": "https://medium.com/swlh/the-reported-mortality-rate-of-coronavirus-is-not-important-369989c8d912",
#             "title": "The Reported Mortality Rate of Coronavirus Is Not Important"
#         }
#     },
#     {
#         "id": 3177,
#         "distance": 0.29999837279319763,
#         "entity": {
#             "link": "https://towardsdatascience.com/following-the-spread-of-coronavirus-23626940c125",
#             "title": "Following the Spread of Coronavirus"
#         }
#     },
#     {
#         "id": 5607,
#         "distance": 0.36103835701942444,
#         "entity": {
#             "link": "https://medium.com/swlh/the-hidden-side-effect-of-the-coronavirus-b6a7a5ee9586",
#             "title": "The Hidden Side Effect of the Coronavirus"
#         }
#     },
#     {
#         "id": 5641,
#         "distance": 0.37674015760421753,
#         "entity": {
#             "link": "https://towardsdatascience.com/why-the-coronavirus-mortality-rate-is-misleading-cc63f571b6a6",
#             "title": "Why The Coronavirus Mortality Rate is Misleading"
#         }
#     },
#     {
#         "id": 3441,
#         "distance": 0.4162980318069458,
#         "entity": {
#             "link": "https://medium.com/swlh/coronavirus-shows-what-ethical-amazon-could-look-like-7c80baf2c663",
#             "title": "Coronavirus shows what ethical Amazon could look like"
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
#             "distance": 0.29999837279319763,
#             "entity": {
#                 "title": "Following the Spread of Coronavirus",
#                 "link": "https://towardsdatascience.com/following-the-spread-of-coronavirus-23626940c125"
#             }
#         },
#         {
#             "id": 5607,
#             "distance": 0.36103835701942444,
#             "entity": {
#                 "title": "The Hidden Side Effect of the Coronavirus",
#                 "link": "https://medium.com/swlh/the-hidden-side-effect-of-the-coronavirus-b6a7a5ee9586"
#             }
#         },
#         {
#             "id": 5641,
#             "distance": 0.37674015760421753,
#             "entity": {
#                 "title": "Why The Coronavirus Mortality Rate is Misleading",
#                 "link": "https://towardsdatascience.com/why-the-coronavirus-mortality-rate-is-misleading-cc63f571b6a6"
#             }
#         },
#         {
#             "id": 3441,
#             "distance": 0.4162980318069458,
#             "entity": {
#                 "title": "Coronavirus shows what ethical Amazon could look like",
#                 "link": "https://medium.com/swlh/coronavirus-shows-what-ethical-amazon-could-look-like-7c80baf2c663"
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
#         },
#         {
#             "id": 3276,
#             "distance": 0.4157174229621887,
#             "entity": {
#                 "title": "How to Import Data to Salesforce Marketing Cloud (ExactTarget) Using Python REST API",
#                 "link": "https://medium.com/swlh/how-to-import-data-to-salesforce-marketing-cloud-exacttarget-using-python-rest-api-1302a26f89c0"
#             }
#         },
#         {
#             "id": 1196,
#             "distance": 0.41766005754470825,
#             "entity": {
#                 "title": "How to Automate Multiple Excel Workbooks and Perform Analysis",
#                 "link": "https://towardsdatascience.com/how-to-automate-multiple-excel-workbooks-and-perform-analysis-13e8aa5a2042"
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
#         "distance": 0.5037478804588318,
#         "entity": {
#             "title": "New Data Shows a Lower Covid-19 Fatality Rate",
#             "reading_time": 13
#         }
#     },
#     {
#         "id": 4331,
#         "distance": 0.5255616307258606,
#         "entity": {
#             "title": "Common Pipenv Errors",
#             "reading_time": 11
#         }
#     },
#     {
#         "id": 2803,
#         "distance": 0.5679889917373657,
#         "entity": {
#             "title": "How Does US Healthcare Compare With Healthcare Around the World?",
#             "reading_time": 12
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
#             "title": "Why The Coronavirus Mortality Rate is Misleading",
#             "claps": 2900,
#             "responses": 47
#         }
#     },
#     {
#         "id": 4701,
#         "distance": 0.6518568992614746,
#         "entity": {
#             "title": "The Discovery of Aliens Would Be Terrible",
#             "claps": 4300,
#             "responses": 95
#         }
#     },
#     {
#         "id": 1394,
#         "distance": 0.6772847175598145,
#         "entity": {
#             "title": "Remote Work Is Not Here to Stay",
#             "claps": 2600,
#             "responses": 212
#         }
#     },
#     {
#         "id": 4573,
#         "distance": 0.6836910247802734,
#         "entity": {
#             "title": "Apple May Lose the Developer Crowd",
#             "claps": 1800,
#             "responses": 40
#         }
#     },
#     {
#         "id": 4799,
#         "distance": 0.7143410444259644,
#         "entity": {
#             "title": "Sorry, Online Courses Won\u2019t Make you a Data Scientist",
#             "claps": 5000,
#             "responses": 45
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
#         "distance": 0.29999837279319763,
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
#     },
#     {
#         "id": 5780,
#         "distance": 0.4586222767829895,
#         "entity": {
#             "title": "Heart Disease Risk Assessment Using Machine Learning",
#             "publication": "Towards Data Science"
#         }
#     },
#     {
#         "id": 3072,
#         "distance": 0.46275582909584045,
#         "entity": {
#             "title": "Can we learn anything from the progression of influenza to analyze the COVID-19 pandemic better?",
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
#             "title": "The Reported Mortality Rate of Coronavirus Is Not Important",
#             "publication": "The Startup"
#         }
#     },
#     {
#         "id": 5607,
#         "distance": 0.36103835701942444,
#         "entity": {
#             "title": "The Hidden Side Effect of the Coronavirus",
#             "publication": "The Startup"
#         }
#     },
#     {
#         "id": 3441,
#         "distance": 0.4162980318069458,
#         "entity": {
#             "title": "Coronavirus shows what ethical Amazon could look like",
#             "publication": "The Startup"
#         }
#     },
#     {
#         "id": 4275,
#         "distance": 0.48886314034461975,
#         "entity": {
#             "title": "How Can AI Help Fight Coronavirus?",
#             "publication": "The Startup"
#         }
#     },
#     {
#         "id": 4145,
#         "distance": 0.4928317666053772,
#         "entity": {
#             "title": "Will Coronavirus Impact Freelancers\u2019 Ability to Rent?",
#             "publication": "The Startup"
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
# [
#     {
#         "id": 3630,
#         "distance": 0.749665379524231,
#         "entity": {
#             "title": "Topic Modeling in Power BI using PyCaret",
#             "link": "https://towardsdatascience.com/topic-modeling-in-power-bi-using-pycaret-54422b4e36d6"
#         }
#     },
#     {
#         "id": 3666,
#         "distance": 0.7691308259963989,
#         "entity": {
#             "title": "Topic Modeling the comment section from a New York Times article",
#             "link": "https://towardsdatascience.com/topic-modeling-the-comment-section-from-a-new-york-times-article-e4775261530e"
#         }
#     },
#     {
#         "id": 3288,
#         "distance": 0.812682032585144,
#         "entity": {
#             "title": "Top 4 Myths About App Store Conversion Rate Optimization (CRO)",
#             "link": "https://medium.com/swlh/top-4-myths-about-app-store-conversion-rate-optimization-cro-c62476901c90"
#         }
#     },
#     {
#         "id": 1626,
#         "distance": 0.8307195901870728,
#         "entity": {
#             "title": "Top VS Code extensions for Web Developers",
#             "link": "https://medium.com/swlh/top-vs-code-extensions-for-web-developers-1e038201a8fc"
#         }
#     },
#     {
#         "id": 3398,
#         "distance": 0.8489705324172974,
#         "entity": {
#             "title": "Top ten mistakes found while performing code reviews",
#             "link": "https://medium.com/swlh/top-ten-mistakes-found-while-doing-code-reviews-b935ef44e797"
#         }
#     }
# ]



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
#         "distance": 0.5037478804588318,
#         "entity": {
#             "title": "New Data Shows a Lower Covid-19 Fatality Rate",
#             "publication": "Towards Data Science",
#             "claps": 161,
#             "responses": 3,
#             "reading_time": 13
#         }
#     },
#     {
#         "id": 4331,
#         "distance": 0.5255616307258606,
#         "entity": {
#             "title": "Common Pipenv Errors",
#             "publication": "Towards Data Science",
#             "claps": 20,
#             "responses": 1,
#             "reading_time": 11
#         }
#     },
#     {
#         "id": 2587,
#         "distance": 0.5877483487129211,
#         "entity": {
#             "title": "Data quality impact on the dataset",
#             "publication": "Towards Data Science",
#             "claps": 61,
#             "responses": 0,
#             "reading_time": 12
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
#         "reading_time": 21,
#         "id": 69,
#         "title": "Top 10 In-Demand programming languages to learn in 2020",
#         "publication": "Towards Data Science",
#         "claps": 3000,
#         "responses": 18
#     },
#     {
#         "reading_time": 12,
#         "id": 73,
#         "title": "Data Cleaning in Python: the Ultimate Guide (2020)",
#         "publication": "Towards Data Science",
#         "claps": 1500,
#         "responses": 7
#     },
#     {
#         "reading_time": 11,
#         "id": 75,
#         "title": "Top Trends of Graph Machine Learning in 2020",
#         "publication": "Towards Data Science",
#         "claps": 1100,
#         "responses": 0
#     },
#     {
#         "reading_time": 12,
#         "id": 79,
#         "title": "Rage Quitting Cancer Research",
#         "publication": "Towards Data Science",
#         "claps": 331,
#         "responses": 3
#     },
#     {
#         "reading_time": 13,
#         "id": 80,
#         "title": "Understanding Natural Language Processing: how AI understands our languages",
#         "publication": "Towards Data Science",
#         "claps": 109,
#         "responses": 0
#     }
# ]



# delete

results = collection.delete(expr='id in [1, 2, 3]')

print(results)

# Output
#
# (insert count: 0, delete count: 3, upsert count: 0, timestamp: 445491462765543426, success count: 0, err count: 0)



# 9. Drop collection
utility.drop_collection(COLLECTION_NAME)