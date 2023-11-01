from os import environ
from glob import glob
from llama_index import download_loader, VectorStoreIndex
from llama_index.vector_stores import MilvusVectorStore
from llama_index.storage.storage_context import StorageContext

# Set up arguments

# 1. Set up the name of the collection to be created.
COLLECTION_NAME = 'document_qa_db'

# 2. Set up the dimension of the embeddings.
DIMENSION = 1536

# 3. Set the inference parameters
BATCH_SIZE = 128
TOP_K = 3

# 4. Set up the connection parameters for your Zilliz Cloud cluster.
URI = 'YOUR_CLUSTER_ENDPOINT'

# For serverless clusters, use your API key as the token.
# For dedicated clusters, use a colon (:) concatenating your username and password as the token.
TOKEN = 'YOUR_CLUSTER_TOKEN'

# 5. Set up OpenAI key
environ['OPENAI_API_KEY'] = 'YOUR_OPENAI_API_KEY'

# Load the markdown reader from the hub
MarkdownReader = download_loader("MarkdownReader")
markdownreader = MarkdownReader()

# Grab all markdown files and convert them using the reader
docs = []
for file in glob("../../../repos/milvus-docs/site/en/**/*.md", recursive=True):
    docs.extend(markdownreader.load_data(file=file))
print(len(docs))

# Push all markdown files into Zilliz Cloud
vector_store = MilvusVectorStore(
    uri=URI, 
    token=TOKEN, 
    collection_name=COLLECTION_NAME, 
    dim=DIMENSION
)

storage_context = StorageContext.from_defaults(vector_store)

index = VectorStoreIndex.from_documents(
    documents=docs, 
    storage_context=storage_context,
    show_progress=True
)

query_engine = index.as_query_engine()
response = query_engine.query("What is Milvus?")
print(str(response))
