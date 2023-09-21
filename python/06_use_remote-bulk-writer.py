from pathlib import Path
import json

import pandas as pd
from minio import Minio

from pymilvus import (
    FieldSchema, CollectionSchema, DataType,
    RemoteBulkWriter,
)

YOUR_OBJECT_STORAGE_ACCESS_KEY = "YOUR_OBJECT_STORAGE_ACCESS_KEY"
YOUR_OBJECT_STORAGE_SECRET_KEY = "YOUR_OBJECT_STORAGE_SECRET_KEY"
YOUR_OBJECT_STORAGE_BUCKET_NAME = "YOUR_OBJECT_STORAGE_BUCKET_NAME"

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

# Extract the ID from the share link of the dataset file.
# For a file at https://drive.google.com/file/d/12RkoDPAlk-sclXdjeXT6DMFVsQr4612w/view?usp=drive_link, the ID should be 12RkoDPAlk-sclXdjeXT6DMFVsQr4612w.
# Concatenate the file ID to the end of the url as follows:

url = Path("../New_Medium_Data.csv")
dataset = pd.read_csv(url)

connect_param = RemoteBulkWriter.ConnectParam(
    endpoint="s3.amazonaws.com", # use 'storage.googleapis.com' for GCS
    access_key=YOUR_OBJECT_STORAGE_ACCESS_KEY,
    secret_key=YOUR_OBJECT_STORAGE_SECRET_KEY,
    bucket_name=YOUR_OBJECT_STORAGE_BUCKET_NAME,
    secure=True
)

remote_writer = RemoteBulkWriter(
    schema=schema,
    remote_path="medium_articles",
    segment_size=50*1024*1024,
    connect_param=connect_param,
)

for i in range(0, len(dataset)):
  row = dataset.iloc[i].to_dict()
  row["vector"] = json.loads(row["vector"])
  remote_writer.append_row(row)

remote_writer.commit()
print("test local writer done!")
print(remote_writer.data_path)

# To check the files in the remote folder

client = Minio(
    endpoint="s3.amazonaws.com", # use 'storage.googleapis.com' for GCS
    access_key=YOUR_OBJECT_STORAGE_ACCESS_KEY,
    secret_key=YOUR_OBJECT_STORAGE_SECRET_KEY,
    secure=True)

objects = client.list_objects(
    bucket_name=YOUR_OBJECT_STORAGE_BUCKET_NAME,
    prefix=str(remote_writer.data_path)[1:],
    recursive=True
)

for obj in objects:
    print(obj.object_name)