import json, os, time
from pymilvus import connections, Role, FieldSchema, CollectionSchema, DataType, Collection, utility

CLUSTER_ENDPOINT="http://localhost:19530" # Set your cluster endpoint
TOKEN="root:Milvus" # Set your token
COLLECTION_NAME="medium_articles_2020" # Set your collection name
DATASET_PATH="{}/../medium_articles_2020_dpr.json".format(os.path.dirname(__file__)) # Set your dataset path

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

userInfo = utility.list_users(include_role_info=True)

print([ u.username for u in userInfo.groups ])
print([ u.roles for u in userInfo.groups ])

# 4. List roles

roleInfo = utility.list_roles(include_user_info=True)

print([ g.role_name for g in roleInfo.groups ])
print([ g.users for g in roleInfo.groups])

# 5. Create role

role = Role('role1')
role.create()

# 6. Grant role
print(role.add_user('user1'))

# 7. Revoke role
print(role.remove_user('user1'))

# 8. Get users

print(role.get_users())

# 8. Delete role
print(role.drop())

# 9. Delete user
utility.delete_user('user1')

