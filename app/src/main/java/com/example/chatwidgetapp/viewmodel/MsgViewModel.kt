package com.example.chatwidgetapp.viewmodel

import androidx.lifecycle.ViewModel
import com.example.chatwidgetapp.data.Msg

// handle all business logic in ViewModel class
class MsgViewModel: ViewModel() {

    val msgList = ArrayList<Msg>()

    init {
        val msg0 = Msg("Hi, I'm Mr.Robot, how can I help you?", Msg.TYPE_RECEIVED)
        addMessage(msg0)
    }

    fun addMessage(msg: Msg) {
        msgList.add(msg)
    }

    fun updateMessage(index: Int, msg: Msg) {
        msgList[index] = msg
    }

    fun createLoadingMsg(): Msg {
        return Msg("I'm thinking...", Msg.TYPE_RECEIVED)
    }

    // simulate the backend logic for dealing the input content
    fun handleChatResponse(inputContent: String): String {
        return when {
            inputContent.length < 2 -> "sorry, I didn't get you"
            inputContent.contains("thank") -> "You're welcome!"
            inputContent.contains("fruit") -> "Apple is a great choice for you"
            inputContent.contains("drink") -> "Coffee is always my first recommendation"
            inputContent.contains("travel") -> "Hawaii gotta be a good place to go"
            inputContent.contains("airline") -> "Well, Alaska might need to make a emergency landing, but still better than Spirit"
            else -> "Let me collect the data and send that to your email"
        }
    }
}