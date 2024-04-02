import random, time
from pymilvus import MilvusClient, DataType

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

# 3. List partitions
res = client.list_partitions(collection_name="quick_setup")
print(res)

# Output
#
# ["_default"]



# 4. Create more partitions
client.create_partition(
    collection_name="quick_setup",
    partition_name="partitionA"
)

client.create_partition(
    collection_name="quick_setup",
    partition_name="partitionB"
)

res = client.list_partitions(collection_name="quick_setup")
print(res)

# Output
#
# ["_default", "partitionA", "partitionB"]



# 5. Check whether a partition exists
res = client.has_partition(
    collection_name="quick_setup",
    partition_name="partitionA"
)
print(res)

# Output
#
# True



res = client.has_partition(
    collection_name="quick_setup",
    partition_name="partitionC"
)
print(res)

# Output
#
# False



# 6. Load a partition independantly
client.release_collection(collection_name="quick_setup")

res = client.get_load_state(collection_name="quick_setup")
print(res)

# Output
#
# {
#     "state": "<LoadState: NotLoad>"
# }



client.load_partitions(
    collection_name="quick_setup",
    partition_names=["partitionA"]
)

res = client.get_load_state(collection_name="quick_setup")
print(res)

# Output
#
# {
#     "state": "<LoadState: Loaded>"
# }



res = client.get_load_state(collection_name="quick_setup", partition_name="partitionA")
print(res)

# Output
#
# {
#     "state": "<LoadState: Loaded>"
# }



res = client.get_load_state(collection_name="quick_setup", partition_name="partitionB")
print(res)

# Output
#
# {
#     "state": "<LoadState: NotLoad>"
# }



# 7. Release a partition
client.release_partitions(
    collection_name="quick_setup",
    partition_names=["partitionA"]
)

res = client.get_load_state(collection_name="quick_setup", partition_name="partitionA")
print(res)

# Output
#
# {
#     "state": "<LoadState: NotLoad>"
# }



# 8. Drop a partition
client.drop_partition(
    collection_name="quick_setup",
    partition_name="partitionB"
)

res = client.list_partitions(collection_name="quick_setup")
print(res)

# Output
#
# ["_default", "partitionA"]



# 9. Drop a collection
client.drop_collection(collection_name="quick_setup")