# Use PyMilvus in development
# Should be replaced with `from pymilvus import *` in production
from pathlib import Path
import sys
sys.path.append(str(Path("/Users/anthony/Documents/play/refine_milvus/pymilvus")))

from pymilvus import MilvusClient, DataType, CollectionSchema, FieldSchema
from datasets import load_dataset_builder, load_dataset, Dataset
from transformers import AutoTokenizer, AutoModel
from torch import clamp, sum

# Set up arguments

# 1. Set the name of a dataset available on HuggingFace.
DATASET = 'squad' 

# 2. Set parameters for the generation of a subset of the dataset.
MODEL = 'bert-base-cased'
TOKENIZATION_BATCH_SIZE = 1000
INFERENCE_BATCH_SIZE = 64
INSERT_RATIO = 0.01

# 3. Set up the name of the collection to be created.
COLLECTION_NAME = 'huggingface_db'

# 4. Set up the dimension of the embeddings.
DIMENSION = 768

# 5. Set the number of records to return.
LIMIT = 100

# 6. Set up the connection parameters for your Zilliz Cloud cluster.
URI = 'YOUR_CLUSTER_ENDPOINT'

# For serverless clusters, use your API key as the token.
# For dedicated clusters, use a colon (:) concatenating your username and password as the token.
TOKEN = 'YOUR_CLUSTER_TOKEN'

# Connect to Zilliz Cloud and create a collection

client = MilvusClient(uri=URI, token=TOKEN)

if COLLECTION_NAME in client.list_collections():
    client.drop_collection(COLLECTION_NAME)

fields = [
    FieldSchema(name='id', dtype=DataType.INT64, is_primary=True, auto_id=True),
    FieldSchema(name='original_question', dtype=DataType.VARCHAR, max_length=1000),
    FieldSchema(name='answer', dtype=DataType.VARCHAR, max_length=1000),
    FieldSchema(name='original_question_embedding', dtype=DataType.FLOAT_VECTOR, dim=DIMENSION)
]

schema = CollectionSchema(fields=fields)

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

# Load the dataset and extract a subset

data_dataset = load_dataset(DATASET, split='all')
data_dataset = data_dataset.train_test_split(test_size=INSERT_RATIO, seed=42)['test']
data_dataset = data_dataset.map(
    lambda val: {'answer': val['answers']['text'][0]}, 
    remove_columns=['answers']
)

tokenizer = AutoTokenizer.from_pretrained(MODEL)

# Tokenize the question into the format that BERT takes
def tokenize_question(batch):
    results = tokenizer(
        batch['question'],
        add_special_tokens=True,
        truncation=True,
        padding = "max_length", 
        return_attention_mask = True, 
        return_tensors = "pt"
    )

    batch['input_ids'] = results['input_ids']
    batch['token_type_ids'] = results['token_type_ids']
    batch['attention_mask'] = results['attention_mask']

    return batch

# Generate the tokens for each entry
data_dataset = data_dataset.map(
    tokenize_question, 
    batched=True, 
    batch_size=TOKENIZATION_BATCH_SIZE
)

# Set the output format to torch so it can be pushed into embedding model
data_dataset.set_format(
    type='torch', 
    columns=['input_ids', 'token_type_ids', 'attention_mask'],
    output_all_columns=True
)

# Embed the tokenized question and take the mean pool with respect to attention mask of hidden layer

model = AutoModel.from_pretrained(MODEL)

def embed(batch):
    sentence_embs = model(
        input_ids=batch['input_ids'],
        token_type_ids=batch['token_type_ids'],
        attention_mask=batch['attention_mask']
    )[0]
    input_mask_expanded = batch['attention_mask'].unsqueeze(-1).expand(sentence_embs.size()).float()
    batch['question_embedding'] = sum(sentence_embs * input_mask_expanded, 1) / clamp(input_mask_expanded.sum(1), min=1e-9)
    return batch

data_dataset = data_dataset.map(
    embed, 
    batched=True, 
    batch_size=INFERENCE_BATCH_SIZE,
    remove_columns=['input_ids', 'token_type_ids', 'attention_mask']
)

# Due to the varchar constraint we are going to limit the question size when inserting
def insert_function(batch):
    insertable = [
        {
            'original_question': x,
            'answer': batch['answer'][i],
            'original_question_embedding': batch['question_embedding'].tolist()[i]
        } for i, x in enumerate(batch['question'])
    ]

    client.insert(collection_name=COLLECTION_NAME, data=insertable)

data_dataset.map(insert_function, batched=True, batch_size=64)

# Flush the data to disk 
# Zilliz Cloud automatically flushes the data to disk once a segment is full. 
# You do not always need to call this method.
client.flush(COLLECTION_NAME)

questions = {'question':['When did the premier league start?', 'Where did people learn russian?']}
question_dataset = Dataset.from_dict(questions)

question_dataset = question_dataset.map(tokenize_question, batched = True, batch_size=TOKENIZATION_BATCH_SIZE)
question_dataset.set_format('torch', columns=['input_ids', 'token_type_ids', 'attention_mask'], output_all_columns=True)
question_dataset = question_dataset.map(embed, remove_columns=['input_ids', 'token_type_ids', 'attention_mask'], batched = True, batch_size=INFERENCE_BATCH_SIZE)

def search(batch):
    res = client.search(
        collection_name=COLLECTION_NAME,
        data=batch['question_embedding'].tolist(),
        output_fields=['answer', 'original_question'], 
        limit = LIMIT
    )
    overall_id = []
    overall_distance = []
    overall_answer = []
    overall_original_question = []
    for hits in res:
        ids = []
        distance = []
        answer = []
        original_question = []
        for hit in hits:
            ids.append(hit['id'])
            distance.append(hit['distance'])
            answer.append(hit['entity']['answer'])
            original_question.append(hit['entity']['original_question'])
        overall_id.append(ids)
        overall_distance.append(distance)
        overall_answer.append(answer)
        overall_original_question.append(original_question)
    return {
        'id': overall_id,
        'distance': overall_distance,
        'answer': overall_answer,
        'original_question': overall_original_question
    }
question_dataset = question_dataset.map(search, batched=True, batch_size = 1)
for x in question_dataset:
    print()
    print('Question:')
    print(x['question'])
    print('Answer, Distance, Original Question')
    for x in zip(x['answer'], x['distance'], x['original_question']):
        print(x)