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
    enable_dynamic_field=True,
)

schema.add_field(field_name="id", datatype=DataType.INT64, is_primary=True)
schema.add_field(field_name="vector", datatype=DataType.FLOAT_VECTOR, dim=5)

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
#         -0.13724213100096905,
#         0.5142203011384239,
#         0.6777028315741731,
#         -0.006594280664368624,
#         -0.38510188233882015
#     ],
#     "color": "pink",
#     "tag": 3957,
#     "color_tag": "pink_3957"
# }



res = client.insert(
    collection_name="test_collection",
    data=data,
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

# 4. Search with dynamic fields
query_vectors = [[random.uniform(-1, 1) for _ in range(5)]]

res = client.search(
    collection_name="test_collection",
    data=query_vectors,
    filter="color in [\"red\", \"green\"]",
    search_params={"metric_type": "L2", "params": {"nprobe": 10}},
    output_fields=["id", "color_tag"],
    limit=3
)

print(res)

# Output
#
# [
#     [
#         {
#             "id": 863,
#             "distance": 0.188413605093956,
#             "entity": {
#                 "id": 863,
#                 "color_tag": "red_2371"
#             }
#         },
#         {
#             "id": 799,
#             "distance": 0.29188022017478943,
#             "entity": {
#                 "id": 799,
#                 "color_tag": "red_2235"
#             }
#         },
#         {
#             "id": 564,
#             "distance": 0.3492690920829773,
#             "entity": {
#                 "id": 564,
#                 "color_tag": "red_9186"
#             }
#         }
#     ]
# ]



# 5. Drop the collection

client.drop_collection(
    collection_name="test_collection"
)