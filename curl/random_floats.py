import random, json
from sys import argv

if __name__ == '__main__':
    data = []
    colors = ['red', 'green', 'blue', 'yellow', 'orange', 'purple']

    for i in range(100):
        data.append({
            'vector': [random.uniform(-1, 1) for _ in range(32)],
            'color': random.choice(colors) + '_' + str(random.randint(1000, 9999))
        })

    print(json.dumps(data))
