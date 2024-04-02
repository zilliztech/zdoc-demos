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
#         -0.34290103559841256,
#         0.5690697802345119,
#         -0.1387570718036084,
#         0.19907356904377238,
#         0.7140011532472272
#     ],
#     "color": "grey",
#     "tag": 2550,
#     "color_tag": "grey_2550"
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
    limit=3
)

print(res)

# Output
#
# [
#     [
#         {
#             "id": 986,
#             "distance": 0.439428448677063,
#             "entity": {}
#         },
#         {
#             "id": 713,
#             "distance": 0.5107282400131226,
#             "entity": {}
#         },
#         {
#             "id": 160,
#             "distance": 0.5383791327476501,
#             "entity": {}
#         }
#     ]
# ]

# 5. Drop the collection

client.drop_collection(
    collection_name="test_collection"
)