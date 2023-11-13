import gdown, zipfile, time, csv, os
from tqdm import tqdm
from pymilvus import connections, DataType, FieldSchema, CollectionSchema, Collection, utility
from sentence_transformers import SentenceTransformer

# Parameters for set up Zilliz Cloud
COLLECTION_NAME = 'movies_db'  # Collection name
DIMENSION = 384  # Embeddings size
URI = 'YOUR_CLUSTER_ENDPOINT'  # Endpoint URI obtained from Zilliz Cloud
TOKEN = 'YOUR_CLUSTER_TOKEN'  # API key or a colon-separated cluster username and password

# Inference Arguments
BATCH_SIZE = 128

# Search Arguments
TOP_K = 3

url = 'https://drive.google.com/uc?id=11ISS45aO2ubNCGaC3Lvd3D7NT8Y7MeO8'
zipball = '{}/../movies.zip'.format(os.path.dirname(__file__))
output_folder = '{}/../movies'.format(os.path.dirname(__file__))
gdown.download(url, zipball)

with zipfile.ZipFile(zipball,"r") as zip_ref:
    zip_ref.extractall(output_folder)

# Connect to Milvus Database
connections.connect(
    uri=URI, 
    token=TOKEN
)

# Remove any previous collections with the same name
if utility.has_collection(COLLECTION_NAME):
    utility.drop_collection(COLLECTION_NAME)


# Create collection which includes the id, title, and embedding.
fields = [
    FieldSchema(name='id', dtype=DataType.INT64, is_primary=True, auto_id=True),
    FieldSchema(name='title', dtype=DataType.VARCHAR, max_length=200),  # VARCHARS need a maximum length, so for this example they are set to 200 characters
    FieldSchema(name='embedding', dtype=DataType.FLOAT_VECTOR, dim=DIMENSION)
]
schema = CollectionSchema(fields=fields)
collection = Collection(name=COLLECTION_NAME, schema=schema)

# Create an IVF_FLAT index for collection.
index_params = {
    'index_type': 'AUTOINDEX',
    'metric_type': 'L2',
    'params': {}
}
collection.create_index(field_name="embedding", index_params=index_params)
collection.load()

transformer = SentenceTransformer('all-MiniLM-L6-v2')

# Extract the book titles
def csv_load(file):
    with open(file, newline='') as f:
        reader = csv.reader(f, delimiter=',')
        for row in reader:
            if '' in (row[1], row[7]):
                continue
            yield (row[1], row[7])


# Extract embedding from text using OpenAI
def embed_insert(data):
    embeds = transformer.encode(data[1])
    ins = [
            data[0],
            [x for x in embeds]
    ]
    collection.insert(ins)



data_batch = [[],[]]

with open('../movies/plots.csv') as f:
    total = len(f.readlines()) / BATCH_SIZE

for title, plot in tqdm(csv_load('{}/plots.csv'.format(output_folder)), total=total):
    data_batch[0].append(title)
    data_batch[1].append(plot)
    if len(data_batch[0]) % BATCH_SIZE == 0:
        embed_insert(data_batch)
        data_batch = [[],[]]

# Embed and insert the remainder
if len(data_batch[0]) != 0:
    embed_insert(data_batch)

# Call a flush to index any unsealed segments.
collection.flush()

# Search for titles that closest match these phrases.
search_terms = ['A movie about cars', 'A movie about monsters']

# Search the database based on input text
def embed_search(data):
    embeds = transformer.encode(data)
    return [x for x in embeds]

search_data = embed_search(search_terms)

start = time.time()
res = collection.search(
    data=search_data,  # Embeded search value
    anns_field="embedding",  # Search across embeddings
    param={},
    limit = TOP_K,  # Limit to top_k results per search
    output_fields=['title']  # Include title field in result
)
end = time.time()

for hits_i, hits in enumerate(res):
    print('Title:', search_terms[hits_i])
    print('Search Time:', end-start)
    print('Results:')
    for hit in hits:
        print( hit.entity.get('title'), '----', hit.distance)
    print()