from pymilvus import MilvusClient

CLUSTER_ENDPOINT = "YOUR_CLUSTER_ENDPOINT"
TOKEN = "YOUR_CLUSTER_TOKEN"

# 1. Set up a Milvus client
client = MilvusClient(
    uri=CLUSTER_ENDPOINT,
    token=TOKEN 
)

# 2. Describe the role
res = client.describe_role(role_name="db_ro")

print(res)

# Output
#
# {
#     "role": "db_ro",
#     "privileges": [
#         {
#             "object_type": "Collection",
#             "object_name": "*",
#             "db_name": "default",
#             "role_name": "db_ro",
#             "privilege": "GetLoadState",
#             "grantor_name": "zcloud_root"
#         },
#         {
#             "object_type": "Collection",
#             "object_name": "*",
#             "db_name": "default",
#             "role_name": "db_ro",
#             "privilege": "GetLoadingProgress",
#             "grantor_name": "zcloud_root"
#         },
#         {
#             "object_type": "Collection",
#             "object_name": "*",
#             "db_name": "default",
#             "role_name": "db_ro",
#             "privilege": "IndexDetail",
#             "grantor_name": "zcloud_root"
#         },
#         {
#             "object_type": "Collection",
#             "object_name": "*",
#             "db_name": "default",
#             "role_name": "db_ro",
#             "privilege": "Load",
#             "grantor_name": "zcloud_root"
#         },
#         {
#             "object_type": "Collection",
#             "object_name": "*",
#             "db_name": "default",
#             "role_name": "db_ro",
#             "privilege": "Query",
#             "grantor_name": "zcloud_root"
#         },
#         {
#             "object_type": "Collection",
#             "object_name": "*",
#             "db_name": "default",
#             "role_name": "db_ro",
#             "privilege": "Search",
#             "grantor_name": "zcloud_root"
#         },
#         {
#             "object_type": "Global",
#             "object_name": "*",
#             "db_name": "default",
#             "role_name": "db_ro",
#             "privilege": "DescribeCollection",
#             "grantor_name": "zcloud_root"
#         },
#         {
#             "object_type": "Global",
#             "object_name": "*",
#             "db_name": "default",
#             "role_name": "db_ro",
#             "privilege": "ListDatabases",
#             "grantor_name": "zcloud_root"
#         },
#         {
#             "object_type": "Global",
#             "object_name": "*",
#             "db_name": "default",
#             "role_name": "db_ro",
#             "privilege": "ShowCollections",
#             "grantor_name": "zcloud_root"
#         }
#     ]
# }


