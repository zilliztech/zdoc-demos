import json, os, time
from pymilvus import connections, Role, utility

CLUSTER_ENDPOINT="YOUR_CLUSTER_ENDPOINT" # Set your cluster endpoint
TOKEN="YOUR_CLUSTER_TOKEN" # Set your token

connections.connect(
    alias='default', 
    #  Public endpoint obtained from Zilliz Cloud
    uri=CLUSTER_ENDPOINT,
    # API key or a colon-separated cluster username and password
    token=TOKEN, 
)

# 1. Create user

if not 'user1' in utility.list_usernames():
    utility.create_user(user='user1', password='P@ssw0rd!')

# 2. Update user credentials

utility.update_password(
    user='user1',
    old_password='P@ssw0rd!',
    new_password='P@ssw0rd!!'
)

# 3. List users

print(utility.list_usernames())

# Output
#
# ["db_admin", "user1"]



userInfo = utility.list_users(include_role_info=True)

users = [ { "user": u.username, "roles": list(u.roles) } for u in userInfo.groups ]

print(users)

# Output
#
# [
#     {
#         "user": "db_admin",
#         "roles": [
#             "db_admin"
#         ]
#     },
#     {
#         "user": "user1",
#         "roles": []
#     }
# ]



# 4. Assign role

role = Role("db_ro")

role.add_user("user1")

# 5. Get users of a specific role

users = list(role.get_users())

print(users)

# Output
#
# ["user1"]



# 6. List roles

roleInfo = utility.list_roles(include_user_info=True)

roles = [ { "role": g.role_name, "users": list(g.users) } for g in roleInfo.groups ]

print(roles)

# Output
#
# [
#     {
#         "role": "db_admin",
#         "users": [
#             "db_admin"
#         ]
#     },
#     {
#         "role": "db_ro",
#         "users": [
#             "user1"
#         ]
#     },
#     {
#         "role": "db_rw",
#         "users": []
#     }
# ]



# 7. Remove user from role

role.remove_user("user1")

# 8. Delete user
utility.delete_user('user1')