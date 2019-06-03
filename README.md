# SimpleElasticSearchRepository

Implementation of a repository to store and access data POJOs in and from ElasticSearch.
Also comes with an ElasitcSearch Query-Builder follow the builder pattern from 
[the guide on type-safe builders](https://kotlinlang.org/docs/reference/type-safe-builders.html).
Why using a builder? Because Kotlin's great.

The response classes and query elements are very basic and nowhere near complete but suffice for basic
interactions with elasticsearch, without the hassle of (de-)serializing JSONs back and forth.

## Usage
Define a POJO, let's say `data class Entity(val foo: String, val bar: Number)`.


The repository can then be initialized and used to save and fetch data. A minimum setup would be:
```kotlin
// initialize repository
val repository = Repository(
    clazz = Entity::class.java,
    index = "entities",
    configuration = ElasticSearchConfiguration(host = "the.host",
                                               port = 9200,
                                               protocol = "https")
)

// save object
val objectId = repository.save(Entity("foo", "bar"))

// fetch object again
val fetchedObject = repository.getById(objectId)

// update object by id
repository.save(Entity("bar", "baz"), objectId)
```

And that's pretty much it. The doctype can be set explicitly with `Repository(doctype = "fancy_doctype")`,

the jackson mapper used to serialize and deserialize the data can also be customized with `Repository(mapper = MyMapper()).`.
Just make sure to set `DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES` to `false`.

## Query DSL
Following a builder pattern, queries can be built and serialized as JSON with a type-safe builder.

#### E.g:
A bool query containing two `term` elements as must and one `terms` element in the bool's should can be created
like this:
```
val query: String = query {
    bool {
        must {
            + term {
                "foo" to "bar"
            }
            + term {
                "bar" to "baz"
            }
        }
        should {
            + terms {
                "baz" to listOf(42, 1337, 666)
            } 
        }
    }
}.toString()
```
__Note__: `must` and `should` are both lists, thus their elements need to be added explicitly with the prefixing `+`.

Queries can then be POSTed against a repository as JSON string like
```
// initialize repository
val repository = Repository(...)

// build query, let's keep it simple
val query = query {
    term { "foo" to "bar" }
}

// execute the query, limit the size and enable scrolling
val response: PagedResult = repository.executeQuery(
    query.toString(),
    QueryParams(size = 1, scroll = "1m"
)
```

Supposed the query matches two objects in your ElasticSearch cluster. We limited the `size=1`, thus we need to scroll
over the results.

The resulting response of type `PagedResult` is an iterable which does the scrolling for you, given that scrolling was
enabled when executing the query.

To finally retrieve all results we can just call
```
// iterate over and flat the pages
val allGetResponses: List<GetResponse<Entity>> = response.flatten()

// retrieve the actual entities from the getResponses
// could be combined with the above statement as flatMap instead of flatten
val allHits: List<Entity> = allGetResponses.map { it._source }
```
