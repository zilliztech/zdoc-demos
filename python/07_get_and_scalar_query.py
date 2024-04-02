import random, time, json
from pymilvus import MilvusClient

CLUSTER_ENDPOINT = "YOUR_CLUSTER_ENDPOINT"
TOKEN = "YOUR_CLUSTER_TOKEN"

# 1. Set up a Milvus client
client = MilvusClient(
    uri=CLUSTER_ENDPOINT,
    token=TOKEN 
)

# 2. Create a collection
client.create_collection(
    collection_name="quick_setup",
    dimension=5,
)

# 3. Insert randomly generated vectors 
colors = ["green", "blue", "yellow", "red", "black", "white", "purple", "pink", "orange", "brown", "grey"]
data = []

for i in range(1000):
    current_color = random.choice(colors)
    current_tag = random.randint(1000, 9999)
    data.append({
        "id": i,
        "vector": [ random.uniform(-1, 1) for _ in range(5) ],
        "color": current_color,
        "tag": current_tag,
        "color_tag": f"{current_color}_{str(current_tag)}"
    })

print(data[0])

# Output
#
# {
#     "id": 0,
#     "vector": [
#         0.7371107800002366,
#         -0.7290389773227746,
#         0.38367002049157417,
#         0.36996000494220627,
#         -0.3641898951462792
#     ],
#     "color": "yellow",
#     "tag": 6781,
#     "color_tag": "yellow_6781"
# }



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

# 4. Create partitions and insert more entities
client.create_partition(
    collection_name="quick_setup",
    partition_name="partitionA"
)

client.create_partition(
    collection_name="quick_setup",
    partition_name="partitionB"
)

data = []

for i in range(1000, 1500):
    current_color = random.choice(colors)
    data.append({
        "id": i,
        "vector": [ random.uniform(-1, 1) for _ in range(5) ],
        "color": current_color,
        "tag": current_tag,
        "color_tag": f"{current_color}_{str(current_tag)}"
    })

res = client.insert(
    collection_name="quick_setup",
    data=data,
    partition_name="partitionA"
)

print(res)

# Output
#
# {
#     "insert_count": 500,
#     "ids": [
#         1000,
#         1001,
#         1002,
#         1003,
#         1004,
#         1005,
#         1006,
#         1007,
#         1008,
#         1009,
#         "(490 more items hidden)"
#     ]
# }



data = []

for i in range(1500, 2000):
    current_color = random.choice(colors)
    data.append({
        "id": i,
        "vector": [ random.uniform(-1, 1) for _ in range(5) ],
        "color": current_color,
        "tag": current_tag,
        "color_tag": f"{current_color}_{str(current_tag)}"
    })

res = client.insert(
    collection_name="quick_setup",
    data=data,
    partition_name="partitionB"
)

print(res)

# Output
#
# {
#     "insert_count": 500,
#     "ids": [
#         1500,
#         1501,
#         1502,
#         1503,
#         1504,
#         1505,
#         1506,
#         1507,
#         1508,
#         1509,
#         "(490 more items hidden)"
#     ]
# }



time.sleep(5)

# 5. Get entities by ID
res = client.get(
    collection_name="quick_setup",
    ids=[0, 1, 2]
)

print(res)

# Output
#
# [
#     {
#         "id": 0,
#         "vector": [
#             0.7371108,
#             -0.72903895,
#             0.38367003,
#             0.36996,
#             -0.3641899
#         ],
#         "color": "yellow",
#         "tag": 6781,
#         "color_tag": "yellow_6781"
#     },
#     {
#         "id": 1,
#         "vector": [
#             -0.10924426,
#             -0.7659806,
#             0.8613359,
#             0.65219676,
#             -0.06385158
#         ],
#         "color": "pink",
#         "tag": 1023,
#         "color_tag": "pink_1023"
#     },
#     {
#         "id": 2,
#         "vector": [
#             0.402096,
#             -0.74742633,
#             -0.901683,
#             0.6292514,
#             0.77286446
#         ],
#         "color": "blue",
#         "tag": 3972,
#         "color_tag": "blue_3972"
#     }
# ]



# 5. Get entities from partitions
res = client.get(
    collection_name="quick_setup",
    ids=[1000, 1001, 1002],
    partition_names=["partitionA"]
)

print(res)

# Output
#
# [
#     {
#         "color": "green",
#         "tag": 1995,
#         "color_tag": "green_1995",
#         "id": 1000,
#         "vector": [
#             0.7807706,
#             0.8083741,
#             0.17276904,
#             -0.8580777,
#             0.024156934
#         ]
#     },
#     {
#         "color": "red",
#         "tag": 1995,
#         "color_tag": "red_1995",
#         "id": 1001,
#         "vector": [
#             0.065074645,
#             -0.44882354,
#             -0.29479212,
#             -0.19798489,
#             -0.77542555
#         ]
#     },
#     {
#         "color": "green",
#         "tag": 1995,
#         "color_tag": "green_1995",
#         "id": 1002,
#         "vector": [
#             0.027934508,
#             -0.44199976,
#             -0.40262738,
#             -0.041511405,
#             0.024782438
#         ]
#     }
# ]



# 6. Use basic operators

res = client.query(
    collection_name="quick_setup",
    # highlight-start
    filter="1000 < tag < 1500",
    output_fields=["color_tag"],
    # highlight-end
    limit=3
)

print(res)

# Output
#
# [
#     {
#         "id": 1,
#         "color_tag": "pink_1023"
#     },
#     {
#         "id": 41,
#         "color_tag": "red_1483"
#     },
#     {
#         "id": 44,
#         "color_tag": "grey_1146"
#     }
# ]



res = client.query(
    collection_name="quick_setup",
    # highlight-start
    filter='color == "brown"',
    output_fields=["color_tag"],
    # highlight-end
    limit=3
)

print(res)

# Output
#
# [
#     {
#         "color_tag": "brown_5343",
#         "id": 15
#     },
#     {
#         "color_tag": "brown_3167",
#         "id": 27
#     },
#     {
#         "color_tag": "brown_3100",
#         "id": 30
#     }
# ]



res = client.query(
    collection_name="quick_setup",
    # highlight-start
    filter='color not in ["green", "purple"]',
    output_fields=["color_tag"],
    # highlight-end
    limit=3
)

print(res)

# Output
#
# [
#     {
#         "color_tag": "yellow_6781",
#         "id": 0
#     },
#     {
#         "color_tag": "pink_1023",
#         "id": 1
#     },
#     {
#         "color_tag": "blue_3972",
#         "id": 2
#     }
# ]



res = client.query(
    collection_name="quick_setup",
    # highlight-start
    filter='color_tag like "red%"',
    output_fields=["color_tag"],
    # highlight-end
    limit=3
)

print(res)

# Output
#
# [
#     {
#         "color_tag": "red_6443",
#         "id": 17
#     },
#     {
#         "color_tag": "red_1483",
#         "id": 41
#     },
#     {
#         "color_tag": "red_4348",
#         "id": 47
#     }
# ]



res = client.query(
    collection_name="quick_setup",
    # highlight-start
    filter='(color == "red") and (1000 < tag < 1500)',
    output_fields=["color_tag"],
    # highlight-end
    limit=3
)

print(res)

# Output
#
# [
#     {
#         "color_tag": "red_1483",
#         "id": 41
#     },
#     {
#         "color_tag": "red_1100",
#         "id": 94
#     },
#     {
#         "color_tag": "red_1343",
#         "id": 526
#     }
# ]



# 7. Use advanced operators

# Count the total number of entities in a collection
res = client.query(
    collection_name="quick_setup",
    # highlight-start
    output_fields=["count(*)"]
    # highlight-end
)

print(res)

# Output
#
# [
#     {
#         "count(*)": 2000
#     }
# ]



# Count the number of entities in a partition
res = client.query(
    collection_name="quick_setup",
    # highlight-start
    output_fields=["count(*)"],
    partition_names=["partitionA"]
    # highlight-end
)

print(res)

# Output
#
# [
#     {
#         "count(*)": 500
#     }
# ]



# Count the number of entities that match a specific filter
res = client.query(
    collection_name="quick_setup",
    # highlight-start
    filter='(color == "red") and (1000 < tag < 1500)',
    output_fields=["count(*)"],
    # highlight-end
)

print(res)

# Output
#
# [
#     {
#         "count(*)": 3
#     }
# ]



# 8. Drop the collection
client.drop_collection(
    collection_name="quick_setup"
)