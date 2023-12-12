/*
 * Copyright (c) 2019 "Neo4j,"
 * Neo4j Sweden AB [https://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.driver.springframework.boot.autoconfigure;

import java.net.URI;
import java.util.List;

import org.neo4j.driver.AuthToken;
import org.neo4j.driver.Config;
import org.neo4j.driver.Driver;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Automatic configuration of Neo4js Java Driver.
 * <p>
 * Provides an instance of {@link org.neo4j.driver.Driver} if the required library is available and no other instance
 * has been manually configured.
 *
 * @author Michael J. Simons
 */
@Configuration
@ConditionalOnClass(Driver.class)
@ConditionalOnMissingBean(Driver.class)
@EnableConfigurationProperties(Neo4jDriverProperties.class)
public class Neo4jDriverAutoConfiguration {

	private static final URI DEFAULT_SERVER_URI = URI.create("bolt://localhost:7687");

	@Bean(name = "driver")
	public FactoryBean<Driver> driverFactory(final Neo4jDriverProperties driverProperties) {

		final AuthToken authToken = driverProperties.getAuthentication().toInternalRepresentation();
		final Config config = driverProperties.getConfig().toInternalRepresentation();

		final List<URI> uris = driverProperties.computeFinalListOfUris();

		Neo4jDriverFactory driverFactory;
		if (uris.isEmpty() || uris.size() == 1) {

			URI uriOfServer = uris.stream().findFirst().orElse(DEFAULT_SERVER_URI);
			driverFactory = new Neo4jDriverFactory.DefaultDriverFactory(uriOfServer, authToken, config);
		} else {

			driverFactory = new Neo4jDriverFactory.RoutingDriverFactory(uris, authToken, config);
		}

		return driverFactory;
	}
}
