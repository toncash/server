This server uses MongoDB.
The choice to use NoSQL DB was made because we don't have the strongest structure at this stage. And it can have super fast changes.
But in the future, we suggest that this structure will migrate to SQL DB (perhaps Postgres). It will be better for maintenance performance and data consistency with management queries.
And when the database will not small, management queries will run faster with a SQL database than without SQL.