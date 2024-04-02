import random, time, json
from pymilvus import connections, MilvusClient, DataType

CLUSTER_ENDPOINT = "YOUR_CLUSTER_ENDPOINT"
TOKEN = "YOUR_CLUSTER_TOKEN"

# 1. Set up a Milvus client
client = MilvusClient(
    uri=CLUSTER_ENDPOINT,
    token=TOKEN 
)

# 2. Create a collection
schema = MilvusClient.create_schema(
    auto_id=False,
    enable_dynamic_field=False,
)

schema.add_field(field_name="id", datatype=DataType.INT64, is_primary=True)
schema.add_field(field_name="vector", datatype=DataType.FLOAT_VECTOR, dim=5)
schema.add_field(field_name="color", datatype=DataType.JSON)

index_params = MilvusClient.prepare_index_params()

index_params.add_index(
    field_name="id",
    index_type="STL_SORT"
)

index_params.add_index(
    field_name="vector",
    index_type="IVF_FLAT",
    metric_type="L2",
    params={"nlist": 1024}
)

client.create_collection(
    collection_name="test_collection",
    schema=schema,
    index_params=index_params
)

res = client.get_load_state(
    collection_name="test_collection"
)

print(res)

# Output
#
# {
#     "state": "<LoadState: Loaded>"
# }



# 3. Insert randomly generated vectors 
colors = ["green", "blue", "yellow", "red", "black", "white", "purple", "pink", "orange", "brown", "grey"]
data = []

for i in range(1000):
    current_color = random.choice(colors)
    current_tag = random.randint(1000, 9999)
    current_coord = [ random.randint(0, 40) for _ in range(3) ]
    current_ref = [ [ random.choice(colors) for _ in range(3) ] for _ in range(3) ]
    data.append({
        "id": i,
        "vector": [ random.uniform(-1, 1) for _ in range(5) ],
        "color": {
            "label": current_color,
            "tag": current_tag,
            "coord": current_coord,
            "ref": current_ref
        }
    })

print(data[0])

# Output
#
# {
#     "id": 0,
#     "vector": [
#         -0.8017921296923975,
#         0.550046715206634,
#         0.764922589768134,
#         0.6371433836123146,
#         0.2705233937454232
#     ],
#     "color": {
#         "label": "blue",
#         "tag": 9927,
#         "coord": [
#             22,
#             36,
#             6
#         ],
#         "ref": [
#             [
#                 "blue",
#                 "green",
#                 "white"
#             ],
#             [
#                 "black",
#                 "green",
#                 "pink"
#             ],
#             [
#                 "grey",
#                 "black",
#                 "brown"
#             ]
#         ]
#     }
# }



res = client.insert(
    collection_name="test_collection",
    data=data
)

print(res)

# Output
#
# {
#     "insert_count": 1000,
#     "ids": [
#         0,
#         1,
#         2,
#         3,
#         4,
#         5,
#         6,
#         7,
#         8,
#         9,
#         "(990 more items hidden)"
#     ]
# }



time.sleep(5)

# 4. Basic search with a JSON field
query_vectors = [ [ random.uniform(-1, 1) for _ in range(5) ]]

res = client.search(
    collection_name="test_collection",
    data=query_vectors,
    filter='color["label"] in ["red"]',
    search_params={
        "metric_type": "L2",
        "params": {"nprobe": 16}
    },
    output_fields=["id", "color"],
    limit=3
)

print(res)

# Output
#
# [
#     [
#         {
#             "id": 460,
#             "distance": 0.4016231596469879,
#             "entity": {
#                 "id": 460,
#                 "color": {
#                     "label": "red",
#                     "tag": 5030,
#                     "coord": [
#                         14,
#                         32,
#                         40
#                     ],
#                     "ref": [
#                         [
#                             "pink",
#                             "green",
#                             "brown"
#                         ],
#                         [
#                             "red",
#                             "grey",
#                             "black"
#                         ],
#                         [
#                             "red",
#                             "yellow",
#                             "orange"
#                         ]
#                     ]
#                 }
#             }
#         },
#         {
#             "id": 785,
#             "distance": 0.451080858707428,
#             "entity": {
#                 "id": 785,
#                 "color": {
#                     "label": "red",
#                     "tag": 5290,
#                     "coord": [
#                         31,
#                         13,
#                         23
#                     ],
#                     "ref": [
#                         [
#                             "yellow",
#                             "pink",
#                             "pink"
#                         ],
#                         [
#                             "purple",
#                             "grey",
#                             "orange"
#                         ],
#                         [
#                             "grey",
#                             "purple",
#                             "pink"
#                         ]
#                     ]
#                 }
#             }
#         },
#         {
#             "id": 355,
#             "distance": 0.5839247703552246,
#             "entity": {
#                 "id": 355,
#                 "color": {
#                     "label": "red",
#                     "tag": 8725,
#                     "coord": [
#                         5,
#                         10,
#                         22
#                     ],
#                     "ref": [
#                         [
#                             "white",
#                             "purple",
#                             "yellow"
#                         ],
#                         [
#                             "white",
#                             "purple",
#                             "white"
#                         ],
#                         [
#                             "orange",
#                             "white",
#                             "pink"
#                         ]
#                     ]
#                 }
#             }
#         }
#     ]
# ]



# 5. Advanced search within a JSON field

res = client.query(
    collection_name="test_collection",
    data=query_vectors,
    filter='JSON_CONTAINS(color["ref"], ["blue", "brown", "grey"])',
    output_fields=["id", "color"],
    limit=3
)

print(res)

# Output
#
# [
#     {
#         "id": 79,
#         "color": {
#             "label": "orange",
#             "tag": 8857,
#             "coord": [
#                 10,
#                 14,
#                 5
#             ],
#             "ref": [
#                 [
#                     "yellow",
#                     "white",
#                     "green"
#                 ],
#                 [
#                     "blue",
#                     "purple",
#                     "purple"
#                 ],
#                 [
#                     "blue",
#                     "brown",
#                     "grey"
#                 ]
#             ]
#         }
#     },
#     {
#         "id": 371,
#         "color": {
#             "label": "black",
#             "tag": 1324,
#             "coord": [
#                 2,
#                 18,
#                 32
#             ],
#             "ref": [
#                 [
#                     "purple",
#                     "orange",
#                     "brown"
#                 ],
#                 [
#                     "blue",
#                     "brown",
#                     "grey"
#                 ],
#                 [
#                     "purple",
#                     "blue",
#                     "blue"
#                 ]
#             ]
#         }
#     },
#     {
#         "id": 590,
#         "color": {
#             "label": "red",
#             "tag": 3340,
#             "coord": [
#                 13,
#                 21,
#                 13
#             ],
#             "ref": [
#                 [
#                     "yellow",
#                     "yellow",
#                     "red"
#                 ],
#                 [
#                     "blue",
#                     "brown",
#                     "grey"
#                 ],
#                 [
#                     "pink",
#                     "yellow",
#                     "purple"
#                 ]
#             ]
#         }
#     }
# ]



res = client.query(
    collection_name="test_collection",
    data=query_vectors,
    filter='JSON_CONTAINS_ALL(color["coord"], [4, 5])',
    output_fields=["id", "color"],
    limit=3
)

print(res)

# Output
#
# [
#     {
#         "id": 281,
#         "color": {
#             "label": "red",
#             "tag": 3645,
#             "coord": [
#                 5,
#                 33,
#                 4
#             ],
#             "ref": [
#                 [
#                     "orange",
#                     "blue",
#                     "pink"
#                 ],
#                 [
#                     "purple",
#                     "blue",
#                     "purple"
#                 ],
#                 [
#                     "black",
#                     "brown",
#                     "yellow"
#                 ]
#             ]
#         }
#     },
#     {
#         "id": 464,
#         "color": {
#             "label": "brown",
#             "tag": 6261,
#             "coord": [
#                 5,
#                 9,
#                 4
#             ],
#             "ref": [
#                 [
#                     "purple",
#                     "purple",
#                     "brown"
#                 ],
#                 [
#                     "black",
#                     "pink",
#                     "white"
#                 ],
#                 [
#                     "brown",
#                     "grey",
#                     "brown"
#                 ]
#             ]
#         }
#     },
#     {
#         "id": 567,
#         "color": {
#             "label": "green",
#             "tag": 4589,
#             "coord": [
#                 5,
#                 39,
#                 4
#             ],
#             "ref": [
#                 [
#                     "purple",
#                     "yellow",
#                     "white"
#                 ],
#                 [
#                     "yellow",
#                     "yellow",
#                     "brown"
#                 ],
#                 [
#                     "blue",
#                     "red",
#                     "yellow"
#                 ]
#             ]
#         }
#     }
# ]



res = client.query(
    collection_name="test_collection",
    data=query_vectors,
    filter='JSON_CONTAINS_ANY(color["coord"], [4, 5])',
    output_fields=["id", "color"],
    limit=3
)

print(res)

# Output
#
# [
#     {
#         "id": 0,
#         "color": {
#             "label": "yellow",
#             "tag": 6340,
#             "coord": [
#                 40,
#                 4,
#                 40
#             ],
#             "ref": [
#                 [
#                     "purple",
#                     "yellow",
#                     "orange"
#                 ],
#                 [
#                     "green",
#                     "grey",
#                     "purple"
#                 ],
#                 [
#                     "black",
#                     "white",
#                     "yellow"
#                 ]
#             ]
#         }
#     },
#     {
#         "id": 2,
#         "color": {
#             "label": "brown",
#             "tag": 9359,
#             "coord": [
#                 38,
#                 21,
#                 5
#             ],
#             "ref": [
#                 [
#                     "red",
#                     "brown",
#                     "white"
#                 ],
#                 [
#                     "purple",
#                     "red",
#                     "brown"
#                 ],
#                 [
#                     "pink",
#                     "grey",
#                     "black"
#                 ]
#             ]
#         }
#     },
#     {
#         "id": 7,
#         "color": {
#             "label": "green",
#             "tag": 3560,
#             "coord": [
#                 5,
#                 9,
#                 5
#             ],
#             "ref": [
#                 [
#                     "blue",
#                     "orange",
#                     "green"
#                 ],
#                 [
#                     "blue",
#                     "blue",
#                     "black"
#                 ],
#                 [
#                     "green",
#                     "purple",
#                     "green"
#                 ]
#             ]
#         }
#     }
# ]



# 5. Drop the collection
client.drop_collection(
    collection_name="test_collection"
)