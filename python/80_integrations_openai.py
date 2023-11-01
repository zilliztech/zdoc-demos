import os, csv, random, time
import openai

from pymilvus import MilvusClient, DataType, CollectionSchema, FieldSchema

# Set up arguments

# 1. Go to https://www.kaggle.com/datasets/jealousleopard/goodreadsbooks, download the dataset, and save it locally.
FILE = '{}/../books.csv'.format(os.path.dirname(os.path.realpath(__file__))) 

# 2. Set up the name of the collection to be created.
COLLECTION_NAME = 'title_db'

# 3. Set up the dimension of the embeddings.
DIMENSION = 1536

# 4. Set up the number of records to process.
COUNT = 100

# 5. Set up the connection parameters for your Zilliz Cloud cluster.
URI = 'YOUR_CLUSTER_ENDPOINT'

# For serverless clusters, use your API key as the token.
# For dedicated clusters, use a colon (:) concatenating your username and password as the token.
TOKEN = 'YOUR_CLUSTER_TOKEN'

# 6. Set up the OpenAI engine and API key to use.
OPENAI_ENGINE = 'text-embedding-ada-002'  # Which engine to use
openai.api_key = 'YOUR_OPENAI_API_KEY'  # Use your own Open AI API Key here

# Connect to Zilliz Cloud and create a collection

client = MilvusClient(uri=URI, token=TOKEN)

if COLLECTION_NAME in client.list_collections():
    client.drop_collection(COLLECTION_NAME)

fields = [
    FieldSchema(name='id', dtype=DataType.INT64, descrition='Ids', is_primary=True, auto_id=False),
    FieldSchema(name='title', dtype=DataType.VARCHAR, description='Title texts', max_length=200),
    FieldSchema(name='embedding', dtype=DataType.FLOAT_VECTOR, description='Embedding vectors', dim=DIMENSION)
]

schema = CollectionSchema(fields=fields, description='Title collection')

index_params = {
    'metric_type': 'L2',
    'index_type': 'AUTOINDEX',
    'params': {'nlist': 1024}
}

client.create_collection_with_schema(
    collection_name=COLLECTION_NAME, 
    schema=schema, 
    index_params=index_params
)

# Load the csv file and extract embeddings from the text

def csv_load(file):
    with open(file, newline='') as f:
        reader=csv.reader(f, delimiter=',')
        for row in reader:
            yield row[1]

def embed(text):
    return openai.Embedding.create(
        input=text, 
        engine=OPENAI_ENGINE)["data"][0]["embedding"]

# Insert each title and its embeddings

inserted = []

for idx, text in enumerate(random.sample(sorted(csv_load(FILE)), k=COUNT)):
    ins = {
        'id': idx,
        'title': (text[:198] + '..') if len(text) > 200 else text,
        'embedding': embed(text)
    }
    client.insert(collection_name=COLLECTION_NAME, data=ins)
    time.sleep(3)
    inserted.append(ins)

# Flush the data to disk 
# Zilliz Cloud automatically flushes the data to disk once a segment is full. 
# You do not always need to call this method.
client.flush(COLLECTION_NAME)

# Search for similar titles
def search(text):
    res = client.search(
        collection_name=COLLECTION_NAME,
        data=[embed(text)],
        output_fields=['title'],
        limit=5,
    )

    ret = []

    for hits in res:
        for hit in hits:
            row = []
            row.extend([hit['id'], hit['distance'], hit['entity']['title']])
            ret.append(row)

    return ret

search_terms = [
    'self-improvement',
    'landscape',
]

result = []

for x in search_terms:
    result.append('Search term: ' + x)
    for x in search(x):
        result.append(x)
    result.append()

print('\n'.join(result))
