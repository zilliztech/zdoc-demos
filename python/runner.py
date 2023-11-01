import os, re
import argparse
import sys
import json
from io import StringIO

def format_line(line, comment=True, debug=False):
    if "{" in line or "[" in line:
        line = line.replace("'", '"').replace("True", "true").replace("False", "false").replace("None", "null")
        if debug:
            print(line)
        obj = json.loads(line)
        obj = reduce_array_length(obj, 10)
        if "{" not in line and len(obj) > 0 and max([ len(str(x)) for x in obj ]) < 17:
            line = json.dumps(obj)
        else:
            line = json.dumps(obj, indent=4)

        if comment:
            line = '\n'.join([ '# '+x for x in line.splitlines()])

    else:
        if comment:
            line = '# ' + line

    return line

def reduce_array_length(obj, length):
    if isinstance(obj, list):
        hidden = max(0, len(obj) - length)
        obj = obj[:length]
        for i, item in enumerate(obj):
            obj[i] = reduce_array_length(item, length)
        if hidden > 0:
            obj.append("({} more items hidden)".format(hidden))
    elif isinstance(obj, dict):
        for key in obj:
            obj[key] = reduce_array_length(obj[key], length)
    return obj


def remove_outputs(script):
    regex = r"^# Output\n^#\s*\n(?:#\s*.*\n)*"
    script = re.sub(regex, '', script, flags=re.MULTILINE)
    script = re.sub(r"\n{3,}", "\n\n", script, flags=re.MULTILINE)
    return script

if __name__ == '__main__':
    parameters = {}
    parser = argparse.ArgumentParser()
    parser.add_argument('-f', help='target file to run')
    parser.add_argument('-t', help='use test python')
    parser.add_argument('-d', help='debug', action='store_true')
    parser.add_argument('-e', help='use env')
    parser.add_argument('-r', help='remove outputs', action='store_true')
    args = parser.parse_args()
    target = args.f
    env = args.e
    remove_comments = args.r

    with open('{}/../{}'.format(os.path.dirname(__file__), args.e), 'r') as f:
        for line in f.readlines():
            key, value = line.split('=')
            parameters[key] = value.strip()

    with open(target, 'r') as f:
        script = f.read()
        
    if remove_comments:
        script = remove_outputs(script)

    for parameter in parameters.keys():
        script = '\n'.join([ x.replace(parameter, parameters[parameter][1:-1]) for x in script.split('\n') ])

    if args.t:
        addons = '''# Use PyMilvus in development
# Should be replaced with `from pymilvus import *` in production
from pathlib import Path
import sys
sys.path.insert(0, str(Path("{}")))
\n'''.format(args.t)

        script = addons + script
            
    stdout = sys.stdout
    sys.stdout = StringIO()

    exec(script)

    output = sys.stdout.getvalue()

    sys.stdout = stdout

    output_lines = output.splitlines()

    script_lines = script.splitlines()

    if args.t:
        script_lines = script_lines[6:]

    for i, line in enumerate(script_lines):
        if 'print(' in line:
            print_output = output_lines.pop(0)
            print_output = format_line(print_output, debug=args.d)
            script_lines[i] = line + '\n\n# Output\n#\n' + print_output + '\n\n'

    script = '\n'.join(script_lines)

    for parameter in parameters.keys():
        script = '\n'.join([ x.replace(parameters[parameter][1:-1], parameter) for x in script.split('\n') ])

    target = target[:-3] + '_copy.py'

    with open(target, 'w') as f:
        f.write(script)


