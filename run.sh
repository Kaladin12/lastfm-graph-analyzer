!#/bin/bash

export $(python3 test.py | xargs) && ./gradlew bootRun
