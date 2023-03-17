This server uses MongoDB. 
The choice to use NoSQL DB was made because we don't have the strongest structure at this stage. And it might have super-fast changes. 
In the future, we suggest that this structure will migrate to SQL DB (perhaps Postgres). It will be better for maintenance performance and data consistency with management queries. 
And when the database will not be small, management queries will run faster with SQL DB instead NoSQL.