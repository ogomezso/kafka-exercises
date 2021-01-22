rs.initiate({_id : 'rs0',
            members: [
                      { _id : 0, host : "mongo1:27017" },
                      { _id : 1, host : "mongo2:27017" },
                      { _id : 2, host : "mongo3:27017" }
            ]} )

db.createCollection( "movies", {
   validator: { $jsonSchema: {
      bsonType: "object",
      required: [ "id" ],
      properties: {
         id: {
            bsonType: "string",
            description: "id"
         },
         title: {
            bsonType : "string",
            description: "title"
         },
         releaseYear: {
            bsonType : "string",
            description: "release year"
         }
      }
   } }
} )