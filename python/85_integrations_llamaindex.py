from os import environ
from glob import glob
from llama_index import download_loader, VectorStoreIndex, ServiceContext
from llama_index.vector_stores import MilvusVectorStore
from llama_index.embeddings import HuggingFaceEmbedding

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

TOKEN = 'YOUR_CLUSTER_TOKEN'

# OpenAI API key
environ["OPENAI_API_KEY"] = "YOUR_OPENAI_API_KEY"
environ["TOKENIZERS_PARALLELISM"] = "false"

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
    similarity_metric="L2",
    dim=DIMENSION,
)

embed_model = HuggingFaceEmbedding(model_name="sentence-transformers/all-MiniLM-L12-v2")
service_context = ServiceContext.from_defaults(embed_model=embed_model)

index = VectorStoreIndex.from_documents(
    documents=docs, 
    service_context=service_context,
    show_progress=True
)

query_engine = index.as_query_engine()
response = query_engine.query("What is IVF_FLAT?")
print(str(response))

# Output
#
# IVF_FLAT is an index used in Milvus that divides vector space into list clusters. It compares the distances between the target vector and the centroids of all the clusters to return the nearest clusters. Then, it compares the distances between the target vector and the vectors in the selected clusters to find the nearest vectors. IVF_FLAT has performance advantages over FLAT when the number of vectors exceeds a certain threshold.

