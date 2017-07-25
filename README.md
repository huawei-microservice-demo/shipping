# shipping
servicecomb shipping micro service
Dependencies
Name 	Version
Docker 	= 1.11.2
jdk <= 8 

example: docker-image-name:version-tag --> sockshop-shipping-service:0.0.1-SNAPSHOT
Step1:
git clone git@github.com:huawei-microservice-demo/shipping.git cd shipping

Step2: Build the the JAR file with using maven install
maven clean install

Step3: Build the docker image with using docker file and JAR
docker build --no-cache=true -t [docker-image-name:version-tag] .
Step3: Tag the image with service stage

docker tag [docker-image-name:version-tag] registry.cn-north-1.hwclouds.com/hwcse/[docker-image-name:version-tag]
Step4: Docker login

docker login -u [username] -p [private-key] [registry-name]
Step5: Docker push

docker push registry.cn-north-1.hwclouds.com/hwcse/[docker-image-name:version-tag]
Step6:

Login to the service and get the ip address / domain to open microservice in browser
