from pymilvus import MilvusClient

CLUSTER_ENDPOINT = "YOUR_CLUSTER_ENDPOINT"
TOKEN = "YOUR_CLUSTER_TOKEN"

# 1. Set up a Milvus client
client = MilvusClient(
    uri=CLUSTER_ENDPOINT,
    token=TOKEN 
)

# 2. Create a role
client.create_role(role_name="read_only")

# 3. Grant permissions
client.grant_privilege(
    role_name="read_only",
    object_type="Global",
    privilege="DescribeCollection",
    object_name="*"
)

# 3. Describe the role
res = client.describe_role(role_name="read_only")

print(res)

# Output
#
# {
#     "role": "read_only",
#     "privileges": [
#         {
#             "object_type": "Global",
#             "object_name": "*",
#             "db_name": "default",
#             "role_name": "read_only",
#             "privilege": "DescribeCollection",
#             "grantor_name": "root"
#         }
#     ]
# }



# 4. Revoke permissions
client.revoke_privilege(
    role_name="read_only",
    object_type="Global",
    privilege="DescribeCollection",
    object_name="*"
)

# 5. Drop the role
client.drop_role(role_name="read_only")