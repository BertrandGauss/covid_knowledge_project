# covid_knowledge_project

covid 知识图谱系统

## step1:导入数据
在neo4j的bin目录下使用
````
neo4j-admin import --database=database_name --nodes=:Author=path/authors.csv --nodes=:Paper=path/papers.csv --relationships=path/authors_and_paper.csv
````
其中database_name为自己的数据库的名字 path为自己电脑存放数据文件的路径（使用绝对路径）

## step2:修改配置文件
修改application.properties文件中的username，passward为自己neo4j的账号密码，修改database为step1中的database_name

## step3:运行后端
直接run MainApplication文件
