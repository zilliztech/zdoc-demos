import json, os, time, random
from contextlib import suppress
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
    FieldSchema(name="id", dtype=DataType.INT64, is_primary=True, auto_id=True, max_length=100),
    FieldSchema(name="title", dtype=DataType.VARCHAR, max_length=512),
    FieldSchema(name="title_vector", dtype=DataType.FLOAT_VECTOR, dim=768),
    # The following field is a JSON field
    FieldSchema(name="article_meta", dtype=DataType.JSON)
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
    list_of_rows = data['rows']

    rows = []
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
        row['article_meta']['tags_1'] = [ random.randint(0, 40) for x in range(40)]
        row['article_meta']['tags_2'] = [ [ random.randint(0, 40) for y in range(4) ] for x in range(10) ]
        # Append this row to the data_rows list
        rows.append(row)

print(rows[:1])

# Output
#
# [
#     {
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
#         "article_meta": {
#             "link": "https://medium.com/swlh/the-reported-mortality-rate-of-coronavirus-is-not-important-369989c8d912",
#             "reading_time": 13,
#             "publication": "The Startup",
#             "claps": 1100,
#             "responses": 18,
#             "tags_1": [
#                 20,
#                 39,
#                 4,
#                 4,
#                 11,
#                 30,
#                 6,
#                 23,
#                 29,
#                 1,
#                 "(30 more items hidden)"
#             ],
#             "tags_2": [
#                 [
#                     37,
#                     29,
#                     23,
#                     27
#                 ],
#                 [
#                     6,
#                     34,
#                     17,
#                     33
#                 ],
#                 [
#                     39,
#                     9,
#                     15,
#                     37
#                 ],
#                 [
#                     3,
#                     28,
#                     21,
#                     10
#                 ],
#                 [
#                     40,
#                     15,
#                     36,
#                     20
#                 ],
#                 [
#                     15,
#                     24,
#                     40,
#                     26
#                 ],
#                 [
#                     8,
#                     36,
#                     18,
#                     19
#                 ],
#                 [
#                     36,
#                     21,
#                     16,
#                     28
#                 ],
#                 [
#                     14,
#                     6,
#                     32,
#                     18
#                 ],
#                 [
#                     23,
#                     31,
#                     36,
#                     39
#                 ]
#             ]
#         }
#     }
# ]



# 7. Insert data
results = collection.insert(rows)

print(f"Data inserted successfully! inserted rows: {results.insert_count}")

# Output
#
# Data inserted successfully! inserted rows: 5979



time.sleep(10)

# 8. Count entities

counts = collection.query(expr="", output_fields=["count(*)"])

print(counts)

# Output
#
# [
#     {
#         "count(*)": 5979
#     }
# ]



# 9. Count entities with condition

expr = 'article_meta["claps"] > 30 and article_meta["reading_time"] < 10'

counts = collection.query(expr=expr, output_fields=["count(*)"])

print(counts)

# Output
#
# [
#     {
#         "count(*)": 4304
#     }
# ]



# 10. Check if a specific element exists in a JSON field

# Search

# matches all articles with tags_1 having the member 16
expr_1 = 'JSON_CONTAINS(article_meta["tags_1"], 16)'

# matches all articles with tags_2 having the member [5, 3, 39, 8]
expr_2 = 'JSON_CONTAINS(article_meta["tags_2"], [5, 3, 39, 8])'

# matches all articles with tags_1 having a member from [5, 3, 39, 8]
expr_3 = 'JSON_CONTAINS_ANY(article_meta["tags_1"], [5, 3, 39, 8])'

# matches all articles with tags_1 having all members from [2, 4, 6]
expr_4 = 'JSON_CONTAINS_ALL(article_meta["tags_1"], [2, 4, 6])'

query_vector = rows[0]['title_vector']

# Define search parameters
search_params = {
    "metric_type": "L2",
    "params": {"nprobe": 10}
}

res = collection.search(
    data=[query_vector],
    anns_field="title_vector",
    param={"metric_type": "L2", "params": {"nprobe": 10}},
    limit=5,
    expr=expr_1,
    output_fields=["title", "article_meta"]
)

ids = [ hits.ids for hits in res ]

print(ids)

# Output
#
# [
#     [
#         445311585782930475,
#         445311585782930509,
#         445311585782928309,
#         445311585782929013,
#         445311585782925710
#     ]
# ]



distances = [ hits.distances for hits in res ]

print(distances)

# Output
#
# [
#     [
#         0.36103832721710205,
#         0.37674015760421753,
#         0.416297972202301,
#         0.4928317368030548,
#         0.49443864822387695
#     ]
# ]



def get_tags_1(value, target):
    try:
        return value.index(target) >= 0
    except (ValueError):
        return False

results = [ {
    "id": hit.id,
    "distance": hit.distance,
    "entity": {
        "title": hit.entity.get("title"),
        "link": hit.entity.get("article_meta")['link'],
        "tags_1": get_tags_1(hit.entity.get("article_meta")["tags_1"], 16),
    }
} for hits in res for hit in hits ]

print(results)

# Output
#
# [
#     {
#         "id": 445311585782930475,
#         "distance": 0.36103832721710205,
#         "entity": {
#             "title": "The Hidden Side Effect of the Coronavirus",
#             "link": "https://medium.com/swlh/the-hidden-side-effect-of-the-coronavirus-b6a7a5ee9586",
#             "tags_1": true
#         }
#     },
#     {
#         "id": 445311585782930509,
#         "distance": 0.37674015760421753,
#         "entity": {
#             "title": "Why The Coronavirus Mortality Rate is Misleading",
#             "link": "https://towardsdatascience.com/why-the-coronavirus-mortality-rate-is-misleading-cc63f571b6a6",
#             "tags_1": true
#         }
#     },
#     {
#         "id": 445311585782928309,
#         "distance": 0.416297972202301,
#         "entity": {
#             "title": "Coronavirus shows what ethical Amazon could look like",
#             "link": "https://medium.com/swlh/coronavirus-shows-what-ethical-amazon-could-look-like-7c80baf2c663",
#             "tags_1": true
#         }
#     },
#     {
#         "id": 445311585782929013,
#         "distance": 0.4928317368030548,
#         "entity": {
#             "title": "Will Coronavirus Impact Freelancers\u2019 Ability to Rent?",
#             "link": "https://medium.com/swlh/will-coronavirus-impact-freelancers-ability-to-rent-ae11f2847bab",
#             "tags_1": true
#         }
#     },
#     {
#         "id": 445311585782925710,
#         "distance": 0.49443864822387695,
#         "entity": {
#             "title": "Choosing the right performance metrics can save lives against Coronavirus",
#             "link": "https://towardsdatascience.com/choosing-the-right-performance-metrics-can-save-lives-against-coronavirus-2f27492f6638",
#             "tags_1": true
#         }
#     }
# ]



# query

res = collection.query(
    limit=5,
    expr=expr_4,
    output_fields=["title", "article_meta"]
)

res = [ {
    "title": x.get("title"),
    "article_meta": {
        "link": x.get("article_meta")['link'],
        "reading_time": x.get("article_meta")['reading_time'],
        "publication": x.get("article_meta")['publication'],
        "claps": x.get("article_meta")['claps'],
        "responses": x.get("article_meta")['responses'],
        "tags_1": get_tags_1(x.get("article_meta")['tags_1'], 2),
    },
} for x in res ]

print(res)

# Output
#
# [
#     {
#         "title": "The Reported Mortality Rate of Coronavirus Is Not Important",
#         "article_meta": {
#             "link": "https://medium.com/swlh/the-reported-mortality-rate-of-coronavirus-is-not-important-369989c8d912",
#             "reading_time": 13,
#             "publication": "The Startup",
#             "claps": 1100,
#             "responses": 18,
#             "tags_1": true
#         }
#     },
#     {
#         "title": "Dashboards in Python: 3 Advanced Examples for Dash Beginners and Everyone Else",
#         "article_meta": {
#             "link": "https://medium.com/swlh/dashboards-in-python-3-advanced-examples-for-dash-beginners-and-everyone-else-b1daf4e2ec0a",
#             "reading_time": 14,
#             "publication": "The Startup",
#             "claps": 726,
#             "responses": 3,
#             "tags_1": true
#         }
#     },
#     {
#         "title": "Would you rather have 8% of $25 or 25% of $8?",
#         "article_meta": {
#             "link": "https://medium.com/swlh/would-you-rather-have-8-of-25-or-25-of-8-486b3bc48f28",
#             "reading_time": 3,
#             "publication": "The Startup",
#             "claps": 208,
#             "responses": 5,
#             "tags_1": true
#         }
#     },
#     {
#         "title": "Big Reasons for Expanding Your Vocabulary, and How to Do It.",
#         "article_meta": {
#             "link": "https://medium.com/swlh/big-reasons-for-expanding-your-vocabulary-and-how-to-do-it-c78766e4df2",
#             "reading_time": 8,
#             "publication": "The Startup",
#             "claps": 417,
#             "responses": 3,
#             "tags_1": true
#         }
#     },
#     {
#         "title": "How to Find Things and Do Research in a Discovery Phase",
#         "article_meta": {
#             "link": "https://medium.com/swlh/how-to-find-things-and-do-research-in-a-discovery-phase-6c3e4f50b208",
#             "reading_time": 5,
#             "publication": "The Startup",
#             "claps": 330,
#             "responses": 0,
#             "tags_1": true
#         }
#     }
# ]



# 11. Drop collection

utility.drop_collection(COLLECTION_NAME)