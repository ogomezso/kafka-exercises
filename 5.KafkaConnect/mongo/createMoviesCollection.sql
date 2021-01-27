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