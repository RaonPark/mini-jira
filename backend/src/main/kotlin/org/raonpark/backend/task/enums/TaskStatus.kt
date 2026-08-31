package org.raonpark.backend.task.enums

enum class TaskStatus (
    val label: String,
) {
    TODO("할 일"),
    IN_PROGRESS("진행 중"),
    DONE("완료")
}
