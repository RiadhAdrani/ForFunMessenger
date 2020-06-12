package com.example.app.messages

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.example.app.R
import com.example.app.registerlogin.RegisterActivity
import com.example.app.registerlogin.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_latest_messages.*
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.latest_messages_row.view.*

class LatestMessagesActivity : AppCompatActivity() {

   companion object { // gloabal variable
        var currentUser : User? = null
       val TAG = "LatestMessages"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_messages)

        recyclerview_latest.adapter = adapter
        recyclerview_latest.addItemDecoration(DividerItemDecoration(this,DividerItemDecoration.VERTICAL))

        adapter.setOnItemClickListener { item, view ->
            Log.d(TAG,"123 - DEBUG")
            val intent = Intent(this, chatlogactivity::class.java)

            val row = item as LatestMessageRow
            intent.putExtra(NewMessageActivity.USER_KEY,row.chatPartnerUser)
            startActivity(intent)
        }

        supportActionBar?.title="ForFun Messenger"

        // setupDummyRows()

        ListenForLatestMessages()

        ListenForLatestMessages()

        fetchCurrentUser()

        verifyUserIsLoggedIn()   // Comment for fast testing


        // Toast.makeText(this, "Connected as ${currentUser!!.username} ", Toast.LENGTH_LONG).show()

    }

    class LatestMessageRow (val chatMessage: chatlogactivity.ChatMessage):Item<ViewHolder>(){
        var chatPartnerUser: User? = null

        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.user_msg.text = chatMessage.text

            val chatPartnerId: String
            if (chatMessage.fromId == FirebaseAuth.getInstance().uid){
                chatPartnerId = chatMessage.toId
            } else {
                chatPartnerId = chatMessage.fromId
            }

            val ref = FirebaseDatabase.getInstance().getReference("users/$chatPartnerId")
            ref.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onDataChange(p0: DataSnapshot) {
                    chatPartnerUser = p0.getValue(User::class.java)
                    viewHolder.itemView.User_name_latestmsg.text = chatPartnerUser?.username
                    val targetImageView = viewHolder.itemView.imageView_latestmessage
                    Picasso.get().load(chatPartnerUser?.profileimageUrl).into(targetImageView)

                }
            })

            //viewHolder.itemView.User_name_latestmsg.text = " Yeet "
        }
        override fun getLayout(): Int {
            return R.layout.latest_messages_row
        }
    }

    val latestMessagesMap = HashMap<String, chatlogactivity.ChatMessage>()


    private fun refreshRecyclerViewMessages(){
        adapter.clear()
        latestMessagesMap.values.forEach {
            adapter.add(LatestMessageRow(it))
        }
    }

    private fun ListenForLatestMessages(){
        val fromId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId")
        ref.addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage=p0.getValue(chatlogactivity.ChatMessage::class.java) ?: return
                latestMessagesMap[p0.key!!] = chatMessage
                refreshRecyclerViewMessages()
                // adapter.add(LatestMessageRow(chatMessage!!))
            }
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(chatlogactivity.ChatMessage::class.java) ?: return
                adapter.add(LatestMessageRow(chatMessage!!))
            }

            override fun onCancelled(p0: DatabaseError) {
            }
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }
            override fun onChildRemoved(p0: DataSnapshot) {
            }
        })
    }

    val adapter = GroupAdapter<ViewHolder>()

    private fun setupDummyRows(){


       // adapter.add(LatestMessageRow())
        //adapter.add(LatestMessageRow())
       // adapter.add(LatestMessageRow())


    }

   private fun fetchCurrentUser(){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                 currentUser = p0.getValue(User::class.java)
                 Log.d("LatestMessages","Current user ${currentUser?.username}")
            }
        })

       // Toast.makeText(this, "Connected as ${currentUser!!.username} ", Toast.LENGTH_LONG).show()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId){
            R.id.menu_new_message ->{
                val intent=Intent(this, NewMessageActivity::class.java)
                startActivity(intent)
            }

            R.id.menu_sign_out ->{
                FirebaseAuth.getInstance().signOut()
                val intent= Intent(this, RegisterActivity::class.java)
                intent.flags=Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun verifyUserIsLoggedIn(){
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null){
            val intent= Intent(this, RegisterActivity::class.java)
            intent.flags=Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main,menu)
        return super.onCreateOptionsMenu(menu)
    }

}

