import re
import os
import pandas as pd
import time

datapath = "/home/bertrand/Downloads/metadata"

def scanfile1(path):
    filelist = os.listdir(path)
    creator = []

    title = []
    for filename in filelist:
        filepath = os.path.join(path, filename)
        if 'e' not in list(filename):
            continue
        with open(filepath) as file:
            nums = 1
            flag = 0
            for line in file:
                if line.startswith("[]"):
                    flag = 1
                    break
                if "dc:creator" in line:
                    line = re.split("\"", line)[1]
                    creator.append(line)
                elif line.startswith("        \""):
                    line = re.split("\"", line)[1]
                    nums += 1
                    creator.append(line)
                elif "dc:title" in line:
                    line = re.split("\"", line)[1]
                    title.extend([line for _ in range(nums)])
            if flag:
                continue
            if len(creator) != len(title) :
                if len(creator) > len(title) :
                    for i in range(nums):
                        creator.pop()
                else:
                    title.pop()
            if len(creator) % 5e5 == 0:
                print(time.ctime(), "  ", len(creator))
            if len(creator) >= 1e8:
                break
    df1 = pd.DataFrame({'name:ID': list(set(creator))})
    df2 = pd.DataFrame({'title:ID': list(set(title))})

    relate1 = pd.DataFrame({':START_ID': creator, ':END_ID': title, ':TYPE': ['write' for _ in range(len(creator))]})


    df1.to_csv("data/authors.csv", index=False)
    df2.to_csv("data/papers.csv", index=False)

    relate1.to_csv("data/authors_and_paper.csv", index=False)


scanfile1(datapath)
