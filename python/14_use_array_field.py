import random, time, json
from pymilvus import MilvusClient, DataType

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
schema.add_field(field_name="color", datatype=DataType.VARCHAR, max_length=512)
schema.add_field(field_name="color_tag", datatype=DataType.INT64)
schema.add_field(field_name="color_coord", datatype=DataType.ARRAY, element_type=DataType.INT64, max_capacity=5)

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
    current_coord = [ random.randint(0, 40) for _ in range(random.randint(3, 5)) ]
    data.append({
        "id": i,
        "vector": [ random.uniform(-1, 1) for _ in range(5) ],
        "color": current_color,
        "color_tag": current_tag,
        "color_coord": current_coord,
    })

print(data[0])

# Output
#
# {
#     "id": 0,
#     "vector": [
#         -0.7281372843021316,
#         0.221083364322598,
#         0.1870935951447279,
#         -0.9014772118747638,
#         -0.1688349915093177
#     ],
#     "color": "green",
#     "color_tag": 9212,
#     "color_coord": [
#         37,
#         36,
#         36,
#         7,
#         9
#     ]
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

# 4. Basic search with the array field
query_vectors = [ [ random.uniform(-1, 1) for _ in range(5) ]]

res = client.search(
    collection_name="test_collection",
    data=query_vectors,
    filter="color_coord[0] < 10",
    search_params={
        "metric_type": "L2",
        "params": {"nprobe": 16}
    },
    output_fields=["id", "color", "color_tag", "color_coord"],
    limit=3
)

print(res)

# Output
#
# [
#     [
#         {
#             "id": 993,
#             "distance": 0.1538649946451187,
#             "entity": {
#                 "color_coord": [
#                     5,
#                     37,
#                     39,
#                     18
#                 ],
#                 "id": 993,
#                 "color": "black",
#                 "color_tag": 6785
#             }
#         },
#         {
#             "id": 452,
#             "distance": 0.2353954315185547,
#             "entity": {
#                 "color_coord": [
#                     2,
#                     27,
#                     34,
#                     32,
#                     30
#                 ],
#                 "id": 452,
#                 "color": "brown",
#                 "color_tag": 2075
#             }
#         },
#         {
#             "id": 862,
#             "distance": 0.27913951873779297,
#             "entity": {
#                 "color_coord": [
#                     0,
#                     19,
#                     0,
#                     26
#                 ],
#                 "id": 862,
#                 "color": "brown",
#                 "color_tag": 1787
#             }
#         }
#     ]
# ]



res = client.query(
    collection_name="test_collection",
    filter="color_coord[0] in [7, 8, 9]",
    output_fields=["id", "color", "color_tag", "color_coord"],
    limit=3
)

print(res)

# Output
#
# [
#     {
#         "id": 5,
#         "color": "blue",
#         "color_tag": 9643,
#         "color_coord": [
#             8,
#             20,
#             20,
#             11
#         ]
#     },
#     {
#         "id": 15,
#         "color": "purple",
#         "color_tag": 6379,
#         "color_coord": [
#             8,
#             27,
#             16,
#             3,
#             16
#         ]
#     },
#     {
#         "id": 20,
#         "color": "pink",
#         "color_tag": 4800,
#         "color_coord": [
#             9,
#             35,
#             14,
#             20
#         ]
#     }
# ]



# 5. Advanced search within the array field

res = client.query(
    collection_name="test_collection",
    filter="ARRAY_CONTAINS(color_coord, 10)",
    output_fields=["id", "color", "color_tag", "color_coord"],
    limit=3
)

print(res)

# Output
#
# [
#     {
#         "id": 21,
#         "color": "white",
#         "color_tag": 4202,
#         "color_coord": [
#             10,
#             5,
#             5
#         ]
#     },
#     {
#         "id": 31,
#         "color": "grey",
#         "color_tag": 7386,
#         "color_coord": [
#             8,
#             10,
#             23,
#             7,
#             31
#         ]
#     },
#     {
#         "id": 45,
#         "color": "purple",
#         "color_tag": 6126,
#         "color_coord": [
#             0,
#             10,
#             24
#         ]
#     }
# ]



res = client.query(
    collection_name="test_collection",
    filter="ARRAY_CONTAINS_ALL(color_coord, [7, 8])",
    output_fields=["id", "color", "color_tag", "color_coord"],
    limit=3
)

print(res)

# Output
#
# [
#     {
#         "color": "grey",
#         "color_tag": 7386,
#         "color_coord": [
#             8,
#             10,
#             23,
#             7,
#             31
#         ],
#         "id": 31
#     },
#     {
#         "color": "purple",
#         "color_tag": 7823,
#         "color_coord": [
#             38,
#             8,
#             36,
#             38,
#             7
#         ],
#         "id": 258
#     },
#     {
#         "color": "purple",
#         "color_tag": 6356,
#         "color_coord": [
#             34,
#             32,
#             11,
#             8,
#             7
#         ],
#         "id": 348
#     }
# ]



res = client.query(
    collection_name="test_collection",
    filter="ARRAY_CONTAINS_ANY(color_coord, [7, 8, 9])",
    output_fields=["id", "color", "color_tag", "color_coord"],
    limit=3
)

print(res)

# Output
#
# [
#     {
#         "id": 0,
#         "color": "green",
#         "color_tag": 9212,
#         "color_coord": [
#             37,
#             36,
#             36,
#             7,
#             9
#         ]
#     },
#     {
#         "id": 5,
#         "color": "blue",
#         "color_tag": 9643,
#         "color_coord": [
#             8,
#             20,
#             20,
#             11
#         ]
#     },
#     {
#         "id": 12,
#         "color": "blue",
#         "color_tag": 3075,
#         "color_coord": [
#             29,
#             7,
#             17
#         ]
#     }
# ]



res = client.query(
    collection_name="test_collection",
    filter="ARRAY_LENGTH(color_coord) == 4",
    output_fields=["id", "color", "color_tag", "color_coord"],
    limit=3
)

print(res)

# Output
#
# [
#     {
#         "id": 1,
#         "color": "pink",
#         "color_tag": 6708,
#         "color_coord": [
#             15,
#             36,
#             38,
#             2
#         ]
#     },
#     {
#         "id": 4,
#         "color": "green",
#         "color_tag": 5386,
#         "color_coord": [
#             13,
#             32,
#             35,
#             5
#         ]
#     },
#     {
#         "id": 5,
#         "color": "blue",
#         "color_tag": 9643,
#         "color_coord": [
#             8,
#             20,
#             20,
#             11
#         ]
#     }
# ]



client.drop_collection(
    collection_name="test_collection"
)