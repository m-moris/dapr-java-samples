
all	: 
	(cd frontend && mvn clean package) && \
	(cd backend  && mvn clean package)