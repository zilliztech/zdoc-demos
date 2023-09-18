#!/usr/bin sh

PUBLIC_ENDPOINT="YOUR_CLUSTER_ENDPOINT"
TOKEN="YOUR_CLUSTER_TOKEN"


# Replace PUBLIC_ENDPOINT and TOKEN with your own
# - For a serverless cluster, use an API key as the token.
# - For a dedicated cluster, use the cluster credentials as the token
#   in the format of 'user:password'.
curl --location --request POST "${PUBLIC_ENDPOINT}/v1/vector/collections/create" \
    --header "Authorization: Bearer ${TOKEN}" \
    --header "Content-Type: application/json" \
    --header "Accept: */*" \
    --data '{
        "collectionName": "medium_articles_2020",
        "dimension": 768
    }'

# Replace PUBLIC_ENDPOINT and TOKEN with your own
curl --request GET \
    --url "${PUBLIC_ENDPOINT}/v1/vector/collections/describe?collectionName=medium_articles_2020" \
    --header "Authorization: Bearer ${TOKEN}" \
    --header 'accept: application/json' \
    --header 'content-type: application/json'

# Read the first record from the dataset
data="$(cat ../medium_articles_2020_dpr.json \
    | jq '.rows' \
    | jq '.[0]' \
    | jq -c '. + {"vector": .title_vector} | del(.title_vector) | del(.id)' )"

echo $data

# Insert a single entity
# Replace PUBLIC_ENDPOINT and TOKEN with your own
curl --request POST \
     --url "${PUBLIC_ENDPOINT}/v1/vector/insert" \
     --header "Authorization: Bearer ${TOKEN}" \
     --header 'accept: application/json' \
     --header 'content-type: application/json' \
     --data "{
        \"collectionName\": \"medium_articles_2020\",
        \"data\": $data
    }"

# read the first 200 records from the dataset
data="$(cat ../medium_articles_2020_dpr.json \
        | jq '.rows' \
        | jq '.[1:200]' \
        | jq -r '.[] | . + {"vector": .title_vector} | del(.title_vector) | del(.id)' \
        | jq -s -c '.')"

echo $data

# Insert multiple entities
# Replace PUBLIC_ENDPOINT and TOKEN with your own
curl --request POST \
     --url "${PUBLIC_ENDPOINT}/v1/vector/insert" \
     --header "Authorization: Bearer ${TOKEN}" \
     --header 'accept: application/json' \
     --header 'content-type: application/json' \
     --data "{
        \"collectionName\": \"medium_articles_2020\",
        \"data\": ${data}
    }"

# read the first vector from the dataset
vector="$(cat ../medium_articles_2020_dpr.json \
    | jq '.rows[0].title_vector' )"

echo $vector

# Replace PUBLIC_ENDPOINT and TOKEN with your own
curl --request POST \
     --url "${PUBLIC_ENDPOINT}/v1/vector/search" \
     --header "Authorization: Bearer ${TOKEN}" \
     --header 'accept: application/json' \
     --header 'content-type: application/json' \
     --data "{
        \"collectionName\": \"medium_articles_2020\",
        \"limit\": 3,
        \"vector\": $vector
      }"

# read the first vector from the dataset
vector="$(cat ../medium_articles_2020_dpr.json \
    | jq '.rows[0].title_vector' )"

echo $vector

# Replace PUBLIC_ENDPOINT and TOKEN with your own
curl --request POST \
     --url "${PUBLIC_ENDPOINT}/v1/vector/search" \
     --header "Authorization: Bearer ${TOKEN}" \
     --header 'accept: application/json' \
     --header 'content-type: application/json' \
     --data "{
        \"collectionName\": \"medium_articles_2020\",
        \"limit\": 3,
        \"vector\": $vector,
        \"filter\": \"claps > 5\",
        \"outputFields\": [\"title\", \"claps\"]
      }"

# Replace PUBLIC_ENDPOINT and TOKEN with your own
curl --request POST \
     --url "${PUBLIC_ENDPOINT}/v1/vector/query" \
     --header "Authorization: Bearer ${TOKEN}" \
     --header 'accept: application/json' \
     --header 'content-type: application/json' \
     --data "{
        \"collectionName\": \"medium_articles_2020\",
        \"limit\": 3,
        \"filter\": \"claps > 100 and publication in [\"The Startup\", \"Towards Data Science\"]\",
        \"outputFields\": [\"title\", \"claps\", \"publication\"]
      }"

# Retrieve a single entity by ID
# Go to Zilliz Cloud console, and look for the ID of an entity
# Fill the ID in the request body
curl --request POST \
     --url "${PUBLIC_ENDPOINT}/v1/vector/get" \
     --header "Authorization: Bearer ${TOKEN}" \
     --header 'accept: application/json' \
     --header 'content-type: application/json' \
     --data '{
        "collectionName": "medium_articles_2020",
        "id": 442169042773492589,
        "outputFields": ["id", "title"]
      }'

# Retrieve a set of entities by their IDs
# Go to Zilliz Cloud console, and look for the ID of some entities
# Fill the IDs in the request body
curl --request POST \
     --url "${PUBLIC_ENDPOINT}/v1/vector/get" \
     --header "Authorization: Bearer ${TOKEN}" \
     --header 'accept: application/json' \
     --header 'content-type: application/json' \
     --data '{
        "collectionName": "medium_articles_2020",
        "id": [442169042773492591, 442169042773492593, 442169042773492595],
        "outputFields": ["id", "title"]
      }'

# Deletes an entity
# Go to Zilliz Cloud console, and look for the ID of an entity
# Fill the ID in the request body
curl --request POST \
     --url "${PUBLIC_ENDPOINT}/v1/vector/delete" \
     --header "Authorization: Bearer ${TOKEN}" \
     --header 'accept: application/json' \
     --header 'content-type: application/json' \
     --data '{
        "collectionName": "medium_articles_2020",
        "id": 442169042773492589
      }'

# Delete a set of entities in a batch
# Go to Zilliz Cloud console, and look for the ID of some entities
# Fill the IDs in the request body
curl --request POST \
     --url "${PUBLIC_ENDPOINT}/v1/vector/delete" \
     --header "Authorization: Bearer ${TOKEN}" \
     --header 'accept: application/json' \
     --header 'content-type: application/json' \
     --data '{
        "collectionName": "medium_articles_2020",
        "id": [442169042773492591, 442169042773492593, 442169042773492595]
      }'

# Drop a collection
# Replace uri and API key with your own
curl --request POST \
     --url "${PUBLIC_ENDPOINT}/v1/vector/collections/drop" \
     --header "Authorization: Bearer ${TOKEN}" \
     --header 'accept: application/json' \
     --header 'content-type: application/json' \
     --data '{
        "collectionName": "medium_articles_2020"
      }'
