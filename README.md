# What is reactor-data-center
to be honestly, the project name is pretty arbitrary, it's just a learning practice of project reactor~

it's major function is to upload excel files, and through a custom simple rpc protocol to transfer excel data to other services, 
or pull data from other services and export to excel by submitting an export task.

# prepare demo test
1. initialing the database via data-center.sql,it's in the 'attachments' directory
2. install xxl-job and create xxl-job task,the task type is Broadcast
    - taskExecutorJob
    - timeoutTaskJob
3. install nacos、redis
4. start web-demo
5. set your oss address :
```
spring:
  data:
    redis:
      database: 0
      host: localhost
      port: 6379
  r2dbc:
    url: r2dbc:mysql://localhost:3306/data-center?allowMultiQueries=true&useUnicode=true&useSSL=false&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&autoReconnect=true&nullCatalogMeansCurrent=true
    username: root
    password: 123456
    name: data-center
  application:
    name: reactor-data-center
s3:
  config:
    enable: true
    type: tencent.cloud
    access-key: 
    access-secret: 
    end-point: 
    bucket: 
task:
  oss:
    bucket: 
    err-path: 
    path: 
    temp-path: # the local temp file path



```
## excel export demo
1. execute taskExecutorJob


## excel import demo
1. upload the file import_test.xlsx in attachments directory to oss
2. save the file url to database (table->template_task   flied -> file_url)
3. execute taskExecutorJob at xxl-job dashboard