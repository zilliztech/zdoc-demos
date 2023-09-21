from urllib.parse import urlparse
import time, json

from minio import Minio

from pymilvus import (
    connections,
    FieldSchema, CollectionSchema, DataType,
    Collection,
    bulk_import,
    get_import_progress,
    list_import_jobs,
)

# Check the prepared data files you have

YOUR_OBJECT_STORAGE_ACCESS_KEY = "YOUR_OBJECT_STORAGE_ACCESS_KEY"
YOUR_OBJECT_STORAGE_SECRET_KEY = "YOUR_OBJECT_STORAGE_SECRET_KEY"
YOUR_OBJECT_STORAGE_BUCKET_NAME = "YOUR_OBJECT_STORAGE_BUCKET_NAME"
PATH_TO_YOUR_PREPARED_DATA_FILES_FOLDER = "PATH_TO_YOUR_PREPARED_DATA_FILES_FOLDER"

client = Minio(
    endpoint="s3.amazonaws.com", # use 'storage.googleapis.com' for GCS
    access_key=YOUR_OBJECT_STORAGE_ACCESS_KEY,
    secret_key=YOUR_OBJECT_STORAGE_SECRET_KEY,
    secure=True)

objects = client.list_objects(
    bucket_name=YOUR_OBJECT_STORAGE_BUCKET_NAME,
    prefix=PATH_TO_YOUR_PREPARED_DATA_FILES_FOLDER,
    recursive=True
)

for obj in objects:
    print(obj.object_name)

# set up your collection

YOUR_CLUSTER_ENDPOINT = "YOUR_CLUSTER_ENDPOINT"
YOUR_CLUSTER_TOKEN = "YOUR_CLUSTER_TOKEN"
YOUR_COLLECTION_NAME = "YOUR_COLLECTION_NAME"
YOUR_API_KEY = "YOUR_API_KEY"
CLUSTER_ID = urlparse(YOUR_CLUSTER_ENDPOINT).netloc.split(".")[0] if urlparse(YOUR_CLUSTER_ENDPOINT).netloc.startswith("in") else None
CLOUD_REGION = urlparse(YOUR_CLUSTER_ENDPOINT).netloc.split(".")[1] if urlparse(YOUR_CLUSTER_ENDPOINT).netloc.startswith("in") else 'aws-us-west-2'
OBJECT_URL = f"s3://{YOUR_OBJECT_STORAGE_BUCKET_NAME}/{PATH_TO_YOUR_PREPARED_DATA_FILES_FOLDER}/"

fields = [
    FieldSchema(name="id", dtype=DataType.INT64, is_primary=True),
    FieldSchema(name="title", dtype=DataType.VARCHAR, max_length=512),
    FieldSchema(name="vector", dtype=DataType.FLOAT_VECTOR, dim=768),
    FieldSchema(name="link", dtype=DataType.VARCHAR, max_length=512),
    FieldSchema(name="reading_time", dtype=DataType.INT64),
    FieldSchema(name="publication", dtype=DataType.VARCHAR, max_length=512),
    FieldSchema(name="claps", dtype=DataType.INT64),
    FieldSchema(name="responses", dtype=DataType.INT64)
]

schema = CollectionSchema(fields)

connections.connect(
    uri=YOUR_CLUSTER_ENDPOINT,
    token=YOUR_CLUSTER_TOKEN,
    secure=True
)

collection = Collection(YOUR_COLLECTION_NAME, schema)

collection.create_index(
    field_name="vector",
    index_params={
        "index_type": "AUTOINDEX",
        "metric_type": "L2"
    }
)

collection.load()

# bulk-import your data from the prepared data files

res = bulk_import(
    url=f"controller.api.{CLOUD_REGION}.zillizcloud.com",
    api_key=YOUR_API_KEY,
    object_url=OBJECT_URL,
    access_key=YOUR_OBJECT_STORAGE_ACCESS_KEY,
    secret_key=YOUR_OBJECT_STORAGE_SECRET_KEY,
    cluster_id=CLUSTER_ID,
    collection_name=YOUR_COLLECTION_NAME
)

print(json.dumps(res.json(), indent=4))

job_id = res.json()['data']['jobId']
res = get_import_progress(
    url=f"controller.api.{CLOUD_REGION}.zillizcloud.com",
    api_key=YOUR_API_KEY,
    job_id=job_id,
    cluster_id=CLUSTER_ID
)

# check the bulk-import progress

while res.json()["data"]["readyPercentage"] < 1:
    time.sleep(5)
    print(res.json())

    res = get_import_progress(
        url=f"controller.api.{CLOUD_REGION}.zillizcloud.com",
        api_key=YOUR_API_KEY,
        job_id=job_id,
        cluster_id=CLUSTER_ID
    )

print(json.dumps(res.json(), indent=4))

# list bulk-import jobs

res = list_import_jobs(
    url=f"controller.api.{CLOUD_REGION}.zillizcloud.com",
    api_key=YOUR_API_KEY,
    cluster_id=CLUSTER_ID,
    page_size=10,
    current_page=1,
)

print(json.dumps(res.json(), indent=4))