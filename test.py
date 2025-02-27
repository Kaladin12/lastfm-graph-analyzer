import os 
import re

envVars = dict()

replace_ = lambda x: envVars.get(x.group(1), x.group(0))

with open(".env", "r") as f:
    f = filter(lambda s: s!="",  map(lambda x: x.replace("\n", ""), f.readlines()))
    f = map(lambda x: x.split("="), f)
    for pair in f:
        envVars[pair[0]] = re.sub(r"\$\{([^}]+)\}", replace_, pair[1])

print(" ".join(f"{key}={envVars[key]}" for key in envVars))
