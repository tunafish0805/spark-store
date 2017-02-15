# Deploying Production Ready Web Applications on ECS
[]()
[![Wercker](https://img.shields.io/badge/open-100%-green.svg)](https://drive.google.com/file/d/0B8XKepQ4JSPxRU9ITGItX19kUjg/view) [![Wercker](https://img.shields.io/badge/connected-100%-orange.svg)](https://drive.google.com/file/d/0B8XKepQ4JSPxRU9ITGItX19kUjg/view) [![Wercker](https://img.shields.io/badge/useful-100%-yellow.svg)](https://drive.google.com/file/d/0B8XKepQ4JSPxRU9ITGItX19kUjg/view) [![Wercker](https://img.shields.io/badge/personal-100%-blue.svg)](https://drive.google.com/file/d/0B8XKepQ4JSPxRU9ITGItX19kUjg/view)

### Overview
This tutorial intends to illustrate how to configure a repeatable autonomous web application deployment built for scalability and high-availability.

  - Build Software Aritfacts
  - Create Software Container
  - Configure Infrastructure as Code
  - Deploy Application

### Tech

This deployment is driven by 4 key technologies:

* [Aritfactory](https://www.jfrog.com/artifactory/) - Universal Artifact Repository Manager.
* [Docker](https://docs.docker.com/) - Software Containerization Platform.
* [CloudFormation](https://aws.amazon.com/cloudformation/) - AWS Service for configuring Infrastructure as Code.
* [ECS](https://aws.amazon.com/ecs/) - AWS Container Managment Service.

### Step 1: Build Software Aritfacts

The method for which you build an applications varies greatly depending of the software language and tech stack your application uses.

This tutorial demostrates how to build a python wheel and upload it to a private Artifactory PyPi (pip) Repository.

```sh
$ cd my_python_web_app/
$ python setup.py bdist_wheel upload -r local
```

### Step 2: Create Software Container

##### 2.a Create Dockerfile:
[]()
```sh
# VERSION: 1.0.0
# DESCRIPTION: My demo python web app
FROM mv/centos:${centos-image.version}
MAINTAINER "Frank Carotenuto" <frank.carotenuto@nielsen.com>

ENV MY_APP_HOME /opt/MY_APP

COPY init /startup

RUN chmod ug+x /startup/* \
 && yum -y install gcc-4.8.5 \
 && yum -y install postgresql94-devel-9.4.10 \
 && yum clean all

RUN pip install -U pip \
 && pip install -U setuptools \
 && pip install cryptography \
 && pip install pyOpenSSL \
 && pip install pandas \
 && pip install psycopg2 \
 && pip install boto3 \
 && pip install <path-to-my_python_web_app-pip-repo>
```

#####  2.b Build Docker Image
[]()
```sh
$ docker build -t <my-repo>/<my-container> .
```
##### 2.c Push to Docker Repository
[]()
```sh
$ docker push <my-repo>/<my-container>
```
### Step 3: Configure Infrastructure as Code

```sh
{
  "AWSTemplateFormatVersion" : "2010-09-09",
  "Description"              : "My Python Web App",

  "Metadata" : {...},

  "Parameters" : {...},

  "Mappings" : {...},

  "Resources" : {
    "MyWebAppECSService" : {
      "Type"       : "AWS::ECS::Service",
      "DependsOn"  : [
        "MyWebAppTaskDefinition"
      ],
      "Properties" : {
        "Cluster"        : "MyECSCluster",
        "DesiredCount"   : 10,
        "TaskDefinition" : "MyWebAppTaskDefinition",
        "DeploymentConfiguration" : {
          "MaximumPercent"        : 100,
          "MinimumHealthyPercent" : 0
        }
      }
    },
    "MyWebAppTaskDefinition" : {
      "Type"       : "AWS::ECS::TaskDefinition",
      "DependsOn"  : [
        "SharedResourcesStackInfo"
      ],
      "Properties" : {
        "ContainerDefinitions" : [
          {
            "Name"         : "MyWebApp",
            "MountPoints": [
              {
                 "SourceVolume"  : "shared-volume",
                 "ContainerPath" : "/opt/MyWebApp"
              }
            ],
            "Environment"  : [
              {
                "Name"  : "ANOTHER_ENV_VAR",
                "Value" : "HI MOM"
              }
            ],
            "Essential"    : true,
            "Image"        : "<repo>/<image>:<tag>",
            "Memory"       : "200"
          }
        ],
        "Volumes": [
          {
            "Host": {
              "SourcePath": "/shared/config"
            },
            "Name": "shared-volume"
          }
        ]
      }
    }
  }
}
```

### Step 4: Deploy
