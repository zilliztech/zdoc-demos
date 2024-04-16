from pymilvus import MilvusClient

CLUSTER_ENDPOINT = "YOUR_CLUSTER_ENDPOINT"
TOKEN = "YOUR_CLUSTER_TOKEN"

# 1. Set up a Milvus client
client = MilvusClient(
    uri=CLUSTER_ENDPOINT,
    token=TOKEN 
)

# 2. List roles and users
roles = client.list_roles()

print(roles)

# Output
#
# ["db_admin", "db_ro", "db_rw"]



users = client.list_users()

print(users)

# Output
#
# ["db_admin"]



# 3. Create a user

if 'user1' not in users:
    client.create_user(
        user_name="user1",
        password="p@ssw0rd!"
    )

users = client.list_users()

print(users)

# Output
#
# ["db_admin", "user1"]



# 4. Update a user credentials

client.update_password(
    user_name="user1",
    old_password="p@ssw0rd!",
    new_password="p@ssw0rd123!"
)

# 5. Describe the role
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
#             "privilege": "GetLoadState"
#         },
#         {
#             "object_type": "Collection",
#             "object_name": "*",
#             "db_name": "default",
#             "role_name": "db_ro",
#             "privilege": "GetLoadingProgress"
#         },
#         {
#             "object_type": "Collection",
#             "object_name": "*",
#             "db_name": "default",
#             "role_name": "db_ro",
#             "privilege": "HasPartition"
#         },
#         {
#             "object_type": "Collection",
#             "object_name": "*",
#             "db_name": "default",
#             "role_name": "db_ro",
#             "privilege": "IndexDetail"
#         },
#         {
#             "object_type": "Collection",
#             "object_name": "*",
#             "db_name": "default",
#             "role_name": "db_ro",
#             "privilege": "Load"
#         },
#         {
#             "object_type": "Collection",
#             "object_name": "*",
#             "db_name": "default",
#             "role_name": "db_ro",
#             "privilege": "Query"
#         },
#         {
#             "object_type": "Collection",
#             "object_name": "*",
#             "db_name": "default",
#             "role_name": "db_ro",
#             "privilege": "Search"
#         },
#         {
#             "object_type": "Collection",
#             "object_name": "*",
#             "db_name": "default",
#             "role_name": "db_ro",
#             "privilege": "ShowPartitions"
#         },
#         {
#             "object_type": "Global",
#             "object_name": "*",
#             "db_name": "default",
#             "role_name": "db_ro",
#             "privilege": "DescribeAlias"
#         },
#         {
#             "object_type": "Global",
#             "object_name": "*",
#             "db_name": "default",
#             "role_name": "db_ro",
#             "privilege": "DescribeCollection"
#         },
#         "(3 more items hidden)"
#     ]
# }



# 6. Assign a role to a user

client.grant_role(
    user_name="user1",
    role_name="db_ro"
)

# 7. Describe a user

user_info = client.describe_user(
    user_name="user1"
)

print(user_info)

# Output
#
# {
#     "user_name": "user1",
#     "roles": "(\"db_ro\")"
# }



# 8. Revoke a role from a user

client.revoke_role(
    user_name="user1",
    role_name="db_ro"
)

# 9. Drop a user

client.drop_user(
    user_name="user1"
)