# mtg-spark-data-import
MTG Spark Data Import is a project responsible for reading cards data and indexing it into databases (currently only Solr is supported)

## Starting a fresh Solr container with an empty cards core
On the root folder of the project run the command:
```
docker-compose up
```

After the initialization takes place, open your favorite browser and access:
http://localhost:8983/solr/#/cards/query.

Executing a query under the cards core should display zero results.

## Indexing cards data
Import the mtg-spark-data-import on your favorite IDE. Keep in mind this is a SBT project, so configure your IDE accordingly.

Execute the main function of the object ***br.com.mhcsor.mtgspark.dataimport.MtgSparkDataImport***

## Execute a new query after indexing documents
Open your browser again and access:
http://localhost:8983/solr/#/cards/query.

Executing a query under the cards core should display over 18k results.
