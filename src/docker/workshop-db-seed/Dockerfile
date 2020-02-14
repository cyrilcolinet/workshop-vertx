FROM mongo:latest

COPY init.json /init.json
CMD mongoimport --host db --username root --password example --authenticationDatabase admin --db workshop --collection users --type json --file /init.json --jsonArray