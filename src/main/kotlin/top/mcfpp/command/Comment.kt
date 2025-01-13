package top.mcfpp.command

class Comment(comment: String, commentLevel: CommentLevel = CommentLevel.INFO) : Command(comment) {

    val level: CommentLevel = commentLevel

    init {
        isCompleted = true
    }

    override fun analyze(): String {
        return this.commandParts[0].toString()
    }

    override fun toString(): String {
        return this.commandParts[0].toString()
    }

}

enum class CommentLevel{
    DEBUG, INFO, WARN, ERROR
}