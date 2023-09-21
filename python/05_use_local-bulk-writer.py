from pathlib import Path
import os, json

import pandas as pd

## Running this requires PyMilvus 2.2.16
## Create a virtualenv and install that.

from pymilvus import (
    FieldSchema, CollectionSchema, DataType,
    LocalBulkWriter,
    BulkFileType
)

# You need to work out a collection schema out of your dataset.
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

# Load the dataset
dataset = pd.read_csv(Path("../New_Medium_Data.csv"))

# Rewrite the above dataset into a JSON file
local_writer = LocalBulkWriter(
    schema=schema,
    local_path=Path("../output").joinpath('json'),
    segment_size=4*1024*1024,
    file_type=BulkFileType.JSON_RB
)

for i in range(0, len(dataset)):
  row = dataset.iloc[i].to_dict()
  row["vector"] = json.loads(row["vector"])
  local_writer.append_row(row)

local_writer.commit()
print("test local writer done!")
print(local_writer.data_path)

# Check what you have in the `output` folder
print(os.listdir(local_writer.data_path))