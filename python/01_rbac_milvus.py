from pymilvus import MilvusClient

CLUSTER_ENDPOINT = "YOUR_CLUSTER_ENDPOINT"
TOKEN = "YOUR_CLUSTER_TOKEN"

# 1. Set up a Milvus client
client = MilvusClient(
    uri=CLUSTER_ENDPOINT,
    token=TOKEN 
)

# 2. Create a role
res = client.list_roles()
print(res)

# Output
#
# ["admin", "public"]



client.create_role(role_name="read_only")

res = client.list_roles()
print(res)

# Output
#
# ["admin", "public", "read_only"]



# 3. Grant permissions
client.grant_privilege(
    role_name="read_only",
    object_type="Global",
    object_name="*",
    privilege="DescribeCollection"
)

# 4. Describe the role
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



# 5. Create a user
res = client.list_users()
print(res)

# Output
#
# ["root"]



client.create_user(
    user_name="alice",
    password="123456"
)

res = client.list_users()
print(res)

# Output
#
# ["alice", "root"]



# 6. Assign the role to the user
client.grant_role(
    user_name="alice",
    role_name="read_only"
)

# 7. Describe the user
res = client.describe_user(user_name="alice")

print(res)

# Output
#
# {
#     "user_name": "alice",
#     "roles": "(\"read_only\")"
# }



# 8. Revoke the role from the user
client.revoke_role(
    user_name="alice",
    role_name="read_only"
)

# 9. Drop the user
client.drop_user(user_name="alice")

# 10. Revoke permissions
client.revoke_privilege(
    role_name="read_only",
    object_type="Global",
    privilege="DescribeCollection",
    object_name="*"
)

# 11. Drop the role
client.drop_role(role_name="read_only")