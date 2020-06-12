package com.example.app.messages

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.app.R
import com.example.app.registerlogin.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chatlogactivity.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*

class chatlogactivity : AppCompatActivity() {

    companion object {
        val TAG = "ChatLog"
    }

    val adapter = GroupAdapter<ViewHolder>()

    var toUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatlogactivity)

        recyclerview_chatlog.adapter = adapter

        toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)

        supportActionBar?.title = toUser?.username

        ListenforMessages()


        send_button.setOnClickListener {
            Log.d(TAG,"Attemp to send message ...")
            performSendMessage()
        }
    }

    private fun ListenforMessages(){
        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.uid
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")

        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class .java)
                if (chatMessage != null) {
                    Log.d(TAG,chatMessage?.text) //?

                    if (chatMessage.fromId == FirebaseAuth.getInstance().uid){
                        val currentUser = LatestMessagesActivity.currentUser ?: return
                        adapter.add(ChatFrom(chatMessage?.text, currentUser!!)) //?
                    }
                    else {
                        adapter.add(ChatTo(chatMessage.text,toUser!!))
                    }
                }

                recyclerview_chatlog.scrollToPosition(adapter.itemCount -1)
            }

            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }
        })
    }

    class ChatMessage(val id:String, val text:String, val fromId: String, val toId:String,val timestamp: Long)
    {
        constructor():this("","","","",-1)
    }

    private fun performSendMessage(){
        // Sending msg to firebase

        val text = text_chatlog.text.toString() // get text from the text field

        val fromId = FirebaseAuth.getInstance().uid // problem here

        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val toId = user.uid

        // Log.d(TAG,"DEBUGGING FromUID:$fromId")

        if (fromId == null) return
        if (text == "") return


       // val reference = FirebaseDatabase.getInstance().getReference("/messages").push() // create a node for messages in Firebase Database
        val reference = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()

        val toReference = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()


        val chatMessage = ChatMessage(reference.key!!, text,fromId!!,toId,System.currentTimeMillis()/1000)
        reference.setValue(chatMessage)
            .addOnSuccessListener {
                Log.d(TAG,"Saved our Chat Message : ${reference.key}")
                text_chatlog.text.clear()
                recyclerview_chatlog.scrollToPosition(adapter.itemCount -1)
            }

        toReference.setValue(chatMessage)

        val latestMessageRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")
        latestMessageRef.setValue(chatMessage)

        val latestMessageToRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId")
        latestMessageToRef.setValue(chatMessage)
    }

}

    class ChatFrom(val text: String, val user: User): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.from_msg.text = text
        val uri = user.profileimageUrl
        val targetImageView = viewHolder.itemView.chat_from_row_pic
        Picasso.get().load(uri).into(targetImageView)

    }

    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }
}

    class ChatTo(val text: String, val user:User): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.to_msg.text = text

        // load image
        val uri = user.profileimageUrl
        val targetImageView = viewHolder.itemView.chat_to_row_pic
        Picasso.get().load(uri).into(targetImageView)

    }

    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }
}


