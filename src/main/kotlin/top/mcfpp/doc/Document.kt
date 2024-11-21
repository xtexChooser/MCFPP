package top.mcfpp.doc

import top.mcfpp.DocumentTag

class Document {

    /**
     * 文档的标签注释
     */
    val tags = ArrayList<DocumentTag>()

    /**
     * 文档的markdown部分
     */
    val markdown: String = ""

    companion object {
        /**
         * 解析一个字符串为文档
         */
        fun parse(input: String): Document{
            val re = Document()
            val sb = StringBuilder()
            val stream = input.lines()
            var isTag = true
            for (line in stream){
                if(line.startsWith("@") && isTag){
                    re.tags.add(DocumentTag.parse(line.substring(1)))
                }else{
                    isTag = false
                    sb.append(line)
                }
            }
            return re
        }
    }

}