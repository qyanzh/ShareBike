package com.example.west2summer.component

const val SPF_FILE_NAME_USER = "user"
const val SPF_USER_ID = "id"
const val SPF_USER_PASSWORD = "password"

const val RESPONSE_OK = "OK"
const val RESPONSE_SAME = "same"
const val RESPONSE_SUCCESS = "success"
const val RESPONSE_DEFEAT = "defeat"

enum class LikeState {
    UNLIKE, LIKED, EDIT, NULL, DONE
}

enum class EditState {
    EDIT, ADD
}