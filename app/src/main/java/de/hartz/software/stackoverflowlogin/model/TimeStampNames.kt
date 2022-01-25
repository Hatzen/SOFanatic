package de.hartz.software.stackoverflowlogin.model

enum class TimeStampNames(val timeStampName: String) {
    LAST_SUCCESS("LAST_SUCCESS"),
    LAST_ERROR("LAST_ERROR"),
    LAST_DAY_CHANGED("LAST_DAY_CHANGED"),
    LAST_ALARM("LAST_ALARM");
}
