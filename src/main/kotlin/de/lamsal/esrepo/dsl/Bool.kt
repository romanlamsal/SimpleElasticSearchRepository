package de.lamsal.esrepo.dsl

class Bool internal constructor(
    init: Bool.() -> Unit
) : QueryElement {

    private val must: BuilderList = BuilderList("must")
    private val should: BuilderList = BuilderList("should")
    private val mustNot: BuilderList = BuilderList("must_not")

    init {
        init()
    }

    fun must(initMust: BuilderList.() -> Unit) = must.initMust()
    fun should(initShould: BuilderList.() -> Unit) = should.initShould()
    fun mustNot(initMustNot: BuilderList.() -> Unit) = mustNot.initMustNot()

    override fun toString(): String =
        """{"bool":{${
        listOf(must.toString(), should.toString(), mustNot.toString())
            .filter { it.isNotEmpty() }
            .joinToString(",")
        }}}"""

    inner class BuilderList(private val tag: String) {
        private val values: MutableList<QueryElement> = mutableListOf()

        operator fun QueryElement.unaryPlus() {
            values.add(this)
        }

        override fun toString(): String = if (values.isEmpty()) "" else
                """"$tag":${values.joinToString(prefix = "[", separator = ",", postfix = "]")}"""
    }
}

val bool = ::Bool