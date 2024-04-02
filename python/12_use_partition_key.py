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
    partition_key_field="color",

)

schema.add_field(field_name="id", datatype=DataType.INT64, is_primary=True)
schema.add_field(field_name="vector", datatype=DataType.FLOAT_VECTOR, dim=5)
schema.add_field(field_name="color", datatype=DataType.VARCHAR, max_length=512)

index_params = MilvusClient.prepare_index_params()

index_params.add_index(
    field_name="id",
    index_type="STL_SORT"
)

index_params.add_index(
    field_name="color",
    index_type="Trie"
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



# 2.1. List all partitions in the collection
partition_names = client.list_partitions(
    collection_name="test_collection"
)

print(partition_names)

# Output
#
# [
#     "_default_0",
#     "_default_1",
#     "_default_2",
#     "_default_3",
#     "_default_4",
#     "_default_5",
#     "_default_6",
#     "_default_7",
#     "_default_8",
#     "_default_9",
#     "(54 more items hidden)"
# ]



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
#         0.057366062744859336,
#         -0.6706579549433886,
#         0.9590931577865847,
#         -0.8454665244821529,
#         0.909587783588482
#     ],
#     "color": "white",
#     "tag": 5236,
#     "color_tag": "white_5236"
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

# 4. Search with partition key
query_vectors = [ [random.uniform(-1, 1) for _ in range(5)] ]

res = client.search(
    collection_name="test_collection",
    data=query_vectors,
    filter="color == 'green'",
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
#             "id": 970,
#             "distance": 0.5770174264907837,
#             "entity": {
#                 "id": 970,
#                 "color_tag": "green_9828"
#             }
#         },
#         {
#             "id": 115,
#             "distance": 0.6898155808448792,
#             "entity": {
#                 "id": 115,
#                 "color_tag": "green_4073"
#             }
#         },
#         {
#             "id": 899,
#             "distance": 0.7028976678848267,
#             "entity": {
#                 "id": 899,
#                 "color_tag": "green_9897"
#             }
#         }
#     ]
# ]



# 5. Drop the collection
client.drop_collection(
    collection_name="test_collection"
)