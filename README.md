# Quarkus - Embedded PostgreSQL

[![Version](https://img.shields.io/maven-central/v/io.quarkiverse.embedded.postgresql/quarkus-embedded-postgresql?logo=apache-maven&style=flat-square)](https://search.maven.org/artifact/io.quarkiverse.embedded-postgresql/quarkus-embedded-postgresql)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg?style=flat-square)](https://opensource.org/licenses/Apache-2.0)
[![Build](https://github.com/quarkiverse/quarkus-embedded-postgresql/actions/workflows/build.yml/badge.svg)](https://github.com/quarkiverse/quarkus-embedded-postgresql/actions/workflows/build.yml)


An alternative to quarkus-jdbc-postgres that rather than instantiating a pgsql server, uses zonky embedded library, reducing memory footprint. 
It is mainly oriented for single pod k8s deployments or integration testing. 

## Usage

Include this dependency in your pom 

```
   <dependency>
     <groupId>io.quarkiverse.embedded.postgresql</groupId>
     <artifactId>quarkus-embedded-postgresql</artifactId>
   </dependency>
```

You can now inject in your code a DataSource object (if you are JDBC friend) or a PgPool reference (if you prefer reactive) without adding any further property.

### Configuration properties

| property                               | description                                                                                                                                        |
|----------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------|
| `quarkus.embedded.postgresql.data.dir` | You can optionally persist information into file system by setting the desired path as value of property                                           |  
| `quarkus.embedded.postgresql.port`     | You can optinally configure the postgresql server port by using this property. If no value is confirured a random available port will be selected. |
| `quarkus.embedded.postgresql.listen-address`     | You can optionally configure the host(s) (listen_addresses) for the PostgreSQL server; if not set, localhost is used. Valid values include localhost, 0.0.0.0, ::, *, a specific hostname or IP (e.g. db.mycompany.local, 192.168.1.100), or multiple comma-separated addresses. |

### Multiple Datasources

This extension supports the usage of multiple datasources when the application has more than datasource which are defined by a specific name for each one of them,
see the [datasources guide](https://quarkus.io/guides/datasource#multiple-datasources) for more details.

The extension uses the configuration of each named datasource matching the `db-kind` property to automatically create the databases on the embedded PostgreSQL for
each defined datasource and inject the proper configuration intro the application, such as the URL for the datasource.

<!-- ALL-CONTRIBUTORS-BADGE:START - Do not remove or modify this section -->
[![All Contributors](https://img.shields.io/badge/all_contributors-2-orange.svg?style=flat-square)](#contributors-)
<!-- ALL-CONTRIBUTORS-BADGE:END -->

## Contributors ✨

Thanks goes to these wonderful people ([emoji key](https://allcontributors.org/docs/en/emoji-key)):

<!-- ALL-CONTRIBUTORS-LIST:START - Do not remove or modify this section -->
<!-- prettier-ignore-start -->
<!-- markdownlint-disable -->
<table>
  <tbody>
    <tr>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/fjtirado"><img src="https://avatars.githubusercontent.com/u/65240126?v=4?s=100" width="100px;" alt="Francisco Javier Tirado Sarti"/><br /><sub><b>Francisco Javier Tirado Sarti</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-embedded-postgresql/commits?author=fjtirado" title="Code">💻</a> <a href="#maintenance-fjtirado" title="Maintenance">🚧</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://ricardozanini.medium.com/"><img src="https://avatars.githubusercontent.com/u/1538000?v=4?s=100" width="100px;" alt="Ricardo Zanini"/><br /><sub><b>Ricardo Zanini</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-embedded-postgresql/commits?author=ricardozanini" title="Code">💻</a> <a href="#maintenance-ricardozanini" title="Maintenance">🚧</a></td>
    </tr>
  </tbody>
</table>

<!-- markdownlint-restore -->
<!-- prettier-ignore-end -->

<!-- ALL-CONTRIBUTORS-LIST:END -->

This project follows the [all-contributors](https://github.com/all-contributors/all-contributors) specification. Contributions of any kind welcome!