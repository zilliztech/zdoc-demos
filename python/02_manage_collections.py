import random, time
from pymilvus import MilvusClient, DataType

CLUSTER_ENDPOINT = "YOUR_CLUSTER_ENDPOINT"
TOKEN = "YOUR_CLUSTER_TOKEN"

# 1. Set up a Milvus client
client = MilvusClient(
    uri=CLUSTER_ENDPOINT,
    token=TOKEN 
)

# 2. Create a collection in quick setup mode
client.create_collection(
    collection_name="quick_setup",
    dimension=5
)

res = client.get_load_state(
    collection_name="quick_setup"
)

print(res)

# Output
#
# {
#     "state": "<LoadState: Loaded>"
# }



# 3. Create a collection in customized setup mode

# 3.1. Create schema
schema = MilvusClient.create_schema(
    auto_id=False,
    enable_dynamic_field=True,
)

# 3.2. Add fields to schema
schema.add_field(field_name="my_id", datatype=DataType.INT64, is_primary=True)
schema.add_field(field_name="my_vector", datatype=DataType.FLOAT_VECTOR, dim=5)

# 3.3. Prepare index parameters
index_params = client.prepare_index_params()

# 3.4. Add indexes
index_params.add_index(
    field_name="my_id",
    index_type="STL_SORT"
)

index_params.add_index(
    field_name="my_vector", 
    index_type="IVF_FLAT",
    metric_type="IP",
    params={ "nlist": 128 }
)

# 3.5. Create a collection with the index loaded simultaneously
client.create_collection(
    collection_name="customized_setup_1",
    schema=schema,
    index_params=index_params
)

time.sleep(5)

res = client.get_load_state(
    collection_name="customized_setup_1"
)

print(res)

# Output
#
# {
#     "state": "<LoadState: Loaded>"
# }



# 3.6. Create a collection and index it separately
client.create_collection(
    collection_name="customized_setup_2",
    schema=schema,
)

res = client.get_load_state(
    collection_name="customized_setup_2"
)

print(res)

# Output
#
# {
#     "state": "<LoadState: NotLoad>"
# }



# 3.6 Create index
client.create_index(
    collection_name="customized_setup_2",
    index_params=index_params
)

res = client.get_load_state(
    collection_name="customized_setup_2"
)

print(res)

# Output
#
# {
#     "state": "<LoadState: NotLoad>"
# }



# 5. View Collections
res = client.describe_collection(
    collection_name="customized_setup_2"
)

print(res)

# Output
#
# {
#     "collection_name": "customized_setup_2",
#     "auto_id": false,
#     "num_shards": 1,
#     "description": "",
#     "fields": [
#         {
#             "field_id": 100,
#             "name": "my_id",
#             "description": "",
#             "type": 5,
#             "params": {},
#             "element_type": 0,
#             "is_primary": true
#         },
#         {
#             "field_id": 101,
#             "name": "my_vector",
#             "description": "",
#             "type": 101,
#             "params": {
#                 "dim": 5
#             },
#             "element_type": 0
#         }
#     ],
#     "aliases": [],
#     "collection_id": 448143479230158446,
#     "consistency_level": 2,
#     "properties": {},
#     "num_partitions": 1,
#     "enable_dynamic_field": true
# }



# 6. List all collection names
res = client.list_collections()

print(res)

# Output
#
# [
#     "customized_setup_2",
#     "quick_setup",
#     "customized_setup_1"
# ]



# 7. Load the collection
client.load_collection(
    collection_name="customized_setup_2"
)

res = client.get_load_state(
    collection_name="customized_setup_2"
)

print(res)

# Output
#
# {
#     "state": "<LoadState: Loaded>"
# }



# 8. Release the collection
client.release_collection(
    collection_name="customized_setup_2"
)

res = client.get_load_state(
    collection_name="customized_setup_2"
)

print(res)

# Output
#
# {
#     "state": "<LoadState: NotLoad>"
# }



# 9. Manage aliases
# 9.1. Create aliases
client.create_alias(
    collection_name="customized_setup_2",
    alias="bob"
)

client.create_alias(
    collection_name="customized_setup_2",
    alias="alice"
)

# 9.2. List aliases
res = client.list_aliases(
    collection_name="customized_setup_2"
)

print(res)

# Output
#
# {
#     "aliases": [
#         "bob",
#         "alice"
#     ],
#     "collection_name": "customized_setup_2",
#     "db_name": "default"
# }



# 9.3. Describe aliases
res = client.describe_alias(
    alias="bob"
)

print(res)

# Output
#
# {
#     "alias": "bob",
#     "collection_name": "customized_setup_2",
#     "db_name": "default"
# }



# 9.4 Reassign aliases to other collections
client.alter_alias(
    collection_name="customized_setup_1",
    alias="alice"
)

res = client.list_aliases(
    collection_name="customized_setup_1"
)

print(res)

# Output
#
# {
#     "aliases": [
#         "alice"
#     ],
#     "collection_name": "customized_setup_1",
#     "db_name": "default"
# }



res = client.list_aliases(
    collection_name="customized_setup_2"
)

print(res)

# Output
#
# {
#     "aliases": [
#         "bob"
#     ],
#     "collection_name": "customized_setup_2",
#     "db_name": "default"
# }



# 9.5 Drop aliases
client.drop_alias(
    alias="bob"
)

client.drop_alias(
    alias="alice"
)

# 10. Drop the collections
client.drop_collection(
    collection_name="quick_setup"
)

client.drop_collection(
    collection_name="customized_setup_1"
)

client.drop_collection(
    collection_name="customized_setup_2"
)