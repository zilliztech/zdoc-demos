import os, json, time
from pymilvus import MilvusClient

CLUSTER_ENDPOINT="YOUR_CLUSTER_ENDPOINT" # Set your cluster endpoint
TOKEN="YOUR_CLUSTER_TOKEN" # Set your token
COLLECTION_NAME="medium_articles_2020" # Set your collection name
DATASET_PATH="{}/../medium_articles_2020_dpr.json".format(os.path.dirname(__file__)) # Set your dataset path

# Initialize a MilvusClient instance
# Replace uri and API key with your own
client = MilvusClient(
    uri=CLUSTER_ENDPOINT, # Cluster endpoint obtained from the console
    token=TOKEN # API key or a colon-separated cluster username and password
)

# Create a collection
client.create_collection(
    collection_name=COLLECTION_NAME,
    dimension=768
)

res = client.describe_collection(
    collection_name=COLLECTION_NAME
)

print(res)

# Output
#
# {
#     "collection_name": "medium_articles_2020",
#     "auto_id": false,
#     "num_shards": 1,
#     "description": "",
#     "fields": [
#         {
#             "field_id": 100,
#             "name": "id",
#             "description": "",
#             "type": 5,
#             "params": {},
#             "element_type": 0,
#             "is_primary": true
#         },
#         {
#             "field_id": 101,
#             "name": "vector",
#             "description": "",
#             "type": 101,
#             "params": {
#                 "dim": 768
#             },
#             "element_type": 0
#         }
#     ],
#     "aliases": [],
#     "collection_id": 445453895051073560,
#     "consistency_level": 2,
#     "properties": [],
#     "num_partitions": 1,
#     "enable_dynamic_field": true
# }



# Insert a single entity
res = client.insert(
    collection_name=COLLECTION_NAME,
    data={
        'id': 0, 
        'title': 'The Reported Mortality Rate of Coronavirus Is Not Important', 
        'link': '<https://medium.com/swlh/the-reported-mortality-rate-of-coronavirus-is-not-important-369989c8d912>', 
        'reading_time': 13, 
        'publication': 'The Startup', 
        'claps': 1100, 
        'responses': 18, 
        'vector': [0.041732933, 0.013779674, -0.027564144, -0.013061441, 0.009748648, 0.00082446384, -0.00071647146, 0.048612226, -0.04836573, -0.04567751, 0.018008126, 0.0063936645, -0.011913628, 0.030776596, -0.018274948, 0.019929802, 0.020547243, 0.032735646, -0.031652678, -0.033816382, -0.051087562, -0.033748355, 0.0039493158, 0.009246126, -0.060236514, -0.017136049, 0.028754413, -0.008433934, 0.011168004, -0.012391256, -0.011225835, 0.031775184, 0.002929508, -0.007448661, -0.005337719, -0.010999258, -0.01515909, -0.005130484, 0.0060212007, 0.0034560722, -0.022935811, -0.04970116, -0.0155887455, 0.06627353, -0.006052789, -0.051570725, -0.109865054, 0.033205193, 0.00041118253, 0.0029823708, 0.036160238, -0.011256539, 0.00023560718, 0.058322437, 0.022275906, 0.015206677, -0.02884609, 0.0016338055, 0.0049200393, 0.014388571, -0.0049061654, -0.04664761, -0.027454877, 0.017526226, -0.005100602, 0.018090058, 0.02700998, 0.04031944, -0.0097965, -0.03674761, -0.0043163053, -0.023320708, 0.012654851, -0.014262311, -0.008081833, -0.018334744, 0.0014025003, -0.003053399, -0.002636383, -0.022398386, -0.004725274, 0.00036367847, -0.012368711, 0.0014739085, 0.03450414, 0.009684024, 0.017912658, 0.06594397, 0.021381201, 0.029343689, -0.0069561847, 0.026152428, 0.04635037, 0.014746184, -0.002119602, 0.034359712, -0.013705124, 0.010691518, 0.04060854, 0.013679299, -0.018990282, 0.035340093, 0.007353945, -0.035990074, 0.013126987, -0.032933377, -0.001756877, -0.0049658176, -0.03380879, -0.07024137, -0.0130426735, 0.010533265, -0.023091802, -0.004645729, -0.03344451, 0.04759929, 0.025985204, -0.040710885, -0.016681142, -0.024664842, -0.025170377, 0.08839205, -0.023733815, 0.019494494, 0.0055427826, 0.045460507, 0.07066554, 0.022181382, 0.018302314, 0.026806992, -0.006066003, 0.046525814, -0.04066389, 0.019001767, 0.021242762, -0.020784091, -0.031635042, 0.04573943, 0.02515421, -0.050663553, -0.05183343, -0.046468202, -0.07910535, 0.017036669, 0.021445233, 0.04277428, -0.020235524, -0.055314954, 0.00904601, -0.01104365, 0.03069203, -0.00821997, -0.035594665, 0.024322856, -0.0068963314, 0.009003657, 0.00398102, -0.008596356, 0.014772055, 0.02740991, 0.025503553, 0.0038213644, -0.0047855405, -0.034888722, 0.030553816, -0.008325959, 0.030010607, 0.023729775, 0.016138833, -0.022967983, -0.08616877, -0.02460819, -0.008210168, -0.06444098, 0.018750126, -0.03335763, 0.022024624, 0.032374356, 0.023870794, 0.021288997, -0.026617877, 0.020435361, -0.003692393, -0.024113296, 0.044870164, -0.030451361, 0.013022849, 0.002278627, -0.027616743, -0.012087787, -0.033232547, -0.022974484, 0.02801226, -0.029057292, 0.060317725, -0.02312559, 0.015558754, 0.073630534, 0.02490823, -0.0140531305, -0.043771528, 0.040756326, 0.01667925, -0.0046050115, -0.08938058, 0.10560781, 0.015044094, 0.003613817, 0.013523503, -0.011039813, 0.06396795, 0.013428416, -0.025031878, -0.014972648, -0.015970055, 0.037022553, -0.013759925, 0.013363354, 0.0039748577, -0.0040822625, 0.018209668, -0.057496265, 0.034993384, 0.07075411, 0.023498386, 0.085871644, 0.028646072, 0.007590898, 0.07037031, -0.05005178, 0.010477505, -0.014106617, 0.013402172, 0.007472563, -0.03131418, 0.020552127, -0.031878896, -0.04170217, -0.03153583, 0.03458349, 0.03366634, 0.021306382, -0.037176874, 0.029069472, 0.014662372, 0.0024123765, -0.025403008, -0.0372993, -0.049923114, -0.014209514, -0.015524425, 0.036377322, 0.04259327, -0.029715618, 0.02657093, -0.0062432447, -0.0024253451, -0.021287171, 0.010478781, -0.029322306, -0.021203341, 0.047209084, 0.025337176, 0.018471811, -0.008709492, -0.047414266, -0.06227469, -0.05713435, 0.02141101, 0.024481304, 0.07176469, 0.0211379, -0.049316987, -0.124073654, 0.0049275495, -0.02461509, -0.02738388, 0.04825289, -0.05069646, 0.012640115, -0.0061352802, 0.034599125, 0.02799496, -0.01511028, -0.046418104, 0.011309801, 0.016673129, -0.033531003, -0.049203333, -0.027218347, -0.03528408, 0.008881575, 0.010736325, 0.034232814, 0.012807507, -0.0100207105, 0.0067757815, 0.009538357, 0.026212366, -0.036120333, -0.019764563, 0.006527411, -0.016437015, -0.009759148, -0.042246807, 0.012492151, 0.0066206953, 0.010672299, -0.44499892, -0.036189068, -0.015703931, -0.031111298, -0.020329623, 0.0047888453, 0.090396516, -0.041484866, 0.033830352, -0.0033847596, 0.06065415, 0.030880837, 0.05558494, 0.022805553, 0.009607596, 0.006682602, 0.036806617, 0.02406229, 0.034229457, -0.0105605405, 0.034754273, 0.02436426, -0.03849325, 0.021132406, -0.01251386, 0.022090863, -0.029137045, 0.0064384523, -0.03175176, -0.0070441505, 0.016025176, -0.023172623, 0.00076795724, -0.024106828, -0.045440633, -0.0074440194, 0.00035374766, 0.024374487, 0.0058897804, -0.012461025, -0.029086761, 0.0029477053, -0.022914894, -0.032369837, 0.020743662, 0.024116345, 0.0020526652, 0.0008596536, -0.000583463, 0.061080184, 0.020812698, -0.0235381, 0.08112197, 0.05689626, -0.003070104, -0.010714772, -0.004864459, 0.027089117, -0.030910335, 0.0017404438, -0.014978656, 0.0127020255, 0.01878998, -0.051732827, -0.0037475713, 0.013033434, -0.023682894, -0.03219574, 0.03736345, 0.0058930484, -0.054040316, 0.047637977, 0.012636436, -0.05820182, 0.013828813, -0.057893142, -0.012405234, 0.030266648, -0.0029184038, -0.021839319, -0.045179468, -0.013123978, -0.021320488, 0.0015718226, 0.020244086, -0.014414709, 0.009535103, -0.004497577, -0.02577227, -0.0085017495, 0.029090486, 0.009356506, 0.0055838437, 0.021151636, 0.039531752, 0.07814674, 0.043186333, -0.0077368533, 0.028967595, 0.025058193, 0.05432941, -0.04383656, -0.027070394, -0.080263995, -0.03616516, -0.026129462, -0.0033627374, 0.035040155, 0.015231506, -0.06372076, 0.046391208, 0.0049725454, 0.003783345, -0.057800908, 0.061461, -0.017880175, 0.022820404, 0.048944063, 0.04725843, -0.013392871, 0.05023065, 0.0069421427, -0.019561166, 0.012953843, 0.06227977, -0.02114757, -0.003334329, 0.023241237, -0.061053444, -0.023145229, 0.016086273, 0.0774039, 0.008069459, -0.0013532874, -0.016790181, -0.027246375, -0.03254919, 0.033754334, 0.00037142826, -0.02387325, 0.0057056695, 0.0084914565, -0.051856343, 0.029254, 0.005583839, 0.011591886, -0.033027634, -0.004170374, 0.018334484, -0.0030969654, 0.0024489106, 0.0030196267, 0.023012564, 0.020529047, 0.00010772953, 0.0017700809, 0.029260442, -0.018829526, -0.024797931, -0.039499596, 0.008108761, -0.013099816, -0.11726566, -0.005652353, -0.008117937, -0.012961832, 0.0152542135, -0.06429504, 0.0184562, 0.058997117, -0.027178442, -0.019294549, -0.01587592, 0.0048053437, 0.043830805, 0.011232237, -0.026841154, -0.0007282251, -0.00862919, -0.008405325, 0.019370917, -0.008112641, -0.014931766, 0.065622255, 0.0149185015, 0.013089685, -0.0028022556, -0.028629888, -0.048105706, 0.009296162, 0.010251239, 0.030800395, 0.028263845, -0.011021621, -0.034127586, 0.014709971, -0.0075270324, 0.010737263, 0.020517904, -0.012932179, 0.007153817, 0.03736311, -0.03391106, 0.03028614, 0.012531187, -0.046059456, -0.0043963846, 0.028799629, -0.06663413, -0.009447025, -0.019833198, -0.036111858, -0.01901045, 0.040701825, 0.0060573653, 0.027482377, -0.019782187, -0.020186251, 0.028398912, 0.027108852, 0.026535714, -0.000995191, -0.020599326, -0.005658084, -0.017271476, 0.026300041, -0.006992451, -0.08593853, 0.03675959, 0.0029454317, -0.040927384, -0.035480253, 0.016498009, -0.03406521, -0.026182177, -0.0007024827, 0.019500641, 0.0047998386, -0.02416359, 0.0019833131, 0.0033488963, 0.037788488, -0.009154958, -0.043469638, -0.024896, -0.017234193, 0.044996973, -0.06303135, -0.051730774, 0.04041444, 0.0075959326, -0.03901764, -0.019851806, -0.008242245, 0.06107143, 0.030118924, -0.016167669, -0.028161867, -0.0025679746, -0.021713274, 0.025275888, -0.012819265, -0.036431268, 0.017991759, 0.040626206, -0.0036572467, -0.0005935883, -0.0037468506, 0.034460746, -0.0182785, -0.00431203, -0.044755403, 0.016463224, 0.041199315, -0.0093387, 0.03919184, -0.01151653, -0.016965209, 0.006347649, 0.021104146, 0.060276803, -0.026659148, 0.026461488, -0.032700688, 0.0012274865, -0.024675943, -0.003006079, -0.009607032, 0.010597691, 0.0043017124, -0.01908524, 0.006748306, -0.03049305, -0.017481703, 0.036747415, 0.036634356, 0.0007106319, 0.045647435, -0.020883067, -0.0593661, -0.03929885, 0.042825453, 0.016104022, -0.03222858, 0.031112716, 0.020407677, -0.013276762, 0.03657825, -0.033871554, 0.004176301, 0.009538976, -0.009995692, 0.0042660628, 0.050545394, -0.018142857, 0.005219403, 0.0006711967, -0.014264284, 0.031044828, -0.01827481, 0.012488852, 0.031393733, 0.050390214, -0.014484084, -0.054758117, 0.055042055, -0.005506624, -0.0066648237, 0.010891078, 0.012446279, 0.061687976, 0.018091502, 0.0026527622, 0.0321537, -0.02469515, 0.01772019, 0.006846163, -0.07471038, -0.024433741, 0.02483875, 0.0497063, 0.0043456135, 0.056550737, 0.035752796, -0.02430349, 0.036570627, -0.027576203, -0.012418993, 0.023442797, -0.03433812, 0.01953399, -0.028003592, -0.021168072, 0.019414881, -0.014712576, -0.0003938545, 0.021453558, -0.023197332, -0.004455581, -0.08799191, 0.0010808896, 0.009281116, -0.0051161298, 0.031497046, 0.034916095, -0.023042161, 0.030799815, 0.017298799, 0.0015253434, 0.013728047, 0.0035838438, 0.016767647, -0.022243451, 0.013371096, 0.053564783, -0.008776885, -0.013133307, 0.015577713, -0.027008705, 0.009490815, -0.04103532, -0.012426461, -0.0050485474, -0.04323231, -0.013291623, -0.01660157, -0.055480026, 0.017622838, 0.017476618, -0.009798125, 0.038226977, -0.03127579, 0.019329516, 0.033461004, -0.0039813113, -0.039526325, 0.03884973, -0.011381027, -0.023257744, 0.03033401, 0.0029607012, -0.0006490531, -0.0347344, 0.029701462, -0.04153701, 0.028073426, -0.025427297, 0.009756264, -0.048082624, 0.021743972, 0.057197016, 0.024082556, -0.013968224, 0.044379756, -0.029081704, 0.003487999, 0.042621125, -0.04339743, -0.027005397, -0.02944044, -0.024172144, -0.07388652, 0.05952364, 0.02561452, -0.010255158, -0.015288555, 0.045012463, 0.012403602, -0.021197597, 0.025847573, -0.016983166, 0.03021369, -0.02920852, 0.035140667, -0.010627725, -0.020431923, 0.03191218, 0.0046844087, 0.056356475, -0.00012615003, -0.0052536936, -0.058609407, 0.009710908, 0.00041168949, -0.22300485, -0.0077232462, 0.0029359192, -0.028645728, -0.021156758, 0.029606635, -0.026473567, -0.0019432966, 0.023867624, 0.021946864, -0.00082128344, 0.01897284, -0.017976845, -0.015677344, -0.0026336901, 0.030096486]
    }
)

print(res)

# Output
#
# [0]



# Read the first 200 records
with open(DATASET_PATH) as f:
  data = json.load(f)
  data = data["rows"][:200]
  for x in data:
    x["vector"] = x.pop("title_vector") 

# Insert multiple entities
res = client.insert(
  collection_name=COLLECTION_NAME,
  data=data
)

print(res)

# Output
#
# [
#     0,
#     1,
#     2,
#     3,
#     4,
#     5,
#     6,
#     7,
#     8,
#     9,
#     "(190 more items hidden)"
# ]



time.sleep(10)

# Read the dataset
with open(DATASET_PATH) as f:
  data = json.load(f)

# single vector search
res = client.search(
    collection_name=COLLECTION_NAME,
    #highlight-next-line
    data=[data["rows"][0]["title_vector"]],
    output_fields=["title", "link"],
    limit=5
)

print(res)

# Output
#
# [
#     [
#         {
#             "id": 0,
#             "distance": 1.0,
#             "entity": {
#                 "title": "The Reported Mortality Rate of Coronavirus Is Not Important",
#                 "link": "https://medium.com/swlh/the-reported-mortality-rate-of-coronavirus-is-not-important-369989c8d912"
#             }
#         },
#         {
#             "id": 70,
#             "distance": 0.7525784969329834,
#             "entity": {
#                 "title": "How bad will the Coronavirus Outbreak get? \u2014 Predicting the outbreak figures",
#                 "link": "https://towardsdatascience.com/how-bad-will-the-coronavirus-outbreak-get-predicting-the-outbreak-figures-f0b8e8b61991"
#             }
#         },
#         {
#             "id": 160,
#             "distance": 0.7132074236869812,
#             "entity": {
#                 "title": "The Funeral Industry is a Killer",
#                 "link": "https://medium.com/swlh/the-funeral-industry-is-a-killer-1775118a7778"
#             }
#         },
#         {
#             "id": 111,
#             "distance": 0.688888430595398,
#             "entity": {
#                 "title": "The role of AI in web-based ADA and WCAG compliance",
#                 "link": "https://towardsdatascience.com/the-role-of-ai-in-web-based-ada-and-wcag-compliance-4fc09e69f416"
#             }
#         }
#     ]
# ]



# bulk vector search
res = client.search(
    collection_name=COLLECTION_NAME,
    #highlight-next-line
    data=[data["rows"][0]['title_vector'], data["rows"][1]['title_vector']],
    output_fields=["title", "link"],
    limit=5
)

print(res)

# Output
#
# [
#     [
#         {
#             "id": 0,
#             "distance": 1.0,
#             "entity": {
#                 "title": "The Reported Mortality Rate of Coronavirus Is Not Important",
#                 "link": "https://medium.com/swlh/the-reported-mortality-rate-of-coronavirus-is-not-important-369989c8d912"
#             }
#         },
#         {
#             "id": 70,
#             "distance": 0.7525784969329834,
#             "entity": {
#                 "title": "How bad will the Coronavirus Outbreak get? \u2014 Predicting the outbreak figures",
#                 "link": "https://towardsdatascience.com/how-bad-will-the-coronavirus-outbreak-get-predicting-the-outbreak-figures-f0b8e8b61991"
#             }
#         },
#         {
#             "id": 160,
#             "distance": 0.7132074236869812,
#             "entity": {
#                 "title": "The Funeral Industry is a Killer",
#                 "link": "https://medium.com/swlh/the-funeral-industry-is-a-killer-1775118a7778"
#             }
#         },
#         {
#             "id": 111,
#             "distance": 0.688888430595398,
#             "entity": {
#                 "title": "The role of AI in web-based ADA and WCAG compliance",
#                 "link": "https://towardsdatascience.com/the-role-of-ai-in-web-based-ada-and-wcag-compliance-4fc09e69f416"
#             }
#         }
#     ],
#     [
#         {
#             "id": 1,
#             "distance": 0.9999999403953552,
#             "entity": {
#                 "title": "Dashboards in Python: 3 Advanced Examples for Dash Beginners and Everyone Else",
#                 "link": "https://medium.com/swlh/dashboards-in-python-3-advanced-examples-for-dash-beginners-and-everyone-else-b1daf4e2ec0a"
#             }
#         },
#         {
#             "id": 4,
#             "distance": 0.7625511884689331,
#             "entity": {
#                 "title": "Python NLP Tutorial: Information Extraction and Knowledge Graphs",
#                 "link": "https://medium.com/swlh/python-nlp-tutorial-information-extraction-and-knowledge-graphs-43a2a4c4556c"
#             }
#         },
#         {
#             "id": 155,
#             "distance": 0.7575345039367676,
#             "entity": {
#                 "title": "How To Use Web Sockets (Socket IO) With Digital Ocean Load Balancers And Kubernetes (DOK8S) With Ingress Nginx",
#                 "link": "https://medium.com/swlh/how-to-use-web-sockets-socket-io-with-digital-ocean-load-balancers-and-kubernetes-dok8s-with-e4dd5531c67e"
#             }
#         },
#         {
#             "id": 17,
#             "distance": 0.7366296052932739,
#             "entity": {
#                 "title": "Blockchain, IoT and AI \u2014 A Perfect Fit",
#                 "link": "https://medium.com/swlh/blockchain-iot-and-ai-a-perfect-fit-1-e04c6ad73fbc"
#             }
#         },
#         {
#             "id": 113,
#             "distance": 0.7317826747894287,
#             "entity": {
#                 "title": "AutoAI: The Magic of Converting Data to Models",
#                 "link": "https://towardsdatascience.com/autoai-the-magic-of-converting-data-to-models-185f26d22234"
#             }
#         }
#     ]
# ]



# search with filters
res = client.search(
    collection_name=COLLECTION_NAME,
    data=[data["rows"][0]["title_vector"]],
    # highlight-start
    filter="10 < reading_time < 15",
    output_fields=["title", "reading_time"],
    # highlight-end
    limit=5
)

print(res)

# Output
#
# [
#     [
#         {
#             "id": 0,
#             "distance": 1.0,
#             "entity": {
#                 "title": "The Reported Mortality Rate of Coronavirus Is Not Important",
#                 "reading_time": 13
#             }
#         },
#         {
#             "id": 7,
#             "distance": 0.6361639499664307,
#             "entity": {
#                 "title": "Building Comprehensible Customer Churn Prediction Models",
#                 "reading_time": 13
#             }
#         },
#         {
#             "id": 103,
#             "distance": 0.6340133547782898,
#             "entity": {
#                 "title": "A Primer on Domain Adaptation",
#                 "reading_time": 12
#             }
#         },
#         {
#             "id": 90,
#             "distance": 0.6230067610740662,
#             "entity": {
#                 "title": "SVM: An optimization problem",
#                 "reading_time": 11
#             }
#         }
#     ]
# ]



res = client.search(
    collection_name=COLLECTION_NAME,
    data=[data["rows"][0]["title_vector"]],
    # highlight-start
    filter='claps > 1500 and responses > 15',
    output_fields=['title', 'claps', 'responses'],
    # highlight-end
    limit=5
)

print(res)

# Output
#
# [
#     [
#         {
#             "id": 130,
#             "distance": 0.5737711787223816,
#             "entity": {
#                 "title": "The Only \u201cCompetition\u201d Slide You\u2019ll Ever Need in a Pitch Deck",
#                 "claps": 1940,
#                 "responses": 25
#             }
#         },
#         {
#             "id": 66,
#             "distance": 0.5508044362068176,
#             "entity": {
#                 "title": "How to Be Memorable in Social Settings",
#                 "claps": 8600,
#                 "responses": 34
#             }
#         },
#         {
#             "id": 69,
#             "distance": 0.4541875422000885,
#             "entity": {
#                 "title": "Top 10 In-Demand programming languages to learn in 2020",
#                 "claps": 3000,
#                 "responses": 18
#             }
#         }
#     ]
# ]



res = client.search(
    collection_name=COLLECTION_NAME,
    data=[data["rows"][0]["title_vector"]],
    # highlight-start
    filter='publication == "Towards Data Science"',
    output_fields=["title", "publication"],
    # highlight-end
    limit=5
)

print(res)

# Output
#
# [
#     [
#         {
#             "id": 70,
#             "distance": 0.7525784969329834,
#             "entity": {
#                 "title": "How bad will the Coronavirus Outbreak get? \u2014 Predicting the outbreak figures",
#                 "publication": "Towards Data Science"
#             }
#         },
#         {
#             "id": 111,
#             "distance": 0.688888430595398,
#             "entity": {
#                 "title": "The role of AI in web-based ADA and WCAG compliance",
#                 "publication": "Towards Data Science"
#             }
#         },
#         {
#             "id": 103,
#             "distance": 0.6340133547782898,
#             "entity": {
#                 "title": "A Primer on Domain Adaptation",
#                 "publication": "Towards Data Science"
#             }
#         },
#         {
#             "id": 94,
#             "distance": 0.6249956488609314,
#             "entity": {
#                 "title": "Why Machine Learning Validation Sets Grow Stale",
#                 "publication": "Towards Data Science"
#             }
#         },
#         {
#             "id": 90,
#             "distance": 0.6230067610740662,
#             "entity": {
#                 "title": "SVM: An optimization problem",
#                 "publication": "Towards Data Science"
#             }
#         }
#     ]
# ]



res = client.search(
    collection_name=COLLECTION_NAME,
    data=[data["rows"][0]["title_vector"]],
    # highlight-start
    filter='publication not in ["Towards Data Science", "Personal Growth"]',
    output_fields=["title", "publication"],
    # highlight-end
    limit=5
)

print(res)

# Output
#
# [
#     [
#         {
#             "id": 0,
#             "distance": 1.0,
#             "entity": {
#                 "title": "The Reported Mortality Rate of Coronavirus Is Not Important",
#                 "publication": "The Startup"
#             }
#         },
#         {
#             "id": 160,
#             "distance": 0.7132074236869812,
#             "entity": {
#                 "title": "The Funeral Industry is a Killer",
#                 "publication": "The Startup"
#             }
#         },
#         {
#             "id": 196,
#             "distance": 0.6882869601249695,
#             "entity": {
#                 "title": "The Question We Should Be Asking About the Cost of Youth Sports",
#                 "publication": "The Startup"
#             }
#         },
#         {
#             "id": 51,
#             "distance": 0.6719912886619568,
#             "entity": {
#                 "title": "What if Facebook had to pay you for the profit they are making?",
#                 "publication": "The Startup"
#             }
#         }
#     ]
# ]



res = client.search(
    collection_name=COLLECTION_NAME,
    data=[data["rows"][0]["title_vector"]],
    # highlight-start
    filter='title like "Top%"',
    output_fields=["title", "link"],
    # highlight-end
    limit=5
)

print(res)

# Output
#
# [
#     [
#         {
#             "id": 75,
#             "distance": 0.5751269459724426,
#             "entity": {
#                 "title": "Top Trends of Graph Machine Learning in 2020",
#                 "link": "https://towardsdatascience.com/top-trends-of-graph-machine-learning-in-2020-1194175351a3"
#             }
#         },
#         {
#             "id": 76,
#             "distance": 0.5366824865341187,
#             "entity": {
#                 "title": "Top 20 Data Science Discord servers to join in 2020",
#                 "link": "https://towardsdatascience.com/top-20-data-science-discord-servers-to-join-in-2020-567b45738e9d"
#             }
#         },
#         {
#             "id": 74,
#             "distance": 0.5235060453414917,
#             "entity": {
#                 "title": "Top 10 Artificial Intelligence Trends for 2020",
#                 "link": "https://towardsdatascience.com/top-10-ai-trends-for-2020-d6294cfee2bd"
#             }
#         },
#         {
#             "id": 97,
#             "distance": 0.5228530168533325,
#             "entity": {
#                 "title": "Top 5 AI Conferences To Visit in Europe in 2020",
#                 "link": "https://towardsdatascience.com/top-5-ai-conferences-to-visit-in-europe-in-2020-7a6f068aff34"
#             }
#         },
#         {
#             "id": 69,
#             "distance": 0.4541875422000885,
#             "entity": {
#                 "title": "Top 10 In-Demand programming languages to learn in 2020",
#                 "link": "https://towardsdatascience.com/top-10-in-demand-programming-languages-to-learn-in-2020-4462eb7d8d3e"
#             }
#         }
#     ]
# ]



res = client.search(
    collection_name=COLLECTION_NAME,
    data=[data["rows"][0]["title_vector"]],
    # highlight-start
    filter='(publication == "Towards Data Science") and ((claps > 1500 and responses > 15) or (10 < reading_time < 15))',
    output_fields=["title", "publication", "claps", "responses", "reading_time"],
    # highlight-end
    limit=5
)

print(res)

# Output
#
# [
#     [
#         {
#             "id": 103,
#             "distance": 0.6340133547782898,
#             "entity": {
#                 "title": "A Primer on Domain Adaptation",
#                 "reading_time": 12,
#                 "publication": "Towards Data Science",
#                 "claps": 74,
#                 "responses": 0
#             }
#         },
#         {
#             "id": 90,
#             "distance": 0.6230067610740662,
#             "entity": {
#                 "title": "SVM: An optimization problem",
#                 "reading_time": 11,
#                 "publication": "Towards Data Science",
#                 "claps": 44,
#                 "responses": 0
#             }
#         },
#         {
#             "id": 75,
#             "distance": 0.5751269459724426,
#             "entity": {
#                 "title": "Top Trends of Graph Machine Learning in 2020",
#                 "reading_time": 11,
#                 "publication": "Towards Data Science",
#                 "claps": 1100,
#                 "responses": 0
#             }
#         },
#         {
#             "id": 99,
#             "distance": 0.5726118087768555,
#             "entity": {
#                 "title": "Finding optimal NBA physiques using data visualization with Python",
#                 "reading_time": 13,
#                 "publication": "Towards Data Science",
#                 "claps": 89,
#                 "responses": 0
#             }
#         },
#         {
#             "id": 80,
#             "distance": 0.564883828163147,
#             "entity": {
#                 "title": "Understanding Natural Language Processing: how AI understands our languages",
#                 "reading_time": 13,
#                 "publication": "Towards Data Science",
#                 "claps": 109,
#                 "responses": 0
#             }
#         }
#     ]
# ]



# Perform a query
res = client.query(
    collection_name=COLLECTION_NAME,
    filter='(publication == "Towards Data Science") and ((claps > 1500 and responses > 15) or (10 < reading_time < 15))',
    output_fields=["title", "publication", "claps", "responses", "reading_time"],
    limit=3,
)

print(res)

# Output
#
# [
#     {
#         "title": "Top 10 In-Demand programming languages to learn in 2020",
#         "reading_time": 21,
#         "publication": "Towards Data Science",
#         "claps": 3000,
#         "responses": 18,
#         "id": 69
#     },
#     {
#         "title": "Data Cleaning in Python: the Ultimate Guide (2020)",
#         "reading_time": 12,
#         "publication": "Towards Data Science",
#         "claps": 1500,
#         "responses": 7,
#         "id": 73
#     },
#     {
#         "title": "Top Trends of Graph Machine Learning in 2020",
#         "reading_time": 11,
#         "publication": "Towards Data Science",
#         "claps": 1100,
#         "responses": 0,
#         "id": 75
#     }
# ]



# Retrieve a single entity by ID
res = client.get(
    collection_name=COLLECTION_NAME,
    ids=1
)

print(res)

# Output
#
# [
#     {
#         "vector": [
#             0.0039737443,
#             0.003020432,
#             -0.0006188639,
#             0.03913546,
#             -0.00089768134,
#             0.021238148,
#             0.014454661,
#             0.025742851,
#             0.0022063442,
#             -0.051130578,
#             "(758 more items hidden)"
#         ],
#         "title": "Dashboards in Python: 3 Advanced Examples for Dash Beginners and Everyone Else",
#         "link": "https://medium.com/swlh/dashboards-in-python-3-advanced-examples-for-dash-beginners-and-everyone-else-b1daf4e2ec0a",
#         "reading_time": 14,
#         "publication": "The Startup",
#         "claps": 726,
#         "responses": 3,
#         "id": 1
#     }
# ]



# Retrieve a set of entities by their IDs
res = client.get(
    collection_name=COLLECTION_NAME,
    ids=[1, 2, 3]
)

print(res)

# Output
#
# [
#     {
#         "id": 1,
#         "vector": [
#             0.0039737443,
#             0.003020432,
#             -0.0006188639,
#             0.03913546,
#             -0.00089768134,
#             0.021238148,
#             0.014454661,
#             0.025742851,
#             0.0022063442,
#             -0.051130578,
#             "(758 more items hidden)"
#         ],
#         "title": "Dashboards in Python: 3 Advanced Examples for Dash Beginners and Everyone Else",
#         "link": "https://medium.com/swlh/dashboards-in-python-3-advanced-examples-for-dash-beginners-and-everyone-else-b1daf4e2ec0a",
#         "reading_time": 14,
#         "publication": "The Startup",
#         "claps": 726,
#         "responses": 3
#     },
#     {
#         "id": 2,
#         "vector": [
#             0.031961977,
#             0.00047043373,
#             -0.018263113,
#             0.027324716,
#             -0.0054595284,
#             -0.014779159,
#             0.017511465,
#             0.030381083,
#             -0.018930407,
#             -0.03372473,
#             "(758 more items hidden)"
#         ],
#         "title": "How Can We Best Switch in Python?",
#         "link": "https://medium.com/swlh/how-can-we-best-switch-in-python-458fb33f7835",
#         "reading_time": 6,
#         "publication": "The Startup",
#         "claps": 500,
#         "responses": 7
#     },
#     {
#         "id": 3,
#         "vector": [
#             0.032572296,
#             -0.011148319,
#             -0.01688577,
#             -0.0026665623,
#             -0.011911687,
#             -0.00067226397,
#             0.00549793,
#             0.024287743,
#             -0.006913468,
#             0.0077994824,
#             "(758 more items hidden)"
#         ],
#         "title": "Maternity leave shouldn\u2019t set women back",
#         "link": "https://medium.com/swlh/maternity-leave-shouldnt-set-women-back-5019dd3129d8",
#         "reading_time": 9,
#         "publication": "The Startup",
#         "claps": 460,
#         "responses": 1
#     }
# ]



# Delete a single entity
res = client.delete(
    collection_name=COLLECTION_NAME,
    pks=0
)

print(res)

# Output
#
# []



# Delete a set of entities in a batch
res = client.delete(
    collection_name=COLLECTION_NAME,
    pks=[1, 2, 3]
)

print(res)

# Output
#
# []



# Drop a collection
res = client.drop_collection(
    collection_name=COLLECTION_NAME
)

print(res)

# Output
#
# None


