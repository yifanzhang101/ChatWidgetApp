package com.example.chatwidgetapp

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import com.example.chatwidgetapp.data.Msg
import com.example.chatwidgetapp.ui.MsgAdapter
import com.example.chatwidgetapp.viewmodel.MsgViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ChatActivity : AppCompatActivity() {

    private val msgViewModel: MsgViewModel by lazy {
        ViewModelProvider(this)[MsgViewModel::class.java]
    }
    private lateinit var chatToolBar: Toolbar
    private lateinit var sendButton: Button
    private lateinit var inputText: EditText
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var msgAdapter: MsgAdapter
    private val processingTimeMillis = 1000L

    //attach toolbar on the top
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar, menu)
        return true
    }

    //tool bar menu Item handling
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.call -> handleCallMenuItem()
            R.id.help -> handleHelpMenuItem()
            R.id.close -> finish()
        }
        return true
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        chatToolBar = findViewById(R.id.chatToolBar)
        sendButton = findViewById(R.id.sendButton)
        inputText = findViewById(R.id.inputText)
        chatRecyclerView = findViewById(R.id.chatRecyclerView)

        setSupportActionBar(chatToolBar)
        setupRecyclerView()

        sendButton.setOnClickListener {
            sendMessage()
        }

        // disable send button if there is no input
        inputText.addTextChangedListener {
            sendButton.isEnabled = it.toString().isNotEmpty()
        }
    }

    private fun setupRecyclerView() {
        chatRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity)
            if (!::msgAdapter.isInitialized) msgAdapter = MsgAdapter(msgViewModel.msgList)
            adapter = this@ChatActivity.msgAdapter
        }
    }

    private fun sendMessage() {
        val inputContent = inputText.text.toString()
        if (inputContent.isNotEmpty()) {
            /* handle the message sent by user */
            val msg = Msg(inputContent, Msg.TYPE_SENT)
            addMessageToChatList(msg)
            inputText.setText("") // clear the input box

            /* loading status of response message */
            val loadingMsg = msgViewModel.createLoadingMsg()
            addMessageToChatList(loadingMsg)

            /* get the response from api call and update from loading to response message */
            CoroutineScope(Dispatchers.Main).launch {
                val index = msgViewModel.msgList.indexOf(loadingMsg)
                delay(processingTimeMillis) // the delay simulate the response time of network request

                val responseContent = msgViewModel.handleChatResponse(inputContent)
                val responseMsg = Msg(responseContent, Msg.TYPE_RECEIVED)
                updateMessageInChatList(index, responseMsg)
            }
        }
    }

    private fun addMessageToChatList(msg: Msg) {
        msgViewModel.addMessage(msg)
        msgAdapter.notifyItemInserted(msgViewModel.msgList.size - 1) // refresh the last item in recycler view
        scrollToBottom()
    }

    private fun updateMessageInChatList(index: Int, msg: Msg) {
        msgViewModel.updateMessage(index, msg)
        msgAdapter.notifyItemChanged(index) // refresh the item of the specific index in recycler view
        scrollToBottom()
    }

    // scroll RecyclerView to the last line, using post to make sure scrolling operation is performed after the layout is calculated
    private fun scrollToBottom() {
        chatRecyclerView.post {
            chatRecyclerView.smoothScrollToPosition(msgAdapter.itemCount - 1)
        }
    }

    // call customer service
    private fun handleCallMenuItem() {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse(getString(R.string.phone_number))
        startActivity(intent)
    }

    // go to faq
    private fun handleHelpMenuItem() {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(getString(R.string.help_url))
        startActivity(intent)
    }
}
