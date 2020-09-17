package util

import com.google.common.base.Strings

object HttpRequestUtils {
    /**
     * @param queryString은
     * URL에서 ? 이후에 전달되는 field1=value1&field2=value2 형식임
     * @return
     */
    fun parseQueryString(queryString: String): Map<String, String> {
        return parseValues(queryString, "&")
    }

    /**
     * @param 쿠키
     * 값은 name1=value1; name2=value2 형식임
     * @return
     */
    fun parseCookies(cookies: String): Map<String, String> {
        return parseValues(cookies, ";")
    }

    private fun parseValues(
        values: String,
        separator: String
    ): MutableMap<String, String> {
        val resultMap = mutableMapOf<String, String>()
        values.split(separator).forEach {
            val keyValue = getKeyValue(it, "=")
            resultMap[keyValue?.key!!] = keyValue?.value!!
        }
        return resultMap
    }

    fun getKeyValue(keyValue: String, regex: String?): Pair? {
        if (Strings.isNullOrEmpty(keyValue)) {
            return null
        }
        val tokens = keyValue.split(regex!!).toTypedArray()
        return if (tokens.size != 2) {
            null
        } else Pair(tokens[0], tokens[1])
    }

    fun parseHeader(header: String): Pair? {
        return getKeyValue(header, ": ")
    }

    class Pair internal constructor(key: String, value: String) {
        var key: String?
        var value: String?

        override fun hashCode(): Int {
            val prime = 31
            var result = 1
            result = prime * result + if (key == null) 0 else key.hashCode()
            result = prime * result + if (value == null) 0 else value.hashCode()
            return result
        }

        override fun equals(obj: Any?): Boolean {
            if (this === obj) return true
            if (obj == null) return false
            if (javaClass != obj.javaClass) return false
            val other = obj as Pair
            if (key == null) {
                if (other.key != null) return false
            } else if (key != other.key) return false
            if (value == null) {
                if (other.value != null) return false
            } else if (value != other.value) return false
            return true
        }

        override fun toString(): String {
            return "Pair [key=$key, value=$value]"
        }

        init {
            this.key = key.trim { it <= ' ' }
            this.value = value.trim { it <= ' ' }
        }
    }
}