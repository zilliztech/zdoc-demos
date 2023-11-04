import gdown
import zipfile
import glob
import torch
import time, os
from torchvision import transforms
from torchvision.models import ResNet50_Weights
from PIL import Image
from tqdm import tqdm
from matplotlib import pyplot as plt
from pymilvus import connections, DataType, CollectionSchema, FieldSchema, Collection, utility

# Set up arguments

# 1. Set up the name of the collection to be created.
COLLECTION_NAME = 'image_search_db'

# 2. Set up the dimension of the embeddings.
DIMENSION = 2048

# 3. Set the inference parameters
BATCH_SIZE = 128
TOP_K = 3

# 4. Set up the connection parameters for your Zilliz Cloud cluster.
URI = 'YOUR_CLUSTER_ENDPOINT'
TOKEN = 'YOUR_CLUSTER_TOKEN'

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
    FieldSchema(name='filepath', dtype=DataType.VARCHAR, max_length=200),  # VARCHARS need a maximum length, so for this example they are set to 200 characters
    FieldSchema(name='image_embedding', dtype=DataType.FLOAT_VECTOR, dim=DIMENSION)
]

schema = CollectionSchema(fields=fields)

collection = Collection(
    name=COLLECTION_NAME,
    schema=schema,
)

index_params = {
    'index_type': 'AUTOINDEX',
    'metric_type': 'L2',
    'params': {}
}

collection.create_index(
    field_name='image_embedding', 
    index_params=index_params
)

collection.load()

# Prepare data
url = 'https://drive.google.com/uc?id=1OYDHLEy992qu5C4C8HV5uDIkOWRTAR1_'
output = '../paintings.zip'
gdown.download(url, output)

with zipfile.ZipFile("{}/../paintings.zip".format(os.path.dirname(__file__)),"r") as zip_ref:
    zip_ref.extractall("../paintings")

# Get the filepaths of the images
paths = glob.glob('{}/../paintings/paintings/**/*.jpg'.format(os.path.dirname(__file__)), recursive=True)
print(len(paths))

# Output
#
# 4978



# Load the embedding model with the last layer removed
model = torch.hub.load('pytorch/vision:v0.10.0', 'resnet50', weights=ResNet50_Weights.DEFAULT)
model = torch.nn.Sequential(*(list(model.children())[:-1]))
model.eval()

# Preprocessing for images
preprocess = transforms.Compose([
    transforms.Resize(256),
    transforms.CenterCrop(224),
    transforms.ToTensor(),
    transforms.Normalize(mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225]),
])

# Embed function that embeds the batch and inserts it
def embed(data):
    with torch.no_grad():
        output = model(torch.stack(data[0])).squeeze()
        data = [ {
            'filepath': x[0],
            'image_embedding': x[1]
        } for x in zip(data[1], output.tolist()) ]
        collection.insert(data)

data_batch = [[],[]]

# Read the images into batches for embedding and insertion
for path in tqdm(paths):
    im = Image.open(path).convert('RGB')
    data_batch[0].append(preprocess(im))
    data_batch[1].append(path)
    if len(data_batch[0]) % BATCH_SIZE == 0:
        embed(data_batch)
        data_batch = [[],[]]

# Embed and insert the remainder
if len(data_batch[0]) != 0:
    embed(data_batch)

time.sleep(5)

# Get the filepaths of the search images
search_paths = glob.glob('{}/../paintings/test_paintings/**/*.jpg'.format(os.path.dirname(__file__)), recursive=True)
print(len(search_paths))

# Output
#
# 2



# Embed the search images
def embed(data):
    with torch.no_grad():
        ret = model(torch.stack(data))
        # If more than one image, use squeeze
        if len(ret) > 1:
            return ret.squeeze().tolist()
        # Squeeze would remove batch for single image, so using flatten
        else:
            return torch.flatten(ret, start_dim=1).tolist()

data_batch = [[],[]]

for path in search_paths:
    im = Image.open(path).convert('RGB')
    data_batch[0].append(preprocess(im))
    data_batch[1].append(path)

embeds = embed(data_batch[0])
start = time.time()
res = collection.search(embeds, anns_field='image_embedding', param={"metric_type": "L2", "params": {"nprobe": 10}}, limit=TOP_K, output_fields=['filepath'])
finish = time.time()

# Show the image results
f, axarr = plt.subplots(len(data_batch[1]), TOP_K + 1, figsize=(20, 10), squeeze=False)

for hits_i, hits in enumerate(res):
    axarr[hits_i][0].imshow(Image.open(data_batch[1][hits_i]))
    axarr[hits_i][0].set_axis_off()
    axarr[hits_i][0].set_title('Search Time: ' + str(finish - start))
    for hit_i, hit in enumerate(hits):
        axarr[hits_i][hit_i + 1].imshow(Image.open(hit.entity.get('filepath')))
        axarr[hits_i][hit_i + 1].set_axis_off()
        axarr[hits_i][hit_i + 1].set_title('Distance: ' + str(hit.distance))

# Save the search result in a separate image file alongside your script.
plt.savefig('search_result.png')