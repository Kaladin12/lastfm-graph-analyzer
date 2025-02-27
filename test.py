import os 
import re

envVars = dict()

with open(".env", "r") as f:
    data = f.readlines()
    for i in data:
        i = i.replace("\n", "")
        if i != "":
            pair = i.split("=")
            envVars[pair[0]] = pair[1]
            replace_ = lambda x: envVars.get(x.group(1), x.group(0))
            #print(re.sub(r"\$\{([^}]+)\}", replace_, pair[1]))
            envVars[pair[0]] = re.sub(r"\$\{([^}]+)\}", replace_, pair[1])
print(" ".join(f"{key}={envVars[key]}" for key in envVars))
