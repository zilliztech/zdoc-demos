from pathlib import Path
import os, json

import pandas as pd
from minio import Minio

from pymilvus import (
    FieldSchema, CollectionSchema, DataType,
    RemoteBulkWriter,
)

ACCESS_KEY = "YOUR_OBJECT_STORAGE_ACCESS_KEY"
SECRET_KEY = "YOUR_OBJECT_STORAGE_SECRET_KEY"
BUCKET_NAME = "YOUR_OBJECT_STORAGE_BUCKET_NAME"
REMOTE_PATH = "DATA_FILES_PATH_IN_BLOCK_STORAGE"
DATASET_PATH = "{}/../New_Medium_Data.csv".format(os.path.dirname(__file__))

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

url = Path(DATASET_PATH)
dataset = pd.read_csv(url)

connect_param = RemoteBulkWriter.ConnectParam(
    endpoint="storage.googleapis.com", # use 's3.amazonaws.com' for GCS
    access_key=ACCESS_KEY,
    secret_key=SECRET_KEY,
    bucket_name=BUCKET_NAME,
    secure=True
)

remote_writer = RemoteBulkWriter(
    schema=schema,
    remote_path=REMOTE_PATH,
    segment_size=50*1024*1024,
    connect_param=connect_param,
)

for i in range(0, len(dataset)):
  row = dataset.iloc[i].to_dict()
  row["vector"] = json.loads(row["vector"])
  remote_writer.append_row(row)

remote_writer.commit()

print(remote_writer.data_path)

# Output
#
# /DATA_FILES_PATH_IN_BLOCK_STORAGE/62391da7-e40f-439a-ba11-ddddb936223b



# To check the files in the remote folder

client = Minio(
    endpoint="storage.googleapis.com", # use 's3.amazonaws.com' for AWS
    access_key=ACCESS_KEY,
    secret_key=SECRET_KEY,
    secure=True)

objects = client.list_objects(
    bucket_name=BUCKET_NAME,
    prefix=str(remote_writer.data_path)[1:],
    recursive=True
)

print([obj.object_name for obj in objects])

# Output
#
# [
#     "DATA_FILES_PATH_IN_BLOCK_STORAGE/62391da7-e40f-439a-ba11-ddddb936223b/1/claps.npy",
#     "DATA_FILES_PATH_IN_BLOCK_STORAGE/62391da7-e40f-439a-ba11-ddddb936223b/1/id.npy",
#     "DATA_FILES_PATH_IN_BLOCK_STORAGE/62391da7-e40f-439a-ba11-ddddb936223b/1/link.npy",
#     "DATA_FILES_PATH_IN_BLOCK_STORAGE/62391da7-e40f-439a-ba11-ddddb936223b/1/publication.npy",
#     "DATA_FILES_PATH_IN_BLOCK_STORAGE/62391da7-e40f-439a-ba11-ddddb936223b/1/reading_time.npy",
#     "DATA_FILES_PATH_IN_BLOCK_STORAGE/62391da7-e40f-439a-ba11-ddddb936223b/1/responses.npy",
#     "DATA_FILES_PATH_IN_BLOCK_STORAGE/62391da7-e40f-439a-ba11-ddddb936223b/1/title.npy",
#     "DATA_FILES_PATH_IN_BLOCK_STORAGE/62391da7-e40f-439a-ba11-ddddb936223b/1/vector.npy"
# ]

