import random, time, json
from pymilvus import connections, MilvusClient, Collection

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

for i in range(10000):
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
#         -0.5705990742218152,
#         0.39844925120642083,
#         -0.8791287928610869,
#         0.024163154953680932,
#         0.6837669917169638
#     ],
#     "color": "purple",
#     "tag": 7774,
#     "color_tag": "purple_7774"
# }



res = client.insert(
    collection_name="quick_setup",
    data=data,
)

print(res)

# Output
#
# {
#     "insert_count": 10000,
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
#         "(9990 more items hidden)"
#     ]
# }



time.sleep(10)

# 4. Search with iterator
connections.connect(uri=CLUSTER_ENDPOINT, token=TOKEN)
collection = Collection("quick_setup")

query_vectors = [[0.3580376395471989, -0.6023495712049978, 0.18414012509913835, -0.26286205330961354, 0.9029438446296592]]
search_params = {
    "metric_type": "IP",
    "params": {"nprobe": 10}
}

iterator = collection.search_iterator(
    data=query_vectors,
    anns_field="vector",
    batch_size=10,
    param=search_params,
    output_fields=["color_tag"]
)

results = []

while True:
    result = iterator.next()
    if not result:
        iterator.close()
        break
    
    for hit in result:
        results.append(hit.to_dict())

# 5. Check the results
print(f"There are {len(results)} entities found. The first 3 are as follows:")

# Output
#
# There are 10000 entities found. The first 3 are as follows:



print(results[:3])

# Output
#
# [
#     {
#         "id": 1756,
#         "distance": 2.0642056465148926,
#         "entity": {
#             "color_tag": "black_9109"
#         }
#     },
#     {
#         "id": 6488,
#         "distance": 1.9437453746795654,
#         "entity": {
#             "color_tag": "purple_8164"
#         }
#     },
#     {
#         "id": 3338,
#         "distance": 1.9107104539871216,
#         "entity": {
#             "color_tag": "brown_8121"
#         }
#     }
# ]



# 6. Query with iterator
iterator = collection.query_iterator(
    batch_size=10,
    expr="color_tag like \"brown_8%\"",
    output_fields=["color_tag"]
)

results = []

while True:
    result = iterator.next()
    if not result:
        iterator.close()
        break

    results += result

# 7. Check the results
print(f"There are {len(results)} entities found. The first 3 are as follows:")

# Output
#
# There are 99 entities found. The first 3 are as follows:



print(results[:3])

# Output
#
# [
#     {
#         "color_tag": "brown_8785",
#         "id": 94
#     },
#     {
#         "color_tag": "brown_8568",
#         "id": 176
#     },
#     {
#         "color_tag": "brown_8721",
#         "id": 289
#     }
# ]



# 8. Drop the collection
client.drop_collection(collection_name="quick_setup")