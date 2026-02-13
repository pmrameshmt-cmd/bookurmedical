package com.bookurmedical.database;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ReadPreference;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

@Configuration
public class MongoConfig {

	@Value("${mongodburl}")
	private String mongodburl;

	@Value("${database}")
	private String database;

	@Bean(name = "bookurmedicalDB")
	public MongoDatabase bookurmedicalDB() throws Exception {
		ConnectionString connectionString = new ConnectionString(mongodburl);
		MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(connectionString)
				.readPreference(ReadPreference.secondary()).build();
		MongoClient mongoClient = MongoClients.create(settings);
		return mongoClient.getDatabase(database);
	}

}
