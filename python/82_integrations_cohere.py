# Use PyMilvus in development
# Should be replaced with `from pymilvus import *` in production
# from pathlib import Path
# import sys
# sys.path.append(str(Path("/Users/anthony/Documents/play/refine_milvus/pymilvus")))

from pymilvus import connections, DataType, CollectionSchema, FieldSchema, Collection, utility
import cohere
import pandas
import numpy as np
from tqdm import tqdm
import time, os

# Set up arguments

# 1. Set the The SQuAD dataset url.
FILE = "{}/../train-v2.0.json".format(os.path.dirname(__file__)) 

# 2. Set up the name of the collection to be created.
COLLECTION_NAME = 'question_answering_db'

# 3. Set up the dimension of the embeddings.
DIMENSION = 768

# 4. Set the number of entities to create and the number of entities to insert at a time.
COUNT = 5000
BATCH_SIZE = 96

# 5. Set up the cohere api key
COHERE_API_KEY = "YOUR_COHERE_API_KEY"

# 6. Set up the connection parameters for your Zilliz Cloud cluster.
URI = 'YOUR_CLUSTER_ENDPOINT'

# For serverless clusters, use your API key as the token.
# For dedicated clusters, use a colon (:) concatenating your username and password as the token.
TOKEN = 'YOUR_CLUSTER_TOKEN'

# Download the dataset
dataset = pandas.read_json(FILE)

# Clean up the dataset by grabbing all the question answer pairs
simplified_records = []
for x in dataset['data']:
    for y in x['paragraphs']:
        for z in y['qas']:
            if len(z['answers']) != 0:
                simplified_records.append({'question': z['question'], 'answer': z['answers'][0]['text']})

# Grab the amount of records based on COUNT
simplified_records = pandas.DataFrame.from_records(simplified_records)
simplified_records = simplified_records.sample(n=min(COUNT, len(simplified_records)), random_state = 42)

# Check if the length of the cleaned dataset matches COUNT
print(len(simplified_records))

# Connect to Zilliz Cloud and create a collection

connections.connect(
    alias='default',
    # Public endpoint obtained from Zilliz Cloud
    uri=URI,
    token=TOKEN
)

if COLLECTION_NAME in utility.list_collections():
    utility.drop_collection(COLLECTION_NAME)

fields = [
    FieldSchema(name='id', dtype=DataType.INT64, is_primary=True, auto_id=True),
    FieldSchema(name='original_question', dtype=DataType.VARCHAR, max_length=1000),
    FieldSchema(name='answer', dtype=DataType.VARCHAR, max_length=1000),
    FieldSchema(name='original_question_embedding', dtype=DataType.FLOAT_VECTOR, dim=DIMENSION)
]

schema = CollectionSchema(fields=fields)

collection = Collection(
    name=COLLECTION_NAME,
    schema=schema,
)

index_params = {
    'metric_type': 'L2',
    'index_type': 'AUTOINDEX',
    'params': {'nlist': 1024}
}

collection.create_index(
    field_name='original_question_embedding', 
    index_params=index_params
)

collection.load()

# Set up a Cohere client
cohere_client = cohere.Client(COHERE_API_KEY)

# Extract embeddings from questions using Cohere
def embed(texts):
    res = cohere_client.embed(texts, model='multilingual-22-12')
    return res.embeddings

# Insert each question, answer, and qustion embedding
total = pandas.DataFrame()
for batch in tqdm(np.array_split(simplified_records, (COUNT/BATCH_SIZE) + 1)):
    questions = batch['question'].tolist()
    embeddings = embed(questions)
    
    data = [
        {
            'original_question': x,
            'answer': batch['answer'].tolist()[i],
            'original_question_embedding': embeddings[i]
        } for i, x in enumerate(questions)
    ]

    collection.insert(data=data)

time.sleep(10)


# Search the cluster for an answer to a question text
def search(text, top_k = 5):

    # AUTOINDEX does not require any search params 
    search_params = {}

    results = collection.search(
        data = embed([text]),  # Embeded the question
        anns_field='original_question_embedding',
        param=search_params,
        limit = top_k,  # Limit to top_k results per search
        output_fields=['original_question', 'answer']  # Include the original question and answer in the result
    )

    # ret = []
    # for hit in results[0]:
    #     row = []
    #     row.extend([hit['entity']['answer'], hit['distance'], hit['entity']['original_question'] ])  # Get the answer, distance, and original question for the results
    #     ret.append(row)
    # return ret

    distances = results[0].distances
    entities = [ x.entity.to_dict()['entity'] for x in results[0] ]

    ret = [ {
        "answer": x[1]["answer"],
        "distance": x[0],
        "original_question": x[1]['original_question']
    } for x in zip(distances, entities)]

    return ret
            

# Ask these questions
search_questions = ['What kills bacteria?', 'What\'s the biggest dog?']

# Print out the results in order of [answer, similarity score, original question]

ret = [ { "question": x, "candidates": search(x) } for x in search_questions ]

print(ret)
    