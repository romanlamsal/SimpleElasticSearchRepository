package de.lamsal.esrepo.dsl

class Bool(
    init: Bool.() -> Unit
) : QueryElement {
    private val mustElements: BuilderList = BuilderList("must")
    private val shouldElements: BuilderList = BuilderList("should")
    private val mustNotElements: BuilderList = BuilderList("must_not")

    init {
        init()
    }

    fun must(initMust: BuilderList.() -> Unit) = mustElements.initMust()
    fun should(initShould: BuilderList.() -> Unit) = shouldElements.initShould()
    fun mustNot(initMustNot: BuilderList.() -> Unit) = mustNotElements.initMustNot()

    override fun toString(): String =
        """{"bool":{${
        listOf(mustElements.toString(), shouldElements.toString(), mustNotElements.toString())
            .filter { it.isNotEmpty() }
            .joinToString(",")
        }}}"""

    inner class BuilderList(val tag: String) {
        private val values: MutableList<QueryElement> = mutableListOf()

        operator fun QueryElement.unaryPlus() {
            values.add(this)
        }

        override fun toString(): String = if (values.isEmpty()) "" else
                """"$tag":${values.joinToString(prefix = "[", separator = ",", postfix = "]")}"""
    }
}