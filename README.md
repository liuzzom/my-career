# My Career 

[![forthebadge](https://forthebadge.com/images/badges/made-with-java.svg)](https://forthebadge.com)

[![Build Status](https://travis-ci.com/liuzzom/my-career.svg?branch=master)](https://travis-ci.com/liuzzom/my-career)

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=liuzzom_my-career&metric=alert_status)](https://sonarcloud.io/dashboard?id=liuzzom_my-career) [![Bugs](https://sonarcloud.io/api/project_badges/measure?project=liuzzom_my-career&metric=bugs)](https://sonarcloud.io/dashboard?id=liuzzom_my-career) [![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=liuzzom_my-career&metric=code_smells)](https://sonarcloud.io/dashboard?id=liuzzom_my-career) [![Coverage](https://sonarcloud.io/api/project_badges/measure?project=liuzzom_my-career&metric=coverage)](https://sonarcloud.io/dashboard?id=liuzzom_my-career) [![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=liuzzom_my-career&metric=duplicated_lines_density)](https://sonarcloud.io/dashboard?id=liuzzom_my-career) [![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=liuzzom_my-career&metric=ncloc)](https://sonarcloud.io/dashboard?id=liuzzom_my-career) [![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=liuzzom_my-career&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=liuzzom_my-career) [![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=liuzzom_my-career&metric=reliability_rating)](https://sonarcloud.io/dashboard?id=liuzzom_my-career) [![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=liuzzom_my-career&metric=security_rating)](https://sonarcloud.io/dashboard?id=liuzzom_my-career) [![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=liuzzom_my-career&metric=sqale_index)](https://sonarcloud.io/dashboard?id=liuzzom_my-career) [![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=liuzzom_my-career&metric=vulnerabilities)](https://sonarcloud.io/dashboard?id=liuzzom_my-career) 

### Table Of Contents

- [Introduction](#Introduction)
- [UIs Screenshots](#UIs-Screenshots)
- [System Requirements](#System-Requirements)
- [Download the Jar file](#Download-the-Jar-file)
- [Build Instruction](#Build-Instruction)
- [Usage](#Usage)
  - [Options](#Options)
- [Author Refernces](#Author-Refernces)

### Introduction

TDD Java project example for APT course.

My Career is a tiny Java application, created using TDD techniques for the APT course, a course held at UniFI. With My Career the user can create and manage students' careers, in terms of the courses that students attend. 

The whole application is created following TDD principles and using Maven as build automation tool. We used Travis CI as continuous integration server; we also used SonarCloud for code quality and code coverage. This application uses a MongoDB database.

This application supports either a GUI (created using Swing) and a CLI. User can choose the UI to use at boot.

You can find a development report [here](https://github.com/liuzzom/my-career/blob/master/readme-resources/Relazione%20Elaborato%20APT.pdf) (The report is written in Italian)

 ### UIs Screenshots

![GUI Screen](https://github.com/liuzzom/my-career/blob/master/readme-resources/gui%20screenshot.png)

![CLI Screen](https://github.com/liuzzom/my-career/blob/master/readme-resources/cli%20screenshot.png)

### System Requirements

- Maven (version 3.6.0 used during development)
- Docker (version 19.03.6 used during development)
- Java (version 8 or higher)

### Download the Jar file

You can find a ready to go jar file [here](https://github.com/liuzzom/my-career/releases)

### Build Instruction

#### Preparatory steps (command for Debian and Ubuntu-based distributions)

1. Install Maven `sudo apt install maven`
2. Install Docker `sudo apt install docker.io`
3. Install Java `sudo apt install openjdk-8-jdk`

#### Jar file creation

If you want to build by yourself the application, you have to do the following steps:

1. Clone this repository.
2. Move to the downloaded directory.
3. Move to my-career-aggregator folder.
4. Run a maven build, with clean and package goals

``` bash
mvn clean package
```

### Usage

Before launching the application, you have to launch a MongoDB instance. We suggest you to launch it using a docker container already configured as a replica set, like [this](https://hub.docker.com/r/krnbr/mongo). You can launch the docker container using

``` bash
docker run -p 27017:27017 --detach --rm krnbr/mongo:4.2.6
```

or 

``` bash
docker run -p 27017:27017 -v <dir_path>:/data/db --detach --rm krnbr/mongo:4.2.6
```

where `<dir_path>` is the path of the directory that you want to use for persistence

<hr></hr>

The application can be launched executing the jar file

``` bash
java -jar <jar_path> [options]...
```

where `<jar_path>` is the path of the jar file you downloaded or built by yourself

#### Options

| Option                         | Description                                                  |
| ------------------------------ | ------------------------------------------------------------ |
| `--ui`<br />`--user-interface` | User Interface type (allowable values: `gui`, `cli`)<br />(default value : `gui`) |
| `--mongo-host`                 | MongoDB host address<br />(default value: `localhost`)       |
| `--mongo-port`                 | MongoDB host port<br />(default value: `27017`)              |
| `--db-name`                    | Database name<br />(default value: `career`)                 |
| `--db-student-collection`      | Students collection name<br />(default value: `students`)    |
| `--db-course-collection`       | Coruses collection name<br />(default value: `courses`)      |

### Author Refernces

- Antonino Mauro Liuzzo
  - [GitHub](https://github.com/liuzzom)
- Davide Nesi
  - [GitHub](https://github.com/DavideNesi)
