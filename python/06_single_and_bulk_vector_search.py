import random, time, json
from pymilvus import connections, MilvusClient, utility

CLUSTER_ENDPOINT = "YOUR_CLUSTER_ENDPOINT"
TOKEN = "YOUR_CLUSTER_TOKEN"

# 0. Get server version
connections.connect(uri=CLUSTER_ENDPOINT, token=TOKEN)
version = utility.get_server_version()

print(version)

# Output
#
# v2.3.10



# 1. Set up a Milvus client
client = MilvusClient(
    uri=CLUSTER_ENDPOINT,
    token=TOKEN 
)

# 2. Create a collection
client.create_collection(
    collection_name="quick_setup",
    dimension=5,
    metric_type="IP"
)

# 3. Insert randomly generated vectors 
colors = ["green", "blue", "yellow", "red", "black", "white", "purple", "pink", "orange", "brown", "grey"]
data = []

for i in range(1000):
    current_color = random.choice(colors)
    data.append({
        "id": i,
        "vector": [ random.uniform(-1, 1) for _ in range(5) ],
        "color": current_color,
        "color_tag": f"{current_color}_{str(random.randint(1000, 9999))}"
    })

res = client.insert(
    collection_name="quick_setup",
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

# 4. Single vector search
query_vector = [random.uniform(-1, 1) for _ in range(5)]

res = client.search(
    collection_name="quick_setup",
    data=[query_vector],
    limit=5,
    search_params={"metric_type": "IP", "params": {"nprobe": 10}}
)

print(res)

# Output
#
# [
#     [
#         {
#             "id": 206,
#             "distance": 2.50616455078125,
#             "entity": {}
#         },
#         {
#             "id": 138,
#             "distance": 2.500145435333252,
#             "entity": {}
#         },
#         {
#             "id": 224,
#             "distance": 2.484044313430786,
#             "entity": {}
#         },
#         {
#             "id": 117,
#             "distance": 2.4002010822296143,
#             "entity": {}
#         },
#         {
#             "id": 934,
#             "distance": 2.3309381008148193,
#             "entity": {}
#         }
#     ]
# ]



# 5. Batch-vector search
query_vectors = [[random.uniform(-1, 1) for _ in range(5)] for _ in range(2)]

res = client.search(
    collection_name="quick_setup",
    data=query_vectors,
    limit=2,
    search_params={"metric_type": "IP", "params": {"nprobe": 10}}
)

print(res)

# Output
#
# [
#     [
#         {
#             "id": 81,
#             "distance": 1.633650779724121,
#             "entity": {}
#         },
#         {
#             "id": 428,
#             "distance": 1.6174099445343018,
#             "entity": {}
#         }
#     ],
#     [
#         {
#             "id": 972,
#             "distance": 1.7308459281921387,
#             "entity": {}
#         },
#         {
#             "id": 545,
#             "distance": 1.670518398284912,
#             "entity": {}
#         }
#     ]
# ]



# 6. Search within a partition
# 6.1 Create partitions 
client.create_partition(
    collection_name="quick_setup",
    partition_name="red"
)

client.create_partition(
    collection_name="quick_setup",
    partition_name="blue"
)

# 6.1 Insert data into partitions
red_data = [ {"id": i, "vector": [ random.uniform(-1, 1) for _ in range(5) ], "color": "red", "color_tag": f"red_{str(random.randint(1000, 9999))}" } for i in range(500) ]
blue_data = [ {"id": i, "vector": [ random.uniform(-1, 1) for _ in range(5) ], "color": "blue", "color_tag": f"blue_{str(random.randint(1000, 9999))}" } for i in range(500) ]

res = client.insert(
    collection_name="quick_setup",
    data=red_data,
    partition_name="red"
)

print(res)

# Output
#
# {
#     "insert_count": 500,
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
#         "(490 more items hidden)"
#     ]
# }



res = client.insert(
    collection_name="quick_setup",
    data=blue_data,
    partition_name="blue"
)

print(res)

# Output
#
# {
#     "insert_count": 500,
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
#         "(490 more items hidden)"
#     ]
# }



time.sleep(5)

# 6.2 Search within a partition
query_vector = [random.uniform(-1, 1) for _ in range(5)]

res = client.search(
    collection_name="quick_setup",
    data=[query_vector],
    limit=5,
    search_params={"metric_type": "IP", "params": {"nprobe": 10}},
    partition_names=["red"]
)

print(res)

# Output
#
# [
#     [
#         {
#             "id": 320,
#             "distance": 1.243729591369629,
#             "entity": {}
#         },
#         {
#             "id": 200,
#             "distance": 1.2299367189407349,
#             "entity": {}
#         },
#         {
#             "id": 154,
#             "distance": 1.1562182903289795,
#             "entity": {}
#         },
#         {
#             "id": 29,
#             "distance": 1.1135238409042358,
#             "entity": {}
#         },
#         {
#             "id": 109,
#             "distance": 1.0907914638519287,
#             "entity": {}
#         }
#     ]
# ]



res = client.search(
    collection_name="quick_setup",
    data=[query_vector],
    limit=5,
    search_params={"metric_type": "IP", "params": {"nprobe": 10}},
    partition_names=["blue"]
)

print(res)

# Output
#
# [
#     [
#         {
#             "id": 59,
#             "distance": 1.3296087980270386,
#             "entity": {}
#         },
#         {
#             "id": 139,
#             "distance": 1.1872179508209229,
#             "entity": {}
#         },
#         {
#             "id": 201,
#             "distance": 1.1474100351333618,
#             "entity": {}
#         },
#         {
#             "id": 298,
#             "distance": 1.117565631866455,
#             "entity": {}
#         },
#         {
#             "id": 435,
#             "distance": 1.0910152196884155,
#             "entity": {}
#         }
#     ]
# ]



# 7. Search with output fields
query_vector = [random.uniform(-1, 1) for _ in range(5)]

res = client.search(
    collection_name="quick_setup",
    data=[query_vector],
    limit=5,
    search_params={"metric_type": "IP", "params": {"nprobe": 10}},
    output_fields=["color"]
)

print(res)

# Output
#
# [
#     [
#         {
#             "id": 29,
#             "distance": 2.6317718029022217,
#             "entity": {
#                 "color": "red"
#             }
#         },
#         {
#             "id": 405,
#             "distance": 2.6302318572998047,
#             "entity": {
#                 "color": "blue"
#             }
#         },
#         {
#             "id": 458,
#             "distance": 2.3892529010772705,
#             "entity": {
#                 "color": "green"
#             }
#         },
#         {
#             "id": 555,
#             "distance": 2.350921154022217,
#             "entity": {
#                 "color": "orange"
#             }
#         },
#         {
#             "id": 435,
#             "distance": 2.29063081741333,
#             "entity": {
#                 "color": "blue"
#             }
#         }
#     ]
# ]



# 8. Filtered search
# 8.1 Filter with "like" operator and prefix wildcard
query_vector = [random.uniform(-1, 1) for _ in range(5)]

res = client.search(
    collection_name="quick_setup",
    data=[query_vector],
    limit=5,
    search_params={"metric_type": "IP", "params": {"nprobe": 10}},
    filter='color_tag like "red%"',
    output_fields=["color_tag"]
)

print(res)

# Output
#
# [
#     [
#         {
#             "id": 58,
#             "distance": 1.4645483493804932,
#             "entity": {
#                 "color_tag": "red_8218"
#             }
#         },
#         {
#             "id": 307,
#             "distance": 1.4149816036224365,
#             "entity": {
#                 "color_tag": "red_3923"
#             }
#         },
#         {
#             "id": 16,
#             "distance": 1.3404488563537598,
#             "entity": {
#                 "color_tag": "red_9524"
#             }
#         },
#         {
#             "id": 142,
#             "distance": 1.31600022315979,
#             "entity": {
#                 "color_tag": "red_4160"
#             }
#         },
#         {
#             "id": 438,
#             "distance": 1.315270185470581,
#             "entity": {
#                 "color_tag": "red_8131"
#             }
#         }
#     ]
# ]



# 8.2 Filter with "like" operator and infix wildcard (Milvus 2.4.x or later)
if version.startswith("v2.4"):
    query_vector = [random.uniform(-1, 1) for _ in range(5)]

    res = client.search(
        collection_name="quick_setup",
        data=[query_vector],
        limit=5,
        search_params={"metric_type": "IP", "params": {"nprobe": 10}},
        filter='color_tag like "%_4%"'
    )

    
else:
    res = f"This demo is not supported in {version}"

print(res)

# Output
#
# This demo is not supported in v2.3.10



# 9. Range search
query_vector = [random.uniform(-1, 1) for _ in range(5)]

res = client.search(
    collection_name="quick_setup",
    data=[query_vector],
    limit=5,
    search_params={"metric_type": "IP", "params": {"nprobe": 10, "radius": 0.1, "range": 1.0}}
)

print(res)

# Output
#
# [
#     [
#         {
#             "id": 136,
#             "distance": 2.4410910606384277,
#             "entity": {}
#         },
#         {
#             "id": 897,
#             "distance": 2.2852015495300293,
#             "entity": {}
#         },
#         {
#             "id": 336,
#             "distance": 2.2819623947143555,
#             "entity": {}
#         },
#         {
#             "id": 50,
#             "distance": 2.2552754878997803,
#             "entity": {}
#         },
#         {
#             "id": 462,
#             "distance": 2.2343976497650146,
#             "entity": {}
#         }
#     ]
# ]



# 10. Grouping search (Milvus 2.4.x or later)
if version.startswith("v2.4"):
    query_vector = [random.uniform(-1, 1) for _ in range(5)]

    res = client.search(
        collection_name="quick_setup",
        data=[query_vector],
        search_params={"metric_type": "IP", "params": {"nprobe": 10}},
        limit=5,
        group_by_field=["color"],
        output_fields=["color", "color_tag"]
    )

else:
    res = f"This demo is not supported in {version}"

print(res)

# Output
#
# This demo is not supported in v2.3.10



# 11. Drop the collection
client.drop_collection(
    collection_name="quick_setup"
)