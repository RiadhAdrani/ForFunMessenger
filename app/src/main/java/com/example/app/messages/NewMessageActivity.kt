package com.example.app.messages

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.util.Log
import com.example.app.R
import com.example.app.registerlogin.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_latest_messages.*
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.user_row_new_message.view.*

class NewMessageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)

        recyclerview_newmessage.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        supportActionBar?.title="Select User" // bar name


        fetchUsers()
    }

    companion object {
         val USER_KEY="USER_KEY"
    }

    private fun fetchUsers(){
        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }
            override fun onDataChange(p0: DataSnapshot) {
                val adapter = GroupAdapter<ViewHolder>()

                p0.children.forEach() {
                    Log.d("NewMessage",it.toString())
                    val user = it.getValue(User::class.java)
                    if (user != null) { adapter.add(UserItem(user))}

                }

                adapter.setOnItemClickListener { item, view ->
                    val userItem = item as UserItem

                    val intent = Intent(view.context, chatlogactivity::class.java)
                    //intent.putExtra(USER_KEY,item.user.username)
                    intent.putExtra(USER_KEY,userItem.user)
                    startActivity(intent)
                }

                recyclerview_newmessage.adapter = adapter
            }
        })
    }

}

class UserItem(val user: User): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.username_textview_new_message.text = user.username

        Picasso.get().load(user.profileimageUrl).into(viewHolder.itemView.imageview_new_message)

    }
    override fun getLayout(): Int {
        return R.layout.user_row_new_message
    }
}

