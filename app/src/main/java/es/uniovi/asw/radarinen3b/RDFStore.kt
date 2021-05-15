package es.uniovi.asw.radarinen3b

import es.uniovi.asw.radarinen3b.models.Friend
import org.eclipse.rdf4j.model.util.Values
import org.eclipse.rdf4j.repository.sail.SailRepository
import org.eclipse.rdf4j.rio.RDFFormat
import org.eclipse.rdf4j.sail.memory.MemoryStore
import java.net.URL

class RDFStore() {
    init {

    }

    companion object {
        private val db = SailRepository(MemoryStore())
        private val getNameQuery = db.connection.prepareTupleQuery(
            "PREFIX foaf:   <http://xmlns.com/foaf/0.1/> " +
                    "SELECT ?name " +
                    "WHERE {" +
                    "GRAPH ?g { ?person foaf:name ?name . }" +
                    "}"
        )

        private val getPhotoQuery = db.connection.prepareTupleQuery(
            "PREFIX vcard:  <http://www.w3.org/2006/vcard/ns#>" +
                    " SELECT ?photo" +
                    " WHERE { GRAPH ?g { ?person vcard:hasPhoto ?photo . } }"
        );

        private val getFriendsQuery = db.connection.prepareTupleQuery(
            "SELECT ?friend WHERE {" +
                    " GRAPH ?g {  ?person <http://xmlns.com/foaf/0.1/knows> ?friend . } " +
                    "}"
        )

        fun getName(webId: String): String {
            val connection = db.connection
            try {
                val url = URL(webId.removeSuffix("#me"))
                connection.add(url, url.toString(), RDFFormat.TURTLE, Values.iri(webId))
                getNameQuery.setBinding("g", Values.iri(webId))
                val evaluated = getNameQuery.evaluate()
                try {
                    if (!evaluated.hasNext())
                        return ""
                    return evaluated.next().getValue("name").stringValue()
                } finally {
                    evaluated.close()
                }
            } finally {
                connection.close()
            }
        }

        fun getPhoto(webId: String): String {
            val connection = db.connection
            try {
                val url = URL(webId.removeSuffix("#me"))
                connection.add(url, url.toString(), RDFFormat.TURTLE, Values.iri(webId))
                getPhotoQuery.setBinding("g", Values.iri(webId))
                val evaluated = getPhotoQuery.evaluate()
                try {
                    if (!evaluated.hasNext())
                        return ""
                    return evaluated.next().getValue("photo")
                        .stringValue()
                } finally {
                    evaluated.close()
                }
            } finally {
                connection.close()
            }
        }

        //
        fun getFriends(webId: String): List<Friend> {
            val connection = db.connection
            try {
                val url = URL(webId.removeSuffix("#me"))
                connection.add(url, url.toString(), RDFFormat.TURTLE, Values.iri(webId))
                val iKnow = getFriendsList(webId)
                iKnow.forEach {
                    val x = URL(it.removeSuffix("#me"))
                    connection.add(x, x.toString(), RDFFormat.TURTLE, Values.iri(it))
                }
                val realFriends = iKnow.filter { fr ->
                    getFriendsList(fr).contains(webId)
                }
                return realFriends.map { Friend(it, getName(it), imgSrcUrl = getPhoto(it)) };
            } finally {
                connection.close()
            }
        }

        private fun getFriendsList(webId: String): List<String> {
            getFriendsQuery.setBinding("g", Values.iri(webId))
            val evaluated = getFriendsQuery.evaluate()
            try {
                val result = evaluated.map { friend ->
                    friend.getValue("friend").stringValue()
                }
                return result
            } finally {
                evaluated.close()
            }
        }

    }
}