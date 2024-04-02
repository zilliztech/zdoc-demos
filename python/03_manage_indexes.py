import random, time
from pymilvus import MilvusClient, DataType

CLUSTER_ENDPOINT = "YOUR_CLUSTER_ENDPOINT"
TOKEN = "YOUR_CLUSTER_TOKEN"

# 1. Set up a Milvus client
client = MilvusClient(
    uri=CLUSTER_ENDPOINT,
    token=TOKEN 
)

# 2. Create schema
# 2.1. Create schema
schema = MilvusClient.create_schema(
    auto_id=False,
    enable_dynamic_field=True,
)

# 2.2. Add fields to schema
schema.add_field(field_name="id", datatype=DataType.INT64, is_primary=True)
schema.add_field(field_name="vector", datatype=DataType.FLOAT_VECTOR, dim=5)

# 3. Create collection
client.create_collection(
    collection_name="customized_setup", 
    schema=schema, 
)

# 4. Set up index
# 4.1. Set up the index parameters
index_params = MilvusClient.prepare_index_params()

# 4.2. Add an index on the vector field.
index_params.add_index(
    field_name="vector",
    metric_type="COSINE",
    index_type="AUTOINDEX",
    index_name="vector_index"
)

# 4.3. Add an index on a scalar field.
index_params.add_index(
    field_name="id",
    index_name="primary_field_index"
)

# 4.4. Create an index file
client.create_index(
    collection_name="customized_setup",
    index_params=index_params
)

# 5. Describe index
res = client.list_indexes(
    collection_name="customized_setup"
)

print(res)

# Output
#
# [
#     "vector_index",
#     "primary_field_index"
# ]



res = client.describe_index(
    collection_name="customized_setup",
    index_name="primary_field_index"
)

print(res)

# Output
#
# {
#     "field_name": "id",
#     "index_name": "primary_field_index"
# }



res = client.describe_index(
    collection_name="customized_setup",
    index_name="vector_index"
)

print(res)

# Output
#
# {
#     "index_type": "AUTOINDEX",
#     "metric_type": "COSINE",
#     "field_name": "vector",
#     "index_name": "vector_index"
# }



# 6. Drop index
client.drop_index(
    collection_name="customized_setup",
    index_name="vector_index"
)

client.drop_index(
    collection_name="customized_setup",
    index_name="primary_field_index"
)

# 7. Drop collection
client.drop_collection(
    collection_name="customized_setup",
)