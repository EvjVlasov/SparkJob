# SparkJob

Создаем в БД (Postgres) схему traffic_limits с таблицей limits_per_hour и заполняем её.

```
CREATE SCHEMA traffic_limits
CREATE TABLE limits_per_hour (
   ID INT NOT NULL PRIMARY KEY,
   limit_name VARCHAR(3) NOT NULL,
   limit_value BIGINT NOT NULL,
   effective_date TIMESTAMP NOT NULL
)
```
    INSERT INTO traffic_limits.limits_per_hour
    VALUES (1, 'max', 1073741824, CURRENT_TIMESTAMP),
		(2, 'min', 1024, CURRENT_TIMESTAMP)
    
Запускаем Zookeeper
```
  bin/zookeeper-server-start.sh config/zookeeper.properties
  ```
Запускаем Kafka
```
  bin/kafka-server-start.sh config/server.properties
  ```
  
Создаем топики
```
  bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic mytopic
  bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic alerts
  ``` 
В качестве генератора сообщение можно использовать KafkaProducer 

  https://github.com/EvjVlasov/KafkaProducer
  
Исполняемый jar файл находится в папке out. В качестве аргумента можно задать количество сообщений.

Запускаем SparJob. 
Исполняемый jar файл SparkJob также находится в папке out. В качестве аргумента можно задать количество сообщений.
```
  spark-submit --class org.example.Main \
> /out/artifacts/SparkJob_jar/SparkJob.jar \
> localhost:9092 test_db USER PASS
``` 
В качестве аргументов нужно задать host:port(или же "-" для учета всего трафика), имя БД, пользователя и пароль в БД.


Проблемы, с которыми столкнулся:

-Вывод полученных Spark соообщений на консоль.

-Логирование, конфликт между Spark's log4j.properties и log4j2.properties.

-Обновления пороговых значений сразу после их обновления в БД.
  
